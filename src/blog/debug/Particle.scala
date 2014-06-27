package blog.debug

import blog.model.Model

/**
 * A wrapper for blog.engine.Particle that extends blog.debug.Particle, thus
 * allowing us to evaluate arbitrary expressions in the particle's partial
 * world.
 *
 * @author cberzan
 * @since Jun 25, 2014
 */
class Particle(model: Model, val particle: blog.engine.Particle)
  extends Sample(model, particle.getLatestWorld) {

  def logWeight = particle.getLatestLogWeight

  override def toString = s"Particle(logWeight: ${logWeight}, world: ${world})"
}