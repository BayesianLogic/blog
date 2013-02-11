/*
 * Copyright (c) 2005, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.  
 *
 * * Neither the name of the University of California, Berkeley nor
 *   the names of its contributors may be used to endorse or promote
 *   products derived from this software without specific prior 
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package blog.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.Set;

import blog.DBLOGUtil;
import blog.common.Util;
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
public class ParticleFilter extends InferenceEngine {

	/**
	 * Creates a new particle filter for the given BLOG model, with configuration
	 * parameters specified by the given properties table.
	 */
	public ParticleFilter(Model model, Properties properties) {
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

		String useDecayedMCMCStr = properties
				.getProperty("useDecayedMCMC", "false");
		useDecayedMCMC = Boolean.parseBoolean(useDecayedMCMCStr);
		// if (useDecayedMCMC) numParticles = 1;

		String numMovesStr = properties.getProperty("numMoves", "1");
		try {
			numMoves = Integer.parseInt(numMovesStr);
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

		if (useDecayedMCMC)
			dmhSampler = new DMHSampler(model, properties);
	}

	/** Answers the queries provided at construction time. */
	public void answerQueries() {
		System.out.println("Evidence: " + evidence);
		System.out.println("Query: " + queries);
		resetAndTakeInitialEvidence();
		answer(queries);
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
		int numTimeSlicesInMemory = useDecayedMCMC ? dmhSampler.getMaxRecall() : 1;
		if (evidence == null)
			evidence = new Evidence();
		if (queries == null)
			queries = new LinkedList();
		if (useDecayedMCMC)
			dmhSampler.initialize(evidence, queries);
		particles = new ArrayList();
		for (int i = 0; i < numParticles; i++) {
			Particle newParticle = makeParticle(idTypes, numTimeSlicesInMemory);
			particles.add(newParticle);
		}
		needsToBeResampledBeforeFurtherSampling = false;
	}

	private List evidenceInOrderOfMaxTimestep;

	private void takeInitialEvidence() {
		if (evidenceInOrderOfMaxTimestep == null)
			evidenceInOrderOfMaxTimestep = DBLOGUtil
					.splitEvidenceByMaxTimestep(evidence);

		for (Iterator it = evidenceInOrderOfMaxTimestep.iterator(); it.hasNext();) {
			Evidence evidenceSlice = (Evidence) it.next();
			take(evidenceSlice);
		}
	}

	/**
	 * A method making a particle (by default, {@link Particle}). Useful for
	 * extensions using specialized particles (don't forget to specialize
	 * {@link Particle#copy()} for it to return an object of its own class).
	 */
	protected Particle makeParticle(Set idTypes, int numTimeSlicesInMemory) {
		return new Particle(idTypes, numTimeSlicesInMemory, particleSampler);
	}

	/** Takes more evidence. */
	public void take(Evidence evidence) {
		if (particles == null)
			// Util.fatalError("ParticleFilter.take(Evidence) called before initialization of particles.");
			resetAndTakeInitialEvidence();

		if (!evidence.isEmpty()) { // must be placed after check on particles ==
																// null because after this method the filter
																// should be ready to take queries.

			if (needsToBeResampledBeforeFurtherSampling) {
				move();
				resample();
			}

			if (beforeTakesEvidence != null)
				beforeTakesEvidence.evaluate(evidence, this);

			for (Iterator it = particles.iterator(); it.hasNext();) {
				Particle p = (Particle) it.next();

				if (beforeParticleTakesEvidence != null)
					beforeParticleTakesEvidence.evaluate(p, evidence, this);
				p.take(evidence);
				if (afterParticleTakesEvidence != null)
					afterParticleTakesEvidence.evaluate(p, evidence, this);

//				if (!useDecayedMCMC) {
//					p.uninstantiatePreviousTimeslices();
//					p.removeAllDerivedVars();
//				}
			}

			double sum = 0;
			ListIterator particleIt = particles.listIterator();
			while (particleIt.hasNext()) {
				Particle particle = (Particle) particleIt.next();
				if (particle.getLatestWeight() == 0.0) {
					particleIt.remove();
				} else
					sum += particle.getLatestWeight();
			}

			if (particles.size() == 0)
				throw new IllegalArgumentException("All particles have zero weight");

			// System.out.println("PF: Num of particles after taking evidence: " +
			// particles.size());
			// System.out.println("PF: Sum of weights after taking evidence: " + sum);

			needsToBeResampledBeforeFurtherSampling = true;

			if (useDecayedMCMC)
				dmhSampler.add(evidence);

			if (afterTakesEvidence != null)
				afterTakesEvidence.evaluate(evidence, this);

		}
	}

	/**
	 * Answer queries according to current distribution represented by filter.
	 */
	public void answer(Collection queries) {
		if (particles == null)
			// Util.fatalError("ParticleFilter.take(Evidence) called before initialization of particles.");
			resetAndTakeInitialEvidence();

		// System.out.println("PF: Updating queries with PF with " +
		// particles.size() + " particles.");
		for (Iterator it = particles.iterator(); it.hasNext();) {
			Particle p = (Particle) it.next();
			p.answer(queries);
		}
		if (useDecayedMCMC)
			dmhSampler.addQueries(queries);
	}

	public void answer(Query query) {
		answer(Util.list(query));
	}

	private void resample() {
		double[] weights = new double[particles.size()];
		boolean[] alreadySampled = new boolean[particles.size()];
		double sum = 0.0;
		List newParticles = new ArrayList();

		for (int i = 0; i < particles.size(); i++) {
			weights[i] = ((Particle) particles.get(i)).getLatestWeight();
			sum += weights[i];
		}

		if (sum == 0.0) {
			throw new IllegalArgumentException("All particles have zero weight");
		}
		// else
		// System.out.println("PF.resample: sum of all particle weights is " + sum);

		for (int i = 0; i < numParticles; i++) {
			int selection = Util.sampleWithWeights(weights, sum);
			if (!alreadySampled[selection]) {
				newParticles.add(particles.get(selection));
				alreadySampled[selection] = true;
			} else
				newParticles.add(((Particle) particles.get(selection)).copy());
		}

		particles = newParticles;
	}

	private void printWeights() {
		for (int i = 0; i < particles.size(); i++) {
			System.out.println(i + ":"
					+ ((Particle) particles.get(i)).getLatestWeight());
		}
		System.out.println();
	}

	private void move() {
		if (!useDecayedMCMC)
			return;

		for (int i = 0; i < numMoves; i++) {
			for (Iterator iter = particles.iterator(); iter.hasNext();) {
				Particle p = (Particle) iter.next();
				p.setWorld(dmhSampler.nextSample(p.getLatestWorld()));
			}
		}
	}

	// ///////////////////////// PARTICLE TAKES EVIDENCE EVENT HANDLING
	// ///////////////////////////
	/**
	 * An interface specifying handlers for before and after a particle takes
	 * evidence.
	 */
	public static interface ParticleTakesEvidenceHandler {
		public void evaluate(Particle particle, Evidence evidence,
				ParticleFilter particleFilter);
	}

	/**
	 * The {@link ParticleTakesEvidenceHandler} invoked right before a particle
	 * takes evidence.
	 */
	public ParticleTakesEvidenceHandler beforeParticleTakesEvidence;

	/**
	 * The {@link ParticleTakesEvidenceHandler} invoked right after a particle
	 * takes evidence.
	 */
	public ParticleTakesEvidenceHandler afterParticleTakesEvidence;

	// ///////////////////////// FILTER TAKES EVIDENCE EVENT HANDLING
	// ///////////////////////////

	/**
	 * An interface specifying handlers for before and after the particle filter
	 * takes evidence.
	 */
	public static interface TakesEvidenceHandler {
		public void evaluate(Evidence evidence, ParticleFilter particleFilter);
	}

	/**
	 * The {@link TakesEvidenceHandler} invoked right before a particle takes
	 * evidence.
	 */
	public TakesEvidenceHandler beforeTakesEvidence;

	/**
	 * The {@link TakesEvidenceHandler} invoked right after a particle takes
	 * evidence.
	 */
	public TakesEvidenceHandler afterTakesEvidence;

	// ///////////////////////// END OF EVENT HANDLING ///////////////////////////

	public AfterSamplingListener getAfterSamplingListener() {
		return afterSamplingListener;
	}

	public void setAfterSamplingListener(
			AfterSamplingListener afterSamplingListener) {
		this.afterSamplingListener = afterSamplingListener;
		particleSampler.afterSamplingListener = afterSamplingListener;
	}

	private Set idTypes; // of Type

	private int numParticles;
	private boolean useDecayedMCMC;
	public List particles; // of Particles
	private int numMoves;
	private boolean needsToBeResampledBeforeFurtherSampling = false;
	private Sampler particleSampler;
	private AfterSamplingListener afterSamplingListener;
	private DMHSampler dmhSampler;
}
