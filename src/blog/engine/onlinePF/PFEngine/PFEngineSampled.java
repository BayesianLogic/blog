package blog.engine.onlinePF.PFEngine;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import blog.common.Util;
import blog.engine.Particle;
import blog.engine.onlinePF.ObservabilitySignature;
import blog.engine.onlinePF.inverseBucket.TimedParticle;
import blog.engine.onlinePF.inverseBucket.UBT;
import blog.engine.pbvi.SimpleObservabilitySignature;
import blog.engine.pbvi.Timer;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.Query;
import blog.msg.ErrorMsg;
import blog.parse.Parse;
import blog.semant.Semant;
import blog.world.PartialWorld;

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
	
	public PFEngineSampled(Model model, Properties properties,
			PartialWorld s, int timestep) {
		super(model, properties);
		for (TimedParticle tp : particles) {
			tp.setWorld(s);
			tp.setTimestep(timestep);
		}
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
		retakeObservability();
		
		resample();
		//super.afterAnsweringQueries();
		//outputIndexData();
		return evs;
	}
	
	public String retakeObservability2() {
		Integer sampledOSindex = sampleOS();
		return retakeObservability2(sampledOSindex);
	}
	
	public String retakeObservability2(Integer osIndex) {
		Timer.start("retakeObs2");
		UBT.Stopwatch resamplePartitionAndParticlesTimer = new UBT.Stopwatch();
		resamplePartitionAndParticlesTimer.startTimer();
		Evidence ev = null;
		ObservabilitySignature selectedOS = ObservabilitySignature.getOSbyIndex(osIndex);
		String evs = selectedOS.prepareEvidence2(this.model);
		ev = selectedOS.getEvidence();
		ev.checkTypesAndScope(model);
		if (ev.compile()!=0)
			System.exit(1);
		for (Particle o : particles){
			TimedParticle p = (TimedParticle) o;
			p.unInstantiateObservables(selectedOS);
			p.take(ev);
			p.setOS(osIndex);
		}
		Timer.record("retakeObs2");
		UBT.resamplePartitionAndParticlesTime += resamplePartitionAndParticlesTimer.elapsedTime();
		return evs;
	}
	
	
	
	/**
	 * Samples a single observability signature, by sampling a single particle
	 * according to its weight
	 * @return the index of the sampled observability signature
	 */
	public Integer sampleOS(){
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
	
	private static Map<SimpleObservabilitySignature, SimpleObservabilitySignature> preparedOS = 
			new HashMap<SimpleObservabilitySignature, SimpleObservabilitySignature>();
	
	public Integer retakeObservability() {
		Integer sampledOSindex = sampleOS();
		return retakeObservability(sampledOSindex);
	}
	
	/**
	 * samples an observabilitySignature, then uninstantiates observability variables (at this timestep) 
	 * for all particles. Finally, make all particles take the sampled observabilitySignature as evidence,
	 * updating their weights accordingly (weight updates handled automatically by p.take(ev)
	 * 
	 * @param numPartitionSampled the number of partitions to be sampled
	 */
	public Integer retakeObservability(Integer sampledOSindex) {
		Timer.start("retakeObs");
		UBT.Stopwatch resamplePartitionAndParticlesTimer = new UBT.Stopwatch();
		resamplePartitionAndParticlesTimer.startTimer();
		Evidence ev = null;
		ObservabilitySignature selectedOS = ObservabilitySignature.getOSbyIndex(sampledOSindex);
		SimpleObservabilitySignature simpleOS = SimpleObservabilitySignature.simplifyOS(selectedOS);
		if (preparedOS.containsKey(simpleOS)) {
			simpleOS = preparedOS.get(simpleOS);
			selectedOS.setNonSymbolEvidence(simpleOS.getNonSymbolEvidence());
		} else {
			Timer.start("retakeObs.prepareEvidence");
			selectedOS.prepareEvidence(model);
			simpleOS.setNonSymbolEvidence(selectedOS.getNonSymbolEvidence());
			preparedOS.put(simpleOS, simpleOS);
			Timer.record("retakeObs.prepareEvidence");
		}
		ev = selectedOS.getNonSymbolEvidence();
		Timer.start("retakeObs.checkTypesAndScope");
		ev.checkTypesAndScope(model);
		Timer.record("retakeObs.checkTypesAndScope");
		Timer.start("retakeObs.compile");
		if (ev.compile()!=0)
			System.exit(1);
		Timer.record("retakeObs.compile");
		if (UBT.obsOutput != null) {
			for (Object o : ev.getValueEvidence())
				UBT.obsOutput.printInput("obs " + o.toString()+";");
			for (Object o : ev.getSymbolEvidence())
				UBT.obsOutput.printInput("obs "+o.toString()+";");
			for (Object o : ev.getDecisionEvidence())
				UBT.obsOutput.printInput(o.toString());
		}
		
		if (particles.size() > 1) {
			Timer.start("reweight");
			int i = 0;
			for (Particle o : particles) {
				TimedParticle p = (TimedParticle) o;
				//for (Object x : p.getLatestWorld().basicVarToValueMap().keySet())
				//	UBT.worldOutput.printInput(x.toString()+"="+p.getLatestWorld().basicVarToValueMap().get(x).toString());
				//UBT.worldOutput.printInput(">>>>>");
				p.unInstantiateObservables(selectedOS);
				Timer.start("takeEv");
				p.take(ev);
				Timer.record("takeEv");
				if (Timer.getElapsed("takeEv") > 2000) {
					System.out.println("ALERT: This particle took more than 2s to take ev" + p.getLatestWorld());
				}
				p.setOS(sampledOSindex);
				if (p.getLatestWeight()>0.00000001)
					i++;
			}
			Timer.record("reweight");
			if (Timer.getElapsed("reweight") > 30000) {
				System.out.println("ALERT: This evidence took more than 30s to reweight" + ev);
			}
			

			if (UBT.worldOutput != null) {
				UBT.worldOutput.printInput("<<<<<>>>>>");
			}
			if (UBT.numParticleOutput != null) {
				UBT.numParticleOutput.printInput(""+i);
			}
		}
		
		//System.out.println("rtObs1" + ev);

		UBT.resamplePartitionAndParticlesTime += resamplePartitionAndParticlesTimer.elapsedTime();
		Timer.record("retakeObs");
		return sampledOSindex;
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
	
	public PFEngineSampled copy() {
		Timer.start("copy pf");
		PFEngineSampled copy = new PFEngineSampled(this.model, this.properties);
		copy.particles = new ArrayList<TimedParticle>();
		for (TimedParticle p : this.particles) {
			copy.particles.add(p.copy());
		}
		copy.idTypes = new HashSet(this.idTypes);
		copy.queries = new ArrayList(this.queries); //TODO: necessary?
		Timer.record("copy pf");
		return copy;
	}

}
