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
			/*
			Map<ObservabilitySignature, Double> OStoCountForThisAction = getUpdatedOS() = new HashMap<ObservabilitySignature, Double>();
			for (ObservabilitySignature os : ActionToOSList.get(actionString)){
				OStoCountForThisAction.put(os, OStoCount.get(os));
			}
			*/
			for (int i = 0; i< totalParticlesGenerated; i++){
				InverseParticle p = canonicalParticle.copy();
				p.take(actionEvidence);
				p.answer(EnclosingSC.nextTimestepQueries);
				Map<ObservabilitySignature, Double> OStoCountForThisAction = getUpdatedOStoCount(p);
				EnclosingSC.nextStateCollection.addParticle(p, OStoCountForThisAction, ratio);
			}
		}
	}
	
	/**
	 * produces a new map of os to count, because the canonical particle has already updated its observability signature
	 * @param p the particle which contains the observability values that must be added to each os in OStoCount
	 * @return
	 */
	public Map<ObservabilitySignature, Double> getUpdatedOStoCount (InverseParticle p){
		Map<ObservabilitySignature, Double> rtn = new HashMap<ObservabilitySignature, Double>();
		for (ObservabilitySignature os : OStoCount.keySet()){
			ObservabilitySignature updatedOS = os.copy();
			updatedOS.update(p);
			rtn.put(updatedOS, OStoCount.get(os));
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
