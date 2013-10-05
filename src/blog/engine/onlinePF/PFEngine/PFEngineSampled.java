package blog.engine.onlinePF.PFEngine;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import blog.common.Util;
import blog.engine.Particle;
import blog.engine.onlinePF.ObservabilitySignature;
import blog.engine.onlinePF.inverseBucket.TimedParticle;
import blog.engine.onlinePF.inverseBucket.UBT;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.Query;
import blog.msg.ErrorMsg;
import blog.parse.Parse;
import blog.semant.Semant;

/**
 * Implementation of the particle filter engine that samples its own observations, keeps a single bucket
 * @author cheng
 *
 */
public class PFEngineSampled extends PFEngineOnline{

	public PFEngineSampled(Model model, Properties properties) {
		super(model, properties);
		// TODO Auto-generated constructor stub
	}


	/**
	 * Actions to perform after answering queries
	 * Overrides the parent method in order to call 
	 * - updateOSforAllParticles()
	 * - retakeObservability()
	 */
	@Override
	public void afterAnsweringQueries() {
		for (TimedParticle p : particles)
			p.advanceTimestep();
		updateOSforAllParticles();
		retakeObservability();
		
		if (UBT.dropHistory){
			dropHistory();
			ObservabilitySignature.dropHistory(((TimedParticle)Util.getFirst(particles)).getTimestep());
		}
		resample();
		//super.afterAnsweringQueries();
		//outputIndexData();
	}
	public String afterAnsweringQueries2() {
		for (TimedParticle p : particles)
			p.advanceTimestep();
		updateOSforAllParticles();
		String evs = retakeObservability2();
		
		resample();
		//super.afterAnsweringQueries();
		//outputIndexData();
		return evs;
	}
	public String retakeObservability2() {
		UBT.Stopwatch resamplePartitionAndParticlesTimer = new UBT.Stopwatch();
		resamplePartitionAndParticlesTimer.startTimer();
		Evidence ev = null;
		Integer sampledOSindex = sampleOS();
		ObservabilitySignature selectedOS = ObservabilitySignature.getOSbyIndex(sampledOSindex);
		String evs = selectedOS.prepareEvidence2();
		ev = selectedOS.getEvidence();
		ev.checkTypesAndScope(model);
		if (ev.compile()!=0)
			System.exit(1);

		for (Particle o : particles){
			TimedParticle p = (TimedParticle) o;
			p.unInstantiateObservables(selectedOS);
			p.take(ev);
			p.setOS(sampledOSindex);
		}

		UBT.resamplePartitionAndParticlesTime += resamplePartitionAndParticlesTimer.elapsedTime();
		return evs;
	}
	
	
	
	/**
	 * Samples a single observability signature, by sampling a single particle
	 * according to its weight
	 * @return the index of the sampled observability signature
	 */
	private Integer sampleOS(){
		return ((TimedParticle) sampleParticle()).getOS();
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
	
	/**
	 * updates the observabilitysignature for all particles
	 * The particles themselves are up-to-date, but the observability signatures
	 * referenced by them are not up to date.
	 */
	public void updateOSforAllParticles(){
		for (TimedParticle p : (List<TimedParticle>) particles){
			ObservabilitySignature newOS = ObservabilitySignature.getOSbyIndex(p.getOS()).spawnChild(p);
			Integer newOSIndex = newOS.getIndex();
			p.setOS(newOSIndex);
		}
	}
	
	/**
	 * samples an observabilitySignature, then uninstantiates observability variables (at this timestep) 
	 * for all particles. Finally, make all particles take the sampled observabilitySignature as evidence,
	 * updating their weights accordingly (weight updates handled automatically by p.take(ev)
	 * 
	 * @param numPartitionSampled the number of partitions to be sampled
	 */
	public void retakeObservability() {
		UBT.Stopwatch resamplePartitionAndParticlesTimer = new UBT.Stopwatch();
		resamplePartitionAndParticlesTimer.startTimer();
		Evidence ev = null;
		Integer sampledOSindex = sampleOS();
		ObservabilitySignature selectedOS = ObservabilitySignature.getOSbyIndex(sampledOSindex);
		selectedOS.prepareEvidence();
		ev = selectedOS.getEvidence();
		ev.checkTypesAndScope(model);
		if (ev.compile()!=0)
			System.exit(1);
		for (Object o : ev.getValueEvidence())
			UBT.obsOutput.printInput("obs " + o.toString()+";");
		for (Object o : ev.getSymbolEvidence())
			UBT.obsOutput.printInput("obs "+o.toString()+";");
		//for (Object o : ev.getDecisionEvidence())
		//	UBT.obsOutput.printInput(o.toString());
		
		int i = 0;
		for (Particle o : particles){
			TimedParticle p = (TimedParticle) o;
			for (Object x : p.getLatestWorld().basicVarToValueMap().keySet())
				UBT.worldOutput.printInput(x.toString()+"="+p.getLatestWorld().basicVarToValueMap().get(x).toString());
			UBT.worldOutput.printInput(">>>>>");
			p.unInstantiateObservables(selectedOS);
			p.take(ev);
			p.setOS(sampledOSindex);
			if (p.getLatestWeight()>0.001)
				i++;
		}
		UBT.worldOutput.printInput("<<<<<>>>>>");
		UBT.numParticleOutput.printInput(""+i);
		
		

		UBT.resamplePartitionAndParticlesTime += resamplePartitionAndParticlesTimer.elapsedTime();
	}

	
	/**
	 * Each particle takes the same decision evidence provided
	 */
	public void takeDecision(Evidence evidence){
		if (!evidence.isEmpty()) { 
			for (TimedParticle p : particles) {
				p.take(evidence);
			}
			if (particles.size() == 0)
				throw new IllegalArgumentException("All particles have zero weight");
		}
	}
	


}
