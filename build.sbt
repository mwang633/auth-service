val akkaV = "2.3.14"
val sprayV = "1.3.3"
val slickV = "3.1.1"

lazy val `auth-client` = project

lazy val `auth-service` = (project in file(".")).
  settings(
    name := "auth-service",
    organizationName := "pave.com",
    version := "1.0",
    scalaVersion := "2.11.7",
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-encoding", "utf8"),
    libraryDependencies ++= Seq(
        "io.spray"            %%   "spray-servlet"     % sprayV,
        "io.spray"            %%   "spray-routing"     % sprayV,
        "io.spray"            %%   "spray-client"      % sprayV,
        "io.spray"            %%   "spray-util"        % sprayV,
        "io.spray"            %%   "spray-caching"     % sprayV,
        "io.spray"            %%   "spray-can"         % sprayV,
        "io.spray"            %%   "spray-testkit"     % sprayV % "test",

        "io.spray"            %%   "spray-json"        % "1.3.1",

        "com.typesafe.akka"   %%  "akka-actor"         % akkaV,
        "com.typesafe.akka"   %%  "akka-testkit"       % akkaV,
        "com.typesafe.akka"   %%  "akka-slf4j"         % akkaV,
        "com.typesafe.akka"   %%  "akka-testkit"       % akkaV % "test",

        "joda-time"           %   "joda-time"          % "2.9.2",

        "com.typesafe.slick"  %%  "slick"              % slickV,
        "com.typesafe.slick"  %%  "slick-codegen"      % slickV,

        "org.postgresql"      %   "postgresql"         % "9.4.1207.jre7",
        "org.scalatest"       %%  "scalatest"          % "2.2.6" % "test",
        "org.apache.kafka"    %   "kafka-log4j-appender" % "0.9.0.1",

        "org.bouncycastle"    %   "bcprov-jdk15on"     % "1.54",
        "net.debasishg"       %%  "redisclient"        % "3.0"
      )
  ).aggregate(`auth-client`).dependsOn(`auth-client`)
