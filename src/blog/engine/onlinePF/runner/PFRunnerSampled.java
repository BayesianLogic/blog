package blog.engine.onlinePF.runner;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

import blog.DBLOGUtil;
import blog.Main;
import blog.common.Histogram;
import blog.common.UnaryProcedure;
import blog.common.Util;
import blog.engine.Particle;
import blog.engine.ParticleFilter;
import blog.engine.ParticleFilterRunner;
import blog.engine.onlinePF.Communicator;
import blog.engine.onlinePF.FileCommunicator;
import blog.engine.onlinePF.ObservableRandomFunction;
import blog.engine.onlinePF.PipedCommunicator;
import blog.engine.onlinePF.PolicyModel;
import blog.engine.onlinePF.PFEngine.PFEngineOnline;
import blog.engine.onlinePF.PFEngine.PFEngineSampled;
import blog.engine.onlinePF.evidenceGenerator.EvidenceGeneratorOnline;
import blog.engine.onlinePF.evidenceGenerator.EvidenceGeneratorwPolicy;
import blog.engine.onlinePF.inverseBucket.TimedParticle;
import blog.engine.onlinePF.inverseBucket.UBT;
import blog.model.ArgSpecQuery;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.Query;
import blog.model.RandomFunction;
import blog.world.AbstractPartialWorld;
import blog.world.PartialWorld;


/**
 * ParticleFilterRunnerOnGenerator extends {@link #ParticleFilterRunner} in
 * order to obtain evidence from an external stream input
 * @author Cheng
 * @since Jan 03 2013
 * 
 */
public class PFRunnerSampled extends PFRunnerOnline {	
	public PFRunnerSampled(Model model, Collection linkStrings,
			Collection queryStrings, Properties particleFilterProperties, PolicyModel pm) {
		super(model, particleFilterProperties, 15);
		
		particleFilter = new PFEngineSampled(model, particleFilterProperties);
		for (RandomFunction orf: (List<RandomFunction>) model.getObsFun()){
			queryStrings.add(((ObservableRandomFunction) orf).queryString);
		}		
		evidenceGenerator = new EvidenceGeneratorwPolicy(model, queryStrings, eviCommunicator, queryResultCommunicator, pm);
	}
}
