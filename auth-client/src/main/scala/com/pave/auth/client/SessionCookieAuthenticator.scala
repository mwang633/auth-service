package com.example.auth.client

import com.example.auth.client.AuthClient.Session
import spray.http.HttpMethods

import concurrent._
import org.slf4j.LoggerFactory
import spray.routing._
import spray.routing.authentication._

class SessionCookieAuthenticator(authClient: AuthClient)(implicit ec: ExecutionContext) extends ContextAuthenticator[Session] {

  import AuthCookies._

  val log = LoggerFactory.getLogger(classOf[SessionCookieAuthenticator])

  def apply(ctx: RequestContext): Future[Authentication[Session]] = {
    log.debug(s"Authenticating request for uri ${ctx.request.uri}")

    getSessionToken(ctx)
    match {
      case Some(token) =>
        log.debug(s"Authenticating session id cookie with value '$token'")

        authClient
          .checkPermission(token, ctx.request.method.toString, ctx.request.uri.toString)
          .map(authResponse => {
            authResponse.session match {
              case Some(session) =>
                log.debug("Session id cookie is valid. Authentication succeeded.")
                Right(session)

              case None =>
                log.warn(s"Permission denied for session '$token' in request for uri ${ctx.request.uri}.")
                Left(AuthorizationFailedRejection)

            }
          })

      case None =>
        log.warn("No session cookie found in request for uri {}.", ctx.request.uri)

        Future.successful(Left(AuthorizationFailedRejection))
    }
  }

  private def getSessionToken(ctx: RequestContext): Option[String] = {
    // need to check both tokens in cookie and headers for all methods except GET

    val cookieToken = ctx.request.cookies.find(_.name == CsrfTokenCookieName).map(_.content)

    if (ctx.request.method != HttpMethods.GET) {
      cookieToken
    }
    else {
      val headerToken =
        ctx.request.headers.find(_.lowercaseName == XCsrfTokenCookieName.toLowerCase).map(_.value)

      (cookieToken, headerToken) match {
        case (Some(c), Some(h)) if c == h => headerToken
        case _ =>
          log.warn(s"Found mismatching header and cookie tokens for request ${ctx.request}")
          None
      }
    }
  }
}
