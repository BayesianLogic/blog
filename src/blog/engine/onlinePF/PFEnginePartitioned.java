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
import java.util.Arrays;
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
import blog.engine.onlinePF.inverseBucket.TimedParticle;
import blog.engine.onlinePF.inverseBucket.UBT;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.Query;
import blog.sample.AfterSamplingListener;
import blog.sample.DMHSampler;
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
 * The ParticleFilter is an unusual {@link InferenceEngine} in that it takes
 * evidence and queries additional to the ones taken by
 * {@link #setEvidence(Evidence)} and {@link #setQueries(List)}. The evidence
 * set by {@link #setEvidence(Evidence)} is used in the very beginning of
 * inference (thus keeping the general InferenceEngine semantics for it) and the
 * queries set by {@link #setQueries(List)} are used by {@link #answerQueries()}
 * only (again keeping the original InferenceEngine semantics).
 */
public abstract class PFEnginePartitioned extends PFEngineOnline {



	public PFEnginePartitioned(Model model, Properties properties) {
		super(model, properties);
	}

	
	/**
	 * On top of creating particles, also create partitions
	 */
	@Override
	protected void initialize() {
		super.initialize();
		partitions = new HashMap<Integer, List<TimedParticle>>();
		ArrayList<TimedParticle> a = new ArrayList<TimedParticle>();		
		partitions.put(((TimedParticle)Util.getFirst(particles)).getOS(), a);
		for (Particle p : (particles)) {
			TimedParticle tp = (TimedParticle) p;
			a.add(tp);
		}
	}

	/**
	 * A method making a particle (by default, {@link Particle}). Useful for
	 * extensions using specialized particles (don't forget to specialize
	 * {@link Particle#copy()} for it to return an object of its own class).
	 */
	protected TimedParticle makeParticle(Set idTypes, int numTimeSlicesInMemory) {
		TimedParticle tp = new TimedParticle(idTypes, numTimeSlicesInMemory, particleSampler);
		ObservabilitySignature os = new ObservabilitySignature();
		os.update(tp);
		tp.setOS(os.getIndex());
		return tp;
	}

	
	/**
	 * Irreversibly zeroes out the collection of queries and update the histogram using only 
	 * particles corresponding to the given observability signature.
	 * @param queries
	 * @param os
	 */
	public void answerWithPartition(Collection<Query> queries, Integer osIndex){
		for (Query q: queries)
			q.zeroOut();
		List<TimedParticle> partition = partitions.get(osIndex);
		for (TimedParticle p: partition)
			p.updateQueriesStats(queries);
	}


	public void takeWithPartition(Evidence evidence, Integer osIndex) {
		UBT.Stopwatch takeWithPartitionTimer = new UBT.Stopwatch();
		takeWithPartitionTimer.startTimer();

		List<TimedParticle> particles = partitions.get(osIndex);
		
		if (particles == null){
			System.err.println("partitionedparticlefilter.takewithpartition: particles should not be null");
			System.exit(1);
		}
		if (!evidence.isEmpty()) { 
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
		}
		UBT.takeWithPartitionTime += takeWithPartitionTimer.elapsedTime();
	}
	
	
	/**
	 * updates partitions
	 */
	public void repartition(){
		UBT.Stopwatch repartitionTimer = new UBT.Stopwatch();
		repartitionTimer.startTimer();
		
		Map<Integer, List<TimedParticle>> newPartitions = new HashMap<Integer, List<TimedParticle>>();
		for (Integer osIndex : partitions.keySet()){
			ObservabilitySignature os = ObservabilitySignature.getOSbyIndex(osIndex);
			for (TimedParticle p : (List<TimedParticle>) partitions.get(osIndex)){
				ObservabilitySignature newOS = os.spawnChild(p);
				//newOS.update(p);
				Integer newOSIndex = newOS.getIndex();
				p.setOS(newOSIndex);
				if (newPartitions.containsKey(newOSIndex)){
					List<TimedParticle> partition = newPartitions.get(newOSIndex);
					partition.add(p);
				}
				else{
					List<TimedParticle> partition = new ArrayList<TimedParticle>();
					partition.add(p);
					newPartitions.put(newOSIndex, partition);
				}
			}
		}
		partitions = newPartitions;
		UBT.repartitionTime+=repartitionTimer.elapsedTime();
		
	}

	
	public Map<Integer, List<TimedParticle>> getPartitions(){
		return partitions;
	}

	public Map<Integer, List<TimedParticle>> partitions;

	@Override
	protected TimedParticle makeParticle(Set idTypes) {
		return new TimedParticle(idTypes, 1, particleSampler);
	}


	@Override
	public void afterAnsweringQueries() {
		super.afterAnsweringQueries();
		repartition();
		
	}
}
