name := "Simple"

version := "0.0.1"

organization := "org.example"

scalaVersion := "2.10.0"

//scalaVersion := "2.9.1"

scalacOptions ++= Seq("-deprecation", "-unchecked")

libraryDependencies += "javax.servlet" % "servlet-api" % "2.5" % "provided"

libraryDependencies += "org.scala-lang" % "scala-library" % "2.10.0"

libraryDependencies += "commons-codec" % "commons-codec" % "1.6"

seq(com.github.siasia.WebPlugin.webSettings :_*)

libraryDependencies +=  "org.eclipse.jetty" % "jetty-webapp" % "8.1.7.v20120910"  % "container,test"

libraryDependencies += "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container,test" artifacts Artifact("javax.servlet", "jar", "jar")

