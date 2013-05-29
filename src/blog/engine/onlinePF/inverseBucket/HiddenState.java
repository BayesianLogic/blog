package blog.engine.onlinePF.inverseBucket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import blog.DBLOGUtil;
import blog.bn.BayesNetVar;
import blog.common.Util;
import blog.engine.onlinePF.ObservabilitySignature;
import blog.model.Evidence;

public class HiddenState {
    
	/**
	 * the state collection 
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
	Map<Integer, Double> OStoCount = new HashMap<Integer, Double> ();
	double totalCount;
	
	public HiddenState (InverseParticle p, StateCollection enc){
		canonicalParticle = p;
		EnclosingSC = enc;
		totalCount = 0;
	}
	
	public void addOSCounts(Map<Integer, Double> mapToAdd){
		for (Integer osIndex : mapToAdd.keySet()){
			Double additionalCount = mapToAdd.get(osIndex);
			if (OStoCount.containsKey(osIndex)){
				OStoCount.put(osIndex, OStoCount.get(osIndex) + additionalCount);
			}
			else {
				OStoCount.put(osIndex, additionalCount);
			}
            ObservabilitySignature.updateOStoBucketSize(osIndex, additionalCount);
                        
			totalCount += additionalCount;
		}
	}
	public void doActionsAndAnswerQueries(){
	
		/////
		Map<String, Map<Integer, Double>> actionToOSCounts = this.ActionToOSCounts();
		Map<String, Double> actionToTotalCounts = this.ActionToTotalCounts();
		Map<String, Evidence> actionToActualEvidence = this.ActionToEvidence();
		/////
		
		
		if (EnclosingSC.nextStateCollection == null){
			System.err.println("State.doActions(): EnclosingSC.nextStateCollection is null");
			System.exit(1);
		}
		//for (ObservabilitySignature os : OStoCount.keySet()){
		for (String str : actionToTotalCounts.keySet()){
			for (int i = 0; i< Math.ceil(actionToTotalCounts.get(str)); i++){
				Evidence ev = actionToActualEvidence.get(str);
				
				InverseParticle np = canonicalParticle.copy();
				
				np.take(ev);
				np.take(EnclosingSC.nextTimestepEvidence);
				np.answer(EnclosingSC.nextTimestepQueries);
				np.advanceTimestep();
				
				
				Map<Integer, Double> newCounts = actionToOSCounts.get(str);//new HashMap<ObservabilitySignature, Double>();
				//Map<ObservabilitySignature, Double> newCounts = new HashMap<ObservabilitySignature, Double>();
				newCounts = getUpdatedOStoCount(np, newCounts);
				//ObservabilitySignature newOS = os.copy();
				//newOS.update(np);
				
				//newCounts.put(newOS, np.getLatestWeight());
				EnclosingSC.nextStateCollection.addParticle(np, newCounts);
				//Pattern timestepPattern = Pattern.compile("@\\d+");
				//Matcher matcher = timestepPattern.matcher(ev.toString());
				//if (matcher.find() && Integer.parseInt(matcher.group().substring(1))==8){
				//	System.err.print("found");
				//	if (!ev.toString().equals("[/*DerivedVar*/ chosen_Move(Listen, @8) = true]")){
				//		System.out.println("found");
				//	}
				//}
				
			}
		}
	}
	public void doActionsAndAnswerQueries2(){
		/////
		//Map<String, Map<Integer, Double>> actionToOSCounts = this.ActionToOSCounts();
		/////


		if (EnclosingSC.nextStateCollection == null){
			System.err.println("State.doActions(): EnclosingSC.nextStateCollection is null");
			System.exit(1);
		}
		for (Integer osIndex : OStoCount.keySet()){
			for (int i = 0; i< OStoCount.get(osIndex); i++){
				
				UBT.specialTimer.startTimer();
				InverseParticle np = canonicalParticle.copy();
				np.take(EnclosingSC.OStoAction.get(osIndex));
				np.take(EnclosingSC.nextTimestepEvidence);
				np.answer(EnclosingSC.nextTimestepQueries);
				np.advanceTimestep();
				
				UBT.specialTimingData5 += (UBT.specialTimer.elapsedTime());

				//Map<ObservabilitySignature, Double> newCounts = actionToOSCounts.get(EnclosingSC.OStoAction.get(os).toString());//new HashMap<ObservabilitySignature, Double>();
				Map<Integer, Double> newCounts = new HashMap<Integer, Double>();
				//newCounts = getUpdatedOStoCount(np, newCounts);
				ObservabilitySignature newOS = ObservabilitySignature.getOSbyIndex(osIndex).spawnChild(np);
				//newOS.update(np);
				Integer newOSIndex = newOS.getIndex();
				newCounts.put(newOSIndex, np.getLatestWeight());
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
	public Map<String, Map<Integer, Double>> ActionToOSCounts(){
		Map<String, Map<Integer, Double>> ActionToOSList = new HashMap<String, Map<Integer, Double>>();
		Map<String, Double> ActionToCount = new HashMap<String, Double>();
		
		for (Integer osIndex : OStoCount.keySet()){
			Evidence actionEvidence = this.EnclosingSC.OStoAction.get(osIndex);
			String actionStr = actionEvidence.toString();
			Double osIndexCount = OStoCount.get(osIndex);
			if (ActionToOSList.containsKey(actionStr)){
				Map<Integer, Double> osList = ActionToOSList.get(actionStr);
				osList.put(osIndex, osIndexCount);
				ActionToCount.put(actionStr, ActionToCount.get(actionStr) + osIndexCount);
			}
			else{
				HashMap<Integer, Double> newOSList = new HashMap<Integer, Double>();
				newOSList.put(osIndex, osIndexCount);
				ActionToOSList.put(actionStr, newOSList);
				ActionToCount.put(actionStr, osIndexCount);
			}
			
		}
		for (String actionStr : ActionToCount.keySet()){
			Double trueCount = Math.ceil(ActionToCount.get(actionStr));
			
			Map<Integer, Double> OSCounts = ActionToOSList.get(actionStr);
			for (Integer thisOSIndex : OSCounts.keySet()){
				OSCounts.put(thisOSIndex, OSCounts.get(thisOSIndex)/trueCount);
			}
			
			//ActionToCount.put(actionStr, trueCount); 
		}
		
		return ActionToOSList;
	}

	/**
	 * generate a map of actions to observability values
	 */
	public Map<String, Double> ActionToTotalCounts(){
		Map<String, Map<Integer, Double>> ActionToOSList = new HashMap<String, Map<Integer, Double>>();
		Map<String, Double> ActionToCount = new HashMap<String, Double>();
		
		for (Integer osIndex : OStoCount.keySet()){
			Evidence actionEvidence = this.EnclosingSC.OStoAction.get(osIndex);
			String actionStr = actionEvidence.toString();
			if (ActionToOSList.containsKey(actionStr)){
				Map<Integer, Double> osList = ActionToOSList.get(actionStr);
				osList.put(osIndex, OStoCount.get(osIndex));
				ActionToCount.put(actionStr, ActionToCount.get(actionStr) + OStoCount.get(osIndex));
			}
			else{
				HashMap<Integer, Double> newOSList = new HashMap<Integer, Double>();
				newOSList.put(osIndex, OStoCount.get(osIndex));
				ActionToOSList.put(actionStr, newOSList);
				ActionToCount.put(actionStr, OStoCount.get(osIndex));
			}
			
		}
		for (String actionStr : ActionToCount.keySet()){
			Double trueCount = Math.ceil(ActionToCount.get(actionStr));
			
			Map<Integer, Double> OSCounts = ActionToOSList.get(actionStr);
			for (Integer thisOSIndex : OSCounts.keySet()){
				OSCounts.put(thisOSIndex, OSCounts.get(thisOSIndex)/trueCount);
			}
			
			//ActionToCount.put(actionStr, trueCount); 
		}
		return ActionToCount;
	}
		
		public Map<String, Evidence> ActionToEvidence(){
			Map<String, Map<ObservabilitySignature, Double>> ActionToOSList = new HashMap<String, Map<ObservabilitySignature, Double>>();
			Map<String, Evidence> ActionToEvidence = new HashMap<String, Evidence>();
			
			for (Integer osIndex : OStoCount.keySet()){
				Evidence actionEvidence = this.EnclosingSC.OStoAction.get(osIndex);
				String actionStr = actionEvidence.toString();
				if (ActionToOSList.containsKey(actionStr)){
				}
				else{
					ActionToEvidence.put(actionStr, actionEvidence);
				}
				
			}
		
			return ActionToEvidence;
		}
	
	
	/**
	 * produces a new map of os to count, because the canonical particle has already updated its observability signature
	 * @param p the particle which contains the observability values that must be added to each os in OStoCount
	 * @return
	 */
	public Map<Integer, Double> getUpdatedOStoCount (InverseParticle p, Map<Integer, Double> original){
		Map<Integer, Double> rtn = new HashMap<Integer, Double>();
		for (Integer osIndex : original.keySet()){
			ObservabilitySignature updatedOS = (ObservabilitySignature.getOSbyIndex(osIndex)).spawnChild(p);
			//updatedOS.update(p);
			Integer updatedOSIndex = updatedOS.getIndex();
			rtn.put(updatedOSIndex, original.get(osIndex));
		}
		return rtn;
	}

	
	
	public boolean equals (Object o){
		return canonicalParticle.equals(((HiddenState)o).canonicalParticle);
	}
	public int hashCode (){
		return canonicalParticle.hashCode();
	}


	
}
