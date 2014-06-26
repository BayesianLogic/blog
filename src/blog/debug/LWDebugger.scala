package blog.debug

import java.util.Properties
import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

import blog.Main
import blog.common.Util
import blog.io.TableWriter
import blog.model.Evidence
import blog.model.Model
import blog.model.Queries
import blog.sample.OldLWSampler

class LWDebugger(model: Model, evidence: Evidence, queries: Queries)
  extends SamplerDebugger[LWSample](model, evidence, queries) {

  def makeSampler = {
    val sampler = new OldLWSampler(model, new Properties())
    sampler.initialize(evidence, queries)
    sampler
  }

  def nextSample = {
    sampler.nextSample()
    new LWSample(model, sampler.getLatestWorld(), sampler.getLatestLogWeight())
  }
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
