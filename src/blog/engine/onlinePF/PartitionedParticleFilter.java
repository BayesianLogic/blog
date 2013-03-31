
package blog.engine.onlinePF;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.Set;

import blog.DBLOGUtil;
import blog.common.Util;
import blog.engine.InferenceEngine;
import blog.engine.Particle;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.Query;
import blog.sample.AfterSamplingListener;
import blog.sample.DMHSampler;
import blog.sample.Sampler;

/**
 * A Particle Filter. It works by keeping a set of {@link Particles}, each
 * representing a partial world, weighted by the
 * evidence. It uses the following properties: <code>numParticles</code> or
 * <code>numSamples</code>: number of particles (default is <code>1000</code>).
 * <code>useDecayedMCMC</code>: takes values <code>true</code> or
 * <code>false</code> (the default). Whether to use rejuvenation, presented by
 * W. R. Gilks and C. Berzuini.
 * "Following a moving target --- Monte Carlo inference for dynamic Bayesian models."
 * Journal of the Royal Statistical Society, Series B, 63:127--146, 2001.
 * <code>numMoves</code>: the number of moves used in specified by the property
 * given at construction time (default is <code>1</code>).
 * 
 * The ParticleFilter is an unusual {@link InferenceEngine} in that it takes
 * evidence and queries additional to the ones taken by
 * {@link #setEvidence(Evidence)} and {@link #setQueries(List)}. The evidence
 * set by {@link #setEvidence(Evidence)} is used in the very beginning of
 * inference (thus keeping the general InferenceEngine semantics for it) and the
 * queries set by {@link #setQueries(List)} are used by {@link #answerQueries()}
 * only (again keeping the original InferenceEngine semantics).
 */
public class PartitionedParticleFilter extends InferenceEngine {

	/**
	 * Creates a new particle filter for the given BLOG model, with configuration
	 * parameters specified by the given properties table.
	 */
	public PartitionedParticleFilter(Model model, Properties properties) {
		super(model);

		String numParticlesStr = properties.getProperty("numParticles");
		String numSamplesStr = properties.getProperty("numSamples");
		if (numParticlesStr != null && numSamplesStr != null
				&& !numParticlesStr.equals(numSamplesStr))
			Util.fatalError("ParticleFilter received both numParticles and numSamples properties with distinct values.");
		if (numParticlesStr == null)
			numParticlesStr = numSamplesStr;
		if (numParticlesStr == null)
			numParticlesStr = "1000";
		try {
			numParticles = Integer.parseInt(numParticlesStr);
		} catch (NumberFormatException e) {
			Util.fatalErrorWithoutStack("Invalid number of particles: "
					+ numParticlesStr); // do not dump stack.
		}

		String numMovesStr = properties.getProperty("numMoves", "1");
		try {
			Integer.parseInt(numMovesStr);
		} catch (NumberFormatException e) {
			Util.fatalErrorWithoutStack("Invalid number of moves: " + numMovesStr);
		}

		String idTypesString = properties.getProperty("idTypes", "none");
		idTypes = model.getListedTypes(idTypesString);
		if (idTypes == null) {
			Util.fatalErrorWithoutStack("Fatal error: invalid idTypes list.");
		}

		String samplerClassName = properties.getProperty("samplerClass",
				"blog.sample.LWSampler");
		System.out.println("Constructing sampler of class " + samplerClassName);
		particleSampler = Sampler.make(samplerClassName, model, properties);
	}

	/** Answers the queries provided at construction time. */
	public void answerQueries() {
		System.out.println("Evidence: " + evidence);
		System.out.println("Query: " + queries);
		resetAndTakeInitialEvidence();
		for (Iterator<ObservabilitySignature> i = partitionedParticles.keySet().iterator(); i.hasNext();)
			answer(queries, partitionedParticles.get(i.next()));
	}

	/**
	 * Prepares particle filter for a new sequence of evidence and queries by
	 * generating a new set of particles from scratch, which are consistent with
	 * evidence set by {@link #setEvidence(Evidence)} (if it has not been invoked,
	 * empty evidence is assumed), ensuring behavior consistent with other
	 * {@link InferenceEngine}s.
	 */
	public void resetAndTakeInitialEvidence() {
		reset();
		takeInitialEvidence();
	}

