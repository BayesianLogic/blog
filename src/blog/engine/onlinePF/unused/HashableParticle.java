package blog.engine.onlinePF.unused;

import java.util.Iterator;
import java.util.Set;


import blog.bn.BayesNetVar;
import blog.bn.RandFuncAppVar;
import blog.engine.Particle;
import blog.engine.onlinePF.ObservableRandomFunction;
import blog.model.Model;
import blog.model.RandomFunction;
import blog.sample.Sampler;
import blog.world.DefaultPartialWorld;
import blog.world.PartialWorld;

public class HashableParticle extends Particle{

	public static Model model;
	public HashableParticle(Set idTypes, int numTimeSlicesInMemory,
			Sampler sampler) {
		super(idTypes, numTimeSlicesInMemory, sampler);
	}
	public HashableParticle copy() {
		HashableParticle copy = new HashableParticle(idTypes, numTimeSlicesInMemory, sampler);
		DefaultPartialWorld newWorld = (DefaultPartialWorld) ((DefaultPartialWorld) curWorld)
				.clone();
		copy.setWorld(newWorld);
		copy.weight = 1;
		return copy;
	}
	public int getHashCode(){
		int rtn = 0;
		PartialWorld world = this.getLatestWorld();
		for (Iterator i = world.getInstantiatedVars().iterator(); i.hasNext();){
			BayesNetVar bnv = (BayesNetVar) i.next();
			if (bnv instanceof RandFuncAppVar){
				RandomFunction f = ((RandFuncAppVar)bnv).func();
				if (f instanceof ObservableRandomFunction){
					if (((Boolean)world.getValue(bnv)).booleanValue()){
						rtn = rtn ^ bnv.hashCode();
					}
				}
			}
		}
		return rtn;
	}


}
