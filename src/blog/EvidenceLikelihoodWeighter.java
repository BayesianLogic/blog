package blog;

import blog.model.Evidence;

/**
 * An interface for classes that know how to do sampling in order to evaluate
 * evidence likelihood. Typically, they will operate on some part of the
 * evidence with a specific structure that can be sampled efficiently (with
 * specific importance sampling, for example) and pass the rest to a fall-back
 * weighter.
 */
public interface EvidenceLikelihoodWeighter {
	/** Given evidence and world, returns a sample of the evidence's likelihood. */
	public LikelihoodAndWeight likelihoodAndWeight(Evidence evidence,
			PartialWorld world);

	/**
	 * Must return the result of
	 * <code>likelihoodAndWeight(evidence, world).weightedLikelihood()</code>.
	 */
	public double likelihoodSample(Evidence evidence, PartialWorld world);
}
