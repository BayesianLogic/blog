package blog.engine.onlinePF.inverseBucket;

import java.util.Map;
import java.util.Set;

import blog.bn.BasicVar;
import blog.bn.BayesNetVar;
import blog.engine.Particle;
import blog.engine.onlinePF.ObservabilitySignature;
import blog.model.Evidence;
import blog.sample.Sampler;
import blog.world.AbstractPartialWorld;
import blog.world.DefaultPartialWorld;

public class TimedParticle extends Particle{
	
	public TimedParticle(Set idTypes, int numTimeSlicesInMemory, Sampler sampler) {
		super(idTypes, numTimeSlicesInMemory, sampler);
		// TODO Auto-generated constructor stub
	}
	protected int timeStep = -1;
	public int getTimestep(){
		return timeStep;
	}
	public void advanceTimestep(){
		timeStep ++;
	}
	public void retractTimestep(){
		timeStep --;
	}
	private Integer myOSIndex;
	public Integer getOS (){
		return myOSIndex;
	}
	public void setOS (Integer osIndex){
		myOSIndex = osIndex;
	}
	
	public TimedParticle copy(){
		TimedParticle copy = new TimedParticle(idTypes, numTimeSlicesInMemory, sampler);
		copy.timeStep=this.timeStep;
		DefaultPartialWorld newWorld = (DefaultPartialWorld) ((DefaultPartialWorld) curWorld)
				.clone();
		copy.setWorld(newWorld);
		copy.weight = this.weight;
		copy.setOS(myOSIndex);
		
		return copy;
	}
	
	public void unInstantiateObservables(ObservabilitySignature os){
		Map<BayesNetVar, BayesNetVar> o2r = ((AbstractPartialWorld) curWorld).getChangedObservableMap();
		for (BayesNetVar bnv : os.observables)
			curWorld.setValue((BasicVar) bnv, null);
		for (BayesNetVar bnv : os.unobservables)
			curWorld.setValue((BasicVar) bnv, null);
		for (BayesNetVar bnv : os.observedValues.keySet())
			curWorld.setValue((BasicVar) bnv, null);
	}
	
	public void unInstantiateObservables(Evidence ev){
		Map<BayesNetVar, BayesNetVar> o2r = ((AbstractPartialWorld) curWorld).getChangedObservableMap();
		for (BayesNetVar bnv : ev.getEvidenceVars())
			curWorld.setValue((BasicVar) bnv, null);
	}
	
	public void setWeight(double newWeight){
		this.weight = newWeight;
	}
	
}
