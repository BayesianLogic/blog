package blog.debug

import java.util.Properties
import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

import blog.common.Util
import blog.Main
import blog.model.Evidence
import blog.model.Queries
import blog.model.Model
import blog.sample.LWSampler
import blog.`type`.Timestep
import blog.world.DefaultPartialWorld

/**
 * Particle filter.
 *
 * Currently used only from the interactive shell.
 * In the future this will be unified with the java ParticleFilter code.
 *
 * You can run the ParticleFilter in batch mode using OfflineFilterFeeder.
 * If you want to run the ParticleFilter online, you have to write your own
 * FilterFeeder to provide evidence and queries at each timestep.
 *
 * @author cberzan
 * @since Jun 23, 2014
 */
class ParticleFilter(
  val model: Model,
  val numParticles: Int,
  feeder: FilterFeeder) {

  var currentTimestep: Int = -1
  var currentEvidence: Evidence = null
  var currentQueries: Queries = null
  var particles: List[Particle] = null

  protected def createInitialParticles: Unit = {
    val newParticles = new ListBuffer[Particle]
    val sampler = new LWSampler(model, new Properties)
    val idTypes = model.getListedTypes("none");
    for (i <- 1 to numParticles) {
      val world = new DefaultPartialWorld(idTypes);
      val underlyingParticle = new blog.engine.Particle(sampler, world)
      newParticles.append(new Particle(model, underlyingParticle))
    }
    particles = newParticles.toList
  }

  protected def resample: Unit = {
    val logWeights = particles.map(particle => particle.logWeight)
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
        val underlyingParticle = oldParticles(selection).particle.copy()
        newParticles.append(new Particle(model, underlyingParticle))
      }
    }
    particles = newParticles.toList
  }

  protected def takeEvidence(evidence: Evidence): Unit = {
    // TODO verify that evidence is either atemporal, or it is for currentTimestep.
    if (evidence != null && !evidence.isEmpty) {
      particles.foreach(particle => particle.particle.take(evidence))
    }
  }

  protected def answerQueries(queries: Queries): Unit = {
    // TODO verify that queries are either atemporal, or they are for currentTimestep.
    if (queries != null && !queries.isEmpty) {
      particles.foreach(particle => particle.particle.answer(queries))
    }
  }

  protected def forgetPast: Unit = {
    particles.foreach(particle =>
      particle.particle.removePriorTimeSlice(Timestep.at(currentTimestep)))
  }

  /**
   * Advance to the next timestep given by the feeder.
   */
  def advance: Queries = {
    if (!feeder.hasNext) {
      println("advance: finished")
      null
    } else {
      val (timestep, evidence, queries) = feeder.next
      assert(timestep >= currentTimestep)
      currentTimestep = timestep
      currentEvidence = evidence
      currentQueries = queries
      if (particles == null) {
        createInitialParticles
      } else {
        resample
      }
      takeEvidence(currentEvidence)
      answerQueries(currentQueries)
      forgetPast
      println(s"advance: processed timestep ${timestep}")
      currentQueries
    }
  }

  /**
   * Advance until after the given timestep.
   */
  def advanceUntil(timestep: Int): Unit = {
    while (feeder.hasNext && currentTimestep < timestep) {
      advance
    }
  }

  /**
   * Advance until the feeder is exhausted.
   */
  def advanceUntilFinished: Unit = {
    while (feeder.hasNext) {
      advance
    }
  }
}

object ParticleFilter {
  def make(path: String, numParticles: Int): ParticleFilter = {
    Util.initRandom(false)

    val model = new Model()
    val evidence = new Evidence(model)
    val queries = new Queries(model)
    Main.simpleSetupFromFiles(model, evidence, queries, path :: Nil)

    val feeder = new OfflineFilterFeeder(evidence, queries)
    new ParticleFilter(model, numParticles, feeder)
  }
}
