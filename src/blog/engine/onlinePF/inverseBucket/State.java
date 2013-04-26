package blog.engine.onlinePF.inverseBucket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	Map<ObservabilitySignature, Double> OStoCount;
	double totalCount;
	
	public void doActions(){
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
				ActionToCount.put(actionStr, ActionToCount.get(actionStr) + OStoCount.get(os));
				if (!(ActionToCount.containsKey(actionStr)) || !ActionToActualEvidence.containsKey(actionStr)){
					System.err.println("State.doActions(): discrepency in keyset for action maps");
					System.exit(1);
				}
			}
		}
		// by this point, actions have been properly initialized
		for (String actionString : ActionToActualEvidence.keySet()){
			Double totalParticlesGenerated = Math.ceil(ActionToCount.get(actionString));
			Double ratio = ActionToCount.get(actionString)/totalParticlesGenerated;
			Evidence actionEvidence = ActionToActualEvidence.get(actionString);
			Map<ObservabilitySignature, Double> OStoCountForThisAction = new HashMap<ObservabilitySignature, Double>();
			for (ObservabilitySignature os : ActionToOSList.get(actionString)){
				OStoCountForThisAction.put(os, OStoCount.get(os));
			}
			for (int i = 0; i< totalParticlesGenerated; i++){
				InverseParticle p = canonicalParticle.copy();
				p.take(actionEvidence);
				p.answer(EnclosingSC.nextStateQueries);
				EnclosingSC.nextStateCollection.addParticle(p, OStoCountForThisAction, ratio);
			}
		}
		
	}
	
}
