package com.example.auth.service

import akka.actor.Actor
import com.example.auth.client.AuthClient._
import com.example.auth.service.Schema._
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import spray.http.StatusCodes._
import spray.routing.{HttpService, _}
import spray.util.LoggingContext
import spray.httpx.SprayJsonSupport._
import scala.concurrent.Future

class AuthServiceActor extends Actor with AuthService {

  def actorRefFactory = context

  def receive = runRoute(route)
}

trait AuthService extends HttpService {

  import Util._

  implicit def ec = actorRefFactory.dispatcher

  implicit def myExceptionHandler(implicit log: LoggingContext) =
    ExceptionHandler {
      case e: Exception =>
        requestUri { uri =>
          log.error("Unexpected Exception: " + e.toString, uri)
          complete(InternalServerError, e.toString)
        }
    }

  val log = LoggerFactory.getLogger(classOf[AuthService])
  val config = new Configuration()
  val sessionStore = new SessionStore(config)
  val da = new DataAccess(config)

  log.info("AuthService started")

  val route =
    pathPrefix("User") {
      post {
        pathPrefix("Login") {
          pathEnd {
            formFields('userEmail, 'password) { (userEmail: String, password: String) =>
              complete {
                log.info(s"User Login $userEmail")

                da.findLogin(userEmail.toLowerCase).flatMap {
                  case Some(login) =>
                    val passwordHash = Util.getPbkdf2Sha256Hash(password, login.salt)
                    if (passwordHash == login.passwordHash) {
                      val token = generateToken(login.userId, login.salt)
                      val expiresOn = DateTime.now().plus(config.sessionStore.sessionLife)
                      val session = new Session(token, login.userId, login.userEmail, expiresOn)

                      log.info(s"User Logged in $userEmail ${login.userId}")
                      sessionStore.updateSession(session).map(s => AuthResponse(session = Some(s)))
                    }
                    else Future {
                      log.info(s"User Login InvalidPassword $userEmail ${login.userId}")
                      AuthResponse(error = Some("InvalidPassword"))
                    }
                  case None => Future {
                    log.info(s"User Login UserEmailNotFound $userEmail")
                    AuthResponse(error = Some("UserEmailNotFound"))
                  }
                }
              }
            }
          }
        } ~
          pathPrefix("Logout") {
            pathEnd {
              formField('token) { token: String =>
                complete {
                  sessionStore.removeSession(token).map(_ => AuthResponse())
                }
              }
            }
          } ~
          pathPrefix("PasswordChange") {
            pathEnd {
              formFields('userEmail, 'oldPassword, 'newPassword) { (userEmail: String, oldPassword: String, newPassword: String) =>
                complete {
                  log.info(s"User PasswordChange $userEmail")

                  da.findLogin(userEmail.toLowerCase).flatMap {
                    case Some(login) =>
                      val oldPasswordHash = Util.getPbkdf2Sha256Hash(oldPassword, login.salt)
                      if (oldPasswordHash == login.passwordHash) {
                        val newPasswordHash = Util.getPbkdf2Sha256Hash(newPassword, login.salt)
                        val newLogin = LoginRow(login.userEmail, login.userId, login.salt, newPasswordHash)

                        log.info(s"User Password changed $userEmail ${login.userId}")
                        da.updateLogin(newLogin).map(_ => AuthResponse())
                      }
                      else Future {
                        log.info(s"User Password InvalidPassword $userEmail ${login.userId}")
                        AuthResponse(error = Some("InvalidPassword"))
                      }
                    case None => Future {
                      log.info(s"User Password UserEmailNotFound $userEmail")
                      AuthResponse(error = Some("UserEmailNotFound"))
                    }
                  }
                }
              }
            }
          }
      } ~
        pathEndOrSingleSlash {
          put {
            entity(as[User]) { user =>
              complete {
                log.info(s"Create/Update User ${user.userEmail} ${user.userId}")

                val salt = genSalt()
                val passwordHash = Util.getPbkdf2Sha256Hash(user.password, salt)
                val newLogin = LoginRow(user.userEmail, user.userId, salt, passwordHash)

                da.updateLogin(newLogin).map(_ => AuthResponse())
              }
            }
          }
        }
    } ~
      pathPrefix("CheckPermission") {
        pathEnd {
          post {
            formFields('token, 'method, 'path) { (token: String, method: String, path: String) =>
              complete {
                sessionStore.findSession(token).flatMap {
                  case Some(session) =>
                    if (session.expiresOn.isBeforeNow) {
                      Future(AuthResponse(error = Some("SessionExpired")))
                    }
                    else {
                      log.info(s"CheckPermission ${session.userId} $method $path")

                      da.checkPermission(session.userId, method.toUpperCase, path).map {
                        case true =>
                          log.info(s"CheckPermission passed ${session.userId} $method $path")
                          AuthResponse(session = Some(session))
                        case false =>
                          log.info(s"CheckPermission denied ${session.userId} $method $path")
                          AuthResponse(error = Some("PermissionDenied"), session = Some(session))
                      }
                    }
                  case None => Future(AuthResponse(error = Some("SessionNotFound")))
                }
              }
            }
          }
        }
      }
}