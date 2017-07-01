name := """boundview"""

lazy val commonSettings = Seq(
  version := "1.0",
  scalaVersion := "2.11.11",
  scalacOptions ++= Seq(
        "-deprecation",
        "-encoding", "UTF-8", // yes, this is 2 args
        "-feature",
        "-unchecked",
        "-Xfatal-warnings",
        "-Xlint",
        "-Yno-adapted-args",
        "-Ywarn-dead-code", // N.B. doesn't work well with the ??? hole
        "-Ywarn-infer-any",
        "-Ywarn-numeric-widen",
        "-Ywarn-unused",
        "-Ywarn-value-discard",
        "-Xfuture",
        "-P:splain:all:true"),
  libraryDependencies ++= Seq(
    compilerPlugin("io.tryp" %% "splain" % "0.2.4"),
    "org.scalaz" %% "scalaz-core" % "7.2.2",
    "com.chuusai" %% "shapeless" % "2.3.1"))

lazy val core = project.in(file("core"))
  .settings(commonSettings: _*)

lazy val example = project.in(file("example"))
  .dependsOn(core)
  .settings(commonSettings: _*)
