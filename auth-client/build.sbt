val akkaV = "2.3.14"
val sprayV = "1.3.3"
val slickV = "3.1.1"

lazy val `auth-client` = (project in file(".")).
  settings(
    name := "auth-client",
    version := "1.0",
    scalaVersion := "2.11.7",
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-encoding", "utf8"),
    libraryDependencies ++= Seq(
        "io.spray"            %%   "spray-client"      % sprayV,
        "io.spray"            %%   "spray-util"        % sprayV,
        "io.spray"            %%   "spray-routing"     % sprayV,

        "io.spray"            %%   "spray-json"        % "1.3.1",

        "com.typesafe.akka"   %%  "akka-actor"         % akkaV,
        "com.typesafe.akka"   %%  "akka-slf4j"         % akkaV,

        "joda-time"           %   "joda-time"          % "2.9.2"
      )
  )