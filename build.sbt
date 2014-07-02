import AssemblyKeys._ // put this at the top of the file
import com.typesafe.sbt.SbtNativePackager._
import NativePackagerKeys._

assemblySettings

name := "blog"

version := "0.9"

javacOptions ++= Seq("-source", "1.5")

//scalaVersion := "2.10.3"

compileOrder := CompileOrder.JavaThenScala

artifactName := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
  artifact.name + "-" + module.revision + "." + artifact.extension
}

mainClass in assembly := Some("blog.Main")

packageArchetype.java_application

// this one is not required during compilation or running

//libraryDependencies += "de.jflex" % "jflex" % "1.6.0"

libraryDependencies += "gov.nist.math" % "jama" % "1.0.3"

libraryDependencies += "junit" % "junit" % "4.10"

libraryDependencies += "com.google.code.gson" % "gson" % "2.2.4"

libraryDependencies += "org.apache.commons" % "commons-math3" % "3.0" % "test"

//addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.11.2")

