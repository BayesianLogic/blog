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
import blog.sample.MHSampler
import blog.sample.GenericProposer

class MHDebugger(model: Model, evidence: Evidence, queries: Queries)
  extends SamplerDebugger[MHSample](model, evidence, queries) {

  def makeSampler = {
    // For now, we only support the default GenericProposer.
    val sampler = new MHSampler(model, new Properties())
    sampler.initialize(evidence, queries)
    sampler
  }

  def nextSample = {
    sampler.nextSample()
    val mhSampler = sampler.asInstanceOf[MHSampler]
    val proposer = mhSampler.getProposer().asInstanceOf[GenericProposer]
    new MHSample(
      model,
      sampler.getLatestWorld(),
      mhSampler.latestAccepted,
      proposer.latestLogProbBackward(),
      proposer.latestLogProbForward(),
      mhSampler.latestLogProbRatio,
      mhSampler.latestLogAcceptRatio)
  }
}

object MHDebugger {
  /**
   * Create a MHDebugger for the given model.
   */
  def make(path: String): MHDebugger = {
    Util.initRandom(false)

    val model = new Model()
    val evidence = new Evidence(model)
    val queries = new Queries(model)
    Main.simpleSetupFromFiles(model, evidence, queries, path :: Nil)

    new MHDebugger(model, evidence, queries)
  }
}
