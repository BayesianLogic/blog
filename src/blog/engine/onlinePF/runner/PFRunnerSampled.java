package blog.engine.onlinePF.runner;

import java.util.*;

import blog.engine.onlinePF.ObservableRandomFunction;
import blog.engine.onlinePF.PFEngine.PFEngineSampled;
import blog.engine.onlinePF.absyn.PolicyModel;
import blog.engine.onlinePF.evidenceGenerator.EvidenceQueryDecisionGeneratorwPolicy;
import blog.model.Model;
import blog.model.RandomFunction;



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
		for (RandomFunction orf: (List<RandomFunction>) model.getObsFun()){
			queryStrings.add(((ObservableRandomFunction) orf).queryString);
		}		
		evidenceGenerator = new EvidenceQueryDecisionGeneratorwPolicy(model, queryStrings, eviCommunicator, queryResultCommunicator, pm);
	}
}
