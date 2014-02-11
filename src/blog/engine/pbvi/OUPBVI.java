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

import blog.Main;
import blog.TemporalQueriesInstantiator;
import blog.engine.Particle;
import blog.engine.experiments.query_parser;
import blog.engine.onlinePF.ObservabilitySignature;
import blog.engine.onlinePF.ObservableRandomFunction;
import blog.engine.onlinePF.PFEngine.PFEngineOnline;
import blog.engine.onlinePF.PFEngine.PFEnginePartitioned;
import blog.engine.onlinePF.PFEngine.PFEngineSampled;
import blog.engine.onlinePF.absyn.PolicyModel;
import blog.engine.onlinePF.evidenceGenerator.EvidenceQueryDecisionGeneratorOnline;
import blog.engine.onlinePF.inverseBucket.TimedParticle;
import blog.engine.onlinePF.runner.PFRunnerSampled;
import blog.model.ArgSpec;
import blog.model.BuiltInTypes;
import blog.model.DecisionEvidenceStatement;
import blog.model.DecisionFunction;
import blog.model.Evidence;
import blog.model.FuncAppTerm;
import blog.model.Function;
import blog.model.Model;
import blog.model.Query;
import blog.model.RandomFunction;
import blog.model.Term;
import blog.model.TrueFormula;
import blog.model.Type;
import blog.world.AbstractPartialWorld;
import blog.world.PartialWorld;
import blog.absyn.DecisionEvidence;
import blog.bn.BasicVar;
import blog.bn.BayesNetVar;
import blog.bn.DerivedVar;
import blog.common.Util;

public class OUPBVI {
	private Model model;
	private List<Query> queries;
	private Properties properties;
	private int horizon;

	private TemporalQueriesInstantiator setQI;
	
	public OUPBVI(Model model, Properties properties, List<Query> queries, int horizon) {
		this.model = model;
		this.horizon = horizon;
		this.properties = properties;
		this.queries = queries;
		
		List<String> queryStrings = new ArrayList<String>();
		for (Type typ: (List<Type>) model.getObsTyp()){
			queryStrings.add("Number_"+typ+"(t)");
		}
		for (RandomFunction orf: (List<RandomFunction>) model.getObsFun()){
			queryStrings.add(((ObservableRandomFunction) orf).queryString);
		}	
		System.out.println(queryStrings);
		setQI = new TemporalQueriesInstantiator(model, EvidenceQueryDecisionGeneratorOnline.makeQueryTemplates(queryStrings));	
		Collection<Query> newQs = setQI.getQueries(1);
		for (Query q : newQs) {
			q.compile();
		}
		System.out.println("Queries " + setQI.getQueries(0));
		queries.addAll(newQs);
	}
	
	public Set<FiniteStatePolicy> run() {
		Set<PFEngineSampled> beliefs = new HashSet<PFEngineSampled>();
		PFEngineSampled initBelief = new PFEngineSampled(model, properties);
		initBelief.answer(queries);
		initBelief.afterAnsweringQueries();
		beliefs.add(initBelief);
		Set<PFEngineSampled> newBeliefs = beliefs;
		
		for (int i = 0; i < 3; i++) {
			newBeliefs = beliefExpansion(newBeliefs);
			System.out.println("run.expansion.iteration: " + i);
			System.out.println("run.expansion.newsize: " + newBeliefs.size());
			beliefs.addAll(newBeliefs);
		}
		System.out.println("run.numbeliefs: " + beliefs.size());

		Map<PartialWorld, Map<DecisionEvidence, PFEnginePartitioned>> stateActionToNextBeliefs = 
				new HashMap<PartialWorld, Map<DecisionEvidence, PFEnginePartitioned>>();
		Set<PartialWorld> states = collectStates(beliefs);
		System.out.println(states.size());
		for (PartialWorld s : states) {
			Map<DecisionEvidence, PFEnginePartitioned> actionToNextBeliefs = new HashMap<DecisionEvidence, PFEnginePartitioned>();
			PFEnginePartitioned bForS = new PFEnginePartitioned(model, properties, s);
			Set<Evidence> actions = getActions(bForS);
			for (Evidence action : actions) {
				PFEnginePartitioned actionPropagated = new PFEnginePartitioned(model, properties, s);
				actionPropagated.take(action);
				actionPropagated.answer(queries);
				actionPropagated.resample();
				actionPropagated.repartition();
				actionToNextBeliefs.put(null, actionPropagated);
			}
			stateActionToNextBeliefs.put(s, actionToNextBeliefs);
		}
		
		Set<FiniteStatePolicy> policies = new HashSet<FiniteStatePolicy>();
		for (int t = 0; t < horizon; t++) {
			policies = singleBackup(policies, beliefs, stateActionToNextBeliefs);
		}
		
		return policies;
	}
	
