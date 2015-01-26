name := "HomeworkOrganizer"

version := "1.2.1"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs
)     

// PostgreSQl Plugin
libraryDependencies += "postgresql" % "postgresql" % "9.1-901-1.jdbc4"

play.Project.playJavaSettings
