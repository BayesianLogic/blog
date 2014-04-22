package blog.engine.pbvi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import blog.TemporalQueriesInstantiator;
import blog.bn.BasicVar;
import blog.bn.BayesNetVar;
import blog.common.Util;
import blog.engine.onlinePF.PFEngine.PFEngineSampled;
import blog.engine.onlinePF.evidenceGenerator.EvidenceQueryDecisionGeneratorOnline;
import blog.model.ArgSpec;
import blog.model.BuiltInTypes;
import blog.model.DecisionEvidenceStatement;
import blog.model.DecisionFunction;
import blog.model.Evidence;
import blog.model.FuncAppTerm;
import blog.model.Model;
import blog.model.Query;
import blog.model.Term;
import blog.model.TrueFormula;
import blog.model.Type;
import blog.world.AbstractPartialWorld;
import blog.world.PartialWorld;

/**
 * Represents the pomdp model.
 * This is in a way, a wrapper around the BLOG model,
 * but has methods specific to a POMDP.
 * 
 */
public class OUPOMDPModel {
	private Model model;
	private List<Query> queries;
	private Properties properties;
	
	//TODO: get from model
	private double gamma = 0.9;
	
	private TemporalQueriesInstantiator setQI;
	
	public OUPOMDPModel(Model model, Properties properties, List<Query> queries, List<String> queryStrings) {
		this.model = model;
		this.queries = queries;
		this.properties = properties;
		
		
		for (Type typ: (List<Type>) model.getObsTyp()){
			queryStrings.add("Number_"+typ+"(t)");
		}

		System.out.println("Queries " + queryStrings);
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
	
	/**
	 * Returns BLOG model.
	 * @return
	 */
	public Model getModel() {
		return model;
	}
	
	public Properties getProperties() {
		return properties;
	}
	
	/**
	 * Returns set of available actions given a belief
	 * @param b
	 * @return
	 */
	public Set<LiftedEvidence> getActions(Belief b) {
		State s = (State) Util.getFirst(b.getStates());
		PartialWorld w = s.getWorld();
		int timestep = b.getTimestep();
		Map<BayesNetVar, BayesNetVar> observableMap = ((AbstractPartialWorld) w).getObservableMap();
	
		Map<Type, Set<BayesNetVar>> observedVarsByType = partitionVarsByType(observableMap, w);
		List<DecisionFunction> decisionFunctions = model.getDecisionFunctions();

		Set<LiftedEvidence> actions = new HashSet<LiftedEvidence>();
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
				actions.add(new LiftedEvidence(action));
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

	public double getGamma() {
		return gamma;
	}
	
	public Belief generateInitialBelief() {
		PFEngineSampled initPF = new PFEngineSampled(this.getModel(), properties);
		initPF.answer(getQueries(0));
		initPF.afterAnsweringQueries2();
		return new Belief(initPF, this);
	}
	
	public Belief generateInitialBelief(int numParticles) {
		Properties properties = (Properties) this.properties.clone();
		properties.setProperty("numParticles", "" + numParticles);
		PFEngineSampled initPF = new PFEngineSampled(this.getModel(), properties);
		initPF.answer(getQueries(0));
		initPF.afterAnsweringQueries2();
		return new Belief(initPF, this);
	}
	
	
	public double getReward(State s, LiftedEvidence a) {
		Belief b = Belief.getSingletonBelief(s, 1, this);
		Belief next = b.sampleNextBelief(a);
		Double reward = next.getLatestReward();
		return reward;
	}

	public double getAvgReward(Belief b, LiftedEvidence a) {
		double total = 0;
		int count = 0;
		for (State s : b.getStates()) {
			int weight = b.getCount(s);
			double reward = getReward(s, a);
			total +=  reward * weight;
			count += weight;
		}
		return total/count;
	}
}
