package com.example.auth.client

import akka.actor.ActorRefFactory
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.slf4j.LoggerFactory
import spray.client.pipelining._
import spray.http.{FormData, HttpRequest, HttpResponse}
import spray.httpx.SprayJsonSupport._
import spray.json._

import scala.concurrent.Future

object AuthClient extends DefaultJsonProtocol {
  case class AuthResponse(error: Option[String] = None,
                          session : Option[Session] = None)

  case class Session(token : String, userId : String, userEmail : String, expiresOn : DateTime)

  case class User(userId : String, userEmail : String, password : String)

  implicit object DateTimeFormat extends RootJsonFormat[DateTime] {
    def write(c: DateTime) = JsString(c.toString)

    def read(value: JsValue) : DateTime = value match {
      case s: JsString => ISODateTimeFormat.dateTimeParser().parseDateTime(s.value)
      case _ => deserializationError("DateTime expected")
    }
  }

  implicit val sessionFormat = jsonFormat4(Session)
  implicit val userFormat = jsonFormat3(User)
  implicit val authResponseFormat = jsonFormat2(AuthResponse)
}

class AuthClient(baseUrl : String, implicit val system: ActorRefFactory) {
  import AuthClient._
  import system.dispatcher

  private val log = LoggerFactory.getLogger(this.getClass)

  private val logRequest: HttpRequest => HttpRequest = { r => log.debug(r.toString); r }
  private val logResponse: HttpResponse => HttpResponse = { r => log.debug(r.toString); r }

  private val jsonQuery = logRequest ~> sendReceive ~> logResponse

  def login(userEmail : String, password: String) : Future[AuthResponse] = {
    val pipeline = jsonQuery ~> unmarshal[AuthResponse]

    pipeline {
      Post(s"$baseUrl/User/Login",
           FormData(Seq("userEmail" -> userEmail, "password" -> password)))
    }
  }

  def logout(token : String) : Future[AuthResponse] = {
    val pipeline = jsonQuery ~> unmarshal[AuthResponse]

    pipeline {
      Post(s"$baseUrl/User/Logout",
          FormData(Seq("token" -> token)))
    }
  }

  def passwordChange(userEmail : String, oldPassword: String, newPassword : String) : Future[AuthResponse] = {
    val pipeline = jsonQuery ~> unmarshal[AuthResponse]

    pipeline {
      Post(s"$baseUrl/User/PasswordChange",
        FormData(Seq("userEmail" -> userEmail, "oldPassword" -> oldPassword, "newPassword" -> newPassword)))
    }
  }

  def updateUser(user : User) : Future[AuthResponse] = {
    val pipeline = jsonQuery ~> unmarshal[AuthResponse]

    pipeline {
      Put(s"$baseUrl/User", user)
    }
  }

  def checkPermission(token : String, method : String, path : String): Future[AuthResponse] = {
    val pipeline = jsonQuery ~> unmarshal[AuthResponse]

    pipeline {
      Post(s"$baseUrl/CheckPermission",
        FormData(Seq("token" -> token, "method" -> method, "path" -> path)))
    }
  }

}
