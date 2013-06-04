package blog.engine.onlinePF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import blog.common.Util;
import blog.engine.Particle;
import blog.engine.onlinePF.inverseBucket.TimedParticle;
import blog.engine.onlinePF.inverseBucket.UBT;
import blog.model.Evidence;
import blog.model.Model;

public class PFEngineSampled extends PFEngineOnline{

	public PFEngineSampled(Model model, Properties properties) {
		super(model, properties);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected TimedParticle makeParticle(Set idTypes) {
		return new TimedParticle(idTypes, 1, particleSampler);
	}

	@Override
	public void beforeTakingEvidence() {
		super.beforeTakingEvidence();		
	}

	@Override
	public void afterAnsweringQueries() {
		super.afterAnsweringQueries();
		retakeObservability();
		
	}
	
	private Integer sampleOS(){
		return ((TimedParticle) sampleParticle()).getOS();
	}
	/**
	 * resamples partitions, currently only samples 1 partition
	 * this is not the same as resampling particles
	 * 
	 * @param numPartitionSampled the number of partitions to be sampled
	 * parallels number of particles in a particle filter
	 */
	public void retakeObservability() {
		UBT.Stopwatch resamplePartitionAndParticlesTimer = new UBT.Stopwatch();
		resamplePartitionAndParticlesTimer.startTimer();

		Integer sampledOSindex = sampleOS();
		ObservabilitySignature selectedOS = ObservabilitySignature.getOSbyIndex(sampledOSindex);
		selectedOS.prepareEvidence();
		Evidence ev = selectedOS.getEvidence();
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
	}

}