	private Set<FiniteStatePolicy> singleBackup(Set<FiniteStatePolicy> oldPolicies, 
			Set<PFEngineSampled> beliefs, 
			Map<PartialWorld, Map<DecisionEvidence, PFEnginePartitioned>> stateActionToNextBeliefs ) {
		Set<FiniteStatePolicy> newPolicies = new HashSet<FiniteStatePolicy>();
		for (PFEngineSampled b : beliefs) {
			newPolicies.add(singleBackupForBelief(oldPolicies, b, stateActionToNextBeliefs));
		}
		return newPolicies;
	}
	
	private FiniteStatePolicy singleBackupForBelief(
			Set<FiniteStatePolicy> oldPolicies,
			PFEngineSampled b,
			Map<PartialWorld, Map<DecisionEvidence, PFEnginePartitioned>> stateActionToNextBeliefs) {
		
		Map<Evidence, FiniteStatePolicy> bestPolicyMap = new HashMap<Evidence, FiniteStatePolicy>();
		AlphaVector bestAlphaVector = null;
		Evidence bestAction = null;
		
		Set<Evidence> actions = getActions(b);
		
		Double bestValue = Double.MIN_VALUE;
		for (Evidence action : actions) {
			Map<Evidence, FiniteStatePolicy> policyMap = new HashMap<Evidence, FiniteStatePolicy>();
			PFEngineSampled actionPropagated = b.copy();
			actionPropagated.take(action);
			Set<Integer> obsIndices = collectObservations(b, stateActionToNextBeliefs, action);
			
			for (Integer obsIndex : obsIndices) {
				ObservabilitySignature os = ObservabilitySignature.getOSbyIndex(obsIndex);
				Evidence obs = os.getEvidence();
				PFEngineSampled next = actionPropagated.copy();
				next.take(obs);
				next.answer(queries);
				next.resample();
				Double bestContingentValue = Double.MIN_VALUE;
				for (FiniteStatePolicy p : oldPolicies) {
					Double value = p.getAlphaVector().getValue(next);
					if (value > bestContingentValue) {
						bestContingentValue = value;
						policyMap.put(obs, p);
					}
				}	
			}
			
			//create alpha vector for policy
			AlphaVector alpha = null;
			int count = 0;
			Set<PartialWorld> states = collectStates(b);
			Set<PartialWorld> nextStates = collectStates(b);
			for (PartialWorld state : states) {
				for (Integer obsIndex : obsIndices) {
					ObservabilitySignature os = ObservabilitySignature.getOSbyIndex(obsIndex);
					Evidence obs = os.getEvidence();
					for (PartialWorld nextState : nextStates) {
						Function rewardFunc = (Function) model.getRandomFunc("reward", 0);
						Number reward = (Number) rewardFunc.getValue(nextState);
						alpha.setValue(state, reward.doubleValue() + policyMap.get(obs).getAlphaVector().getValue(nextState));
						count++;
					}
				}
			}
			
			alpha.normalizeValues(count);
			
			Double value = alpha.getValue(b);
			if (value < bestValue) {
				bestValue = value;
				bestAlphaVector = alpha;
				bestAction = action;
			}
			
		}
		
		FiniteStatePolicy newPolicy = new FiniteStatePolicy(bestAction, bestPolicyMap);
		newPolicy.setAlphaVector(bestAlphaVector);
		return newPolicy;		
	}

	private Set<Integer> collectObservations(
			PFEngineSampled b,
			Map<PartialWorld, Map<DecisionEvidence, PFEnginePartitioned>> stateActionToNextBeliefs,
			Evidence action) {
		Set<Integer> obsIndices = new HashSet<Integer>();
		for (Particle p : b.particles) {
			PartialWorld w = p.curWorld;
			PFEnginePartitioned temp = stateActionToNextBeliefs.get(w).get(action);
			if (temp == null) continue;
			obsIndices.addAll(temp.getPartitions().keySet());
		}
		return obsIndices;
	}
	
	private Set<PartialWorld> collectStates(PFEngineSampled b) {
		Set<PartialWorld> states = new HashSet<PartialWorld>();
		for (TimedParticle p : b.particles) {
			states.add(p.curWorld);			
		}
		System.out.println("collectStates: num states for b: " + states.size());
		return states;
	}

	private Set<PartialWorld> collectStates(Set<PFEngineSampled> beliefs) {
		Set<PartialWorld> states = new HashSet<PartialWorld>();
		for (PFEngineSampled b : beliefs) {
			states.addAll(collectStates(b));
		}
		return states;
	}

