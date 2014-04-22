package blog.engine.pbvi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import blog.DBLOGUtil;
import blog.Main;
import blog.engine.experiments.query_parser;
import blog.engine.onlinePF.ObservabilitySignature;
import blog.model.Evidence;
import blog.model.Model;
import blog.common.Util;

public class OUPBVI {
	private OUPOMDPModel pomdp;
	
	private Properties properties;
	private int horizon;
	
	private double minReward = -10;
	
	private double epsilon = 0.1;
	
	private int numBeliefs;
	private int numParticles;
	private int numPbviIterations = 1;
	private boolean usePerseus = true;
	private static boolean debugOn = false;
	
	private Map<State, State> alphaKeys;
	private Map<State, State> addedAlphaKeys;
	private boolean useVisitedStates = true;
	private int maxNumAlphaKeys; 
	
	private Map<Belief, Integer> beliefIDs = new HashMap<Belief, Integer>();
	private int beliefID = 0;
	

	
	public OUPBVI(OUPOMDPModel model, Properties properties, int horizon, int numBeliefs) {
		this.pomdp = model;
		this.horizon = horizon;
		this.properties = properties;

		this.numBeliefs = numBeliefs;
		numParticles = Integer.parseInt((String) properties.get("numParticles"));
		this.maxNumAlphaKeys = numParticles * numBeliefs;
		alphaKeys = new HashMap<State, State>();
	}
	
	private void debug(String s) {
		if (debugOn) {
			System.out.println(s);
		}
	}
	
	private Pair<FiniteStatePolicy, Double> bestPolicyValue(Collection<FiniteStatePolicy> policies, Belief b) {
		double bestValue = Double.NEGATIVE_INFINITY;
		FiniteStatePolicy bestPolicy = null;
		for (FiniteStatePolicy p : policies) {
			double value = evalPolicy(b, p);
			if (value > bestValue) {
				bestValue = value;
				bestPolicy = p;
			}
		}
		return new Pair<FiniteStatePolicy, Double>(bestPolicy, bestValue);
	}
	
	public Set<FiniteStatePolicy> run() {
		Set<Belief> beliefs = new HashSet<Belief>();

		Belief initBelief = pomdp.generateInitialBelief();
		System.out.println(initBelief);
		beliefs.add(initBelief);
		beliefIDs.put(initBelief, beliefID);
		beliefID++;
		
		Set<Belief> newBeliefs = new HashSet<Belief>(); //beliefs;
		newBeliefs.addAll(beliefs);
		
		int numIter = 0;
		while (newBeliefs.size() < numBeliefs) {
			newBeliefs = maxNormBeliefExpansion(newBeliefs);
			System.out.println("run.expansion.iteration: " + numIter);
			System.out.println("run.expansion.newsize: " + newBeliefs.size());
			numIter++;
		}
		beliefs = newBeliefs;
		
		for (Belief b : beliefs) {
			System.out.println("Belief: " + beliefIDs.get(b) + " " + b);
		}
		
		updateAlphaKeys(beliefs);
		System.out.println("run.expansion.beliefsize: " + beliefs.size());
		
		addedAlphaKeys = new HashMap<State, State>();
		Set<FiniteStatePolicy> policies = new HashSet<FiniteStatePolicy>();
		int pbviIteration = 0;
		Pair<FiniteStatePolicy, Double> bestPolicyValue;
		while (true) {
			System.out.println("Starting pbvi iteration: " + pbviIteration + "/" + numPbviIterations);
			System.out.println("Number of beliefs: " + beliefs.size());
			System.out.println("Number of policies: " + policies.size());
			for (int t = horizon - 1; t >= 0; t--) {
				Set<FiniteStatePolicy> newPolicies; 
				if (usePerseus)
					newPolicies = singleBackupPerseus(policies, beliefs, t);
				else
					newPolicies = singleBackup(policies, beliefs, t);
				setAlphaVectors(newPolicies, policies, t);
				policies = newPolicies;

				System.out.println("run policies " + policies.size() + " " + t);
				int i = 0;
				for (FiniteStatePolicy p : policies) {
					System.out.println(p.toDotString("p" + "_t" + t + "_i" + i));
					i++;
				}
				System.out.println("Number of OS: " + ObservabilitySignature.OStoIndex.size());
				
				System.out.println(Timer.getElapsedStr() + "[DELTA]");
				double maxDelta = 0;
				for (Belief b : beliefs) {
					Pair<FiniteStatePolicy, Double> bestPolicyValueForBelief = bestPolicyValue(policies, b);
					Double oldValue = prevBestVals.get(b);
					if (oldValue == null) {
						oldValue = Double.NEGATIVE_INFINITY;
					}
					double delta = Math.abs(bestPolicyValueForBelief.y - oldValue);
					if (delta > maxDelta) {
						maxDelta = delta;
					}
					prevBestVals.put(b, bestPolicyValueForBelief.y);
					prevBestPolicies.put(b, bestPolicyValueForBelief.x);
				}
				
				System.out.println("Best value for initial belief " + prevBestVals.get(initBelief));
				System.out.println(Timer.getElapsedStr() + "[DELTA_DONE]");
				System.out.println("Max Delta" + maxDelta);
				if (maxDelta < epsilon) {
					System.out.println("Converged: " + maxDelta);
					break;
				}
			}
			pbviIteration++;
			bestPolicyValue = bestPolicyValue(policies, initBelief);
			System.out.println("Best policy for initial belief: " + bestPolicyValue.x.toDotString("p0"));
			System.out.println("Best value: " + bestPolicyValue.y);
			System.out.println("Init belief: " + initBelief);
			System.out.println();
			System.out.println("run num beliefs " + beliefs.size());
			System.out.println("run num states " + alphaKeys.size());
			
			if (pbviIteration < numPbviIterations) {
				numBeliefs = numBeliefs * 2;
				newBeliefs = maxNormBeliefExpansion(newBeliefs);
				beliefs = newBeliefs;
				updateAlphaKeys(beliefs);
			} else {
				break;
			}
		}
		System.out.println("Value function's predicted value: " + bestPolicyValue.y);
		
		System.out.println("Evaluating best policy");
		evaluate(bestPolicyValue.x);
		debug(Timer.getElapsedStr() + "[EVAL]");
		debug(Timer.getElapsedStr() + "[EVAL_DONE]");
		
		return policies;
	}
	
