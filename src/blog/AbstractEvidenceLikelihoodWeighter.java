package blog;

import blog.model.Evidence;

/**
 * An {@link EvidenceLikelihoodWeighter} that selects a part of the evidence for
 * itself, computes a likelihood sample for it, and sends the rest of the
 * evidence to a given fall-back {@link EvidenceLikelihoodWeighter}.
 */
public abstract class AbstractEvidenceLikelihoodWeighter implements
		EvidenceLikelihoodWeighter {

	public AbstractEvidenceLikelihoodWeighter(EvidenceLikelihoodWeighter fallback) {
		this.fallback = fallback;
	}

	public double likelihoodSample(Evidence evidence, PartialWorld world) {
		return likelihoodAndWeight(evidence, world).weightedLikelihood();
	}

	public LikelihoodAndWeight likelihoodAndWeight(Evidence evidence,
			PartialWorld world) {
		Evidence fallbackEvidence = keepRelevantEvidenceAndReturnTheRest(evidence);
		LikelihoodAndWeight mineLW = specificLikelihoodSample(world);
		if (mineLW.weightedLikelihood() > 0.0 && fallbackEvidence != null) {
			LikelihoodAndWeight restLW = fallback.likelihoodAndWeight(
					fallbackEvidence, world);
			mineLW.multiply(restLW);
		}
		return mineLW;
	}

	/**
	 * Returns the weighted sum of likelihood samples from this
	 * EvidenceLikelihoodWeighter.
	 */
	public double importanceSampling(Evidence evidence, PartialWorld world,
			int numSamples) {
		double sumOfRatios = 0;
		for (int i = 0; i != numSamples; i++) {
			LikelihoodAndWeight lw = likelihoodAndWeight(evidence,
					new PartialWorldDiff(world));
			sumOfRatios += lw.weightedLikelihood();
		}
		return sumOfRatios / numSamples;
	}

	public abstract Evidence keepRelevantEvidenceAndReturnTheRest(
			Evidence evidence);

	abstract public LikelihoodAndWeight specificLikelihoodSample(
			PartialWorld world);

	private EvidenceLikelihoodWeighter fallback;
}