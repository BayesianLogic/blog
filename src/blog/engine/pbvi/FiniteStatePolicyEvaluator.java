package blog.engine.pbvi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blog.engine.onlinePF.inverseBucket.TimedParticle;
import blog.model.Evidence;
import blog.world.AbstractPartialWorld;

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
		return eval(b, p, numTrials, 0);
	}
	
	public Double eval(Belief b, FiniteStatePolicy p, int numTrials, int numTrialsToPrint) {
		Double value = 0D;
		int totalCount = 0;
		/*List<TimedParticle> particles = b.getParticleFilter().particles;
		for (TimedParticle particle : particles) {
			State s = new State((AbstractPartialWorld) particle.curWorld, particle.getTimestep());
			Double v = eval(s, p, numTrials, numTrialsToPrint);
			if (v == null) return v;
			//value += v * b.getCount(s);
			//totalCount += b.getCount(s);
			value += v;
			totalCount += 1;
		}*/
		for (State s : b.getStates()) {
			Double predictedValue = p.getAlphaVector().getValue(s);
			if (numTrialsToPrint > 0 && predictedValue != null)
				System.out.println("Predicted Value for state to evaluate: " + predictedValue);
			Double v = eval(s, p, numTrials, numTrialsToPrint);
			if (numTrialsToPrint > 0)
				System.out.println("Evaluated Value for state: " + v);
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
		return eval(state, p, numTrials, 0);
	}
	
	public Double eval(State state, FiniteStatePolicy p, int numTrials, int numTrialsToPrint) {
		numMissingObs = new HashMap<Evidence, Integer>();
		int numPathsPrinted = 0;
		Belief initState = Belief.getSingletonBelief(state, 1, pomdp);
		double accumulatedValue = 0;
		
		for (int i = 0; i < numTrials; i++) {
			Belief curState = initState;
			FiniteStatePolicy curPolicy = p;
			double curValue = 0D;
			double discount = 1;
			List<Evidence> curPath = new ArrayList<Evidence>();
			LiftedProperties policyHistory = new LiftedProperties();
			LiftedProperties history = new LiftedProperties();
			while (curPolicy != null) {
				if (curState.ended()) break;
				//policyHistory.debug = true;
				Map<Object, Object> subst = policyHistory.findNgoSubstitution(history);
				//System.out.println(policyHistory + " " + history + " " + subst);
				LiftedEvidence nextAction = curPolicy.getAction();
				Evidence timeGroundedAction = nextAction.getEvidence(curState);
				Evidence groundedAction = timeGroundedAction.replace(subst);
				curPath.add(groundedAction);
				
				LiftedEvidence policyLiftedAction = new LiftedEvidence(timeGroundedAction, null, policyHistory);
				LiftedEvidence liftedAction = new LiftedEvidence(groundedAction, null, history);
				
				policyHistory = policyLiftedAction.getLiftedProperties();
				history = liftedAction.getLiftedProperties();
				
				curState = curState.sampleNextBelief(groundedAction);
				
				Evidence nextObs = curState.getLatestEvidence();
				curPath.add(nextObs);
				LiftedEvidence liftedEvidence = new LiftedEvidence(nextObs, null, history);
				LiftedEvidence policyEvidence = curPolicy.getMatchingEvidence(liftedEvidence, policyHistory, curState);
				FiniteStatePolicy nextPolicy = curPolicy.getNextPolicy(policyEvidence);
				
				if (nextPolicy == null && !curState.ended()) { 
					nextPolicy = curPolicy.getApplicableNextPolicy(new LiftedEvidence(nextObs, curState), curState);
					if (nextPolicy != null) {
						addMissingObs(nextObs);
					}
				}

				if (nextPolicy != null) {
					history = liftedEvidence.getLiftedProperties();
					LiftedEvidence policyLiftedEvidence = new LiftedEvidence(policyEvidence.getEvidence(curState.getTimestep()), null, policyHistory);
					policyHistory = policyLiftedEvidence.getLiftedProperties();
				}
				
				curPolicy = nextPolicy;
				
				curValue += discount * curState.getLatestReward();
				discount = discount * gamma;
			}
			if (numPathsPrinted < numTrialsToPrint) {
				System.out.println("Value: " + curValue + ", Path: " + curPath);
				System.out.println(history + " " + policyHistory);
				numPathsPrinted++;
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
				LiftedEvidence nextAction = p.getAction(pomdp.getActions(curState));
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