	private void updateAlphaKeys(Set<Belief> beliefs) {
		for (Belief b: beliefs) {
			for (State s : b.getStates()) {
				alphaKeys.put(s, s);
			}
		}
		System.out.println("Alpha keys: ");
		for (State s : alphaKeys.keySet()) {
			System.out.println(s);
		}
	}
	
	private Set<FiniteStatePolicy> createMergedPolicySet(Set<FiniteStatePolicy> policies, FiniteStatePolicy policy) {
		Set<FiniteStatePolicy> newPolicies = new HashSet<FiniteStatePolicy>();
		for (FiniteStatePolicy p : policies) {
			if (!policy.merge(p)) {
				newPolicies.add(p);
			} 
		}
		newPolicies.add(policy);
		return newPolicies;
	}
	
	private Belief getSingletonBelief(State state, int numParticles) {
		return Belief.getSingletonBelief(state, numParticles, this.pomdp);
	}


	private Set<Belief> maxNormBeliefExpansion(Set<Belief> beliefs) {
		System.out.println(Timer.getElapsedStr() + "[EXPAND]");
		Set<Belief> newBeliefs = new HashSet<Belief>(beliefs);
		for (Belief belief : beliefs) {
			if (belief.getTimestep() == horizon) continue;
			if (newBeliefs.size() >= numBeliefs) break;
			Set<LiftedEvidence> actions = pomdp.getActions(belief);
			Belief bestBelief = null;
			int maxDiff = 0;
			for (LiftedEvidence action : actions) {
				int minDiff = Integer.MAX_VALUE;
				Belief next = belief.sampleNextBelief(action);
				if (next.ended()) continue;
				if (bestBelief == null) bestBelief = next;
				for (Belief other : newBeliefs) {
					//if (other.getTimestep() != next.getTimestep()) continue;
					int diff = next.diffNorm(other);
					if (minDiff > diff) {
						minDiff = diff;
					}
				}
				if (maxDiff < minDiff) {
					bestBelief = next;
					maxDiff = minDiff;
				}
			}
			newBeliefs.add(bestBelief);
			beliefIDs.put(bestBelief, beliefID);
			beliefID++;
			//addToSampledPOMDP(bestBelief, pomdp);
			System.out.println("max diff: " + maxDiff);
		}
		System.out.println(Timer.getElapsedStr() + "[EXPAND_DONE]");
		return newBeliefs;
	}
	
