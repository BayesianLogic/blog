package blog.engine.pbvi;

import java.util.ArrayList;
import java.util.Collection;
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
import blog.engine.Particle;
import blog.engine.experiments.query_parser;
import blog.engine.onlinePF.PFEngine.PFEngineOnline;
import blog.engine.onlinePF.PFEngine.PFEngineSampled;
import blog.engine.onlinePF.evidenceGenerator.EvidenceQueryDecisionGeneratorOnline;
import blog.engine.onlinePF.inverseBucket.TimedParticle;
import blog.engine.onlinePF.inverseBucket.UBT;
import blog.model.ArgSpec;
import blog.model.BuiltInTypes;
import blog.model.DecisionEvidenceStatement;
import blog.model.DecisionFunction;
import blog.model.Evidence;
import blog.model.FuncAppTerm;
import blog.model.Function;
import blog.model.Model;
import blog.model.Query;
import blog.model.SkolemConstant;
import blog.model.Term;
import blog.model.TrueFormula;
import blog.model.Type;
import blog.world.AbstractPartialWorld;
import blog.world.PartialWorld;
import blog.bn.BasicVar;
import blog.bn.BayesNetVar;
import blog.bn.DerivedVar;
import blog.common.Util;

public class OUPBVI {
	private Model model;
	private List<Query> queries;
	private Properties properties;
	private int horizon;
	
	private static int numParticles = 1000;

	private TemporalQueriesInstantiator setQI;
	
	public OUPBVI(Model model, Properties properties, List<Query> queries, List<String> queryStrings, int horizon) {
		this.model = model;
		this.horizon = horizon;
		this.properties = properties;
		this.queries = queries;
		System.out.println("Queries " + queries);
		
		for (Type typ: (List<Type>) model.getObsTyp()){
			queryStrings.add("Number_"+typ+"(t)");
		}
		
		setQI = new TemporalQueriesInstantiator(model, EvidenceQueryDecisionGeneratorOnline.makeQueryTemplates(queryStrings));	
	}
	
	public Collection<Query> getQueries(int t) {
		Collection<Query> newQs = setQI.getQueries(t);
		for (Query q : newQs) {
			q.compile();
		}
		newQs.addAll(this.queries);
		return newQs;
	}
	
