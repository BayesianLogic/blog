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
import java.util.Stack;
import blog.Main;
import blog.TemporalQueriesInstantiator;
import blog.engine.experiments.query_parser;
import blog.engine.onlinePF.ObservabilitySignature;
import blog.engine.onlinePF.PFEngine.PFEngineSampled;
import blog.engine.onlinePF.evidenceGenerator.EvidenceQueryDecisionGeneratorOnline;
import blog.engine.onlinePF.inverseBucket.TimedParticle;
import blog.model.ArgSpec;
import blog.model.BuiltInTypes;
import blog.model.DecisionEvidenceStatement;
import blog.model.DecisionFunction;
import blog.model.Evidence;
import blog.model.FuncAppTerm;
import blog.model.Function;
import blog.model.Model;
import blog.model.Query;
import blog.model.Term;
import blog.model.TrueFormula;
import blog.model.Type;
import blog.world.AbstractPartialWorld;
import blog.world.PartialWorld;
import blog.bn.BasicVar;
import blog.bn.BayesNetVar;
import blog.common.Util;

public class OUPBVI {
	private Model model;
	private List<Query> queries;
	private Properties properties;
	private int horizon;
	
	// should be gotten from model
	private double gamma = .9;
	private double minReward = -10; 
	private double maxReward = 100;
	
	private double epsilon = 0.05;
	
	private int numBeliefs;
	private int numParticles;
	private boolean usePerseus = true;
	private static boolean debugOn = false;
	
	private Map<State, State> alphaKeys;
	private Map<State, State> addedAlphaKeys;
	private boolean useVisitedStates = true;
	private int maxNumAlphaKeys; 
	
	private Map<Belief, Integer> beliefIDs = new HashMap<Belief, Integer>();
	private int beliefID = 0;
	
	private TemporalQueriesInstantiator setQI;
	
	public OUPBVI(Model model, Properties properties, List<Query> queries, List<String> queryStrings, int horizon, int numBeliefs) {
		this.model = model;
		this.horizon = horizon;
		this.properties = properties;
		this.queries = queries;
		this.numBeliefs = numBeliefs;
		numParticles = Integer.parseInt((String) properties.get("numParticles"));
		this.maxNumAlphaKeys = numParticles * numBeliefs;
		alphaKeys = new HashMap<State, State>();
		System.out.println("Queries " + queries);
		
		for (Type typ: (List<Type>) model.getObsTyp()){
			queryStrings.add("Number_"+typ+"(t)");
		}
		
		setQI = new TemporalQueriesInstantiator(model, EvidenceQueryDecisionGeneratorOnline.makeQueryTemplates(queryStrings));	
	}
	
	private Map<Integer, Collection<Query>> compiledQueries = new HashMap<Integer, Collection<Query>>();
	
