package blog

import scala.collection.JavaConversions._
import blog.model.Model
import blog.model.Evidence
import blog.model.Queries
import blog.sample.LWSampler
import blog.engine.SamplerRunner

/**
 * @author cberzan
 * @since Jun 26, 2014
 */
object NewMain {
  /**
   * Convenience method for reading a Model, Evidence, and Queries from a file.
   */
  def readModelEvidenceQueries(path: String): (Model, Evidence, Queries) = {
    val model = new Model()
    val evidence = new Evidence(model)
    val queries = new Queries(model)
    println(model)
    println(evidence)
    println(queries)
    Main.simpleSetupFromFiles(model, evidence, queries, path :: Nil)
    (model, evidence, queries)
  }

  /**
   * Convenience method for running LWSampler on a model.
   */
  def LW(modelPath: String, numSamples: Int = 10000): Unit = {
    val (model, evidence, queries) = readModelEvidenceQueries(modelPath)
    val sampler = new LWSampler(model, evidence, queries)
    val engine = new SamplerRunner(sampler)
    println(model)
    println(evidence)
    println(queries)
    println(sampler)
    println(engine)
    engine.runNonInteractive(numSamples)
  }

  // FIXME: Currently we cannot run everything twice in the same iblog session.
  // This is because Type.allTypes is static, so it doesn't get reset between runs.
  // On the second run, Semant will fail with "Type already defined".
}