	public Set<FiniteStatePolicy> run() {
		Set<Belief> beliefs = new HashSet<Belief>();
		PFEngineSampled initBelief = new PFEngineSampled(model, properties);
		initBelief.answer(getQueries(0));
		initBelief.afterAnsweringQueries2();
		
		//beliefs.add(new Belief(initBelief, this));
		System.out.println(new Belief(initBelief, this));
		SampledPOMDP pomdp = new SampledPOMDP();
		beliefs.add(addToSampledPOMDP(new Belief(initBelief, this), pomdp));
		
		Set<Belief> newBeliefs = new HashSet<Belief>(); //beliefs;
		newBeliefs.addAll(beliefs);
		/*for (int i = 0; i < horizon - 1; i++) {
			newBeliefs = addToSampledPOMDP(beliefExpansion(newBeliefs), pomdp);
			System.out.println("run.expansion.iteration: " + i);
			System.out.println("run.expansion.newsize: " + newBeliefs.size());
			beliefs.addAll(newBeliefs);
		}*/
		
		for (int i = 0; i < 100; i++) {
			newBeliefs.addAll(beliefExpansionDepthFirst(
					new Belief(initBelief.copy(), this), pomdp));
			System.out.println("run.expansion.iteration: " + i);
			System.out.println("run.expansion.newsize: " + newBeliefs.size());
		}
		beliefs = newBeliefs;
		//SampledPOMDP pomdp = createSampledPOMDP(beliefs);
		//System.out.println("run.beliefs: " + beliefs);
		//System.exit(0);
/*
		Map<State, Map<Evidence, ActionPropagated>> stateActionToNextBeliefs = 
				new HashMap<State, Map<Evidence, ActionPropagated>>();
		Set<State> states = collectStatesUnique(beliefs);
		
		System.out.println("run.number_of_states: " + states.size());
		for (State state : states) {
			PartialWorld world = state.getWorld();
			Map<Evidence, ActionPropagated> actionToNextBeliefs = new HashMap<Evidence, ActionPropagated>();
			PFEngineSampled pf = new PFEngineSampled(model, properties, world, state.getTimestep());
			Belief b = new Belief(pf, this);
			Set<Evidence> actions = getActions(b);
			for (Evidence action : actions) {
				actionToNextBeliefs.put(action, b.beliefsAfterAction(action));
			}
			stateActionToNextBeliefs.put(new State((AbstractPartialWorld) world, b.getTimestep()), actionToNextBeliefs);
		}
		
		Set<State> moreStates = new HashSet<State>();
		for (State state : states) {
			Map<Evidence, ActionPropagated> actionToNextBeliefs = stateActionToNextBeliefs.get(state);
			for (Evidence a : actionToNextBeliefs.keySet()) {
				ActionPropagated ap = stateActionToNextBeliefs.get(state).get(a);
				for (Evidence obs : ap.getObservations()) {
					Belief next = ap.getNextBelief(obs);
					moreStates.addAll(next.getStates());
				}
			}
		}
		states.addAll(moreStates);
		
		for (State state : moreStates) {	
			Map<Evidence, ActionPropagated> actionToNextBeliefs = new HashMap<Evidence, ActionPropagated>();
			
			Belief b = getSingletonBelief(state);
			Set<Evidence> actions = getActions(b);
			for (Evidence action : actions) {
				actionToNextBeliefs.put(action, b.beliefsAfterAction(action));
			}
			PartialWorld world = state.getWorld();
			stateActionToNextBeliefs.put(new State((AbstractPartialWorld) world, b.getTimestep()), actionToNextBeliefs);
		}
*/
		Set<FiniteStatePolicy> policies = new HashSet<FiniteStatePolicy>();
		for (int t = horizon - 1; t >= 0; t--) {
			policies = singleBackup(policies, beliefs, pomdp, t);
			System.out.println("run policies " + policies.size() + " " + t);
		}
		System.out.println("run num beliefs " + beliefs.size());
		System.out.println("run num states " + pomdp.getStates().size());
		return policies;
	}

	private Belief getSingletonBelief(State state) {
		PFEngineSampled pf = new PFEngineSampled(model, properties, state.getWorld(), state.getTimestep());
		Belief b = new Belief(pf, this);
		return b;
	}
	
	private Set<FiniteStatePolicy> singleBackup(Set<FiniteStatePolicy> oldPolicies, 
			Set<Belief> beliefs, 
			Map<State, Map<Evidence, ActionPropagated>> stateActionToNextBeliefs,
			int t) {
		Set<FiniteStatePolicy> newPolicies = new HashSet<FiniteStatePolicy>();
		for (Belief b : beliefs) {
			if (b.getTimestep() != t) continue;
			newPolicies.add(singleBackupForBelief(oldPolicies, b, stateActionToNextBeliefs));
		}
		return newPolicies;
	}
	
