package com.example.auth.client

import com.example.auth.client.AuthClient.Session
import spray.http._

object AuthCookies {
  val CsrfTokenCookieName = "XSRF-TOKEN"
  val XCsrfTokenCookieName = "X-XSRF-TOKEN"

  private val path = Some("/")

  def sessionCookie(session: Session, domain: String, secure: Boolean): HttpCookie = {
      HttpCookie(
        name = CsrfTokenCookieName,
        content = session.token,
        expires = Some(DateTime(session.expiresOn.getMillis)),
        domain = Some(domain),
        path = path,
        secure = secure
      )
  }

  def removeSessionCookie(domain: String, secure: Boolean): HttpCookie =
      HttpCookie(
        name = CsrfTokenCookieName,
        content = "",
        expires = None,
        domain = Some(domain),
        path = path,
        secure = secure
      )
}