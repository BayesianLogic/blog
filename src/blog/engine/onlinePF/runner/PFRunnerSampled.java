package blog.engine.onlinePF.runner;

import java.util.*;

import blog.engine.onlinePF.ObservableRandomFunction;
import blog.engine.onlinePF.PolicyModel;
import blog.engine.onlinePF.PFEngine.PFEngineSampled;
import blog.engine.onlinePF.evidenceGenerator.EvidenceGeneratorwPolicy;
import blog.model.Model;
import blog.model.RandomFunction;


/**
 * ParticleFilterRunnerOnGenerator extends {@link #ParticleFilterRunner} in
 * order to obtain evidence from an external stream input
 * @author Cheng
 * @since Jan 03 2013
 * 
 */
public class PFRunnerSampled extends PFRunnerOnline {	
	public PFRunnerSampled(Model model,
			Collection queryStrings, Properties particleFilterProperties, PolicyModel pm) {
		super(model, particleFilterProperties, 15);
		
		particleFilter = new PFEngineSampled(model, particleFilterProperties);
		for (RandomFunction orf: (List<RandomFunction>) model.getObsFun()){
			queryStrings.add(((ObservableRandomFunction) orf).queryString);
		}		
		evidenceGenerator = new EvidenceGeneratorwPolicy(model, queryStrings, eviCommunicator, queryResultCommunicator, pm);
	}
}
