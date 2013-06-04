package blog.engine.onlinePF;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.Set;

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
	public void beforeTakingEvidence() {
		super.beforeTakingEvidence();		
	}

	@Override
	public void afterAnsweringQueries() {
		for (TimedParticle p : particles)
			p.advanceTimestep();
		updateOSforAllParticles();
		retakeObservability();
		
		if (UBT.dropHistory)
			dropHistory();
		resample();
		//super.afterAnsweringQueries();
		
	}
	
	private Integer sampleOS(){
		return ((TimedParticle) sampleParticle()).getOS();
	}
	
	@Override
	protected TimedParticle makeParticle(Set idTypes) {
		TimedParticle tp = new TimedParticle(idTypes, 1, particleSampler);
		ObservabilitySignature os = new ObservabilitySignature();
		os.update(tp);
		tp.setOS(os.getIndex());
		return tp;
	}
	public void updateOSforAllParticles(){
		for (TimedParticle p : (List<TimedParticle>) particles){
			ObservabilitySignature newOS = ObservabilitySignature.getOSbyIndex(p.getOS()).spawnChild(p);
			Integer newOSIndex = newOS.getIndex();
			p.setOS(newOSIndex);
		}
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
		ObservabilitySignature foo = new ObservabilitySignature();
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

	public void takeDecision(Evidence evidence){
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
	}
}
