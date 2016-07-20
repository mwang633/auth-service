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

class UserServiceSpec extends FlatSpec with BeforeAndAfterAll with ScalatestRouteTest with AuthService {
  val userId = "569eb3eb081d2944571f0b0c"
  val password = "testPassword"
  val userEmail = "test@example.com"

  val testTimeOut = 30.second
  implicit val routeTestTimeout = RouteTestTimeout(30.second)
  def actorRefFactory = system

  override def afterAll() {
    import Schema._
    import slick.driver.PostgresDriver.api._

    Await.ready(da.db.run(loginTable.filter(_.user_email === userEmail).delete), testTimeOut)
  }

  private val logRequest: HttpRequest => HttpRequest = { r => println(r.toString); r }

  "create a new login" should "pass" in {
    Put("/User", HttpEntity(ContentTypes.`application/json`,
      string = s"""{"userEmail":"$userEmail","userId":"$userId","password":"$password"}""")) ~>
      logRequest ~> route ~> check {
      assert(status === OK)
    }
  }

  "login user" should "pass" in {
    Post("/User/Login", FormData(Seq("userEmail"-> userEmail, "password"-> password))) ~>
      logRequest ~> route ~> check {
      assert(status === OK)
      val res = responseAs[AuthResponse]
      assert(res != null)
      assert(res.error.isEmpty)
      assert(res.session.isDefined)
      assert(res.session.get.token.length > 0)
      assert(res.session.get.userEmail == userEmail)
      assert(res.session.get.userId == userId)
    }
  }
}
