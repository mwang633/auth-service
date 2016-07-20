package com.example.auth.service

import java.sql.Timestamp

import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.util.encoders.Base64
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import spray.http.{DateTime => SprayDateTime}

import scala.util.Random

object Util {

  private val chars =  "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toList

  implicit class TimestampEx(ts: Timestamp) {
    def toDateTime = new DateTime(ts.getTime)
  }

  implicit class DateTimeEx(dateTime: DateTime) {
    def toTimestamp = new Timestamp(dateTime.getMillis)

    def toSprayDateTime = SprayDateTime(dateTime.getMillis)

    def truncDay() = dateTime.withMillisOfDay(0)
  }

  def parseDateTime(s: String): DateTime = ISODateTimeFormat.dateTimeParser().parseDateTime(s)

  def getPbkdf2Sha256Hash(password : String, salt : String, iteration : Int = 10000) : String = {
    val gen = new PKCS5S2ParametersGenerator(new SHA256Digest())
    gen.init(password.getBytes(), salt.getBytes, iteration)
    val dk = gen.generateDerivedParameters(256).asInstanceOf[KeyParameter]
    Base64.toBase64String(dk.getKey)
  }

  def generateToken(userId : String, salt : String, iteration : Int = 10000) : String = {
    val stab = s"$userId+${DateTime.now().toString}"

    val gen = new PKCS5S2ParametersGenerator(new SHA256Digest())
    gen.init(stab.getBytes(), salt.getBytes, iteration)
    val dk = gen.generateDerivedParameters(256).asInstanceOf[KeyParameter]
    Base64.toBase64String(dk.getKey)
  }

  def genSalt(length : Int = 12): String = {
    val ran = new Random()
    ran.shuffle(chars).take(length).mkString
  }
}
