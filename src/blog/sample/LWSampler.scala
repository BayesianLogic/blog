package blog.sample

import blog.model.Evidence
import blog.model.Model
import blog.model.Queries
import blog.world.PartialWorld
import blog.BLOGUtil
import blog.world.DefaultPartialWorld

/**
 * Likelihood-Weighting sampler.
 *
 * @author cberzan
 * @since Jun 26, 2014
 */
class LWSampler(model: Model, evidence: Evidence, queries: Queries)
  extends Sampler[LWSample](model, evidence, queries) {

  protected val idTypes = model.getListedTypes("none")
  protected val queryVars = queries.getVariables

  /**
   * Return a new sample starting from the given world.
   */
  def sampleInWorld(world: PartialWorld): LWSample = {
    // Ensure evidence is supported and compute its likelihood.
    evidence.setEvidenceAndEnsureSupported(world)
    val logWeight = evidence.getEvidenceLogProb(world)

    // Ensure queries are supported too.
    BLOGUtil.ensureDetAndSupported(queryVars, world)

    new LWSample(model, world, logWeight)
  }

  /**
   * Return a new sample starting from an empty world.
   */
  def sample: LWSample = sampleInWorld(new DefaultPartialWorld(idTypes))

}