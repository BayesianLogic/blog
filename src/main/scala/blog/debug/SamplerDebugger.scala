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
import blog.BLOGUtil

/**
 * A debugger for a BLOG sampling algorithm.
 *
 * Concrete subclasses must implement the following methods:
 * - makeSampler
 * - nextSample
 *
 * @author cberzan
 * @since Jun 23, 2014
 */
abstract class SamplerDebugger[SampleType <: Sample](
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
  protected def makeSampler: Sampler

  // Compute and return next sample.
  protected def nextSample: SampleType

  // Compute next sample, print it, and update queries.
  def sampleOne = {
    lastSample = nextSample
    println(lastSample)

    // Update queries. This causes additional sampling if the world is not
    // complete enough to support the query vars.
    BLOGUtil.ensureDetAndSupported(queries.getVariables(), lastSample.world);
    queries.foreach(query => query.updateStats(
      lastSample.world, lastSample.logWeight))

    samples.append(lastSample)
    // TODO: print number of samples so far
    // TODO: stats method to print out sampler stats
  }

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