	private double evalPolicy(Belief b, FiniteStatePolicy p) {
		Double value = 0D;
		int totalCount = 0;
		for (State s : b.getStates()) {
			value += evalPolicy(s, p) * b.getCount(s);
			totalCount += b.getCount(s);
		}
		return value/totalCount;
	}
	
	int evalStateCount = 0;

	private Double evalPolicy(State s, FiniteStatePolicy p) {
		Double val = evalPolicyDFS(getSingletonBelief(s, 1), p);
		if (alphaKeys.containsKey(s)) { 
			p.getAlphaVector().setValue(alphaKeys.get(s), val);
		} else if (addedAlphaKeys.containsKey(s)) {
			p.getAlphaVector().setValue(addedAlphaKeys.get(s), val);
		} else if (addedAlphaKeys.size() < maxNumAlphaKeys) {
			addedAlphaKeys.put(s, s);
			p.getAlphaVector().setValue(s, val);
		}
		return val;
	}
	
	Map<Integer, Long> evalPolicyTimes = new HashMap<Integer, Long>();
	Map<Integer, Integer> evalPolicyCounts= new HashMap<Integer, Integer>();
	private double evalPolicyDFS(Belief b, FiniteStatePolicy p) {
		State state = null;
		if (b.getStates().size() > 1) {
			System.out.println("Why is it not a singleton belief in DFS?");
		}
		
		for (State s : b.getStates()) {
			if (b.getCount(s) > 1) {
				System.out.println("Why is it not a single count belief in DFS?");
			}
			state = s;
		}
		
		if (b.ended() && !usePerseus) {
			debug(Timer.getElapsedStr() + "[EVAL]->pid" + p.getID() + " end " + state);
			return worstValue();
		}
		
		Double v = p.getAlphaVector().getValue(b);
		if (v != null) {
			debug(Timer.getElapsedStr() + "[EVAL]->pid" + p.getID() + " found " + state);
			return v;
		}
		
		debug(Timer.getElapsedStr() + "[EVAL]->pid" + p.getID() + " not found " + state);
		
		v = 0D;
		
		int numTrials = 100;
		for (int i = 0; i < numTrials; i++) {
			Belief curBelief = b;
			FiniteStatePolicy curPolicy = p;
			boolean alphaFound = false;
			double nextValue = 0; //set to nonzero if alpha vector can calc value of belief
			double curValue = 0D;
			double discount = 1;
			while (curPolicy != null) {//curBelief.getTimestep() < horizon) {
				if (!usePerseus && curBelief.ended()) break;
				Double alphaValue = curPolicy.getAlphaVector().getValue(curBelief);
				if (alphaValue != null) {
					nextValue = discount * alphaValue;
					alphaFound = true;
					break;
				}
				LiftedEvidence nextAction = curPolicy.getAction();
				curBelief = curBelief.sampleNextBelief(nextAction);		
				Evidence nextObs = curBelief.getLatestEvidence();
				FiniteStatePolicy nextPolicy = curPolicy.getNextPolicy(nextObs);
				if (nextPolicy == null && !curBelief.ended() && !curPolicy.isLeafPolicy()) { 
					nextPolicy = curPolicy.getApplicableNextPolicy(nextObs, curBelief);
					if (nextPolicy != null) {
						curPolicy.setNextPolicy(new LiftedEvidence(nextObs), nextPolicy);
						curPolicy.addObsNote(nextObs, "random applicable");
					} else {
						System.out.println("No applicable next policy for " + curBelief);
						System.exit(0);
					}
				}
				curPolicy = nextPolicy;
				
				curValue += discount * curBelief.getLatestReward();
				discount = discount * pomdp.getGamma();
			}
			
			if (usePerseus && !alphaFound) {
				nextValue = worstValue() * discount;
			}
			v += curValue + nextValue;
		}
		
		/*
		if (!evalPolicyTimes.containsKey(startingTimestep)) {
			evalPolicyTimes.put(startingTimestep, 0L);
			evalPolicyCounts.put(startingTimestep, 0);
		}
		evalPolicyTimes.put(startingTimestep, evalPolicyTimes.get(startingTimestep) + (System.currentTimeMillis() - startTime));
		evalPolicyCounts.put(startingTimestep, evalPolicyCounts.get(startingTimestep) + 1);
		*/
		return v/numTrials;
	}

