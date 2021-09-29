val Http4sVersion          = "0.23.4"
val CirceVersion           = "0.14.1"
val MunitVersion           = "0.7.27"
val LogbackVersion         = "1.2.5"
val MunitCatsEffectVersion = "1.0.5"

lazy val root = (project in file("."))
  .settings(
    organization := "dev.rmaiun",
    name := "mabel",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.6",
    libraryDependencies ++= Seq(
      "org.http4s"    %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s"    %% "http4s-blaze-client" % Http4sVersion,
      "org.http4s"    %% "http4s-circe"        % Http4sVersion,
      "org.http4s"    %% "http4s-dsl"          % Http4sVersion,
      "io.circe"      %% "circe-generic"       % CirceVersion,
      "org.scalameta" %% "munit"               % MunitVersion           % Test,
      "org.typelevel" %% "munit-cats-effect-3" % MunitCatsEffectVersion % Test,
      "ch.qos.logback" % "logback-classic"     % LogbackVersion,
      "dev.zio"       %% "zio"                 % "1.0.12",
      "dev.zio"       %% "zio-streams"         % "1.0.12",
      "dev.zio"       %% "zio-interop-cats"    % "3.1.1.0",
      "dev.zio"       %% "zio-logging"         % "0.5.12",
      "dev.zio"       %% "zio-logging-slf4j"   % "0.5.12",
      "nl.vroste"     %% "zio-amqp"            % "0.2.2"
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.13.0" cross CrossVersion.full),
    addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1"),
    testFrameworks += new TestFramework("munit.Framework")
  )