	private Set<Evidence> getActions(PFEngineOnline belief) {
		PartialWorld w = belief.particles.get(0).curWorld;
		int timestep = belief.particles.get(0).getTimestep() + 1; //TODO: why does it start as -1?
		Map<BayesNetVar, BayesNetVar> observableMap =((AbstractPartialWorld) w).getObservableMap();
		Map<Type, Set<BayesNetVar>> observedVarsByType = partitionVarsByType(observableMap, w);
		List<DecisionFunction> decisionFunctions = model.getDecisionFunctions();
		Set<Evidence> actions = new HashSet<Evidence>();
		for (DecisionFunction f : decisionFunctions) {
			Set<List<BayesNetVar>> argLists = enumArgListsForFunc(f, observedVarsByType);
			for (List<BayesNetVar> argList : argLists) {
				Evidence action = new Evidence();
				List<ArgSpec> argTerms = new ArrayList<ArgSpec>();
				for (BayesNetVar bv : argList) {
					ArgSpec arg = ((BasicVar) bv).getCanonicalTerm();
					argTerms.add(arg);
				}
				argTerms.add(
						BuiltInTypes.TIMESTEP.getCanonicalTerm(
								BuiltInTypes.TIMESTEP.getGuaranteedObject(timestep)));
				FuncAppTerm left = new FuncAppTerm(f, argTerms);
				DecisionEvidenceStatement decisionStatement = new DecisionEvidenceStatement(left, TrueFormula.TRUE);
				action.addDecisionEvidence(decisionStatement);
				action.compile();
				actions.add(action);
				System.out.println(action);
			}
			
		}
		return actions;
		
	}
	
	private Set<List<BayesNetVar>> enumArgListsForFunc(DecisionFunction f,
			Map<Type, Set<BayesNetVar>> observedVarsByType) {
		return enumArgListsForFunc(f, observedVarsByType, 0);
		
	}

	private Set<List<BayesNetVar>> enumArgListsForFunc(DecisionFunction f,
			Map<Type, Set<BayesNetVar>> observedVarsByType,
			int argNum) {
		Type[] argTypes = f.getArgTypes();
		Set<List<BayesNetVar>> result = new HashSet<List<BayesNetVar>>();
		if (argNum == argTypes.length) {
			result.add(new ArrayList<BayesNetVar>());
			return result;
		}
		Set<List<BayesNetVar>> restOfArgs = enumArgListsForFunc(f, observedVarsByType, argNum + 1);
		Type argType = argTypes[argNum];
		Set<BayesNetVar> vars = observedVarsByType.get(argType);
		System.out.println(argType);
		if (argType.equals(Type.getType("Timestep"))) {
			return restOfArgs;
		} else if (vars == null) {
			return result;
		}
		for (BayesNetVar v : vars) {
			for (List<BayesNetVar> rest : restOfArgs) {
				List<BayesNetVar> newList = new ArrayList<BayesNetVar>();
				newList.add(v);
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
			//if (!(Boolean) observabilityMap.get(var).getValue(w)) continue;
			Type type = ((BasicVar) var).getType();
			if (!result.containsKey(type))
				result.put(type, new HashSet<BayesNetVar>());
			result.get(type).add(var);
		}
		return result;
	}


	private Set<PFEngineSampled> beliefExpansion(Set<PFEngineSampled> beliefs) {
		Set<PFEngineSampled> newBeliefs = new HashSet<PFEngineSampled>();
		for (PFEngineSampled b : beliefs) {
			Set<Evidence> actions = getActions(b);
			for (Evidence a : actions) {
				PFEngineSampled next = b.copy();
				next.beforeTakingEvidence();
				next.takeDecision(a);
				next.answer(queries);
				System.out.println("queries: " + queries);
				next.afterAnsweringQueries2();
				//for (TimedParticle p : next.particles)
					//p.advanceTimestep();
				//next.updateOSforAllParticles();
				//int index = next.retakeObservability();//TODO
				//System.out.println("Evidence: " + ObservabilitySignature.getOSbyIndex(index).getEvidence());
				//next.resample();
				newBeliefs.add(next);
			}
		}
		return newBeliefs;
	}
	
	public static OUPBVI makeOUPBVI(List modelFilePath, String queryFile) {
		query_parser file = new query_parser(queryFile);
		Collection linkStrings = Util.list();
		Collection queryStrings = file.queries;
		Model model = new Model();
		Evidence evidence = new Evidence();
		LinkedList queries = new LinkedList();
		Main.simpleSetupFromFiles(model, evidence, queries,
				modelFilePath);
		Properties properties = new Properties();
		properties.setProperty("numParticles", "100");
		properties.setProperty("useDecayedMCMC", "false");
		properties.setProperty("numMoves", "1");
		Util.initRandom(true);
		Util.setVerbose(false);
		return new OUPBVI(model, properties, queries, 2);
	}
	
	public static void main(String[] args) {
		List<String> modelFiles = new ArrayList<String>();
		modelFiles.add(args[0]);
		OUPBVI oupbvi = makeOUPBVI(modelFiles, args[1]);
		oupbvi.run();
	}
}
