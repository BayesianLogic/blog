package blog.engine.onlinePF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import blog.DBLOGUtil;
import blog.bn.BayesNetVar;
import blog.engine.onlinePF.inverseBucket.UBT;
import blog.world.AbstractPartialWorld;
import blog.engine.onlinePF.inverseBucket.TimedParticle;

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
		rtn.myTimestep = this.myTimestep;
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
	public void update (TimedParticle p){
		indexValid = false;
		AbstractPartialWorld world = ((AbstractPartialWorld) p.getLatestWorld());
		int maxTimestep = ((TimedParticle) p).getTimestep();
		Map<BayesNetVar, BayesNetVar> o2r = world.getChangedObservableMap();
		for (BayesNetVar bnv : o2r.keySet()) {
			Boolean myObs = (Boolean) world.getValue(bnv);
			if (myObs.booleanValue()){
				BayesNetVar referenced = o2r.get(bnv);
				int bnvTimestep = DBLOGUtil.getTimestepIndex(bnv);
				if (!observedValues.containsKey(referenced) &&  bnvTimestep <= maxTimestep && bnvTimestep > myTimestep){
					observedValues.put(referenced, world.getValue(referenced));
				}
			}
		}
		myTimestep = maxTimestep;
		parentIndex = myIndex;
		myIndex = ObservabilitySignature.getOrSetIndexByOS(this);
		this.indexValid = true;
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
			rtn = parentIndex != -1 ? ObservabilitySignature.getOSbyIndex(parentIndex).hashCode() : 0;
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
	private int myTimestep = -1;
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
        public static Map<Integer, Integer> OStoBucketSize = new HashMap<Integer, Integer>();
        public static void updateOStoBucketSize(Integer osIndex, Double additionalCount) {
            /*code for updating OStoBucketSize*/
            if (ObservabilitySignature.OStoBucketSize.containsKey(osIndex))
                ObservabilitySignature.OStoBucketSize.put(osIndex, 
                        ObservabilitySignature.OStoBucketSize.get(osIndex)+ additionalCount.intValue());
            else
                ObservabilitySignature.OStoBucketSize.put(osIndex, additionalCount.intValue());
            //
        }
        
        public static void resetBucketCount(){
            maxBucketSize = 0;
            minBucketSize = Integer.MAX_VALUE;
            OStoBucketSize.clear();
        }
        
    public static void updateBucketCount() {
        for (Integer osIndex : OStoBucketSize.keySet()){
            Integer thisBucketSize = OStoBucketSize.get(osIndex);
            maxBucketSize = Math.max(thisBucketSize, maxBucketSize);
            minBucketSize = Math.min(thisBucketSize, minBucketSize);
        }
    }
        public static int maxBucketSize = 0;
        public static int minBucketSize = Integer.MAX_VALUE;
	public static int index = 0;
	
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
			index = index + 1;
			return index -1;
		}
	}
	public int getIndex(){
		return myIndex;
	}
	public static ObservabilitySignature getOSbyIndex(int index){
		ObservabilitySignature os = IndextoOS.get(index);
		if (os.myIndex!=index){
			System.err.println("error with index");
			System.exit(1);
		}
			
		return os;
	}
}
