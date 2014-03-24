package blog.engine.pbvi;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import blog.engine.onlinePF.ObservabilitySignature;
import blog.engine.onlinePF.PFEngine.PFEngineSampled;
import blog.engine.onlinePF.inverseBucket.UBT;
import blog.model.Evidence;

public class ActionPropagated {
	private Evidence action;
	private Belief belief;
	
	private Map<Evidence, Double> observationWeight;
	//private Map<Evidence, Belief> nextBeliefs;
	private Map<Evidence, Integer> osIndices;
	
	private double reward;
	private PFEngineSampled actionPropagatedPF;
	
	public ActionPropagated(Belief belief, Evidence action) {
		this.belief = belief;
		this.action = action;
		
		observationWeight = new HashMap<Evidence, Double>();
		//nextBeliefs = new HashMap<Evidence, Belief>();
		osIndices = new HashMap<Evidence, Integer>();
	}
	
	public Set<Evidence> getObservations() {
		return observationWeight.keySet();
	}
	
	public double getObservationCount(Evidence obs) {
		if (!observationWeight.containsKey(obs)) 
			return 0;
		return observationWeight.get(obs);
	}
	
	/*public void setNextBelief(Evidence obs, Belief b) {
		nextBeliefs.put(obs, b);
	}*/
	
	public void setActionPropagatedPF(PFEngineSampled pf) {
		this.actionPropagatedPF = pf;
	}
	
	/*public Belief getNextBelief(Evidence obs) {
		return nextBeliefs.get(obs);
		
	}*/
	
	public Boolean ended() {
		return belief.ended();
	}
	
	public Belief getNextBelief(Evidence o, OUPBVI pbvi) {
		Timer.start("BELIEF_PROP");
		int osIndex = osIndices.get(o);
		PFEngineSampled nextPF = actionPropagatedPF.copy();
		nextPF.retakeObservability2(osIndex);
		nextPF.retakeObservability(osIndex);
		if (UBT.dropHistory) {
			nextPF.dropHistory();
			//ObservabilitySignature.dropHistory(((TimedParticle)Util.getFirst(nextPF.particles)).getTimestep());
		}
		nextPF.resample();
		Belief nextBelief = new Belief(nextPF, pbvi);
		Timer.record("BELIEF_PROP");
		Belief.updateResampleStateCountStats(nextBelief);
		return nextBelief;
	}

	public void setObservationWeight(Evidence o, Double weight) {
		observationWeight.put(o, weight);
	}
	
	public void setOSIndex(Evidence o, int osIndex) {
		osIndices.put(o, osIndex);
	}
	
	public void setReward(double r) { reward = r; }
	public double getReward() { return reward; }

}
