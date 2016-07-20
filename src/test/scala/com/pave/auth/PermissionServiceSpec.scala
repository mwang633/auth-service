package com.example.auth

import com.example.auth.service._
import com.example.auth.client.AuthClient._
import Schema._
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfter, FlatSpec}
import org.scalatest.Matchers._
import spray.http._
import spray.http.StatusCodes._
import spray.testkit.ScalatestRouteTest
import scala.concurrent.Await
import scala.concurrent.duration._
import spray.httpx.SprayJsonSupport._


class PermissionServiceSpec extends FlatSpec with BeforeAndAfterAll with ScalatestRouteTest with AuthService {
  val userId = "569eb3eb081d2944571f0b0c"
  val password = "testPassword"
  val userEmail = "test@example.com"
  val token = "TestToken"
  val expiredToken = "TestExpiredToken"
  val role = "FakeRole"

  val testTimeOut = 30.second

  implicit val routeTestTimeout = RouteTestTimeout(testTimeOut)
  def actorRefFactory = system

  private val logRequest: HttpRequest => HttpRequest = { r => println(r.toString); r }
  private val logResponse: HttpResponse => HttpResponse = { r => println(r.toString); r }

  override def beforeAll() {
    import Schema._
    import slick.driver.PostgresDriver.api._

    Await.ready(da.db.run(roleTable.insertOrUpdate(UserRole(userId, "FakeRole"))), testTimeOut)
    Await.ready(da.db.run(permissionTable.insertOrUpdate(Permission(role, "GET", "FakeService/FakePath"))), testTimeOut)

    Await.ready(
      sessionStore.updateSession(Session(token, userId, userEmail, expiresOn = org.joda.time.DateTime.now.plusMinutes(1))),
      testTimeOut)

    Await.ready(
      sessionStore.updateSession(Session(expiredToken, userId, userEmail, expiresOn = org.joda.time.DateTime.now.minusMinutes(1))),
      testTimeOut)
  }

  override def afterAll() {
    import Schema._
    import slick.driver.PostgresDriver.api._
    Await.ready(da.db.run(roleTable.filter(_.user_id === userId).delete), testTimeOut)
    Await.ready(da.db.run(permissionTable.filter(_.role === role).delete), testTimeOut)

    Await.ready(sessionStore.removeSession(token), testTimeOut)

    Await.ready(sessionStore.removeSession(expiredToken), testTimeOut)
  }

  "valid permission" should "pass" in {
    Post("/CheckPermission", FormData(Seq("token" -> token, "method" -> "GET", "path" -> "FakeService/FakePath"))) ~>
      logRequest ~> route ~> check {
      assert(status === OK)

      val res = responseAs[AuthResponse]
      assert(res != null)
      assert(res.error.isEmpty)
      assert(res.session.isDefined)
      assert(res.session.get.token == token)
      assert(res.session.get.userEmail == userEmail)
      assert(res.session.get.userId == userId)
    }
  }

  "invalid method" should "pass" in {
    Post("/CheckPermission", FormData(Seq("token" -> token, "method" -> "PUT", "path" -> "FakeService/FakePath"))) ~>
      logRequest ~> route ~> check {
      assert(status === OK)

      val res = responseAs[AuthResponse]
      assert(res != null)
      assert(res.error.isDefined)
      assert(res.error.get == "PermissionDenied")
      assert(res.session.isDefined)
      assert(res.session.get.token == token)
      assert(res.session.get.userEmail == userEmail)
      assert(res.session.get.userId == userId)
    }
  }

  "invalid path" should "pass" in {
    Post("/CheckPermission", FormData(Seq("token" -> token, "method" -> "GET", "path" -> "FakeService/OtherPath"))) ~>
      logRequest ~> route ~> check {
      assert(status === OK)

      val res = responseAs[AuthResponse]
      assert(res != null)
      assert(res.error.isDefined)
      assert(res.error.get == "PermissionDenied")
      assert(res.session.isDefined)
      assert(res.session.get.token == token)
      assert(res.session.get.userEmail == userEmail)
      assert(res.session.get.userId == userId)
    }
  }

  "invalid token" should "pass" in {
    Post("/CheckPermission", FormData(Seq("token" -> "invalid", "method" -> "GET", "path" -> "FakeService/FakePath"))) ~>
      logRequest ~> route ~> check {
      assert(status === OK)

      val res = responseAs[AuthResponse]
      assert(res != null)
      assert(res.error.isDefined)
      assert(res.error.get == "SessionNotFound")
      assert(res.session.isEmpty)
    }
  }

  "expired token" should "pass" in {
    Post("/CheckPermission", FormData(Seq("token" -> expiredToken, "method"->"GET", "path"-> "FakeService/FakePath"))) ~>
      logRequest ~> route ~> check {
      assert(status === OK)

      val res = responseAs[AuthResponse]
      assert(res != null)
      assert(res.error.isDefined)
      assert(res.error.get == "SessionExpired")
      assert(res.session.isEmpty)
    }
  }
}
