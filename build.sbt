name := "jsondiff"

version := "1.0"

scalaVersion := "2.11.8"
lazy val root = (project in file(".")).enablePlugins(PlayScala)

shellPrompt := getPrompt()

libraryDependencies += "com.typesafe" % "config" % "1.3.0"
libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.37"
libraryDependencies += "org.playframework.anorm" %% "anorm" % "2.6.2"
libraryDependencies += "joda-time" % "joda-time" % "2.9.1"
libraryDependencies += "org.specs2" % "specs2-core_2.11" % "3.6.6" % "test"
libraryDependencies += "org.specs2" % "specs2-junit_2.11" % "3.6.6" % "test"
libraryDependencies += "org.specs2" % "specs2-mock_2.11" % "3.6.6" % "test"
libraryDependencies += "io.swagger" % "swagger-core" % "1.5.18"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.6" % Test
libraryDependencies += "org.mockito" % "mockito-core" % "1.9.5" % Test

libraryDependencies ++= Seq(
  ws,
  cache,
  evolutions,
  jdbc
)
javaOptions in Test += "-Dconfig.file=conf/application.sample.conf"

def getPrompt(): (State => String) = { state: State =>
  "[" + scala.Console.CYAN + "Json_Diff" + scala.Console.RESET + "] $ "
}


    