	private double worstValue() {
		return minReward/(1 - pomdp.getGamma());
	}
	/*
	private Belief addToSampledPOMDP(Belief belief) {
		Stack<State> toExpand = new Stack<State>();
		Set<State> expanded = new HashSet<State>();
		Set<State> initStates = new HashSet<State>();
		initStates.addAll(belief.getStates());	
		//initStates.removeAll(pomdp.getStates());
		toExpand.addAll(initStates);
		
		while (!toExpand.isEmpty()) {
			State s = toExpand.pop();
			if (expanded.contains(s)) continue;
			if (pomdp.getActions(s) != null) continue;
			pomdp.addState(s);
			s = pomdp.getState(s);
			expanded.add(s);
			Belief b = getSingletonBelief(s, 1);
			Set<Evidence> actions = model.getActions(b);
			
			pomdp.addStateActions(s, actions);
	
			for (Evidence a : actions) {
				pomdp.addReward(s, a, b.getReward(a));
			}
			System.out.println("# states left to expand " + toExpand.size());
			System.out.println("# states expanded " + expanded.size());
		}
		System.out.println("# states " + pomdp.getStates().size());
		System.out.println("# states expanded " + expanded.size());
		
		Belief newBelief = new Belief(this.model);
		for (State s : belief.getStates()) {
			newBelief.addState(pomdp.getState(s), belief.getCount(s));
		}
		return newBelief;
	}*/
	
	private Map<Belief, Double> prevBestVals = new HashMap<Belief, Double>();
	private Map<Belief, FiniteStatePolicy> prevBestPolicies = 
			new HashMap<Belief, FiniteStatePolicy>();
	
	private Set<FiniteStatePolicy> singleBackup(Set<FiniteStatePolicy> oldPolicies, 
			Set<Belief> beliefs, 
			int t) {
		Set<FiniteStatePolicy> newPolicies = new HashSet<FiniteStatePolicy>();
		for (Belief b : beliefs) {
			newPolicies = createMergedPolicySet(newPolicies, singleBackupForBelief(oldPolicies, b, t));
			System.out.println("handled bid" + beliefIDs.get(b));
		}
		System.out.println("SingleBackup numbeliefs: " + beliefs.size());
		Timer.print();
		return newPolicies;
	}
	
	private double maxValueDelta;
	
	private void resetMaxDelta() {
		maxValueDelta = Double.NEGATIVE_INFINITY;
	}
	
	private void updateMaxDelta(double delta) {
		if (delta > maxValueDelta) {
			maxValueDelta = delta;
		}
	}
	
	private Set<FiniteStatePolicy> singleBackupPerseus(Set<FiniteStatePolicy> oldPolicies, 
			Set<Belief> beliefs, 
			int t) {
		resetMaxDelta();
		Set<FiniteStatePolicy> newPolicies = new HashSet<FiniteStatePolicy>();
		List<FiniteStatePolicy> tempNewPolicies = new ArrayList<FiniteStatePolicy>();
		List<Belief> shuffledBeliefs = new ArrayList<Belief>(beliefs);
		Collections.shuffle(shuffledBeliefs);
		for (Belief b : shuffledBeliefs) {
			System.out.println("New policies size:" + tempNewPolicies.size());
			Double oldValue = prevBestVals.get(b);
			if (oldValue == null) {
				oldValue = worstValue();
			}
			boolean foundBetterPolicy = false;
			for (FiniteStatePolicy newPolicy : tempNewPolicies) {
				double v = evalPolicy(b, newPolicy);
				Double bestPrevVal = oldValue;
				if (v > bestPrevVal) {
					foundBetterPolicy = true;
					break;
				}
			}
			if (foundBetterPolicy) {
				System.out.println("Skipping belief");
				continue;
			}

			FiniteStatePolicy newPolicy = singleBackupForBelief(oldPolicies, b, t);
			setAlphaVector(newPolicy);
			double newVal = evalPolicy(b, newPolicy);
			if (!prevBestVals.containsKey(b) || prevBestVals.get(b) < newVal) {
				tempNewPolicies.add(newPolicy);
			} else {
				double bestOldVal = oldValue;
				FiniteStatePolicy bestOldPolicy = prevBestPolicies.get(b);
				for (FiniteStatePolicy oldPolicy : oldPolicies) {
					double oldVal = evalPolicy(b, oldPolicy);
					if (oldVal > bestOldVal) {
						bestOldVal = oldVal;
						bestOldPolicy = oldPolicy;
					}
				}
				tempNewPolicies.add(bestOldPolicy);
			}
			System.out.println("handled bid" + beliefIDs.get(b));
		}
		for (FiniteStatePolicy p : tempNewPolicies) {
			newPolicies = createMergedPolicySet(newPolicies, p);
			System.out.println("SingleBackup numbeliefs: " + beliefs.size());
		}
		Timer.print();
		return newPolicies;
	}
	
