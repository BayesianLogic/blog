package blog.sample

import blog.model.Model
import blog.model.Evidence
import blog.model.Queries
import scala.collection.mutable.ListBuffer

/**
 * Abstract sampler.
 *
 * @author cberzan
 * @since Jun 26, 2014
 */
abstract class Sampler[SampleType <: Sample](
  val model: Model,
  val evidence: Evidence,
  val queries: Queries) {

  /**
   * Return the next sample.
   */
  def sample: SampleType
}
