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

sources in (Compile, doc) ~= (_ filter (_.getName endsWith ".___")) // do not generate java doc, since it creates problem // TODO in the future, remove this line and fix documentation issue 

// this one is not required during compilation or running

lazy val jflex = "de.jflex" % "jflex" % "1.6.0"

libraryDependencies += jflex

libraryDependencies += "gov.nist.math" % "jama" % "1.0.3"

libraryDependencies += "junit" % "junit" % "4.10"

libraryDependencies += "com.google.code.gson" % "gson" % "2.2.4"

libraryDependencies += "org.apache.commons" % "commons-math3" % "3.0" % "test"

libraryDependencies += "com.typesafe" % "config" % "1.2.0"

EclipseKeys.eclipseOutput := Some("target/eclipse")

EclipseKeys.withSource := true

lazy val html = taskKey[Unit]("Generate html documentation")

html := { """pelican docs/content -o target/pelican -s docs/pelicanconf.py""" ! }

lazy val ghpages = taskKey[Unit]("Push updated html docs to github pages")

ghpages := { """docs/update_ghpages.sh""" ! }

lazy val parser = taskKey[Unit]("Generating parser files")

lazy val lexer = taskKey[Unit]("Generating lexer files")

lexer := {
  val cpfiles = (fullClasspath in Runtime).value.files
  val cpString = cpfiles.map(_.getAbsolutePath).mkString(System.getProperty("path.separator"))
  """java -cp "%s" jflex.Main -d src/main/java/blog/parse src/parser/BLOGLexer.flex""".format(cpString) !                      
}

parser := { 
  val cpString = unmanagedBase.value.getName() + "/java-cup-11b.jar"
  """java -cp %s java_cup.Main -locations -destdir src/main/java/blog/parse -symbols BLOGTokenConstants -parser BLOGParser src/parser/BLOGParser.cup""".format(cpString) !
} 

packageSummary in Linux := "blog"

packageSummary in Windows := "blog"

packageDescription := "BLOG Probabilistic Programming Lanaguage"

maintainer in Windows := "UC Berkeley RUGS"

maintainer in Debian := "UC Berkeley RUGS"

mappings in Universal ++= directory("example")

mappings in Universal ++= directory("target/pelican") map {case (f, s) => (f, s.replaceFirst("pelican", "docs"))}

mappings in Universal += file("iblog") -> "bin/iblog"

mappings in Universal += file("dblog") -> "bin/dblog"

mappings in Universal += file("bloglint") -> "bin/bloglint"

mappings in Universal += file("bloglint.bat") -> "bin/bloglint.bat"

mappings in Universal += file("src/main/scala/iblog.scala") -> "bin/iblog.scala"

// junit
libraryDependencies += "com.novocode" % "junit-interface" % "0.10" % "test" 
