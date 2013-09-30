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

package blog.engine.onlinePF.PFEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import blog.common.Histogram;
import blog.common.Util;
import blog.engine.InferenceEngine;
import blog.engine.onlinePF.Util.Communicator;
import blog.engine.onlinePF.inverseBucket.TimedParticle;
import blog.engine.onlinePF.inverseBucket.UBT;
import blog.model.ArgSpecQuery;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.Query;
import blog.sample.Sampler;
import blog.world.AbstractPartialWorld;

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
 */
public abstract class PFEngineOnline extends InferenceEngine {

	/**
	 * Creates a new particle filter for the given BLOG model, with configuration
	 * parameters specified by the given properties table.
	 */
	public PFEngineOnline(Model model, Properties properties) {
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
		
		initialize();
	}

	/** Does not work. */
	public void answerQueries() {
		System.err.println("answerQueries should not be called");
		System.exit(1);
	}

	/**
	 * Initializes particle filter by
	 * - populating the particles arraylist
	 */
	protected void initialize() {
		System.out.println("Using " + numParticles + " particles...");
		if (evidence == null)
			evidence = new Evidence();
		if (queries == null)
			queries = new LinkedList();
		particles = new ArrayList<TimedParticle>();
		List<TimedParticle> a = new ArrayList<TimedParticle>();
		TimedParticle tp = makeParticle(idTypes);
		for (int i = 0; i < numParticles; i++) {
			TimedParticle newParticle = tp.copy();
			particles.add(newParticle);
		}
	}


	/**
	 * A method making a particle (by default, {@link Particle}). Useful for
	 * extensions using specialized particles (don't forget to specialize
	 * {@link Particle#copy()} for it to return an object of its own class).
	 */
	protected abstract TimedParticle makeParticle(Set idTypes);
	
	/**
	 * takes evidence, does not work by default
	 * @param evidence evidence to taken
	 */
	public void take(Evidence evidence) {
		if (particles == null){ 
			System.err.println("SampledPartitionedParticleFilter.take does not work right now");
			System.exit(1);
		}
		if (!evidence.isEmpty()) { 
			System.err.println("SampledPartitionedParticleFilter.take does not work right now");
			System.exit(1);
		}
	}
	
	/**
	 * answers the queries using existing particles
	 * @param queries collection of queries to be answered
	 */
	public void answer(Collection queries) {
		UBT.Stopwatch answerTimer = new UBT.Stopwatch();
		answerTimer.startTimer();
		if (particles == null)
			Util.fatalError("ParticleFilter.take(Evidence) called before initialization of particles.");
		for (TimedParticle p : particles) {
			p.answer(queries);
		}
		UBT.answerTime += answerTimer.elapsedTime();
	}
	
	/**
	 * Actually update the query stats.
	 * zeroes out the give query first
	 * @param queries
	 */
	public void updateQuery(Collection queries){
		for (Query q : (Collection<Query>)queries)
			q.zeroOut();
		for (TimedParticle p : particles) {
			p.answer(queries);
		}
	}

	/**
	 * Actions to perform before evidence is taken
	 * By default calls emptyCache only
	 */
	public void beforeTakingEvidence(){
		emptyCache();
	}
	
	/**
	 * Actions to perform after answering queries
	 * By default, increments the timestep of each particle
	 * Also calls dropHistory if the option has been set 
	 */
	public void afterAnsweringQueries(){
		for (TimedParticle p : particles)
			p.advanceTimestep();
		if (UBT.dropHistory)
			dropHistory();
		resample();
	}
	
	public void backtrack(){
		for (TimedParticle p : particles)
			p.retractTimestep();
	}
	