	private FiniteStatePolicy singleBackupForBelief(
			Set<FiniteStatePolicy> oldPolicies,
			Belief b,
			int t) {
		Map<Evidence, FiniteStatePolicy> bestPolicyMap = new HashMap<Evidence, FiniteStatePolicy>();
		LiftedEvidence bestAction = null;
		
		Set<LiftedEvidence> actions = pomdp.getActions(b);
		System.out.println("single backup actions: " + actions);
		System.out.println("single backup num oldpolicies: " + oldPolicies.size());
		Double bestValue = null;
		for (LiftedEvidence action : actions) {
			double value = 0;
			double totalWeight = 0;
			Map<Evidence, FiniteStatePolicy> policyMap = new HashMap<Evidence, FiniteStatePolicy>();
			if (oldPolicies.size() > 0) {
				ActionPropagated ap = b.beliefsAfterAction(action);
				for (Evidence obs : ap.getObservations()) {
					Belief next = ap.getNextBelief(obs, this.pomdp);
					debug(Timer.getElapsedStr() + "[FWD]->bid" + beliefIDs.get(b) + " " + action + " " + obs);
					if (next.ended() && !usePerseus) {
						break;
					}
					Double bestContingentValue = Double.NEGATIVE_INFINITY;
					FiniteStatePolicy bestContingentPolicy = null;
					for (FiniteStatePolicy p : oldPolicies) {
						if (!p.isApplicable(next)) continue;
						
						Double v = evalPolicy(next, p);
						if (v > bestContingentValue) {
							bestContingentValue = v;
							bestContingentPolicy = p;
						}
					}	
					double weight = ap.getObservationCount(obs);
					policyMap.put(obs, bestContingentPolicy);
					value += bestContingentValue * weight;
					totalWeight += weight;
				}
				if (totalWeight > 0)
					value = value/totalWeight;
			} else if (usePerseus){
				value = worstValue();
			}
			double reward = pomdp.getAvgReward(b, action);
			value = reward + value * pomdp.getGamma();
			System.out.println("backup reward for action " + reward + " " + action + "value " + value + " depth: " + (horizon - t) + " belief.timestep: " + b.getTimestep());
			if (bestValue == null || value > bestValue) {
				bestValue = value;
				bestPolicyMap = policyMap;
				bestAction = action;
			}
		}
			
		
		System.out.println("bestAction: " + bestAction + " " + bestValue + " depth: " + (horizon - t));
		
		FiniteStatePolicy newPolicy = new FiniteStatePolicy(bestAction, bestPolicyMap);
		System.out.println("singlebackupforbelief.time " + Timer.getElapsedStr());
		System.out.println("num dfs visited states " + addedAlphaKeys.size());
		return newPolicy;		
	}
	
	private void setAlphaVector(
			FiniteStatePolicy policy) {
		if (policy.getAlphaVector() == null) {
			policy.setAlphaVector(new AlphaVector());
		}
		Set<State> states = alphaKeys.keySet();
		for (State state : states) {
			evalPolicy(state, policy);
		}

		System.out.println("num dfs visited states " + addedAlphaKeys.size());
	}
	
	private void setAlphaVectors(
			Set<FiniteStatePolicy> policies,
			Set<FiniteStatePolicy> oldPolicies,
			int timestep) {
		System.out.println("Start alphavector calculations");
		
		Set<State> states = alphaKeys.keySet();
		
		for (FiniteStatePolicy policy : policies) {
			if (policy.getAlphaVector() == null)
				policy.setAlphaVector(new AlphaVector());
		}

		System.out.println("singleBackup.states.size " + states.size());
		System.out.println("oldPolicies.size " + oldPolicies.size());
		System.out.println("policies.size " + policies.size());
		long now = System.currentTimeMillis();
		int numStatesUpdated = 0;
		for (State state : states) {
			numStatesUpdated++;
			for (FiniteStatePolicy policy : policies) {
				evalPolicy(state, policy);
			}
		}

		System.out.println("backup per state took on avg " 
				+ Timer.niceTimeString((System.currentTimeMillis() - now)/numStatesUpdated));
	}
	
