package blog.engine.experiments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blog.engine.onlinePF.PFEngine.PFEngineSampled;
import blog.engine.onlinePF.absyn.PolicyModel;
import blog.engine.onlinePF.runner.PFRunnerSampled;
import blog.model.ArgSpecQuery;
import blog.model.Evidence;
import blog.model.Model;

public class OUPBVI {
	private Collection<PFEngineSampled> beliefs;
	private Collection<PolicyModel> policies;
	private Model model;
	private int numObsSamples;
	private List<ArgSpecQuery> queries;
	
	private void generateBeliefs() {
		//TODO
	}
	
	private Double evaluatePolicy(PolicyModel pm, PFEngineSampled belief) {
		//assumes first query is value
		ArgSpecQuery query = this.queries.get(0); //TODO: copy query
		List<ArgSpecQuery> queries = new ArrayList<ArgSpecQuery>();
		queries.add(query);
		PFRunnerSampled runner = new PFRunnerSampled(model, queries, belief, pm);
		runner.run();
		
		belief.updateQuery(queries);
		return query.averageQueryResult(); 
	}
	
	/**
	 * Given policies of iteration i, compute policies for iteration i+1
	 */
	private void pbviIteration() {
		List<PolicyModel> newPolicies = new ArrayList<PolicyModel>();
		for (PFEngineSampled belief : beliefs) {
			Evidence bestAction = null;
			Map<Integer, PolicyModel> bestNextPolicies = null;
			Double bestValue = null;
			
			//TODO
			//get decision functions
			//generate possible actions
			Collection<Evidence> actions = null; 
		
			for (Evidence action : actions) {
				PFEngineSampled beliefAfterAction = belief.copy();
				beliefAfterAction.take(action);

				// map_{b,a}
				Map<Integer, PolicyModel> nextPolicies = new HashMap<Integer, PolicyModel>();
				Map<Integer, Double> nextValues = new HashMap<Integer, Double>();

				// for calculating P(o|b,a)
				Map<Integer, Integer> observationCounts = new HashMap<Integer, Integer>();
				
				computeBestNext(beliefAfterAction, nextPolicies,
						nextValues, observationCounts);
				
				//Reward of action dot belief + sum over o P(o|b, a) bestNextValues(o)
				Double value = 0.0;
				for(Integer o : observationCounts.keySet()) {
					double prob = observationCounts.get(o) / (double) numObsSamples;
					value += prob * nextValues.get(o);
				}
				//TODO: reward?
				
				//argmax over actions
				if (bestValue == null || value > bestValue) {
					bestValue = value;
					bestNextPolicies = nextPolicies;
				}
			}
			
			//create new policy
			PolicyModel newPolicy = createPolicy(bestAction, bestNextPolicies);
			newPolicies.add(newPolicy);
		}
		policies = newPolicies;
	}

	/**
	 * Given a belief state after taking an action, 
	 * sample the next belief states and compute 
	 * 1) the best policies for them
	 * 2) the corresponding best values
	 * 3) number of times each belief state was sampled
	 * 
	 * @param beliefAfterAction
	 * @param bestNextPolicies
	 * @param bestNextValues
	 * @param observationCounts
	 */
	private void computeBestNext(PFEngineSampled beliefAfterAction,
			Map<Integer, PolicyModel> bestNextPolicies,
			Map<Integer, Double> bestNextValues,
			Map<Integer, Integer> observationCounts) {
		// sample the next beliefs
		for (int s = 0; s < numObsSamples; s++) {
			PFEngineSampled nextBelief = beliefAfterAction.copy();

			//sample observation
			Integer sampledOSindex = nextBelief.retakeObservability(); // get the observation
			if (observationCounts.containsKey(sampledOSindex)) {
				observationCounts.put(sampledOSindex, observationCounts.get(sampledOSindex) + 1);
				continue;
			}
			
			//max, argmax over policies of 
			Double bestNextValue = null;
			PolicyModel bestNextPolicy = null;
			for (PolicyModel pm : policies) {
				Double value = evaluatePolicy(pm, nextBelief);
				if (bestNextValue == null || bestNextValue < value) {
					bestNextValue = value;
					bestNextPolicy = pm;
				}
			}
			bestNextValues.put(sampledOSindex, bestNextValue);
			bestNextPolicies.put(sampledOSindex, bestNextPolicy);
		}
	}
	
	//TODO: build policy given top action and contingent polices
	private PolicyModel createPolicy(Evidence action, Map<Integer, PolicyModel> nextPolicies) {
		return null;
	}
}