	/**
	 * Resamples particles according to their weight
	 */
	public void resample() {
		UBT.Stopwatch resampleTimer = new UBT.Stopwatch();
		resampleTimer.startTimer();

		double[] weights = new double[particles.size()];
		boolean[] alreadySampled = new boolean[particles.size()];
		double sum = 0.0;
		List<TimedParticle> newParticles = new ArrayList<TimedParticle>();

		for (int i = 0; i < particles.size(); i++) {
			weights[i] = ((TimedParticle) particles.get(i)).getLatestWeight();
			sum += weights[i];
		}

		if (sum == 0.0) {
			throw new IllegalArgumentException("All particles have zero weight");
		}

		for (int i = 0; i < numParticles; i++) {
			int selection = Util.sampleWithWeights(weights, sum);
			TimedParticle selectedParticle = particles.get(selection);
			if (!alreadySampled[selection])
				alreadySampled[selection] = true;
			else
				selectedParticle = selectedParticle.copy();
			newParticles.add(selectedParticle);
		}
		for (TimedParticle p : newParticles)
			p.resetWeight();
		particles = newParticles;
		UBT.resampleTime += resampleTimer.elapsedTime();
	}
	
	/**
	 * samples a single particle according to particle weight
	 */
	public TimedParticle sampleParticle(){
		double[] weights = new double[particles.size()];
		double sum = 0.0;
		for (int i = 0; i < particles.size(); i++) {
			weights[i] = ((TimedParticle) particles.get(i)).getLatestWeight();
			sum += weights[i];
		}
		if (sum == 0.0) {
			throw new IllegalArgumentException("All particles have zero weight");
		}

		int selection = Util.sampleWithWeights(weights, sum);
		TimedParticle selectedParticle = particles.get(selection);
		return selectedParticle;

	}
	
	
	/**
	 * Clears all variables associated with past time slices
	 * Also clears the decisionInterps in abstractPartialWorld
	 */
	public void dropHistory(){
		for (TimedParticle p : particles) {
			p.uninstantiatePreviousTimeslices();
			p.removeAllDerivedVars();
		}
	}
	
	/**
	 * empties cached variables (cached variables are used for efficiently updating ObservabilitySignature), should always be called
	 */
	public void emptyCache(){
		UBT.Stopwatch emptyCacheTimer = new UBT.Stopwatch();
		emptyCacheTimer.startTimer();
		for (TimedParticle p : particles)
			((AbstractPartialWorld)p.getLatestWorld()).emptyChanged();
		UBT.emptyCacheTime += emptyCacheTimer.elapsedTime();
	}
	
	/**
	 * utility method for printing queries to a communicator
	 * @param queries
	 * @param queryResultCommunicator
	 */
	public void printResultToCommunicator(Collection queries, Communicator queryResultCommunicator){
		//this.updateQuery(queries);
		for (Iterator it = queries.iterator(); it.hasNext();) {
			ArgSpecQuery query = (ArgSpecQuery) it.next();
			queryResultCommunicator.printInputNL(formatQueryString(query));
			queryResultCommunicator.printInputNL("-----");
		}
		queryResultCommunicator.printInput("");
		queryResultCommunicator.p.flush();
	}
	
	/**
	 * produces a properly formatted representation of the query string, so
	 * the policy model can parse it easily
	 * @param q
	 * @return
	 */
	protected static String formatQueryString(ArgSpecQuery q) {
		String rtn = "";
		rtn += q.getArgSpec().toString();
		Histogram histogram = q.getHistogram();
		List<Histogram.Entry> entries = new ArrayList<Histogram.Entry>(histogram.entrySet());
		for (Iterator<Histogram.Entry> iter = entries.iterator(); iter.hasNext();) {
			Histogram.Entry entry = iter.next();
			double prob = entry.getWeight() / histogram.getTotalWeight();
			rtn += ("\t[" + entry.getElement() + ":" + String.format("%.9f", prob) + "]");
		}
		return rtn;
	}
	private Set idTypes; // of Type

	int numParticles;
	public List<TimedParticle> particles; // of Particles
	protected Sampler particleSampler;
	
	/**
	 * Takes collection of decision evidence
	 * does not work by default
	 * @param evidence decisiion evidence to be taken
	 */
	public void takeDecision(Evidence evidence) {
		if (!evidence.isEmpty()){
			System.err.println("Decision evidence is not supported");
			System.exit(1);
		}
	}
}
