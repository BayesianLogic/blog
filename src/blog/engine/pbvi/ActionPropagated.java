package blog.engine.pbvi;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import blog.model.Evidence;

public class ActionPropagated {
	private Evidence action;
	private Belief belief;
	
	private Map<Evidence, Double> observationWeight;
	private Map<Evidence, Belief> nextBeliefs;
	
	public ActionPropagated(Belief belief, Evidence action) {
		this.belief = belief;
		this.action = action;
		
		observationWeight = new HashMap<Evidence, Double>();
		nextBeliefs = new HashMap<Evidence, Belief>();
	}
	
	public Set<Evidence> getObservations() {
		return nextBeliefs.keySet();
	}
	
	public double getObservationCount(Evidence obs) {
		if (!observationWeight.containsKey(obs)) 
			return 0;
		return observationWeight.get(obs);
	}
	
	public void setNextBelief(Evidence obs, Belief b) {
		nextBeliefs.put(obs, b);
	}
	
	public Belief getNextBelief(Evidence obs) {
		return nextBeliefs.get(obs);
	}

	public void setObservationWeight(Evidence o, Double weight) {
		observationWeight.put(o, weight);
		
	}

}
