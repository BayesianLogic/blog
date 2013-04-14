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

package blog.engine.onlinePF;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
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

		String useDecayedMCMCStr = properties
				.getProperty("useDecayedMCMC", "false");
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
		int numTimeSlicesInMemory =  1;
		if (evidence == null)
			evidence = new Evidence();
		if (queries == null)
			queries = new LinkedList();
		particles = new ArrayList();
		partitions = new HashMap();
		
		List a = new ArrayList();
		partitions.put(new ObservabilitySignature(makeParticle(idTypes, numTimeSlicesInMemory)), a);
		
		for (int i = 0; i < numParticles; i++) {
			Particle newParticle = makeParticle(idTypes, numTimeSlicesInMemory);
			particles.add(newParticle);
			a.add(newParticle);
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
	/*
	public void take(List<Evidence> listOfEvidence) {
		if (particles == null)
			// Util.fatalError("ParticleFilter.take(Evidence) called before initialization of particles.");
			resetAndTakeInitialEvidence();

		if (needsToBeResampledBeforeFurtherSampling) {
			move();
			resample();
		}
		if (listOfEvidence.size() != partitions.size()){
			System.err.println("partitionedparticlefilter.take: evidence list length differs from number of partitions");
			System.exit(1);
		}

		Iterator<Evidence> eit = listOfEvidence.iterator();
		for (Object canonical : partitions.keySet()){
			List particles = (List) partitions.get(canonical);
			Evidence evidence = eit.next();
			for (Iterator it = particles.iterator(); it.hasNext();) {
				Particle p = (Particle) it.next();
				p.take(evidence);
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
			needsToBeResampledBeforeFurtherSampling = true;

		}
	}
	*/

	public void take(Evidence evidence) {
		if (particles == null)
			resetAndTakeInitialEvidence();
		if (!evidence.isEmpty()) { 
			if (needsToBeResampledBeforeFurtherSampling) {
				move();
				resample();
			}
			for (Iterator it = particles.iterator(); it.hasNext();) {
				Particle p = (Particle) it.next();
				p.take(evidence);
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
			needsToBeResampledBeforeFurtherSampling = true;
		}
	}

	
	public void answer(Collection queries) {
		//System.err.println("partitionedparticlefilter.answer() should not have been called");
		//System.exit(1);
		if (particles == null)
			// Util.fatalError("ParticleFilter.take(Evidence) called before initialization of particles.");
			resetAndTakeInitialEvidence();
		for (Iterator it = particles.iterator(); it.hasNext();) {
			Particle p = (Particle) it.next();
			p.answer(queries);
		}
	}
	
	/**
	 * Irreversibly zeroes out the collection of queries and update the histogram using only 
	 * particles corresponding to the given observability signature.
	 * @param queries
	 * @param os
	 */
	public void answerWithPartition(Collection<Query> queries, ObservabilitySignature os){
		for (Query q: queries)
			q.zeroOut();
		List<Particle> partition = (List) partitions.get(os);
		for (Particle p: partition)
			p.updateQueriesStats(queries);
	}


	public void takeWithPartition(Evidence evidence, ObservabilitySignature os) {
		List particles = (List) partitions.get(os);
		
		if (particles == null){
			System.err.println("partitionedparticlefilter.takewithpartition: particles should not be null");
			System.exit(1);
		}
		if (!evidence.isEmpty()) { 
			//if (needsToBeResampledBeforeFurtherSampling) {
			//	move();
			//	resample();
			//}
			for (Iterator it = particles.iterator(); it.hasNext();) {
				Particle p = (Particle) it.next();
				p.take(evidence);
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
			//needsToBeResampledBeforeFurtherSampling = true;
		}
	}

	
	
	public void resample() {
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
		//repartition(); no longer repartition here.
	}

	public void repartition(){
		partitions.clear();
		for(Iterator i = particles.iterator(); i.hasNext();){
			Particle p = (Particle) i.next();
			if (!(p instanceof Particle)){
				int x= 1+1;
			}
			ObservabilitySignature os = new ObservabilitySignature(p);
			if (os.observedValues.size()==0){
				System.err.println("error");
				ObservabilitySignature osd = new ObservabilitySignature(p);
			}
			if (partitions.containsKey(os)){
				List partition = (List) partitions.get(os);
				partition.add(p);
			}
			else{
				List partition = new ArrayList();
				partitions.put(os, partition);
				partition.add(p);
			}
		}
	}
	
	public Map getPartitions(){
		return partitions;
	}
	

	private void move() {
	}

	private Set idTypes; // of Type

	private int numParticles;
	public List particles; // of Particles
	public Map partitions;
	private int numMoves;
	private boolean needsToBeResampledBeforeFurtherSampling = false;
	private Sampler particleSampler;
	private AfterSamplingListener afterSamplingListener;
}
