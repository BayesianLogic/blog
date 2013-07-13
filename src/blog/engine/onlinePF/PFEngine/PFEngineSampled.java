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
		
		/*
		if (UBT.singleObs){
			String accstr = null;
			try {
				accstr = UBT.obsReader.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}
			ev = new Evidence();
			parseAndTranslateEvidence(ev, new ArrayList(), new StringReader((String) accstr));
			ev.checkTypesAndScope(model);
			if (ev.compile()!=0)
				System.exit(1);

			
			for (Particle o : particles){
				TimedParticle p = (TimedParticle) o;
				p.unInstantiateObservables(ev);
				p.take(ev);
			}
		}*/
		//else{
			Integer sampledOSindex = sampleOS();
			ObservabilitySignature selectedOS = ObservabilitySignature.getOSbyIndex(sampledOSindex);
			selectedOS.prepareEvidence();
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
		//}
		
		

		UBT.resamplePartitionAndParticlesTime += resamplePartitionAndParticlesTimer.elapsedTime();
		
		/*code for debugging high variance*/
		/*
		List list = new LinkedList();
		list.addAll(ev.getValueEvidence());
		list.addAll(ev.getSymbolEvidence());
		list.addAll(ev.getDecisionEvidence());
		for (Object o : list){
			UBT.obsOutput.printInputNL("obs " + o.toString() + ";");
			//UBT.obsOutput.printInput(ev.toString());
		}
		UBT.obsOutput.printInput(" ");
		*/
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
	
	/*code for debugging high variance*/
	/*
	public void outputIndexData(){
		HashSet<Integer> set_0 = new HashSet<Integer>();
		HashSet<Integer> set_50 = new HashSet<Integer>();
		HashSet<Integer> set_100 = new HashSet<Integer>();
		HashSet<Integer> set_150 = new HashSet<Integer>();
		HashSet<Integer> set_200 = new HashSet<Integer>();
		HashSet<Integer> set_250 = new HashSet<Integer>();
		for (TimedParticle p : particles){
			set_0.add(p.my_ind_0);
			set_50.add(p.my_ind_50);
			set_100.add(p.my_ind_100);
			set_150.add(p.my_ind_150);
			set_200.add(p.my_ind_200);
			set_250.add(p.my_ind_250);
		}
		String output = "0_count: "+set_0.size()+"| 50_count: "+set_50.size()+"| 100_count: "+set_100.size()+"| 150_count: "+set_150.size()+"| 200_count: "+set_200.size()+"| 250_count: "+set_250.size();
		UBT.specialIndexOutput.printInput(output);
	}
	*/
	
	//need to fix the error message for empty evidence string inputs
	private boolean parseAndTranslateEvidence(Evidence e, List<Query> q, Reader reader) {
		Parse parse = new Parse(reader, null);
		Semant sem = new Semant(model, e, q, new ErrorMsg.quietErrorMsg("ParticleFilterRunnerOnGenerator.parseAndTranslateEvidence()")); //ignore this error message for now
		sem.transProg(parse.getParseResult());
		return true;
	}

}
