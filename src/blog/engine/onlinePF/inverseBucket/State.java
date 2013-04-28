package blog.engine.onlinePF.inverseBucket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blog.common.Util;
import blog.engine.onlinePF.ObservabilitySignature;
import blog.model.Evidence;

public class State {
	/**
	 * the state collection that encloses this state
	 */
	StateCollection EnclosingSC;
	/**
	 * the particle which represents this state, note only
	 * the basicVar aspect of the particle is correct (i.e.
	 * the particle's observable values are wrong)
	 * refer to OStoCount.keySet for actual set of 
	 * observability signatures.
	 */
	InverseParticle canonicalParticle;
	Map<ObservabilitySignature, Double> OStoCount = new HashMap<ObservabilitySignature, Double> ();
	double totalCount;
	
	public State (InverseParticle p, StateCollection enc){
		canonicalParticle = p;
		EnclosingSC = enc;
		totalCount = 0;
	}
	
	public void addOSCounts(Map<ObservabilitySignature, Double> mapToAdd){
		for (ObservabilitySignature os : mapToAdd.keySet()){
			Double additionalCount = mapToAdd.get(os);
			if (OStoCount.containsKey(os)){
				OStoCount.put(os, OStoCount.get(os) + additionalCount);
			}
			else {
				OStoCount.put(os, additionalCount);
			}
			totalCount += additionalCount;
		}
	}
	public void doActionsAndAnswerQueries(){
		/////
		Map<String, Map<ObservabilitySignature, Double>> actionToOSCounts = this.ActionToOSCounts();
		/////
		
		
		if (EnclosingSC.nextStateCollection == null){
			System.err.println("State.doActions(): EnclosingSC.nextStateCollection is null");
			System.exit(1);
		}
		for (ObservabilitySignature os : OStoCount.keySet()){
			for (int i = 0; i< OStoCount.get(os); i++){
				InverseParticle np = canonicalParticle.copy();
				np.take(EnclosingSC.OStoAction.get(os));
				np.take(EnclosingSC.nextTimestepEvidence);
				np.answer(EnclosingSC.nextTimestepQueries);
				
				//Map<ObservabilitySignature, Double> newCounts = actionToOSCounts.get(EnclosingSC.OStoAction.get(os).toString());//new HashMap<ObservabilitySignature, Double>();
				Map<ObservabilitySignature, Double> newCounts = new HashMap<ObservabilitySignature, Double>();
				//newCounts = getUpdatedOStoCount(np, newCounts);
				ObservabilitySignature newOS = os.copy();
				newOS.update(np);
				
				newCounts.put(newOS, np.getLatestWeight());
				EnclosingSC.nextStateCollection.addParticle(np, newCounts);
			}
		}
	}
	
