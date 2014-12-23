import com.typesafe.sbt.SbtNativePackager._
import NativePackagerKeys._
import NativePackagerHelper._

name := "blog"

version := "0.10.alpha2"

javacOptions ++= Seq("-source", "1.5", "-target", "1.5")

//scalaVersion := "2.10.3"

compileOrder := CompileOrder.JavaThenScala

artifactName := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
  artifact.name + "-" + module.revision + "." + artifact.extension
}

mainClass in (Compile) := Some("blog.Main")

sources in (Compile, doc) ~= (_ filter (_.getName endsWith ".___")) // do not generate java doc, since it creates problem // TODO in the future, remove this line and fix documentation issue 

libraryDependencies += "gov.nist.math" % "jama" % "1.0.3"

libraryDependencies += "com.google.code.gson" % "gson" % "2.2.4"

libraryDependencies += "org.apache.commons" % "commons-math3" % "3.0"

// this one is not required during compilation or running
libraryDependencies += "de.jflex" % "jflex" % "1.6.0" 

libraryDependencies += "junit" % "junit" % "4.11" % "test"

parallelExecution in Test := false // disable parallel test

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
  (packageBin in Universal).value
  (packageBin in Debian).value
  """docs/update_ghpages.sh""" ! 
}

lazy val parser = taskKey[Unit]("Generating parser files")

lazy val lexer = taskKey[Unit]("Generating lexer files") 

lexer := {
  val cpfiles = (fullClasspath in Test).value.files
  println(cpfiles)
  val cpString = cpfiles.map(_.getAbsolutePath).mkString(System.getProperty("path.separator"))
  """java -cp "%s" jflex.Main -d src/main/java/blog/parse src/parser/BLOGLexer.flex""".format(cpString) !                      
}

parser := { 
  val cpString = unmanagedBase.value.getName() + "/java-cup-11b.jar"
  """java -cp %s java_cup.Main -locations -destdir src/main/java/blog/parse -symbols BLOGTokenConstants -parser BLOGParser src/parser/BLOGParser.cup""".format(cpString) !
} 

// the following are packaging settings
packageArchetype.java_application // native package

packageSummary in Linux := "BLOG Probabilistic Programming Language Inference Engine and Tools"

packageSummary in Windows := "BLOG Probabilistic Programming Language Inference Engine and Tools"

packageDescription := "BLOG Probabilistic Programming Language"

maintainer in Windows := "UC Berkeley RUGS"

maintainer in Debian := "UC Berkeley RUGS"

debianPackageDependencies in Debian ++= Seq("java2-runtime", "bash (>= 2.05a-11)")

debianPackageRecommends in Debian += "scala"

mappings in Universal ++= directory("example")

mappings in Universal ++= directory("target/pelican") map {case (f, s) => (f, s.replaceFirst("pelican", "docs"))}

mappings in Universal += file("iblog") -> "bin/iblog"

mappings in Universal += file("iblog.bat") -> "bin/iblog.bat"

mappings in Universal += file("dblog") -> "bin/dblog"

mappings in Universal += file("bloglint") -> "bin/bloglint"

mappings in Universal += file("bloglint.bat") -> "bin/bloglint.bat"

mappings in Universal += file("src/main/scala/iblog.scala") -> "bin/iblog.scala"

// Include debugging symbols in the compiled classes.
javacOptions += "-g"

scalacOptions += "-g:vars"

// Include the sources in the assembled jar.
unmanagedResourceDirectories in Compile += { baseDirectory.value / "src" }
