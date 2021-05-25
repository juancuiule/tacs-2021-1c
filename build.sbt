val Http4sVersion = "0.21.16"
val CirceVersion = "0.13.0"
val MunitVersion = "0.7.20"
val LogbackVersion = "1.2.3"
val MunitCatsEffectVersion = "0.13.0"
val tsecV = "0.2.1"
val CatsVersion = "2.2.0"
val ScalaTestVersion = "3.2.9"
val ScalaTestPlusVersion = "3.2.2.0"


lazy val root = (project in file("."))
  .settings(
    organization := "com.utn",
    name := "tacs-1c-2021",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.4",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % CatsVersion
    ),
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-blaze-server",
      "org.http4s" %% "http4s-blaze-client",
      "org.http4s" %% "http4s-circe",
      "org.http4s" %% "http4s-dsl").map(_ % Http4sVersion),
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-literal").map(_ % CirceVersion),
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % MunitVersion % Test,
      "org.typelevel" %% "munit-cats-effect-2" % MunitCatsEffectVersion % Test,
      "ch.qos.logback" % "logback-classic" % LogbackVersion,
      "org.scalameta" %% "svm-subs" % "20.2.0"
    ),
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % ScalaTestVersion % Test,
      "org.scalatestplus" %% "scalacheck-1-14" % ScalaTestPlusVersion % Test,
    ),
    libraryDependencies ++= Seq(
      "io.github.jmcardon" %% "tsec-common",
      "io.github.jmcardon" %% "tsec-password",
      "io.github.jmcardon" %% "tsec-mac",
      "io.github.jmcardon" %% "tsec-signatures",
      "io.github.jmcardon" %% "tsec-jwt-mac",
      "io.github.jmcardon" %% "tsec-jwt-sig",
      "io.github.jmcardon" %% "tsec-http4s").map(_ % tsecV),
    libraryDependencies ++= (scalaBinaryVersion.value match {
      case "2.10" =>
        compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full) :: Nil
      case _ =>
        Nil
    }),
    addCompilerPlugin("org.typelevel" % "kind-projector" % "0.11.3" cross CrossVersion.full),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
    testFrameworks += new TestFramework("munit.Framework")
  )

mainClass in Compile := Some("com.utn.tacs.Main")