	private void reset() {
		System.out.println("Using " + numParticles + " particles...");
		int numTimeSlicesInMemory = 1;
		if (evidence == null)
			evidence = new Evidence();
		if (queries == null)
			queries = new LinkedList<Query>();
		partitionedParticles = new HashMap<ObservabilitySignature, List<Particle>>();
		ArrayList<Particle> firstPartition = new ArrayList<Particle>();
		partitionedParticles.put(new ObservabilitySignature(makeParticle(idTypes, numTimeSlicesInMemory)),firstPartition);
		for (int i = 0; i < numParticles; i++) {
			Particle newParticle = makeParticle(idTypes, numTimeSlicesInMemory);
			firstPartition.add(newParticle);
		}
		needsToBeResampledBeforeFurtherSampling = false;
	}

	private List<Evidence> evidenceInOrderOfMaxTimestep;

	private void takeInitialEvidence() {
		if (evidenceInOrderOfMaxTimestep == null)
			evidenceInOrderOfMaxTimestep = DBLOGUtil.splitEvidenceByMaxTimestep(evidence);

		for (Iterator<Evidence> it = evidenceInOrderOfMaxTimestep.iterator(); it.hasNext();) {
			Evidence evidenceSlice = (Evidence) it.next();
			for (Iterator<ObservabilitySignature> i = partitionedParticles.keySet().iterator(); i.hasNext();)
				take(evidenceSlice, partitionedParticles.get(i.next()));
		}
	}

	/**
	 * A method making a particle (by default, {@link Particle}). Useful for
	 * extensions using specialized particles (don't forget to specialize
	 * {@link Particle#copy()} for it to return an object of its own class).
	 */
	protected HashableParticle makeParticle(Set idTypes, int numTimeSlicesInMemory) {
		return new HashableParticle(idTypes, numTimeSlicesInMemory, particleSampler);
	}

	/** Takes more evidence. */
	public void take(Evidence evidence, List<Particle> partition) {
		if (partition == null)
			resetAndTakeInitialEvidence();

		if (!evidence.isEmpty()) {
			if (needsToBeResampledBeforeFurtherSampling) {
				resample();
			}

			for (Iterator<Particle> it = partition.iterator(); it.hasNext();) {
				Particle p = (Particle) it.next();
				p.take(evidence);
			}

			ListIterator<Particle> particleIt = partition.listIterator();
			while (particleIt.hasNext()) {
				Particle particle = (Particle) particleIt.next();
				if (particle.getLatestWeight() == 0.0) {
					particleIt.remove();
				} else {
				}
			}

			if (partition.size() == 0)
				throw new IllegalArgumentException("All particles have zero weight");
			needsToBeResampledBeforeFurtherSampling = true;
		}
	}

	/**
	 * Answer queries according to current distribution represented by filter.
	 */
	public void answer(Collection<Query> queries, List<Particle> partition) {
		if (partition == null)
			resetAndTakeInitialEvidence();
		for (Iterator<Particle> it = partition.iterator(); it.hasNext();) {
			Particle p = (Particle) it.next();
			p.answer(queries);
		}
	}

	public void answer(Query query, List<Particle> partition) {
		answer(Util.list(query), partition);
	}

	private void resample(ObservabilitySignature obs) {
		List<Particle> partition = partitionedParticles.get(obs);
		double[] weights = new double[partition.size()];
		boolean[] alreadySampled = new boolean[partition.size()];
		double sum = 0.0;
		List<Particle> newParticles = new ArrayList<Particle>();

		for (int i = 0; i < partition.size(); i++) {
			weights[i] = ((Particle) partition.get(i)).getLatestWeight();
			sum += weights[i];
		}

		if (sum == 0.0) {
			throw new IllegalArgumentException("All particles have zero weight");
		}
		
		for (int i = 0; i < numParticles; i++) {
			int selection = Util.sampleWithWeights(weights, sum);
			if (!alreadySampled[selection]) {
				newParticles.add(partition.get(selection));
				alreadySampled[selection] = true;
			} else
				newParticles.add(((Particle) partition.get(selection)).copy());
		}

		partition = newParticles;
	}
	private Set idTypes; // of Type
	private int numParticles;
	public HashMap<ObservabilitySignature, List<Particle>> partitionedParticles; // of Particles
	private boolean needsToBeResampledBeforeFurtherSampling = false;
	private Sampler particleSampler;
}
