package com.example.auth.service

import com.example.auth.client.AuthClient.Session
import com.redis.RedisClient

import scala.concurrent.{ExecutionContextExecutor, Future}
import spray.json._
import com.example.auth.client.AuthClient._

class SessionStore(conf : Configuration)(implicit val ec : ExecutionContextExecutor) {
  val redis = new RedisClient(conf.sessionStore.redisUrl, conf.sessionStore.redisPort)

  def findSession(sessionId : String) : Future[Option[Session]] =
    Future {
      redis.get(sessionId).map(_.parseJson.convertTo[Session])
    }

  def updateSession(session : Session) : Future[Session] = Future {
    require(redis.set(session.token, session.toJson.compactPrint)) // should never return false
    redis.expireat(session.token, session.expiresOn.getMillis)
    session
  }

  def removeSession(token : String) : Future[Unit] = Future {
    redis.del(token)
  }
}