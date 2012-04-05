package blog;

import java.util.Properties;

import blog.model.Model;

/**
 * An evidence likelihood weighting sampler that uses a chain of
 * {@link EvidenceLikelihoodWeighter}s. Currently, the chain is formed by
 * {@link SymbolEvidenceLikelihoodWeighter} and
 * {@link TupleSetSpecEvidenceLikelihoodWeighter}. Ideally, it should include
 * all {@link EvidenceLikelihoodWeighter} developed.
 */
public class LWImportanceSampler extends LWSampler {

	public LWImportanceSampler(Model model, Properties properties) {
		super(model, properties);
		weighter = new SymbolEvidenceLikelihoodWeighter(
				new TupleSetSpecEvidenceLikelihoodWeighter());
	}

	protected double calculateWeight() {
		return weighter.likelihoodAndWeight(evidence, curWorld)
				.weightedLikelihood();
	}

	private static EvidenceLikelihoodWeighter weighter;
}