	public Collection<Query> getQueries(int t) {
		if (!compiledQueries.containsKey(t)) {
			Collection<Query> newQs = setQI.getQueries(t);
			for (Query q : newQs) {
				q.compile();
			}
			newQs.addAll(this.queries);
			compiledQueries.put(t, newQs);
		}
		
		return compiledQueries.get(t);
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
		PFEngineSampled initPF = new PFEngineSampled(model, properties);
		initPF.answer(getQueries(0));
		initPF.afterAnsweringQueries2();

		SampledPOMDP pomdp = new SampledPOMDP(this);
		Belief initBelief = addToSampledPOMDP(new Belief(initPF, this), pomdp);
		System.out.println(initBelief);
		beliefs.add(initBelief);
		beliefIDs.put(initBelief, beliefID);
		beliefID++;
		
		Set<Belief> newBeliefs = new HashSet<Belief>(); //beliefs;
		newBeliefs.addAll(beliefs);
		
		int numIter = 0;
		while (newBeliefs.size() < numBeliefs) {
			newBeliefs = maxNormBeliefExpansion(newBeliefs, pomdp);
			System.out.println("run.expansion.iteration: " + numIter);
			System.out.println("run.expansion.newsize: " + newBeliefs.size());
			numIter++;
		}
		beliefs = newBeliefs;
		
		for (Belief b : beliefs) {
			System.out.println("Belief: " + beliefIDs.get(b) + " " + b);
		}
		
		System.out.println("Alpha keys: ");
		for (State s : pomdp.getStates()) {
			alphaKeys.put(s, s);
			System.out.println(s);
		}
		System.out.println("run.expansion.beliefsize: " + beliefs.size());
		
		addedAlphaKeys = new HashMap<State, State>();
		Set<FiniteStatePolicy> policies = new HashSet<FiniteStatePolicy>();
		for (int t = horizon - 1; t >= 0; t--) {
			Set<FiniteStatePolicy> newPolicies; 
			if (usePerseus)
				newPolicies = singleBackupPerseus(policies, beliefs, pomdp, t);
			else 
				newPolicies = singleBackup(policies, beliefs, pomdp, t);
			setAlphaVectors(newPolicies, policies, t, pomdp);
			policies = newPolicies;
			
			System.out.println("run policies " + policies.size() + " " + t);
			int i = 0;
			for (FiniteStatePolicy p : policies) {
				System.out.println(p.toDotString("p" + "_t" + t + "_i" + i));
				i++;
			}
			System.out.println("Number of OS: " + ObservabilitySignature.OStoIndex.size());
		
			double maxDelta = 0;
			for (Belief b : beliefs) {
				Pair<FiniteStatePolicy, Double> bestPolicyValue = bestPolicyValue(policies, b);
				Double oldValue = prevBestVals.get(b);
				if (oldValue == null) {
					oldValue = Double.NEGATIVE_INFINITY;
				}
				double delta = Math.abs(bestPolicyValue.y - oldValue);
				if (delta > maxDelta) {
					maxDelta = delta;
				}
				prevBestVals.put(b, bestPolicyValue.y);
				prevBestPolicies.put(b, bestPolicyValue.x);
			}
			if (maxDelta < epsilon)
				break;
		}
		
		Pair<FiniteStatePolicy, Double> bestPolicyValue = bestPolicyValue(policies, initBelief);
		
		System.out.println("Best policy for initial belief: " + bestPolicyValue.x.toDotString("p0"));
		System.out.println("Best value: " + bestPolicyValue.y);
		System.out.println("Best value previously computed: " + prevBestVals.get(initBelief));
		System.out.println("Init belief: " + initBelief);
		System.out.println();
		System.out.println("run num beliefs " + beliefs.size());
		System.out.println("run num states " + pomdp.getStates().size());
		System.out.println("Eval Policy Times " + evalPolicyTimes);
		System.out.println("Eval Policy Counts " + evalPolicyCounts);
		System.out.println("Eval State Count " + evalStateCount);
		
		/*
		System.out.println("Evaluating best policy");
		FiniteStatePolicyEvaluator evaluator = new FiniteStatePolicyEvaluator(this, gamma);
		for (State state : initBelief.getStates()) {
			evaluator.eval(state, bestPolicy, horizon, 100);
		}*/
		return policies;
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
		return Belief.getSingletonBelief(state, numParticles, this);
	}

	
	private Set<Evidence> getActions(Belief b) {
		State s = (State) Util.getFirst(b.getStates());
		PartialWorld w = s.getWorld();
		int timestep = b.getTimestep();
		Map<BayesNetVar, BayesNetVar> observableMap = ((AbstractPartialWorld) w).getObservableMap();
	
		Map<Type, Set<BayesNetVar>> observedVarsByType = partitionVarsByType(observableMap, w);
		List<DecisionFunction> decisionFunctions = model.getDecisionFunctions();

		Set<Evidence> actions = new HashSet<Evidence>();
		for (DecisionFunction f : decisionFunctions) {
			Set<List<Term>> argLists = enumArgListsForFunc(f, observedVarsByType);
			for (List<Term> argList : argLists) {
				Evidence action = new Evidence();
				List<ArgSpec> argTerms = new ArrayList<ArgSpec>();
				for (Term term : argList) {
					argTerms.add(term);
				}
				argTerms.add(
						BuiltInTypes.TIMESTEP.getCanonicalTerm(
								BuiltInTypes.TIMESTEP.getGuaranteedObject(timestep)));
				FuncAppTerm left = new FuncAppTerm(f, argTerms);
				DecisionEvidenceStatement decisionStatement = new DecisionEvidenceStatement(left, TrueFormula.TRUE);
				action.addDecisionEvidence(decisionStatement);
				action.compile();
				actions.add(action);
			}
			
		}
		return actions;
		
	}
	
	private Set<List<Term>> enumArgListsForFunc(DecisionFunction f,
			Map<Type, Set<BayesNetVar>> observedVarsByType) {
		return enumArgListsForFunc(f, observedVarsByType, 0);
		
	}

