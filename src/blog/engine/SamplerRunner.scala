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

/**
 * @author cberzan
 * @since Jun 26, 2014
 */
class SamplerRunner(
  val model: Model,
  val evidence: Evidence,
  val queries: Queries,
  val sampler: Sampler[_ <: Sample]) {

  /**
   * Get the next sample and update the queries.
   */
  def sampleOnce = {
    val sample = sampler.sample
    queries.foreach(query => query.updateStats(sample.world, sample.logWeight))
  }

  /**
   * Get the next n samples and update the queries.
   */
  def sampleTimes(n: Int) {
    for (i <- 1 to n) {
      sampleOnce
    }
  }

}
