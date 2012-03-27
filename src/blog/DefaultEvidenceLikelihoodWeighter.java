package blog;

/**
 * A default {@link EvidenceLikelihoodWeighter} that calls
 * <code>evidence.setEvidenceEnsureSupportedAndReturnLikelihood(world)</code>
 * for the given evidence and world.
 */
public class DefaultEvidenceLikelihoodWeighter implements
		EvidenceLikelihoodWeighter {

	public LikelihoodAndWeight likelihoodAndWeight(Evidence evidence,
			PartialWorld world) {
		double likelihood = evidence
				.setEvidenceEnsureSupportedAndReturnLikelihood(world);
		return new LikelihoodAndWeight(likelihood, 1.0);
	}

	public double likelihoodSample(Evidence evidence, PartialWorld world) {
		return evidence.setEvidenceEnsureSupportedAndReturnLikelihood(world);
	}

}
