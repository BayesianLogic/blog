import NativePackagerHelper._
import scala.sys.process._

enablePlugins(JavaAppPackaging, UniversalPlugin) 

name := "blog"

version := "0.10.alpha1"

javacOptions ++= Seq("-source", "11", "-target", "11")

//scalaVersion := "2.10.3"

compileOrder := CompileOrder.JavaThenScala

artifactName := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
  artifact.name + "-" + module.revision + "." + artifact.extension
}

Compile / mainClass := Some("blog.Main")

Compile / doc / sources ~= (_ filter (_.getName endsWith ".___")) // do not generate java doc, since it creates problem // TODO in the future, remove this line and fix documentation issue 

libraryDependencies += "gov.nist.math" % "jama" % "1.0.3"

libraryDependencies += "com.google.code.gson" % "gson" % "2.2.4"

libraryDependencies += "org.apache.commons" % "commons-math3" % "3.0"

// this one is not required during compilation or running
libraryDependencies += "de.jflex" % "jflex" % "1.6.0" 

libraryDependencies += "junit" % "junit" % "4.11" % "test"

Test / parallelExecution := false // disable parallel test

// enable sbt to use junit 
libraryDependencies += "com.novocode" % "junit-interface" % "0.10" % "test" 

EclipseKeys.eclipseOutput := Some("target/eclipse")

EclipseKeys.withSource := true

lazy val html = taskKey[Unit]("Generate html documentation")

html := { 
  val s: TaskStreams = streams.value
  s.log.info("Generating html documentation")
  """pelican docs/content -o target/pelican -s docs/pelicanconf.py""" ! 
}

lazy val ghpages = taskKey[Unit]("Push updated html docs to github pages")

ghpages := { 
  html.value
  (Universal / packageBin).value
  (Debian / packageBin).value
  """docs/update_ghpages.sh""" ! 
}

lazy val parser = taskKey[Unit]("Generating parser files")

lazy val lexer = taskKey[Unit]("Generating lexer files") 

lexer := {
  val cpfiles = (Test / fullClasspath).value.files
  println(cpfiles)
  val cpString = cpfiles.map(_.getAbsolutePath).mkString(System.getProperty("path.separator"))
  """java -cp "%s" jflex.Main -d src/main/java/blog/parse src/parser/BLOGLexer.flex""".format(cpString) !                      
}

parser := { 
  val cpString = unmanagedBase.value.getName() + "/java-cup-11b.jar"
  """java -cp %s java_cup.Main -locations -destdir src/main/java/blog/parse -symbols BLOGTokenConstants -parser BLOGParser src/parser/BLOGParser.cup""".format(cpString) !
} 

// the following are packaging settings
// packageArchetype.java_application // native package

Linux / packageSummary := "BLOG Probabilistic Programming Language Inference Engine and Tools"

Windows / packageSummary := "BLOG Probabilistic Programming Language Inference Engine and Tools"

packageDescription := "BLOG Probabilistic Programming Language"

Windows / maintainer := "UC Berkeley RUGS"

Debian / maintainer := "UC Berkeley RUGS"

Debian / debianPackageDependencies ++= Seq("java2-runtime", "bash (>= 2.05a-11)")

Debian / debianPackageRecommends += "scala"

Universal / mappings ++= directory("example")

Universal / mappings ++= directory("target/pelican") map {case (f, s) => (f, s.replaceFirst("pelican", "docs"))}

Universal / mappings += file("iblog") -> "bin/iblog"

Universal / mappings += file("iblog.bat") -> "bin/iblog.bat"

Universal / mappings += file("dblog") -> "bin/dblog"

Universal / mappings += file("bloglint") -> "bin/bloglint"

Universal / mappings += file("bloglint.bat") -> "bin/bloglint.bat"

Universal / mappings += file("src/main/scala/iblog.scala") -> "bin/iblog.scala"

// Include debugging symbols in the compiled classes.
javacOptions += "-g"

scalacOptions += "-g:vars"

// Include the sources in the assembled jar.
Compile / unmanagedResourceDirectories += { baseDirectory.value / "src" }

// Stuff below if for ppaml-slam:
libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "1.3.10"

// This makes Eclipse see the stuff in src/main/resources properly:
EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

// When adding new libraryDependencies, Eclipse will complain about not finding
// them. Run "sbt eclipse" to fix this.