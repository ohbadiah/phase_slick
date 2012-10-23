import sbt._
import sbt.Keys._

object SlicktalkBuild extends Build {

  lazy val slicktalk = Project(
    id = "slicktalk",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "slicktalk",
      organization := "me.thefalcon",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.10.0-M7",
      libraryDependencies ++= Seq(
        "com.typesafe" % "slick_2.10.0-M7" % "0.11.1",
        "mysql" % "mysql-connector-java" % "5.1.10",
        "org.slf4j" % "slf4j-nop" % "1.6.4"
      )
    )
  )
}
