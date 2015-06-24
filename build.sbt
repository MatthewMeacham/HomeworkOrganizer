//import com.github.play2war.plugin._

name := "HomeworkOrganizer"

version := "1.0-SNAPSHOT"

//Comment the two lines below in when wanting to make war
//Play2WarPlugin.play2WarSettings

//Play2WarKeys.servletVersion := "3.0"

libraryDependencies ++= Seq(
//  "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
  javaJdbc,
  javaEbean,
  cache,
  anorm
)     

play.Project.playJavaSettings
