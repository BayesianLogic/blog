import AssemblyKeys._ 
import com.typesafe.sbt.SbtNativePackager._
import NativePackagerKeys._

assemblySettings

packageArchetype.java_application

name := "blog"

version := "0.9"

javacOptions ++= Seq("-source", "1.5")

//scalaVersion := "2.10.3"

compileOrder := CompileOrder.JavaThenScala

artifactName := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
  artifact.name + "-" + module.revision + "." + artifact.extension
}

mainClass in assembly := Some("blog.Main")

mainClass in (Compile) := Some("blog.Main")

sources in (Compile, doc) ~= (_ filter (_.getName endsWith ".___"))

// this one is not required during compilation or running

libraryDependencies += "de.jflex" % "jflex" % "1.6.0"

libraryDependencies += "gov.nist.math" % "jama" % "1.0.3"

libraryDependencies += "junit" % "junit" % "4.10"

libraryDependencies += "com.google.code.gson" % "gson" % "2.2.4"

libraryDependencies += "org.apache.commons" % "commons-math3" % "3.0" % "test"

libraryDependencies += "com.typesafe" % "config" % "1.2.0"

EclipseKeys.eclipseOutput := Some("target/eclipse")

EclipseKeys.withSource := true

packageSummary in Linux := "blog"

packageSummary in Windows := "blog"

packageDescription := "BLOG Probabilistic Programming Lanaguage"

maintainer in Windows := "UC Berkeley RUGS"

maintainer in Debian := "UC Berkeley RUGS"