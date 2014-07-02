import AssemblyKeys._ 
import com.typesafe.sbt.SbtNativePackager._
import NativePackagerKeys._
import NativePackagerHelper._

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

lazy val html = taskKey[Unit]("Generate html documentation")

html := { """pelican docs/content -o docs/output -s docs/pelicanconf.py""" ! }


lazy val parser = taskKey[Unit]("Generating parser files")

lazy val lexer = taskKey[Unit]("Generating lexer files")

parser := {
  val cpfiles = (fullClasspath in Runtime).value.files
  val cpString = cpfiles.map(_.getAbsolutePath).mkString(System.getProperty("path.separator"))
  """java -cp "%s" java_cup.Main -locations -destdir src/main/java/blog/parse -symbols BLOGTokenConstants -parser BLOGParser src/parser/BLOGParser.cup""".format(cpString) !
}

lexer := {
  val cpfiles = (fullClasspath in Runtime).value.files
  val cpString = cpfiles.map(_.getAbsolutePath).mkString(System.getProperty("path.separator"))
  """java -cp "%s" jflex.Main -d src/main/java/blog/parse src/parser/BLOGLexer.flex""".format(cpString) !                      
}


packageSummary in Linux := "blog"

packageSummary in Windows := "blog"

packageDescription := "BLOG Probabilistic Programming Lanaguage"

maintainer in Windows := "UC Berkeley RUGS"

maintainer in Debian := "UC Berkeley RUGS"

mappings in Universal ++= directory("docs/output") map {case (f, s) => (f, s.replaceFirst("output", "docs"))}

