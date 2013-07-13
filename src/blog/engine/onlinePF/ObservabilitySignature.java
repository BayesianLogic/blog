package blog.engine.onlinePF;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import blog.DBLOGUtil;
import blog.bn.BayesNetVar;
import blog.common.Util;
import blog.engine.onlinePF.inverseBucket.UBT;
import blog.parse.Parse;
import blog.semant.Semant;
import blog.world.AbstractPartialWorld;
import blog.engine.onlinePF.inverseBucket.TimedParticle;
import blog.engine.onlinePF.runner.PFRunnerSampled;
import blog.model.Evidence;
import blog.model.Query;
import blog.msg.ErrorMsg;

public class ObservabilitySignature {

	public HashMap<BayesNetVar, Object> observedValues = new HashMap<BayesNetVar, Object>();
	public HashSet<BayesNetVar> unobservables = new HashSet<BayesNetVar>();
	public HashSet<BayesNetVar> observables = new HashSet<BayesNetVar>();
	
	/**
	 * returns a clone of the original (this)
	 * @return
	 */
	public ObservabilitySignature spawnChild (TimedParticle p){
		ObservabilitySignature rtn = new ObservabilitySignature();
		//for (BayesNetVar os : this.observedValues.keySet()){
		//	rtn.observedValues.put(os, this.observedValues.get(os));
		//}
		rtn.myTimestep = this.myTimestep;
		rtn.myIndex = myIndex;
		rtn.parentIndex = parentIndex;
		rtn.indexValid = true;
		rtn.update(p);
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
				if (!observedValues.containsKey(referenced) &&  bnvTimestep <= maxTimestep ){//&& bnvTimestep > myTimestep){
					observedValues.put(referenced, world.getValue(referenced));
					observables.add(bnv);
				}
			}
			else
				unobservables.add(bnv);
				
		}
		//world.getChangedObservableMap().clear();
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
		if (parentIndex!=-1)
			return getOSbyIndex(parentIndex).toString() + observedValues.toString();
		else
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
	public static Map<Integer, ObservabilitySignature> IndextoOS= new HashMap<Integer, ObservabilitySignature> ();
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
            if (thisBucketSize > maxBucketSize){
            	maxBucketSize = thisBucketSize;
            	maxBucketIndex = osIndex;
            }
            if (thisBucketSize < minBucketSize){
            	minBucketSize = thisBucketSize;
            	minBucketIndex = osIndex;
            }
            //maxBucketSize = Math.max(thisBucketSize, maxBucketSize);
            //minBucketSize = Math.min(thisBucketSize, minBucketSize);
        }
    }
    
    public static int maxBucketSize = 0;
    public static int minBucketSize = Integer.MAX_VALUE;
    public static int maxBucketIndex = 0;
    public static int minBucketIndex = 0;
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
			IndextoOS.put(index, os);
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
	
	public String generateObservableString(){
		String rtn = "";
		for (BayesNetVar referenced : observedValues.keySet()){
			rtn += ("obs " + referenced.toString() + "=" + observedValues.get(referenced) + ";");
		}
		for (BayesNetVar observable : observables){
			rtn += ("obs " + observable.toString() + "=" + "true" + ";");
		}
		for (BayesNetVar unobservable : unobservables){
			rtn += ("obs " + unobservable.toString() + "=" + "false" + ";");
		}
		return rtn;
		
	}
	
	public void prepareEvidence(){
		String eviString = generateObservableString();
		myEvidence = new Evidence();
		parseAndTranslateEvidence(myEvidence, Util.list(), new StringReader(eviString));
	}
	
	private boolean parseAndTranslateEvidence(Evidence e, List<Query> q, Reader reader) {
		Parse parse = new Parse(reader, null);
		Semant sem = new Semant(PFRunnerSampled.model, e, q, new ErrorMsg.quietErrorMsg("ParticleFilterRunnerOnGenerator.parseAndTranslateEvidence()")); //ignore this error message for now
		sem.transProg(parse.getParseResult());
		return true;
	}
	public Evidence getEvidence(){
		return myEvidence;
	}
	
	private Evidence myEvidence;
	
	/**
	 * Clears the cached observability signatures from before.
	 */
	public static void dropHistory(int currentTimestep){
		for (Iterator it = OStoIndex.keySet().iterator(); it.hasNext(); ){
			ObservabilitySignature os = (ObservabilitySignature) it.next();
			if (os.myTimestep < currentTimestep - 2){
				IndextoOS.remove(OStoIndex.get(os));
				it.remove();
			}
				
		}
		OStoBucketSize.clear();
		
	}
}