	/*
	public void doActionsAndAnswerQueries(){
		if (EnclosingSC.nextStateCollection == null){
			System.err.println("State.doActions(): EnclosingSC.nextStateCollection is null");
			System.exit(1);
		}
		Map<String, List<ObservabilitySignature>> ActionToOSList = new HashMap<String, List<ObservabilitySignature>>();
		Map<String, Double> ActionToCount = new HashMap<String, Double>();
		Map<String, Evidence> ActionToActualEvidence = new HashMap<String, Evidence>();
		for (ObservabilitySignature os : OStoCount.keySet()){
			Evidence actionEvidence = this.EnclosingSC.OStoAction.get(os);
			String actionStr = actionEvidence.toString();
			if (ActionToOSList.containsKey(actionStr)){
				List<ObservabilitySignature> osList = ActionToOSList.get(actionStr);
				osList.add(os);
				ActionToCount.put(actionStr, ActionToCount.get(actionStr) + OStoCount.get(os));
				if (!(ActionToCount.containsKey(actionStr)) || !ActionToActualEvidence.containsKey(actionStr)){
					System.err.println("State.doActions(): discrepency in keyset for action maps");
					System.exit(1);
				}
			}
			else{
				ArrayList<ObservabilitySignature> newOSList = new ArrayList<ObservabilitySignature>();
				newOSList.add(os);
				ActionToOSList.put(actionStr, newOSList);
				ActionToCount.put(actionStr, OStoCount.get(os));
				ActionToActualEvidence.put(actionStr, actionEvidence);
			}
		}
		// by this point, actions have been properly initialized
		for (String actionString : ActionToActualEvidence.keySet()){
			Double totalParticlesGenerated = Math.ceil(ActionToCount.get(actionString));
			Double ratio = ActionToCount.get(actionString)/totalParticlesGenerated;
			Evidence actionEvidence = ActionToActualEvidence.get(actionString);
			
			//Map<ObservabilitySignature, Double> OStoCountForThisAction = getUpdatedOS() = new HashMap<ObservabilitySignature, Double>();
			//for (ObservabilitySignature os : ActionToOSList.get(actionString)){
			//	OStoCountForThisAction.put(os, OStoCount.get(os));
			//}
			
			for (int i = 0; i< totalParticlesGenerated; i++){
				InverseParticle p = canonicalParticle.copy();
				p.take(actionEvidence);
				p.take(EnclosingSC.nextTimestepEvidence);
				p.answer(EnclosingSC.nextTimestepQueries);
				Map<ObservabilitySignature, Double> OStoCountForThisAction = getUpdatedOStoCount(p, totalParticlesGenerated);
				EnclosingSC.nextStateCollection.addParticle(p, OStoCountForThisAction, ratio);
			}
		}
	}
	*/
	
	/**
	 * generate a map of actions to observability values
	 */
	public Map<String, Map<ObservabilitySignature, Double>> ActionToOSCounts(){
		Map<String, Map<ObservabilitySignature, Double>> ActionToOSList = new HashMap<String, Map<ObservabilitySignature, Double>>();
		Map<String, Double> ActionToCount = new HashMap<String, Double>();
		
		for (ObservabilitySignature os : OStoCount.keySet()){
			Evidence actionEvidence = this.EnclosingSC.OStoAction.get(os);
			String actionStr = actionEvidence.toString();
			if (ActionToOSList.containsKey(actionStr)){
				Map<ObservabilitySignature, Double> osList = ActionToOSList.get(actionStr);
				osList.put(os, OStoCount.get(os));
				ActionToCount.put(actionStr, ActionToCount.get(actionStr) + OStoCount.get(os));
			}
			else{
				HashMap<ObservabilitySignature, Double> newOSList = new HashMap<ObservabilitySignature, Double>();
				newOSList.put(os, OStoCount.get(os));
				ActionToOSList.put(actionStr, newOSList);
				ActionToCount.put(actionStr, OStoCount.get(os));
			}
			
		}
		for (String actionStr : ActionToCount.keySet()){
			Double trueCount = Math.ceil(ActionToCount.get(actionStr));
			
			Map<ObservabilitySignature, Double> OSCounts = ActionToOSList.get(actionStr);
			for (ObservabilitySignature thisOS : OSCounts.keySet()){
				OSCounts.put(thisOS, OSCounts.get(thisOS)/trueCount);
			}
			
			ActionToCount.put(actionStr, trueCount); 
		}
		
		return ActionToOSList;
	}

	
	
	/**
	 * produces a new map of os to count, because the canonical particle has already updated its observability signature
	 * @param p the particle which contains the observability values that must be added to each os in OStoCount
	 * @return
	 */
	public Map<ObservabilitySignature, Double> getUpdatedOStoCount (InverseParticle p, Map<ObservabilitySignature, Double> original){
		Map<ObservabilitySignature, Double> rtn = new HashMap<ObservabilitySignature, Double>();
		for (ObservabilitySignature os : original.keySet()){
			ObservabilitySignature updatedOS = os.copy();
			updatedOS.update(p);
			rtn.put(updatedOS, original.get(os));
		}
		return rtn;
	}

	
	
	public boolean equals (Object o){
		return canonicalParticle.equals(((State)o).canonicalParticle);
	}
	public int hashCode (){
		return canonicalParticle.hashCode();
	}
	
}
