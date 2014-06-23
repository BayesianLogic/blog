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
import blog.sample.Sampler

/**
 * A debugger for a BLOG sampling algorithm.
 *
 * Concrete subclasses must implement makeSampler and sampleOne.
 *
 * @author cberzan
 * @since Jun 23, 2014
 */
abstract class SamplerDebugger[SampleType](
  val model: Model,
  val evidence: Evidence,
  val queries: Queries) {

  // All samples so far.
  val samples: ListBuffer[SampleType] = new ListBuffer()

  // Latest sampled world.
  var lastSample: SampleType = null.asInstanceOf[SampleType]

  // The underlying sampler.
  val sampler = makeSampler

  // Create the underlying sampler.
  def makeSampler: Sampler

  // Compute next sample, print it, and add it to samples.
  def sampleOne: Unit

  // Compute next n samples.
  def sampleMany(n: Int) {
    for (i <- 1 to n) {
      sampleOne
    }
  }

  // Print query results so far.
  def printResults {
    val writer = new blog.io.TableWriter(queries)
    writer.writeResults(System.out)
  }

  // Turn verbosity on or off.
  def verbose = Util.setVerbose(true)
  def noverbose = Util.setVerbose(false)

  // Shortcuts.
  def m = model
  def e = evidence
  def q = queries
  def n = sampleOne
  def s = lastSample
  def hist = printResults
}
