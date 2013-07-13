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
	
	/*code for debugging high variance*/
	static int ind_0 = 0;
	static int ind_50 = 0;
	static int ind_100 = 0;
	static int ind_150 = 0;
	static int ind_200 = 0;
	static int ind_250 = 0;
	public int my_ind_0 = 0;
	public int my_ind_50 = 0;
	public int my_ind_100 = 0;
	public int my_ind_150 = 0;
	public int my_ind_200 = 0;
	public int my_ind_250 = 0;
	
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
		
		/*code for debugging high variance*/
		if (timeStep == 0) {my_ind_0 = ind_0; ind_0++;}
		else if (timeStep == 50) {my_ind_50 = ind_50; ind_50++;}
		else if (timeStep == 100) {my_ind_100 = ind_100; ind_100++;}
		else if (timeStep == 150) {my_ind_150 = ind_150; ind_150++;}
		else if (timeStep == 200) {my_ind_200 = ind_200; ind_200++;}
		else if (timeStep == 250) {my_ind_250 = ind_250; ind_250++;}
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
		
		/*code for debugging high variance*/
		copy.my_ind_0 = my_ind_0;
		copy.my_ind_50 = my_ind_50;
		copy.my_ind_100 = my_ind_100;
		copy.my_ind_150 = my_ind_150;
		copy.my_ind_200 = my_ind_200;
		copy.my_ind_250 = my_ind_250;
		
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
