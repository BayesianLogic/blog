package test.blog;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import blog.BLOGUtil;
import blog.Main;
import blog.common.Histogram;
import blog.common.UnaryFunction;
import blog.common.Util;
import blog.engine.InferenceEngine;
import blog.engine.ParticleFilter;
import blog.engine.SamplingEngine;
import blog.model.ArgSpecQuery;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.ModelEvidenceQueries;

/**
 * Compares the particle filter and likelihood weighting by running a temporal
 * query with increasing numbers of samples or particles and keeping track of
 * the results variance. We should see a faster decrease in variance for the
 * particle filtering.
 * 
 * The query is hard-coded for the PF and in a file for LW
 * (examples/ParticleFilteringVsLikelihoodWeightingMay2008Experiment.eqblog) (in
 * the latter case, we are simply doing BLOG inference from files). The reason
 * it is hard coded for PF is that reading a query from a file would present the
 * query as a whole at once to the algorithm (this is the normal BLOG semantic
 * for them), but PF takes them one time step at a time and has an API suited
 * for that, which is used here.
 * 
 * The model is read, for both algorithms, from
 * examples/ParticleFilteringVsLikelihoodWeightingMay2008Experiment.mblog
 * 
 * @author Rodrigo
 */
public class ParticleFilteringVsLikelihoodWeightingMay2008Experiment {

	// Experiments parameters:
	private static String queryValueString = "2"; // The query value whose
																								// probability is being
																								// computed.
	private static int numTrials = 5;

	private static int initialNumSamplesOrParticles = 1000;
	private static int finalNumSamplesOrParticles = 10000;
	private static int incrementNumSamplesOrParticles = 500;

	private static String modelFilename = "examples/ParticleFilteringVsLikelihoodWeightingMay2008Experiment.mblog";
	private static String evidenceAndQueryFilename = "examples/ParticleFilteringVsLikelihoodWeightingMay2008Experiment.eqblog";

	// Used by LW only.

	public static void main(String[] args) throws Exception {
		Util.initRandom(true);

		List variances = collectVariances();

		/* Print them out */
		for (Iterator it = variances.iterator(); it.hasNext();) {
			Object[] result = (Object[]) it.next();
			System.out.println(result[0] + ", " + result[1] + ", " + result[2]);
		}
	}

	/** Collect variances of LW and PF as number of sample/particles increases. */
	private static List collectVariances() throws Exception {

		List results = new LinkedList();
		for (int currentNumberOfSamplesOrParticles = initialNumSamplesOrParticles; currentNumberOfSamplesOrParticles != finalNumSamplesOrParticles; currentNumberOfSamplesOrParticles += incrementNumSamplesOrParticles) {
			double lwVariance = collectVariance(numTrials,
					currentNumberOfSamplesOrParticles, probFromLWTrialFunction);
			double pfVariance = collectVariance(numTrials,
					currentNumberOfSamplesOrParticles,
					probFromParticleFilterTrialFunction);
			Object[] result = { currentNumberOfSamplesOrParticles, pfVariance,
					lwVariance };
			results.add(result);
		}
		return results;
	}

	/**
	 * Answers the designated query a certain number of trials using a given
	 * function and returns the answers variance.
	 */
	private static double collectVariance(int numTrials,
			int currentNumberOfSamplesOrParticles, UnaryFunction probFromTrial)
			throws Exception {

		Collection probs = new LinkedList();

		for (int i = 0; i != numTrials; i++) {
			probs.add(probFromTrial.evaluate(currentNumberOfSamplesOrParticles));
		}

		System.out.println("Probs: " + probs);
		System.out.println("Mean: " + Util.mean(probs));
		System.out.println("Variance: " + Util.variance(probs));

		return Util.variance(probs);
	}

	private static double probFromParticleFilterTrial(
			int currentNumberOfSamplesOrParticles) throws Exception {
		Model model = new Model();
		Collection filenames = new LinkedList();
		filenames.add(modelFilename);
		Main.setup(model, new Evidence(), new LinkedList(),
				Main.makeReaders(filenames), new LinkedList(), false, false);
		ParticleFilter particleFilter = new ParticleFilter(model,
				getParticleFilterProperties(currentNumberOfSamplesOrParticles));

		takeStep(particleFilter, model, "obs {Blip b: Time(b) = @0} = {B01, B02};");
		takeStep(particleFilter, model, "obs {Blip b: Time(b) = @1} = {B11, B12};");
		takeStep(particleFilter, model, "obs {Blip b: Time(b) = @2} = {B21, B22};");
		String queryString = "query #{Blip r : Time(r) = @3};";

		return valueProbability(particleFilter, model, queryString,
				queryValueString);
	}

