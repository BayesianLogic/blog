package blog.engine

import java.util.Properties
import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer
import blog.Main
import blog.common.Util
import blog.io.TableWriter
import blog.model.Evidence
import blog.model.Model
import blog.model.Queries
import blog.sample.OldSampler
import blog.BLOGUtil
import blog.sample.Sample
import blog.sample.Sampler
import blog.io.JsonWriter

/**
 * @author cberzan
 * @since Jun 26, 2014
 */
class SamplerRunner(val sampler: Sampler[_ <: Sample]) {

  /**
   * Get the next sample and update the queries.
   */
  def sampleOnce = {
    val sample = sampler.sample
    sampler.queries.foreach(query => query.updateStats(sample.world, sample.logWeight))
  }

  /**
   * Get the next n samples and update the queries.
   */
  def sampleTimes(n: Int) {
    for (i <- 1 to n) {
      sampleOnce
    }
  }

  /**
   * docs TODO
   */
  def runNonInteractive(numSamples: Int, outputPath: String = null): Unit = {
    // Run inference.
    Util.initRandom(false)
    sampleTimes(numSamples)
    // TODO progress report

    // Print query results
    val tableWriter = new TableWriter(sampler.queries)
    tableWriter.setHeader("======== Query Results =========\n"
      + "Number of samples: " + numSamples)
    tableWriter.writeResults(System.out)
    println("======== Done ========")

    // Write query results to file, in JSON format.
    if (outputPath != null) {
      println("Writing query results to " + outputPath + "...")
      val jsonWriter = new JsonWriter(sampler.queries)
      jsonWriter.writeResults(outputPath)
      println("Done.")
    }
  }

}