	public static OUPBVI makeOUPBVI(List modelFilePath, 
			String queryFile,
			int numParticles,
			int horizon,
			int numBeliefs) {
		query_parser file = new query_parser(queryFile);
		Collection linkStrings = Util.list();
		List<String> queryStrings = file.queries;
		System.out.println(queryStrings);
		Model model = new Model();
		Evidence evidence = new Evidence();
		LinkedList queries = new LinkedList();
		Main.simpleSetupFromFiles(model, evidence, queries,
				modelFilePath);
		Properties properties = new Properties();
		properties.setProperty("numParticles", "" + numParticles);
		properties.setProperty("useDecayedMCMC", "false");
		properties.setProperty("numMoves", "1");
		
		Util.setVerbose(false);
		return new OUPBVI(new OUPOMDPModel(model, properties, queries, queryStrings), properties, horizon, numBeliefs);
	}
	
	public static void main(String[] args) {
		System.out.println("Usage: modelFile queryFile numParticles horizon numBeliefs (seed)");
		DBLOGUtil.nsim = 1;
		List<String> modelFiles = new ArrayList<String>();
		modelFiles.add(args[0]);
		Timer.off = false;
		Timer.start();
		if (args.length > 7) {
			Util.initRandom(Long.parseLong(args[7]));
		} else {
			Util.initRandom(true);
		}
		
		if (args.length > 5) {
			 boolean debugOff = Boolean.parseBoolean(args[5]);
			 Timer.off = debugOff;
			 //OUPBVI.debugOn = !debugOff;
			 System.out.println("Turn timer off?" + debugOff);
		}
		
		OUPBVI oupbvi = makeOUPBVI(modelFiles, 
				args[1], 
				Integer.parseInt(args[2]), 
				Integer.parseInt(args[3]),
				Integer.parseInt(args[4]));
		
		if (args.length > 6) {
			 if (Boolean.parseBoolean(args[6])) {
				 oupbvi.setUsePerseus(true);
			 } else {
				 oupbvi.setUsePerseus(false);
			 }
		}
		
		if (args.length > 8)
			oupbvi.evaluate(args[8]);
		else
			oupbvi.run();
		Belief.printTimingStats();
		System.out.println("Total elapsed: " + Timer.getElapsedStr());
		Timer.print();
		
	}
	
	private void evaluate(FiniteStatePolicy p) {
		Timer.start("FINAL EVALUATION");
		FiniteStatePolicyEvaluator evaluator = new FiniteStatePolicyEvaluator(this.getPOMDP(), pomdp.getGamma());
		Belief b;

		for (int i = 0; i < 10; i++) {
			b = pomdp.generateInitialBelief(500);
			System.out.println(b);
			System.out.println("Iter: " + i);
			System.out.println("Evaluated: " + evaluator.eval(b, p, 100));
			System.out.println("Unhandled obs: " + evaluator.getMissingObs());
		}
		Timer.record("FINAL EVALUATION");
	}

	private void evaluate(String string) {
		DotToPolicy p = new DotToPolicy();
		p.createPolicy(string);
		FiniteStatePolicyEvaluator evaluator = new FiniteStatePolicyEvaluator(this.getPOMDP(), pomdp.getGamma());
		Belief b;

		for (int i = 0; i < 10; i++) {
			b = pomdp.generateInitialBelief();
			System.out.println("Iter: " + i);
			System.out.println("Evaluated: " + evaluator.eval(b, p, 100));
			System.out.println("Unhandled obs: " + p.unhandledObs);
			p.unhandledObs = 0;
		}
	}

	private void setUsePerseus(boolean usePerseus) {
		this.usePerseus = usePerseus;
		System.out.println("Setting usePerseus: " + usePerseus);
	}

	public OUPOMDPModel getPOMDP() {
		return pomdp;
	}

	public Properties getProperties() {
		return properties;
	}
}