	private static double probFromLWTrial(int currentNumberOfSamplesOrParticles)
			throws Exception {
		Model model = new Model();
		Evidence evidence = new Evidence();
		List queries = new LinkedList();
		List filenames = new LinkedList();
		filenames.add(modelFilename);
		filenames.add(evidenceAndQueryFilename);
		Main.setup(model, evidence, queries, Main.makeReaders(filenames),
				new LinkedList(), false /* verbose */, false /* parseFromMessage */);
		InferenceEngine lw = new SamplingEngine(model,
				getSamplingProperties(currentNumberOfSamplesOrParticles));
		lw.setEvidence(evidence);
		lw.setQueries(queries);
		lw.answerQueries();
		return valueProbability(queries, queryValueString);
	}

	static private Properties getParticleFilterProperties(
			int currentNumberOfSamplesOrParticles) {
		Properties properties = new Properties();
		properties.setProperty("numParticles",
				Integer.toString(currentNumberOfSamplesOrParticles));
		properties.setProperty("useDecayedMCMC", "true");
		properties.setProperty("numMoves", "1");
		return properties;
	}

	static private Properties getSamplingProperties(
			int currentNumberOfSamplesOrParticles) {
		Properties properties = new Properties();
		properties.setProperty("numSamples",
				Integer.toString(currentNumberOfSamplesOrParticles));
		return properties;
	}

	static private void takeStep(ParticleFilter particleFilter, Model model,
			String evidenceAndQuery) throws Exception {
		ModelEvidenceQueries meq = BLOGUtil.parseAndTranslateFromString(model,
				evidenceAndQuery);
		particleFilter.take(meq.evidence);
		particleFilter.answer(meq.queries);
	}

	static private double valueProbability(ParticleFilter particleFilter,
			Model model, String queryString, String valueString) throws Exception {
		ModelEvidenceQueries meq = BLOGUtil.parseAndTranslateFromString(model,
				queryString);
		particleFilter.answer(meq.queries);
		return valueProbability(meq.queries, valueString);
	}

	private static double valueProbability(List queries, String valueString)
			throws Exception {
		ArgSpecQuery query = getQuery(queries);
		outputQueries(queries);
		for (Iterator it = query.getHistogram().entrySet().iterator(); it.hasNext();) {
			Histogram.Entry entry = (Histogram.Entry) it.next();
			if (valueString.equals(entry.getElement().toString())) {
				double prob = entry.getWeight() / query.getHistogram().getTotalWeight();
				return prob;
			}
		}
		throw new Exception(valueString + " not present.");
	}

	static private void outputQueries(Collection queries) {
		for (Iterator it = queries.iterator(); it.hasNext();) {
			ArgSpecQuery query = (ArgSpecQuery) it.next();
			for (Iterator it2 = query.getHistogram().entrySet().iterator(); it2
					.hasNext();) {
				Histogram.Entry entry = (Histogram.Entry) it2.next();
				double prob = entry.getWeight() / query.getHistogram().getTotalWeight();
				System.out.println("Prob. of " + query + " = " + entry.getElement()
						+ " is " + prob);
			}
		}
	}

	/**
	 * Gets a collection assumed to contain a single query and returns that query.
	 */
	static private ArgSpecQuery getQuery(Collection singleton) {
		return (ArgSpecQuery) Util.getFirst(singleton);
	}

	private static UnaryFunction probFromParticleFilterTrialFunction = new UnaryFunction() {
		public Object evaluate(Object currentNumberOfSamplesOrParticlesObject) {
			try {
				int currentNumberOfSamplesOrParticles = ((Integer) currentNumberOfSamplesOrParticlesObject)
						.intValue();
				return probFromParticleFilterTrial(currentNumberOfSamplesOrParticles);
			} catch (Exception e) {
				Util.fatalError(e.getMessage());
			}
			return null;
		}
	};

	private static UnaryFunction probFromLWTrialFunction = new UnaryFunction() {
		public Object evaluate(Object currentNumberOfSamplesOrParticlesObject) {
			try {
				int currentNumberOfSamplesOrParticles = ((Integer) currentNumberOfSamplesOrParticlesObject)
						.intValue();
				return probFromLWTrial(currentNumberOfSamplesOrParticles);
			} catch (Exception e) {
				Util.fatalError(e.getMessage());
			}
			return null;
		}
	};
}
