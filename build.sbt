name := "HomeworkOrganizer"

version := "1.2.1"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache
)     

// PostgreSQl Plugin
libraryDependencies += "postgresql" % "postgresql" % "9.1-901-1.jdbc4"

// Assets Pipeline
pipelineStages := Seq(digest, gzip)

play.Project.playJavaSettings
