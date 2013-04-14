package blog.engine.onlinePF;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import blog.bn.BayesNetVar;
import blog.bn.RandFuncAppVar;
import blog.engine.Particle;
import blog.model.RandomFunction;
import blog.world.PartialWorld;
import blog.world.AbstractPartialWorld;

public class ObservabilitySignature {
	public HashMap<BayesNetVar, Object> observedValues = new HashMap<BayesNetVar, Object>();
	
	public ObservabilitySignature(Particle p){
		PartialWorld world = p.getLatestWorld();
		Map<BayesNetVar, BayesNetVar> o2r = ((AbstractPartialWorld)world).getObservableMap();
		for (Iterator i = o2r.keySet().iterator(); i.hasNext();){
			BayesNetVar bnv = (BayesNetVar) i.next();
			Boolean myObs = (Boolean) world.getValue(bnv);
			if (myObs.booleanValue()){
				BayesNetVar referenced = o2r.get(bnv);
				observedValues.put(referenced, world.getValue(referenced));
			}
		}
	}
		
	public int hashCode(){
		int rtn = 0;
		for (Iterator<BayesNetVar> i = observedValues.keySet().iterator(); i.hasNext();){
			BayesNetVar bnv = i.next();
			rtn = rtn ^ bnv.hashCode();
			rtn = rtn ^ observedValues.get(bnv).hashCode();
		}
		return rtn;
	}
	public boolean equals(Object o){
		ObservabilitySignature other = (ObservabilitySignature) o;
		if (this.observedValues.size() != other.observedValues.size())
			return false;
		HashMap<BayesNetVar, Object> m = other.observedValues;
		for (Iterator<BayesNetVar> i = observedValues.keySet().iterator(); i.hasNext();){
			BayesNetVar bnv = i.next();
			if (m.get(bnv)==null || !observedValues.get(bnv).equals(m.get(bnv)))
				return false;
		}
		return true;
	}
	public String toString(){
		return observedValues.toString();
	}
	
}
