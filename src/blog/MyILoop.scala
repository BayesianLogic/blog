package blog;

import java.io.BufferedReader
import scala.collection.mutable.ListBuffer
import scala.tools.nsc.interpreter._
import scala.tools.nsc.interpreter.ILoop
import scala.tools.nsc.Settings


class MyILoop() extends ILoop()
{
  val valuesToBind = ListBuffer[(String, String, Any)]()

  override def printWelcome() {
  }

  override def createInterpreter() {
    super.createInterpreter()

    // Enable debug mode.
    // (By default, bindings can fail quietly. This outputs a stack trace when
    // a binding fails. Unfortunately, it also outputs a lot of other garbage
    // to stdout, so I commented it out by default.)
    // replProps.debug.enable()

    // Bind all values that were requested to be bound.
    valuesToBind.foreach{ case (name, boundType, value) =>
      intp.beQuietDuring(intp.bind(name, boundType, value)) }
    valuesToBind.clear()

    // Disable debug mode to avoid further stdout pollution.
    replProps.debug.disable()
  }

  def bind(name: String, boundType: String, value: Any) {
    valuesToBind.append((name, boundType, value));
  }

  def run() {
    println("--------- entering scala repl -------")
    val settings = new Settings
    settings.Yreplsync.value = true
    // In order to use BLOG classes from the REPL, we need the REPL's
    // classloader to be a child of the main classloader used by BLOG.
    // Otherwise we get weird ClassCastExceptions "cannot cast FOO to FOO".
    settings.embeddedDefaults(this.getClass().getClassLoader())
    // Need to set classpath so that interpreter knows about BLOG types.
    // HACK: Need a more robust way to set interpreter's classpath
    settings.classpath.value = "bin/"
    process(settings)
    println("\n--------- exiting scala repl --------")
  }
}
