package blog.debug

import blog.engine.Particle
import blog.model.Evidence
import blog.model.Queries
import scala.collection.mutable.ListBuffer
import blog.world.DefaultPartialWorld
import blog.model.Model
import java.util.Properties
import blog.sample.LWSampler
import blog.common.Util
import blog.`type`.Timestep

/**
 * @author cberzan
 * @since Jun 23, 2014
 */
class ParticleFilter(
  val model: Model,
  val numParticles: Int,
  feeder: FilterFeeder) {

  object State extends Enumeration {
    type State = Value
    val TakingInitialEvidence, TemporalLoop, TakingFinalQueries, Finished = Value
  }
  import State._

  var state = TakingInitialEvidence
  var currentTimestep: Int = null.asInstanceOf[Int]
  var particles: List[Particle] = null

  protected def createInitialParticles: Unit = {
    val newParticles = new ListBuffer[Particle]
    val sampler = new LWSampler(model, new Properties)
    val idTypes = model.getListedTypes("none");
    for (i <- 1 to numParticles) {
      val world = new DefaultPartialWorld(idTypes);
      newParticles.append(new Particle(sampler, world))
    }
    particles = newParticles.toList
  }

  protected def resample: Unit = {
    val logWeights = particles.map(particle => particle.getLatestLogWeight())
    val logSumWeights = logWeights.reduceLeft(Util.logSum)
    if (logSumWeights == Double.NegativeInfinity) {
      throw new IllegalArgumentException("All particles have zero weight");
    }
    val normalizedWeights = logWeights.map(logw => Math.exp(logw - logSumWeights)).toArray
    val alreadySampled = Array.fill[Boolean](numParticles)(false)
    val oldParticles = particles.toArray
    val newParticles = new ListBuffer[Particle]
    for (i <- 1 to numParticles) {
      val selection = Util.sampleWithProbs(normalizedWeights)
      if (!alreadySampled(selection)) {
        newParticles.append(oldParticles(selection))
        alreadySampled(selection) = true
      } else {
        newParticles.append(oldParticles(selection).copy())
      }
    }
    particles = newParticles.toList
  }

  protected def takeEvidence(evidence: Evidence): Unit = {
    // TODO verify that evidence is either atemporal, or it is for currentTimestep.
    if (evidence != null && !evidence.isEmpty) {
      particles.foreach(particle => particle.take(evidence))
    }
  }

  protected def answerQueries(queries: Queries): Unit = {
    // TODO verify that queries are either atemporal, or they are for currentTimestep.
    if (queries != null && !queries.isEmpty) {
      particles.foreach(particle => particle.answer(queries))
    }
  }

  protected def forgetPast: Unit = {
    particles.foreach(particle =>
      particle.removePriorTimeSlice(Timestep.at(currentTimestep)))
  }

  /**
   * Advance to the next timestep given by the feeder.
   */
  def advance: Unit = {
    if (state == TakingInitialEvidence) {
      createInitialParticles
      val evidence = feeder.initialEvidence
      takeEvidence(evidence)
      state = TemporalLoop
      currentTimestep = 0
    } else if (state == TemporalLoop) {
      if (!feeder.hasNext) {
        state = TakingFinalQueries
        advance
      } else {
        val (timestep, evidence, queries) = feeder.next
        assert(timestep > currentTimestep)
        currentTimestep = timestep
        resample
        takeEvidence(evidence)
        answerQueries(queries)
        forgetPast
      }
    } else if (state == TakingFinalQueries) {
      val queries = feeder.finalQueries
      answerQueries(queries)
      state = Finished
    } else if (state == Finished) {
      println("advance called, but PF is already finished")
    }
  }

  /**
   * Advance until after the given timestep.
   */
  def advanceUntil(timestep: Int): Unit = {
    Iterator.continually(advance).takeWhile(_ =>
      state != Finished && (currentTimestep <= timestep))
  }

  /**
   * Advance until the feeder is exhausted.
   */
  def advanceUntilFinished: Unit = {
    Iterator.continually(advance).takeWhile(_ => state != Finished)
  }
}