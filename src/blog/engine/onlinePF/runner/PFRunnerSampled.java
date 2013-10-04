package blog.engine.onlinePF.runner;

import java.util.*;

import blog.TemporalQueriesInstantiator;
import blog.engine.onlinePF.ObservableRandomFunction;
import blog.engine.onlinePF.PFEngine.PFEngineSampled;
import blog.engine.onlinePF.absyn.PolicyModel;
import blog.engine.onlinePF.evidenceGenerator.EvidenceQueryDecisionGeneratorOnline;
import blog.engine.onlinePF.evidenceGenerator.EvidenceQueryDecisionGeneratorwPolicy;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.Query;
import blog.model.RandomFunction;
import blog.model.Type;



/**
 * A kind of particle filter runner, specialized to handle particle filters which are partitioned (bucketed) by observation variables
 * Furthermore, it uses a depth-first approach, i.e. it samples a single bucket at each timestep
 * It also uses EvidenceGeneratorwPolicy
 * @author cheng
 *
 */
public class PFRunnerSampled extends PFRunnerOnline {	
	/**
	 * Overloaded constructor Main difference is it replaces the 
	 * - particleFilter with a PFEngineSampled
	 * - evidenceGenerator with EvidenceGeneratorwPolicy
	 * @param model see parent
	 * @param queryStrings see parent 
	 * @param particleFilterProperties see parent
	 * @param pm a file that specifies the policy model to be used.
	 */
	public PFRunnerSampled(Model model,
			Collection queryStrings, Properties particleFilterProperties, PolicyModel pm) {
		super(model, particleFilterProperties, 15);
		
		particleFilter = new PFEngineSampled(model, particleFilterProperties);
		/*
		for (RandomFunction orf: (List<RandomFunction>) model.getObsFun()){
			queryStrings.add(((ObservableRandomFunction) orf).queryString);
		}
		*/
		evidenceGenerator = new EvidenceQueryDecisionGeneratorwPolicy(model, queryStrings, eviCommunicator, queryResultCommunicator, pm);
		
		
		queryStrings = new ArrayList();
		for (Type typ: (List<Type>) model.getObsTyp()){
			queryStrings.add("Number_"+typ+"(t)");
		}
		setQI = new TemporalQueriesInstantiator(model, EvidenceQueryDecisionGeneratorOnline.makeQueryTemplates(queryStrings));
	}
	public TemporalQueriesInstantiator setQI;
	
	/**
	 * Phase0 refers to the phase in which 
	 * - set queries are answered and then provided to the policy module
	 */
	public void advancePhase0() {
		Evidence evidence;
		Collection queries = setQI.getQueries(evidenceGenerator.lastTimeStep+1);
		for (Query query : (Collection<Query>)queries){
			if (!query.checkTypesAndScope(model)){
				System.err.println("OPFevidencegenerator.getinput: error checking query");
				System.exit(1);
			}
			if (query.compile()!=0){
				System.err.println("OPFevidencegenerator.getinput: error compiling query");
				System.exit(1);
			}
		}
		particleFilter.beforeTakingEvidence();
		particleFilter.answer(queries);
		String evs = ((PFEngineSampled)particleFilter).afterAnsweringQueries2();
		((EvidenceQueryDecisionGeneratorwPolicy)this.evidenceGenerator).updateGenObj(evs);
		particleFilter.backtrack();
	}
	
	/** Runs until shouldTerminate returns true */
	@Override
	public void run() {
		//int i=0;
		while (!shouldTerminate()){
			advancePhase0();
			advancePhase1();
			advancePhase2();
			//i++;
		}
	}
}
