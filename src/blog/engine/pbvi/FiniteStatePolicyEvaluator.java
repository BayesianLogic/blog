package blog.engine.pbvi;

import java.util.HashMap;
import java.util.Map;

import blog.model.Evidence;

public class FiniteStatePolicyEvaluator {
	private OUPOMDPModel pomdp;
	private double gamma;
	private Map<Evidence, Integer> numMissingObs;
	
	public FiniteStatePolicyEvaluator(OUPOMDPModel pbvi, double gamma) {
		this.pomdp = pbvi;
		this.gamma = gamma;
	}
	
	public Map<Evidence, Integer> getLastMissingObs() {
		return numMissingObs;
	}

	private void addMissingObs(Evidence nextObs) {
		if (!numMissingObs.containsKey(nextObs))
			numMissingObs.put(nextObs, 0);
		numMissingObs.put(nextObs, numMissingObs.get(nextObs) + 1);
	}
	
	public Double eval(Belief b, FiniteStatePolicy p, int numTrials) {
		Double value = 0D;
		int totalCount = 0;
		for (State s : b.getStates()) {
			Double v = eval(s, p, numTrials);
			if (v == null) return v;
			value += v * b.getCount(s);
			totalCount += b.getCount(s);
		}
		
		return value/totalCount;
	}
	
	public String getMissingObs() {
		String result = "";
		for (Evidence o : numMissingObs.keySet()) {
			result += o + " " + numMissingObs.get(o) + "\n";
		}
		return result;
	}
	
	public Double eval(State state, FiniteStatePolicy p, int numTrials) {
		numMissingObs = new HashMap<Evidence, Integer>();
		Belief initState = Belief.getSingletonBelief(state, 1, pomdp);
		double accumulatedValue = 0;
		
		for (int i = 0; i < numTrials; i++) {
			Belief curState = initState;
			FiniteStatePolicy curPolicy = p;
			double curValue = 0D;
			double discount = 1;
			
			while (curPolicy != null) {
				if (curState.ended()) break;
				Evidence nextAction = curPolicy.getAction();
				curState = curState.sampleNextBelief(nextAction);		
				Evidence nextObs = curState.getLatestEvidence();
				FiniteStatePolicy nextPolicy = curPolicy.getNextPolicy(nextObs);
				if (nextPolicy == null && !curState.ended()) { 
					nextPolicy = curPolicy.getApplicableNextPolicy(nextObs, curState);
					if (nextPolicy != null) {
						addMissingObs(nextObs);
					}
				}
				curPolicy = nextPolicy;
				
				curValue += discount * curState.getLatestReward();
				discount = discount * gamma;
			}
			accumulatedValue += curValue;
		}
		return accumulatedValue/numTrials;
	}
	
	public Double eval(Belief b, DotToPolicy p, int numTrials) {
		Double value = 0D;
		int totalCount = 0;
		for (State s : b.getStates()) {
			Double v = eval(s, p, numTrials);
			if (v == null) return v;
			value += v * b.getCount(s);
			totalCount += b.getCount(s);
		}
		System.out.println("Total count " + totalCount);
		return value/totalCount;
	}
	
	public Double eval(State state, DotToPolicy p, int numTrials) {
		numMissingObs = new HashMap<Evidence, Integer>();
		Belief initState = Belief.getSingletonBelief(state, 1, pomdp);
		//System.out.println(initState);
		double accumulatedValue = 0;
		
		for (int i = 0; i < numTrials; i++) {
			Belief curState = initState;
			double curValue = 0D;
			double discount = 1;
			int iter = 0;
			p.resetSim();
			while (true) {
				if (curState.ended()) break;
				Evidence nextAction = p.getAction(pomdp.getActions(curState));
				//System.out.println(nextAction);
				curState = curState.sampleNextBelief(nextAction);		
				Evidence nextObs = curState.getLatestEvidence();
				boolean nextAvailable = p.advancePolicy(nextObs);
				curValue += discount * curState.getLatestReward();
				discount = discount * gamma;
				iter++;
				if (!nextAvailable) break;
			}

			accumulatedValue += curValue;
		}
		//System.out.println("Accum value " + accumulatedValue);
		return accumulatedValue/numTrials;
	}

}