	private FiniteStatePolicy singleBackupForBelief(
			Set<FiniteStatePolicy> oldPolicies,
			Belief b,
			Map<State, Map<Evidence, ActionPropagated>> stateActionToNextBeliefs) {
		
		Map<Evidence, FiniteStatePolicy> bestPolicyMap = new HashMap<Evidence, FiniteStatePolicy>();
		AlphaVector bestAlphaVector = null;
		Evidence bestAction = null;
		
		Set<Evidence> actions = getActions(b);
		System.out.println(b);
		System.out.println(actions);
		Double bestValue = null;
		for (Evidence action : actions) {
			Map<Evidence, FiniteStatePolicy> policyMap = new HashMap<Evidence, FiniteStatePolicy>();
			ActionPropagated nexts = b.beliefsAfterAction(action);
			for (Evidence obs : nexts.getObservations()) {
				Belief next = nexts.getNextBelief(obs);
				Double bestContingentValue = Double.NEGATIVE_INFINITY;
				for (FiniteStatePolicy p : oldPolicies) {
					if (!p.isApplicable(next)) continue;
					Double value = p.getAlphaVector().getValue(next);
					if (value > bestContingentValue) {
						bestContingentValue = value;
						policyMap.put(obs, p);
					}
				}	
			}
			
			//create alpha vector for policy
			AlphaVector alpha = new AlphaVector();
			Set<State> states = stateActionToNextBeliefs.keySet();
			
			System.out.println("singleBackup.states.size " + states.size());
			double totalCount = 0;
			for (State state : states) {
				if (state.getTimestep() != b.getTimestep()) continue;
				double count = 0;
				double value = 0;
				ActionPropagated ap = stateActionToNextBeliefs.get(state).get(action);
				for (Evidence obs : ap.getObservations()) {
					Belief next = ap.getNextBelief(obs);
					double obsWeight = ap.getObservationCount(obs);
					for (State nextState : next.getStates().keySet()) {
						int stateWeight = next.getStates().get(nextState);
						double weight = obsWeight * stateWeight;
						Function rewardFunc = (Function) model.getRandomFunc("reward", 1);
						Object timestep = Type.getType("Timestep").getGuaranteedObject(state.getTimestep());
						if (timestep == null) continue;
						Number reward = (Number) rewardFunc.getValueSingleArg(timestep, nextState.getWorld());
						//System.out.println(timestep + " " + action + " " + reward);//  + " " + nextState.getWorld());
						FiniteStatePolicy contingentPolicy = policyMap.get(obs);
						if (contingentPolicy == null && oldPolicies.size() > 0) {
							contingentPolicy = (FiniteStatePolicy) Util.getFirst(oldPolicies); //TODO: a hack for now
							policyMap.put(obs, contingentPolicy);
						}
						
						if (contingentPolicy != null)
							value += weight * (reward.doubleValue() + contingentPolicy.getAlphaVector().getValue(nextState));
						else
							value += weight * (alpha.getValue(state) + reward.doubleValue());
						count += weight;
					}
				}
				//System.out.println(count);
				alpha.setValue(state, value/count);
				totalCount += count;
			}
			if (totalCount == 0) continue;
			
			Double value = alpha.getValue(b);
			if (value == null) continue;
			if (bestValue == null || value > bestValue) {
				bestValue = value;
				bestAlphaVector = alpha;
				bestAction = action;
				bestPolicyMap = policyMap;
			}
			System.out.println("action: " + action + " " + value);
		}

		System.out.println("bestAction: " + bestAction + " " + bestValue);
		FiniteStatePolicy newPolicy = new FiniteStatePolicy(bestAction, bestPolicyMap);
		System.out.println(bestPolicyMap);
		newPolicy.setAlphaVector(bestAlphaVector);
		return newPolicy;		
	}

	private Set<Evidence> collectObservations(
			Belief b,
			Map<State, Map<Evidence, Map<Evidence, Belief>>> stateActionToNextBeliefs,
			Evidence action) {
		System.out.println("collectObs.action: " + action);
		Set<Evidence> evidences = new HashSet<Evidence>();
		for (Particle p : b.getParticleFilter().particles) {
			State w = new State((AbstractPartialWorld) p.curWorld, b.getTimestep());
			//System.out.println("collectObs.stateActionToNextBeliefs " + stateActionToNextBeliefs.keySet());
			//System.out.println("collectObs " + stateActionToNextBeliefs.get(w) + " " + w);
			//if (stateActionToNextBeliefs.get(w) == null) continue;
			//System.out.println(stateActionToNextBeliefs.get(w).containsKey(action));
			Map<Evidence, Belief> nextBeliefs = stateActionToNextBeliefs.get(w).get(action);
			//System.out.println(nextBeliefs);
			if (nextBeliefs == null) continue;
			System.out.println("collectionObs " + nextBeliefs.keySet());
			evidences.addAll(nextBeliefs.keySet());
		}
		return evidences;
	}
	
	private Set<State> collectStatesUnique(Belief b) {
		Set<State> states = new HashSet<State>();
		for (TimedParticle p : b.getParticleFilter().particles) {
			states.add(new State((AbstractPartialWorld) p.curWorld, p.getTimestep()));			
		}
		System.out.println("collectStates: num states for b: " + states.size());
		return states;
	}

	private Set<State> collectStatesUnique(Set<Belief> beliefs) {
		Set<State> states = new HashSet<State>();
		for (Belief b : beliefs) {
			states.addAll(collectStatesUnique(b));
		}
		for (Belief b : beliefs) {
			states.addAll(collectStatesUnique(b));
		}
		return states;
	}
	
