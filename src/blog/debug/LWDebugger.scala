package blog.debug

import java.util.Properties
import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

import blog.Main
import blog.common.Util
import blog.model.Evidence
import blog.model.Model
import blog.model.Queries
import blog.sample.LWSampler

class LWDebugger(
  model: Model,
  evidence: Evidence,
  queries: Queries) {

  // All samples so far.
  val samples: ListBuffer[LWSample] = new ListBuffer()

  // Latest sampled world.
  var lastSample: LWSample = null

  // The underlying sampler.
  val sampler = new LWSampler(model, new Properties())
  sampler.initialize(evidence, queries)

  // Compute next sample, print it, and add it to samples.
  def sampleOne = {
    sampler.nextSample()
    lastSample = new LWSample(
      model, sampler.getLatestWorld(), sampler.getLatestLogWeight())
    println(lastSample)
    samples.append(lastSample)
  }

  // Compute next n samples.
  def sampleMany(n: Int) {
    for (i <- 1 to n) {
      sampleOne
    }
  }

  // Shortcuts.
  def m = model
  def e = evidence
  def q = queries
  def n = sampleOne
  def s = lastSample
}

object LWDebugger {
  /**
   * Create a LWDebugger for the given model.
   *
   * Example usage from iblog:
   * <code>
   * scala> val d = LWDebugger.make("tmp/burglary.all")
   * scala> import d._
   * scala> n
   * scala> s.eval("Earthquake | JohnCalls")
   * </code>
   */
  def make(path: String): LWDebugger = {
    Util.initRandom(false)

    val model = new Model()
    val evidence = new Evidence(model)
    val queries = new Queries(model)
    Main.simpleSetupFromFiles(model, evidence, queries, path :: Nil)

    new LWDebugger(model, evidence, queries)
  }
}
