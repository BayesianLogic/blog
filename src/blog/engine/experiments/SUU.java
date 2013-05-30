package blog.engine.experiments;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

import blog.Main;
import blog.common.Util;
import blog.engine.onlinePF.PolicyModel;
import blog.engine.onlinePF.SampledParticleFilterRunner;
import blog.model.Evidence;
import blog.model.Model;

public class SUU {
	
	public SUU (){
		setDefaultParticleFilterProperties();
	}
	public void setNumParticle(int number){
		properties.setProperty("numParticles", ""+number);
	}
	
	public SampledParticleFilterRunner makeRunner(String modelFilePath, String policyFilePath) {
		Collection linkStrings = Util.list();
		Collection queryStrings = Util.list("capital(0, t)", "rentPaymentRequired(0, 1, t)", "rentPaymentRequired(1, 0, t)", "owner(0, t)", "observation_rent(t)");
		//setDefaultParticleFilterProperties();
		setModel(modelFilePath);
		PolicyModel pm = PolicyModel.policyFromFile(policyFilePath);

		SampledParticleFilterRunner runner = new SampledParticleFilterRunner(
				model, linkStrings, queryStrings, properties, pm);
		return runner;
	}
	public SampledParticleFilterRunner makeRunner(String modelFilePath, String policyFilePath, String queryFile) {
		query_parser file = new query_parser(queryFile);
		Collection linkStrings = Util.list();
		Collection queryStrings = file.queries;//Util.list("capital(0, t)", "rentPaymentRequired(0, 1, t)", "rentPaymentRequired(1, 0, t)", "owner(0, t)", "observation_rent(t)");
		//setDefaultParticleFilterProperties();
		setModel(modelFilePath);
		PolicyModel pm = PolicyModel.policyFromFile(policyFilePath);
		
		SampledParticleFilterRunner runner = new SampledParticleFilterRunner(
				model, linkStrings, queryStrings, properties, pm);
		return runner;
	}

	private static void setDefaultParticleFilterProperties() {
		properties.setProperty("numParticles", "1000");
		properties.setProperty("useDecayedMCMC", "false");
		properties.setProperty("numMoves", "1");
		Util.initRandom(true);
		Util.setVerbose(true);
		
	}

	private static Properties properties = 	properties = new Properties();;

	private static Model model;

	private static void setModel(String modelFilePath) {
		model = new Model();
		Evidence evidence = new Evidence();
		LinkedList queries = new LinkedList();
		Main.simpleSetupFromFiles(model, evidence, queries,
				Util.list(modelFilePath));
	}

}
