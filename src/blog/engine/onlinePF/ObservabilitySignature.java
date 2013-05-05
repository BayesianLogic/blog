package blog.engine.onlinePF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import blog.DBLOGUtil;
import blog.bn.BasicVar;
import blog.bn.BayesNetVar;
import blog.bn.RandFuncAppVar;
import blog.engine.Particle;
import blog.engine.onlinePF.inverseBucket.InverseParticle;
import blog.engine.onlinePF.inverseBucket.UBT;
import blog.model.RandomFunction;
import blog.world.PartialWorld;
import blog.world.AbstractPartialWorld;
import blog.engine.onlinePF.inverseBucket.InverseParticle;

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
		rtn.myIndex = myIndex;
		rtn.parentIndex = parentIndex;
		rtn.indexValid = true;
		return rtn;
	}
	
	/**
	 * updates observability signature with new (i.e. next timestep) observability values
	 * from a particle
	 * @param p
	 */
	public void update (Particle p){
		indexValid = false;
		AbstractPartialWorld world = ((AbstractPartialWorld) p.getLatestWorld());
		int maxTimestep = ((InverseParticle) p).getTimestep();
		int maxTimestep2 = -1;
		for (Object o : world.getChangedVars().keySet()){
			BasicVar v = (BasicVar) o;
			maxTimestep2 = Math.max(maxTimestep2, DBLOGUtil.getTimestepIndex(v));
		}
		if (maxTimestep2 != maxTimestep){
			System.err.println("bug in observabilitysignature.update regarding incorrect timesteps");
			System.exit(1);
		}
		
		Map<BayesNetVar, BayesNetVar> o2r = world.getChangedObservableMap();
		for (BayesNetVar bnv : o2r.keySet()) {
			Boolean myObs = (Boolean) world.getValue(bnv);
			if (myObs.booleanValue()){
				BayesNetVar referenced = o2r.get(bnv);
				if (!(DBLOGUtil.getTimestepIndex(bnv) == maxTimestep)){
					System.err.println("bug in observabilitysignature.update regarding incorrect timesteps");
					System.exit(1);
				}
				if (!observedValues.containsKey(referenced) && DBLOGUtil.getTimestepIndex(bnv) == maxTimestep){
					observedValues.put(referenced, world.getValue(referenced));
				}
			}
		}
		
		parentIndex = myIndex;
		myIndex = ObservabilitySignature.getOrSetIndexByOS(this);
		this.indexValid = true;
		
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
		UBT.Stopwatch timer = new UBT.Stopwatch();
		timer.startTimer();
		int rtn = 0;
		if (cachedHashcodeValid){
			rtn = this.cachedHashcode;
		}
		else{
			rtn = ObservabilitySignature.getOSbyIndex(parentIndex).hashCode();
			for (Iterator<BayesNetVar> i = observedValues.keySet().iterator(); i.hasNext();){
				BayesNetVar bnv = i.next();
				rtn = rtn ^ bnv.hashCode();
				rtn = rtn ^ observedValues.get(bnv).hashCode();
			}
			this.cachedHashcode = rtn;
			this.cachedHashcodeValid = true;
		}
		
		UBT.specialTimingData4 += (timer.elapsedTime());
		return rtn;
	}
	public boolean equals(Object o){
		UBT.Stopwatch timer = new UBT.Stopwatch();
		timer.startTimer();
		boolean rtn = true;
		ObservabilitySignature other = (ObservabilitySignature) o;
		if (this.observedValues.size() != other.observedValues.size())
			rtn = false;
		else{
			if (this.indexValid && other.indexValid){
				rtn = rtn && (myIndex == other.myIndex);
			}
			else{
				rtn = rtn && (parentIndex == other.parentIndex);
				HashMap<BayesNetVar, Object> m = other.observedValues;
				for (Iterator<BayesNetVar> i = observedValues.keySet().iterator(); i.hasNext();){
					BayesNetVar bnv = i.next();
					if (m.get(bnv)==null || !observedValues.get(bnv).equals(m.get(bnv)))
						rtn = false;
				}
			}
		}
		UBT.specialTimingData3 += (timer.elapsedTime());
		return rtn;
	}
	public String toString(){
		//return ""+ myIndex;
		return observedValues.toString();
	}
	int parentIndex = -1;
	int myIndex = -1;
	int cachedHashcode;
	boolean cachedHashcodeValid = false;
	boolean indexValid = false;
	/**
	 * This hashmap stores the possible values for observability signature
	 */
	public static Map<ObservabilitySignature, Integer> OStoIndex = new HashMap<ObservabilitySignature, Integer>();
	public static ArrayList<ObservabilitySignature> IndextoOS= new ArrayList<ObservabilitySignature> ();
	public static int index = 0;
	
	/**
	 * Empties the OSDictionary, resets index
	 */
	public static void emptyOSDictionaryForNewTimestep(){
		OStoIndex.clear();
		index = 0;
	}
	
	/**
	 * Gets the index for the given os, if not already in OSDictionary, then put it there and return index, incrementing index by 1
	 * @param os
	 * @return
	 */
	public static Integer getOrSetIndexByOS(ObservabilitySignature os){
		if (OStoIndex.containsKey(os))
			return OStoIndex.get(os);
		else{
			OStoIndex.put(os, index);
			IndextoOS.add(os);
			index ++;
			return index -1;
		}
	}
	
	public static ObservabilitySignature getOSbyIndex(int index){
		return IndextoOS.get(index);
	}
	
	
}
