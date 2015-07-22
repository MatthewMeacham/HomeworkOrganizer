// Import for Play2War Plugin
import com.github.play2war.plugin._

name := """HomeworkOrganizer"""

version := "1.0-SNAPSHOT"

// Add Play2War settings to project
Play2WarPlugin.play2WarSettings

// Play2War Servlet settings
Play2WarKeys.servletVersion := "3.0"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean, SbtWeb)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "javax.mail" % "mail" % "1.4.5",
  "mysql" % "mysql-connector-java" % "5.1.18",
  evolutions,
  javaJdbc,
  cache,
  javaWs
)

// Forces compile before Eclipse file generation when running "activator eclipse"
EclipseKeys.preTasks := Seq(compile in Compile)

// Builds Eclipse project with library sources (includes library javadocs, etc.)
EclipseKeys.withSource := true

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

// Sets assets pipeline to run assets through these SBT plugins
pipelineStages := Seq(digest, gzip)

// LESS filters for files in app.assets
includeFilter in (Assets, LessKeys.less) := "*.less"

excludeFilter in (Assets, LessKeys.less) := "_*.less"