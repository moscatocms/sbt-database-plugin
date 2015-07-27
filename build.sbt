sbtPlugin := true

organization := "org.moscatocms"

name := "sbt-database-plugin"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.10.4"

scalacOptions += "-deprecation"

libraryDependencies ++= Seq(
  "org.reflections" % "reflections" % "0.9.10"
)

addSbtPlugin("org.moscatocms" %% "liquibase-utils" % "1.0.0-SNAPSHOT")