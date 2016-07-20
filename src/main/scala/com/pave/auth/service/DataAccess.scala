package com.example.auth.service

import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContextExecutor, Future}

class DataAccess(conf: Configuration)(implicit val ec: ExecutionContextExecutor) {

  import Schema._
  import slick.driver.PostgresDriver.api._

  val db = Database.forConfig("database")
  val log = LoggerFactory.getLogger(classOf[DataAccess])

  def findLogin(userEmail: String): Future[Option[LoginRow]] = {
    log.debug(s"findLogin $userEmail")
    db.run(loginTable.filter(_.user_email === userEmail).result.headOption)
  }

  def updateLogin(login: LoginRow): Future[LoginRow] = {
    log.debug(s"updateLogin for user ${login.userEmail}")
    db.run(loginTable.insertOrUpdate(login)).map(_ => login)
  }

  def checkPermission(userId: String, method: String, path: String): Future[Boolean] = {
    log.debug(s"check permission for user $userId $method $path")
    db.run(
      (roleTable join permissionTable on (_.role === _.role))
        .filter { case (r, p) => r.user_id === userId && p.method === method && p.path === path }
        .exists.result)
  }
}
