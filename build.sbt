ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.7"

lazy val zioVersion = "2.1.17"
lazy val catsCore = "2.13.0"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % catsCore,
  "dev.zio" %% "zio" % zioVersion,
  "dev.zio" %% "zio-test" % zioVersion,
  "dev.zio" %% "zio-test-sbt" % zioVersion,
  "dev.zio" %% "zio-streams" % zioVersion,
  "dev.zio" %% "zio-logging" % zioVersion,
  "dev.zio" %% "zio-test-junit" % zioVersion
)

assembly / mainClass := Some("com.acme.AcmeApp")
assembly / assemblyJarName := "acme-zio.jar"