	private List<State> collectStates(Belief b) {
		List<State> states = new ArrayList<State>();
		for (TimedParticle p : b.getParticleFilter().particles) {
			states.add(new State((AbstractPartialWorld) p.curWorld, p.getTimestep()));			
		}
		System.out.println("collectStates: num states for b: " + states.size());
		return states;
	}

	private List<State> collectStates(Set<Belief> beliefs) {
		List<State> states = new ArrayList<State>();
		for (Belief b : beliefs) {
			states.addAll(collectStates(b));
		}
		return states;
	}

	private Set<Evidence> getActions(Belief b) {
		State s = (State) Util.getFirst(b.getStates().keySet());
		PartialWorld w = s.getWorld();
		int timestep = b.getTimestep();
		Map<BayesNetVar, BayesNetVar> observableMap =((AbstractPartialWorld) w).getObservableMap();
	
		Map<Type, Set<BayesNetVar>> observedVarsByType = partitionVarsByType(observableMap, w);
		List<DecisionFunction> decisionFunctions = model.getDecisionFunctions();
		//System.out.println(observedVarsByType);
		/*Collection<Function> funcs = model.getFunctions();
		for (Function f : funcs) {
			if (f instanceof SkolemConstant) {
				Type type = f.getRetType();
				if (!observedVarsByType.containsKey(type)) {
					observedVarsByType.put(type, new HashSet<BayesNetVar>());
				}
				observedVarsByType.get(type).add(((SkolemConstant) f).rv());
			}
		}*/
		
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
				//System.out.println(action);
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


	private Set<Belief> beliefExpansion(Set<Belief> beliefs) {
		Set<Belief> newBeliefs = new HashSet<Belief>();
		for (Belief b : beliefs) {
			Set<Evidence> actions = getActions(b);
			for (Evidence action : actions) {
				ActionPropagated nexts = b.beliefsAfterAction(action);
				Belief next = nexts.getNextBelief(pickRandomObs(nexts.getObservations()));
				newBeliefs.add(next);
			}
			b.setParticleFilter(null);
		}
		return newBeliefs;
	}
	
	private Set<Belief> beliefExpansionDepthFirst(Belief b, SampledPOMDP pomdp) {
		Set<Belief> newBeliefs = new HashSet<Belief>();
		Belief next;
		for (int i = 0; i < horizon; i++) {
			Set<Evidence> actions = getActions(b);
			Evidence action = pickRandomObs(actions);
			ActionPropagated nexts = b.beliefsAfterAction(action);
			next = nexts.getNextBelief(pickRandomObs(nexts.getObservations()));
			newBeliefs.add(next);
			b.setParticleFilter(null);
			b = next;
			addToSampledPOMDP(b, pomdp);
		}
		b.setParticleFilter(null);
		return newBeliefs;
	}
	
	private Evidence pickRandomObs(Set<Evidence> evidences) {
		int rand = (int)(Math.random() * evidences.size());
		int i = 0;
		for (Evidence e : evidences) {
			if (i == rand) return e;
			i++;
		}
		return null;
	}
	/*
	private Belief downsampled(Belief b) {
		List<State> result = new ArrayList<State>();
		for (int i = 0; i < numParticles; i++) {
			int randIndex = (int) (Math.random() * b.size());
			result.add(b.get(randIndex));
		}
		return result;
	}
	
	private Set<Belief> beliefExpansion(Set<Belief> beliefs, SampledPOMDP pomdp) {
		Set<Belief> newBeliefs = new HashSet<Belief>();
		int i = 0;
		for (Belief b : beliefs) {
			Set<Evidence> actions = pomdp.getActions(b);
			for (Evidence action : actions) {
				Map<Evidence, Integer> observations = pomdp.getObservations(b, action);
				if (observations.keySet().isEmpty()) {
					System.err.println("beliefExpansion error: no obs " + b);
					System.err.println("action " + action);
					continue;
				}
				//Evidence o = (Evidence) Util.getFirst(observations.keySet());
				Evidence o = pickRandomObs(observations.keySet());
				//for (Evidence o : observations.keySet()) { //TODO select all beliefs now
					Belief next = pomdp.getNextBelief(b, action, o);
					if (next.getStates().keySet().size() == 0) {
						System.err.println("beliefExpansion error " + b);
						System.err.println("action " + action);
						System.err.println("obs " + o);
					} else {
						next = downsampled(next);
						newBeliefs.add(next);
					}
				//}
			}
			System.out.println("beliefExpansion beliefNumber" + i);
			i++;
		}
		return newBeliefs;
	}
	*/
	
	private Belief addToSampledPOMDP(Belief belief, SampledPOMDP pomdp) {
		Stack<State> toExpand = new Stack<State>();
		Set<State> expanded = new HashSet<State>();
		Set<State> initStates = new HashSet<State>();
		initStates.addAll(belief.getStates().keySet());	
		initStates.removeAll(pomdp.getStates());
		toExpand.addAll(initStates);
		
		while (!toExpand.isEmpty()) {
			State s = toExpand.pop();
			if (expanded.contains(s)) continue;
			expanded.add(s);
			pomdp.addState(s);
			Belief b = getSingletonBelief(s);
			Set<Evidence> actions = getActions(b);
			pomdp.addStateActions(s, actions);
			for (Evidence a : actions) {
				ActionPropagated ap = b.beliefsAfterAction(a);
				Set<Evidence> observations = ap.getObservations();
				for (Evidence o : observations) {
					Belief next = ap.getNextBelief(o);
					Set<State> nextStates = new HashSet<State>(next.getStates().keySet());
					pomdp.addStates(nextStates);
					int count = (int) ap.getObservationCount(o);
					pomdp.addObsWeights(s, a, o, count);
					pomdp.addNextBelief(s, a, o, next);
					if (next.getTimestep() < horizon) {
						nextStates.removeAll(expanded);
						toExpand.addAll(nextStates);
					}
				}
			}
			System.out.println("# states left to expand " + toExpand.size());
			System.out.println("# states expanded " + expanded.size());
		}
		
		/*for (State s : expanded) {
			System.out.println(s.getTimestep() + " " + s.getWorld());
		}*/
		System.out.println("# states " + pomdp.getStates().size());
		System.out.println("# states expanded " + expanded.size());
		
		Belief newBelief = new Belief();
		for (State s : belief.getStates().keySet()) {
			newBelief.addState(pomdp.getState(s), belief.getStates().get(s));
		}
		newBelief.setParticleFilter(belief.getParticleFilter());
		newBelief.setPBVI(this);
		
		return newBelief;
	}
	
	private Set<FiniteStatePolicy> singleBackup(Set<FiniteStatePolicy> oldPolicies, 
			Set<Belief> beliefs, 
			SampledPOMDP pomdp,
			int t) {
		Set<FiniteStatePolicy> newPolicies = new HashSet<FiniteStatePolicy>();
		for (Belief b : beliefs) {
			if (b.getTimestep() != t) continue;
			newPolicies.add(singleBackupForBelief(oldPolicies, b, pomdp));
			System.out.println("SingleBackup numbeliefs: " + beliefs.size());
		}
		// no longer need old alpha vectors
		for (FiniteStatePolicy p : oldPolicies) {
			p.setAlphaVector(null);
		}
		
		return newPolicies;
	}
	
	private FiniteStatePolicy singleBackupForBelief(
			Set<FiniteStatePolicy> oldPolicies,
			Belief b,
			SampledPOMDP pomdp) {
		Map<Evidence, FiniteStatePolicy> bestPolicyMap = new HashMap<Evidence, FiniteStatePolicy>();
		AlphaVector bestAlphaVector = null;
		Evidence bestAction = null;
		
		Set<Evidence> actions = pomdp.getActions(b);
		System.out.println("single backup actions: " + actions);
		System.out.println("single backup belief: " + b);
		System.out.println("single backup num oldpolicies: " + oldPolicies.size());
		Double bestValue = null;
		for (Evidence action : actions) {
			Map<Evidence, FiniteStatePolicy> policyMap = new HashMap<Evidence, FiniteStatePolicy>();
			Map<Evidence, Integer> observations = pomdp.getObservations(b, action);
			for (Evidence obs : observations.keySet()) {
				Belief next = pomdp.getNextBelief(b, action, obs);
				Double bestContingentValue = Double.NEGATIVE_INFINITY;
				for (FiniteStatePolicy p : oldPolicies) {
					Double value = p.getAlphaVector().getValue(next);
					if (value > bestContingentValue) {
						bestContingentValue = value;
						policyMap.put(obs, p);
					}
				}	
			}
			
			//create alpha vector for policy
			AlphaVector alpha = new AlphaVector();
			Set<State> states = pomdp.getStates();
			
			System.out.println("singleBackup.states.size " + states.size());
			double totalCount = 0;
			for (State state : states) {
				if (state.getTimestep() != b.getTimestep()) continue;
				double count = 0;
				double value = 0;
				Map<Evidence, Integer> obsWeights = pomdp.getObservations(state, action);
				if (obsWeights == null) {
					/*System.err.println("singleBackup error(?):");
					System.err.println("state " + state.getWorld());
					System.err.println("skolem constants " + state.getWorld().getSkolemConstants());
					System.err.println("observables " + state.getWorld().getObservableMap().keySet());
					System.err.println("action " + action);*/
					continue;
				}
				for (Evidence obs : obsWeights.keySet()) {
					Belief next = pomdp.getNextBelief(state, action, obs);
					if (next == null) {
						System.err.println("single backup missing " + obs);
						System.err.println("single backup for state " + state.getWorld());
						continue;
					}
					int obsWeight = obsWeights.get(obs);
					for (State nextState : next.getStates().keySet()) {
						int stateWeight = next.getStates().get(nextState);
						int weight = stateWeight * obsWeight;
						Function rewardFunc = (Function) model.getRandomFunc("reward", 1);
						Object timestep = Type.getType("Timestep").getGuaranteedObject(state.getTimestep());
						if (timestep == null) continue;
						Number reward = (Number) rewardFunc.getValueSingleArg(timestep, nextState.getWorld());
						//System.out.println(timestep + " " + action + " " + reward);//  + " " + nextState.getWorld());
						FiniteStatePolicy contingentPolicy = policyMap.get(obs);
						if (contingentPolicy == null && oldPolicies.size() > 0) {
							contingentPolicy = (FiniteStatePolicy) Util.getFirst(oldPolicies); //TODO: a hack for now
							policyMap.put(obs, contingentPolicy);
							System.err.println("hack in singleBackup for " + obs);
						}
						
						if (contingentPolicy != null)
							value += weight * (reward.doubleValue() + contingentPolicy.getAlphaVector().getValue(nextState));
						else {
							if (oldPolicies.size() != 0)
								value += weight * (alpha.getValue(state) + reward.doubleValue());
							else
								value += weight * reward.doubleValue();
						}
						count += weight;
					}
				}
				//System.out.println(count);
				alpha.setValue(state, value/count);
				totalCount += count;
			}
			if (totalCount == 0) continue;
			
			Double value = alpha.getValue(b);
			if (value == null) continue;
			if (bestValue == null || value > bestValue) {
				bestValue = value;
				bestAlphaVector = alpha;
				bestAction = action;
				bestPolicyMap = policyMap;
			}
			System.out.println("action: " + action + " " + value);
		}

		System.out.println("bestAction: " + bestAction + " " + bestValue);
		FiniteStatePolicy newPolicy = new FiniteStatePolicy(bestAction, bestPolicyMap);
		System.out.println(bestPolicyMap);
		newPolicy.setAlphaVector(bestAlphaVector);
		return newPolicy;		
	}
	
	public static OUPBVI makeOUPBVI(List modelFilePath, String queryFile) {
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
		Util.initRandom(true);
		Util.setVerbose(false);
		return new OUPBVI(model, properties, queries, queryStrings, 5);
	}
	
	public static void main(String[] args) {
		List<String> modelFiles = new ArrayList<String>();
		modelFiles.add(args[0]);
		OUPBVI oupbvi = makeOUPBVI(modelFiles, args[1]);
		Set<FiniteStatePolicy> policies = oupbvi.run();
		for (FiniteStatePolicy p : policies) {
			System.out.println(p.toDotString("p0"));
		}
		
	}
}
