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
import blog.engine.onlinePF.ObservabilitySignature;
import blog.engine.onlinePF.Util.Communicator;
import blog.engine.onlinePF.inverseBucket.TimedParticle;
import blog.engine.onlinePF.inverseBucket.UBT;
import blog.model.ArgSpecQuery;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.Query;
import blog.sample.AfterSamplingListener;
import blog.sample.DMHSampler;
import blog.sample.Sampler;
import blog.world.AbstractPartialWorld;
import blog.world.PartialWorld;

/**
 * Implementation of the particle filter engine that samples its own observations, keeps multiple buckets
 * @author cheng
 *
 */
public class PFEnginePartitioned extends PFEngineOnline {


	public PFEnginePartitioned(Model model, Properties properties) {
		super(model, properties);
	}

	
	public PFEnginePartitioned(Model model, Properties properties,
			PartialWorld s) {
		super(model, properties);
		for (TimedParticle tp : particles) {
			tp.setWorld(s);
		}
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
	 * Makes a timed particle, and updates is observability signature
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


	/**
	 * Have all particles in the partition specified by osIndex take the evidence given
	 * Only decision evidence is allowed to be taken this way
	 * @param evidence (decision) evidence to be taken
	 * @param osIndex index of ObservabilitySignature which identifies the partition to be used to take the evidence
	 */
	public void takeWithPartition(Evidence evidence, Integer osIndex) {
		UBT.Stopwatch takeWithPartitionTimer = new UBT.Stopwatch();
		takeWithPartitionTimer.startTimer();

		List<TimedParticle> particles = partitions.get(osIndex);
		
		if (particles == null){
			System.err.println("partitionedparticlefilter.takewithpartition: particles should not be null");
			System.exit(1);
		}
		if (!evidence.getValueEvidence().isEmpty() || !evidence.getSymbolEvidence().isEmpty()){
			System.err.println("only decision evidence allowed");
			System.exit(1);
		}
		if (!evidence.isEmpty()) 
			for (Particle p : particles)
				p.take(evidence);
					
		UBT.takeWithPartitionTime += takeWithPartitionTimer.elapsedTime();
	}
	
	/**
	 * Overrides printResultToCommunicator in parent so that the query results for each partition is printed separately
	 */
	public void printResultToCommunicator(Collection queries, Communicator queryResultCommunicator){
		//this.updateQuery(queries);
		for (Integer osIndex: partitions.keySet()){
			answerWithPartition(queries, osIndex);
			for (Iterator it = queries.iterator(); it.hasNext();) {
				ArgSpecQuery query = (ArgSpecQuery) it.next();
				queryResultCommunicator.printInputNL(formatQueryString(query));
				queryResultCommunicator.printInputNL("-----");
			}
			queryResultCommunicator.printInput("");
			queryResultCommunicator.p.flush();
		}
	}
	
	/**
	 * After answering queries and taking evidence, the particles' ObservabilitySignatures are not out of date
	 * updates the observabilitySignatures
	 */
	public void repartition(){
		UBT.Stopwatch repartitionTimer = new UBT.Stopwatch();
		repartitionTimer.startTimer();
		
		Map<Integer, List<TimedParticle>> newPartitions = new HashMap<Integer, List<TimedParticle>>();
		for (Integer osIndex : partitions.keySet()){
			ObservabilitySignature os = ObservabilitySignature.getOSbyIndex(osIndex);
			for (TimedParticle p : (List<TimedParticle>) partitions.get(osIndex)){
				ObservabilitySignature newOS = os.spawnChild(p);
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
	
	/**
	 * Makes a timed particle, and updates is observability signature
	 */
	@Override
	protected TimedParticle makeParticle(Set idTypes) {
		TimedParticle tp = new TimedParticle(idTypes, 1, particleSampler);
		ObservabilitySignature os = new ObservabilitySignature();
		os.update(tp);
		tp.setOS(os.getIndex());
		return tp;
	}

	
	public Map<Integer, List<TimedParticle>> getPartitions(){
		return partitions;
	}

	/**
	 * hashmap that mapes index of observability signature to list of particles with given observability signature
	 */
	public Map<Integer, List<TimedParticle>> partitions;
	



	/**
	 * Overrides afterAnsweringQueries() in parent so as to call repartition() and updateOS(p) for all particles
	 * DOES NOT resample, because evidence is always empty, so weights of particles are always 1
	 */
	@Override
	public void afterAnsweringQueries() {
		for (TimedParticle p : particles){
			p.advanceTimestep();
			updateOS(p);
		}
		
		repartition();
		//resample();
		//TODO: currently does not resample 
		if (UBT.dropHistory)
			dropHistory();
	}

	/**
	 * updates the observability signature of particle p
	 * @param p
	 */
	private void updateOS(TimedParticle p){
		ObservabilitySignature newOS = ObservabilitySignature.getOSbyIndex(p.getOS()).spawnChild(p);
		Integer newOSIndex = newOS.getIndex();
		p.setOS(newOSIndex);
	}
}