	private Set<List<Term>> enumArgListsForFunc(DecisionFunction f,
			Map<Type, Set<BayesNetVar>> observedVarsByType,
			int argNum) {
		Type[] argTypes = f.getArgTypes();
		Set<List<Term>> result = new HashSet<List<Term>>();
		if (argNum == argTypes.length) {
			result.add(new ArrayList<Term>());
			return result;
		}
		Set<List<Term>> restOfArgs = enumArgListsForFunc(f, observedVarsByType, argNum + 1);
		Type argType = argTypes[argNum];
		Set<Term> terms = new HashSet<Term>();
		Set<BayesNetVar> observedVars = observedVarsByType.get(argType);
		if (observedVarsByType.get(argType) != null) {
			for (BayesNetVar v : observedVars) {
				terms.add(((BasicVar) v).getCanonicalTerm());
			}
		}
		Collection guaranteed = argType.getGuaranteedObjects();
		if (guaranteed != null) {
			for (Object g : guaranteed) {
				terms.add(argType.getCanonicalTerm(g));
			}
		}
		if (argType.equals(Type.getType("Timestep"))) {
			return restOfArgs;
		} 
		for (Term term : terms) {
			for (List<Term> rest : restOfArgs) {
				List<Term> newList = new ArrayList<Term>();
				newList.add(term);
				newList.addAll(rest);
				result.add(newList);
			}
		}
		return result;	
	}

	private Map<Type, Set<BayesNetVar>> partitionVarsByType(
			Map<BayesNetVar, BayesNetVar> observabilityMap, PartialWorld w) {
		Map<Type, Set<BayesNetVar>> result = new HashMap<Type, Set<BayesNetVar>>();
		for (BayesNetVar var : observabilityMap.keySet()) {
			Type type = ((BasicVar) var).getType();
			if (!result.containsKey(type))
				result.put(type, new HashSet<BayesNetVar>());
			result.get(type).add(var);
		}
		return result;
	}


