package blog.debug

import blog.model.Evidence
import blog.model.Queries

/**
 * @author cberzan
 * @since Jun 23, 2014
 */
class ParticleFilter(feeder: ParticleFilterFeeder) {

  var currentTimestep = 0
  var finished = false

  protected def takeEvidence(evidence: Evidence) = {
    // TODO
  }

  protected def answerQueries(queries: Queries) = {
    // TODO
  }

  protected def forgetPast = {
    // TODO
  }

  /**
   * Advance to the next timestep given by the feeder.
   */
  def advance = {
    if (!finished) {
      if (!feeder.hasNext) {
        finished = true
      } else {
        val (timestep, evidence, queries) = feeder.next
        assert(timestep > currentTimestep)
        currentTimestep = timestep
        takeEvidence(evidence)
        answerQueries(queries)
        forgetPast
      }
    }
  }

  /**
   * Advance until after the given timestep.
   */
  def advanceUntil(timestep: Int) {
    Iterator.continually(advance).takeWhile(_ =>
      !finished && (currentTimestep <= timestep))
  }

  /**
   * Advance until the feeder is exhausted.
   */
  def advanceUntilFinished = {
    Iterator.continually(advance).takeWhile(_ => !finished)
  }
}