package blog.engine.onlinePF.runner;

import java.util.*;

import blog.engine.onlinePF.ObservableRandomFunction;
import blog.engine.onlinePF.PFEngine.PFEnginePartitioned;
import blog.engine.onlinePF.absyn.PolicyModel;
import blog.engine.onlinePF.evidenceGenerator.EvidenceQueryDecisionGeneratorwPolicy;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.RandomFunction;


/**
 * A kind of particle filter runner, specialized to handle particle filters which are partitioned (bucketed) by observation variables
 * It also uses EvidenceGeneratorwPolicy
 * @author cheng
 *
 */
public class PFRunnerPartitioned extends PFRunnerOnline {	
	
	/**
	 * Overloaded constructor Main difference is it replaces the 
	 * - particleFilter with a PFEnginePartitioned
	 * - evidenceGenerator with EvidenceGeneratorwPolicy
	 * @param model see parent
	 * @param queryStrings see parent 
	 * @param particleFilterProperties see parent
	 * @param pm a file that specifies the policy model to be used.
	 */
	public PFRunnerPartitioned(Model model, 
			Collection queryStrings, Properties particleFilterProperties, PolicyModel pm) {
		super(model, particleFilterProperties, 15);
		
		particleFilter = new PFEnginePartitioned(model, particleFilterProperties);
		for (RandomFunction orf: (List<RandomFunction>) model.getObsFun()){
			queryStrings.add(((ObservableRandomFunction) orf).queryString);
		}		
		evidenceGenerator = new EvidenceQueryDecisionGeneratorwPolicy(model, queryStrings, eviCommunicator, queryResultCommunicator, pm);
	}
	
	/**
	 * Overridden method to handle each partition separately when obtaining decision
	 */
	@Override
	public void advancePhase2() {
		PFEnginePartitioned castedParticleFilter = (PFEnginePartitioned) particleFilter;
		Evidence evidence;
		for (Integer osIndex : castedParticleFilter.getPartitions().keySet()){
			evidenceGenerator.updateDecision();
			if ((evidence = evidenceGenerator.getLatestDecision()) == null) {
				System.err.println("Decision should not be null");
				System.exit(1);
			}
			castedParticleFilter.takeWithPartition(evidence, osIndex);
		}
	}
}
