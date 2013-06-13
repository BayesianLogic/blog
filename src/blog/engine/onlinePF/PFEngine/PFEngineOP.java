package blog.engine.onlinePF.PFEngine;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import blog.engine.Particle;
import blog.engine.onlinePF.ObservabilitySignature;
import blog.engine.onlinePF.inverseBucket.TimedParticle;
import blog.engine.onlinePF.inverseBucket.UBT;
import blog.model.Evidence;
import blog.model.Model;

/**
 * Implementation of the particle filter engine that samples its own observations, keeps a single bucket
 * @author cheng
 *
 */
public class PFEngineOP extends PFEngineOnline{

	public PFEngineOP(Model model, Properties properties) {
		super(model, properties);
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
	 * Actions to perform after answering queries
	 * Overrides the parent method in order to call 
	 * - updateOSforAllParticles()
	 */
	@Override
	public void afterAnsweringQueries() {
		for (TimedParticle p : particles)
			p.advanceTimestep();
		updateOSforAllParticles();
		
		if (UBT.dropHistory)
			dropHistory();
		resample();
		//super.afterAnsweringQueries();
		
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