	private Set<Belief> maxNormBeliefExpansion(Set<Belief> beliefs, SampledPOMDP pomdp) {
		Set<Belief> newBeliefs = new HashSet<Belief>(beliefs);
		for (Belief belief : beliefs) {
			if (belief.getTimestep() == horizon) continue;
			if (newBeliefs.size() >= numBeliefs) break;
			Set<Evidence> actions = pomdp.getActions(belief);
			Belief bestBelief = null;
			int maxDiff = 0;
			for (Evidence action : actions) {
				int minDiff = Integer.MAX_VALUE;
				Belief next = belief.sampleNextBelief(action);
				if (next.ended()) continue;
				if (bestBelief == null) bestBelief = next;
				for (Belief other : newBeliefs) {
					if (other.getTimestep() != next.getTimestep()) continue;
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
			addToSampledPOMDP(bestBelief, pomdp);
			System.out.println("max diff: " + maxDiff);
		}
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
		
		/*if (p.getAlphaVector().getSize() == alphaKeys.size()) {
			System.out.println("alpha vector missing states in belief");
			System.out.println(b);
			System.out.println(p.getAlphaVector());
			//System.exit(0);
		}*/
		
		Function valueFunc = (Function) model.getRandomFunc("value", 1);
		v = 0D;
		
		Double initialValue = 0D;
		int startingTimestep = b.getTimestep();
		Object initialTime = Type.getType("Timestep").getGuaranteedObject(startingTimestep);
		for (TimedParticle particle : b.getParticleFilter().particles) {
			Number value = (Number) valueFunc.getValueSingleArg(initialTime, particle.curWorld);
			initialValue += value.doubleValue();
		}
		initialValue /= b.getParticleFilter().particles.size();
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
				Evidence nextAction = curPolicy.getAction();
				curBelief = curBelief.sampleNextBelief(nextAction);		
				Evidence nextObs = curBelief.getLatestEvidence();
				FiniteStatePolicy nextPolicy = curPolicy.getNextPolicy(nextObs);
				if (nextPolicy == null && !curBelief.ended()) { 
					nextPolicy = curPolicy.getApplicableNextPolicy(nextObs, curBelief);
					if (nextPolicy != null) {
						curPolicy.setNextPolicy(nextObs, nextPolicy);
						curPolicy.addObsNote(nextObs, "random applicable");
					}
				}
				curPolicy = nextPolicy;
				
				curValue += discount * curBelief.getLatestReward();
				discount = discount * gamma;
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
		return minReward/(1 - gamma);
	}
	
	private Belief addToSampledPOMDP(Belief belief, SampledPOMDP pomdp) {
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
			Set<Evidence> actions = getActions(b);
			
			pomdp.addStateActions(s, actions);
	
			for (Evidence a : actions) {
				pomdp.addReward(s, a, b.getReward(a));
			}
			System.out.println("# states left to expand " + toExpand.size());
			System.out.println("# states expanded " + expanded.size());
		}
		System.out.println("# states " + pomdp.getStates().size());
		System.out.println("# states expanded " + expanded.size());
		
		Belief newBelief = new Belief(this);
		for (State s : belief.getStates()) {
			newBelief.addState(pomdp.getState(s), belief.getCount(s));
		}
		return newBelief;
	}
	
	private Map<Belief, Double> prevBestVals = new HashMap<Belief, Double>();
	private Map<Belief, FiniteStatePolicy> prevBestPolicies = 
			new HashMap<Belief, FiniteStatePolicy>();
	
	private Set<FiniteStatePolicy> singleBackup(Set<FiniteStatePolicy> oldPolicies, 
			Set<Belief> beliefs, 
			SampledPOMDP pomdp,
			int t) {
		Set<FiniteStatePolicy> newPolicies = new HashSet<FiniteStatePolicy>();
		for (Belief b : beliefs) {
			newPolicies = createMergedPolicySet(newPolicies, singleBackupForBelief(oldPolicies, b, pomdp, t));
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
			SampledPOMDP pomdp,
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

			FiniteStatePolicy newPolicy = singleBackupForBelief(oldPolicies, b, pomdp, t);
			setAlphaVector(newPolicy, pomdp);
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
			SampledPOMDP pomdp,
			int t) {
		Map<Evidence, FiniteStatePolicy> bestPolicyMap = new HashMap<Evidence, FiniteStatePolicy>();
		Evidence bestAction = null;
		
		Set<Evidence> actions = pomdp.getActions(b);
		System.out.println("single backup actions: " + actions);
		System.out.println("single backup num oldpolicies: " + oldPolicies.size());
		Double bestValue = null;
		for (Evidence action : actions) {
			double value = 0;
			double totalWeight = 0;
			boolean ended = false;
			Map<Evidence, FiniteStatePolicy> policyMap = new HashMap<Evidence, FiniteStatePolicy>();
			if (t < horizon - 1) {
				ActionPropagated ap = b.beliefsAfterAction(action);
				for (Evidence obs : ap.getObservations()) {
					Belief next = ap.getNextBelief(obs, this);
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
			value = reward + value * gamma;
			System.out.println("backup reward for action " + reward + " " + action + "value " + value + " timestep: " + t);
			if (bestValue == null || value > bestValue) {
				bestValue = value;
				bestPolicyMap = policyMap;
				bestAction = action;
			}
		}
			
		
		System.out.println("bestAction: " + bestAction + " " + bestValue + " timestep: " + t);
		FiniteStatePolicy newPolicy = new FiniteStatePolicy(bestAction, bestPolicyMap);
		System.out.println("singlebackupforbelief.time " + Timer.getElapsedStr());
		System.out.println("num dfs visited states " + addedAlphaKeys.size());
		return newPolicy;		
	}
	
	private void setAlphaVector(
			FiniteStatePolicy policy,
			SampledPOMDP pomdp) {
		if (policy.getAlphaVector() == null) {
			policy.setAlphaVector(new AlphaVector());
		}
		Set<State> states = pomdp.getStates();
		for (State state : states) {
			evalPolicy(state, policy);
		}

		System.out.println("num dfs visited states " + addedAlphaKeys.size());
	}
	
	private void setAlphaVectors(
			Set<FiniteStatePolicy> policies,
			Set<FiniteStatePolicy> oldPolicies,
			int timestep,
			SampledPOMDP pomdp) {
		System.out.println("Start alphavector calculations");
		
		Set<State> states = pomdp.getStates();
		
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
			System.out.println("Computing alpha values for state " + state);
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
		return new OUPBVI(model, properties, queries, queryStrings, horizon, numBeliefs);
	}
	
	public static void main(String[] args) {
		System.out.println("Usage: modelFile queryFile numParticles horizon numBeliefs (seed)");
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
			 OUPBVI.debugOn = !debugOff;
			 System.out.println("Turn debug off?" + debugOff);
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
		
		oupbvi.run();

		Belief.printTimingStats();
		System.out.println("Total elapsed: " + Timer.getElapsedStr());
		Timer.print();
		
	}

	private void setUsePerseus(boolean usePerseus) {
		this.usePerseus = usePerseus;
		System.out.println("Setting usePerseus: " + usePerseus);
	}

	public Model getModel() {
		return model;
	}

	public Properties getProperties() {
		return properties;
	}
}
