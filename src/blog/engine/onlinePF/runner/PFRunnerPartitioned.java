package blog.engine.onlinePF.runner;

import java.util.*;

import blog.engine.onlinePF.ObservableRandomFunction;
import blog.engine.onlinePF.PolicyModel;
import blog.engine.onlinePF.PFEngine.PFEnginePartitioned;
import blog.engine.onlinePF.evidenceGenerator.EvidenceGeneratorwPolicy;
import blog.engine.onlinePF.inverseBucket.TimedParticle;
import blog.engine.onlinePF.inverseBucket.UBT;
import blog.model.ArgSpecQuery;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.RandomFunction;
import blog.world.AbstractPartialWorld;


/**
 * ParticleFilterRunnerOnGenerator extends {@link #ParticleFilterRunner} in
 * order to obtain evidence from an external stream input
 * @author Cheng
 * @since Jan 03 2013
 * 
 */
public class PFRunnerPartitioned extends PFRunnerOnline {	
	public PFRunnerPartitioned(Model model, 
			Collection queryStrings, Properties particleFilterProperties, PolicyModel pm) {
		super(model, particleFilterProperties, 15);
		
		particleFilter = new PFEnginePartitioned(model, particleFilterProperties);
		for (RandomFunction orf: (List<RandomFunction>) model.getObsFun()){
			queryStrings.add(((ObservableRandomFunction) orf).queryString);
		}		
		evidenceGenerator = new EvidenceGeneratorwPolicy(model, queryStrings, eviCommunicator, queryResultCommunicator, pm);
	}
	
	/**
	 * Overridden method to handle each partition separately
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
