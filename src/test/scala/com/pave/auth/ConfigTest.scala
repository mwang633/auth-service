package com.example.auth

import com.example.auth.service.Configuration
import org.scalatest.FlatSpec
import org.scalatest.Matchers._

class ConfigTest extends FlatSpec {

  "Config Test" should "pass" in {
    val config = new Configuration()

    assert(config.sessionStore.sessionLife != null)
    assert(config.sessionStore.redisPort > 0)
    assert(config.sessionStore.redisUrl.length > 0)
  }
}