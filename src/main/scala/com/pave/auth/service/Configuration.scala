package com.example.auth.service

import java.io.File
import java.util.concurrent.TimeUnit

import com.typesafe.config.{Config, ConfigFactory}
import org.joda.time.Duration

class Configuration() {
  val config : Config = {
    val configDefaults = ConfigFactory.load(this.getClass.getClassLoader, "application.conf")

    scala.sys.props.get("application.config") match {
      case Some(filename) => ConfigFactory.parseFile(new File(filename)).withFallback(configDefaults)
      case None => configDefaults
    }
  }

  object sessionStore {
    val redisUrl : String = config.getString("sessionStore.redisUrl")
    val redisPort : Int = config.getInt("sessionStore.redisPort")
    val sessionLife : Duration = new Duration(config.getDuration("sessionStore.sessionLife", TimeUnit.MILLISECONDS))
  }
}
