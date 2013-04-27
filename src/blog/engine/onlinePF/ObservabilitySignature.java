package blog.engine.onlinePF;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import blog.DBLOGUtil;
import blog.bn.BayesNetVar;
import blog.bn.RandFuncAppVar;
import blog.engine.Particle;
import blog.engine.onlinePF.inverseBucket.InverseParticle;
import blog.model.RandomFunction;
import blog.world.PartialWorld;
import blog.world.AbstractPartialWorld;

public class ObservabilitySignature {
	public HashMap<BayesNetVar, Object> observedValues = new HashMap<BayesNetVar, Object>();

	/**
	 * returns a clone of the original (this)
	 * @return
	 */
	public ObservabilitySignature copy (){
		ObservabilitySignature rtn = new ObservabilitySignature();
		for (BayesNetVar os : this.observedValues.keySet()){
			rtn.observedValues.put(os, this.observedValues.get(os));
		}
		return rtn;
	}
	
	/**
	 * updates observability signature with new (i.e. next timestep) observability values
	 * from a particle
	 * @param p
	 */
	private static Pattern checkPattern = Pattern.compile("getSound\\(@1\\)=noiseLeft");
	public void update (Particle p){
		AbstractPartialWorld world = ((AbstractPartialWorld) p.getLatestWorld());
		
		Matcher matcher = checkPattern.matcher(this.toString());
		boolean matches = matcher.find();
		//System.err.println(this.toString());
		Map<BayesNetVar, BayesNetVar> o2r = world.getObservableMap();
		for (BayesNetVar bnv : o2r.keySet()) {
			Boolean myObs = (Boolean) world.getValue(bnv);
			if (myObs.booleanValue()){
				BayesNetVar referenced = o2r.get(bnv);
				if (!observedValues.containsKey(referenced))
					observedValues.put(referenced, world.getValue(referenced));
			}
		}
		
		Matcher matcher2 = checkPattern.matcher(this.toString());
		boolean matches2 = matcher2.find();
		//if (this.toString().equals("{getSound(@1)=noiseLeft}"))
		//	System.exit(1);
		//System.err.println(this.toString());
		if (matches && (!matches2)){
			System.err.println("Error found");
		}
	}
	
	public ObservabilitySignature(Particle p){
		PartialWorld world = p.getLatestWorld();
		Map<BayesNetVar, BayesNetVar> o2r = ((AbstractPartialWorld)world).getObservableMap(); //observability to referenced
		for (Iterator i = o2r.keySet().iterator(); i.hasNext();){
			BayesNetVar bnv = (BayesNetVar) i.next();
			Boolean myObs = (Boolean) world.getValue(bnv);
			if (myObs.booleanValue()){
				BayesNetVar referenced = o2r.get(bnv);
				observedValues.put(referenced, world.getValue(referenced));
			}
		}
	}
	
	/**
	 * empty observability signature
	 */
	public ObservabilitySignature(){
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
		//return ""+ myIndex;
		return observedValues.toString();
	}
	
}
