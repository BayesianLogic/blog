package blog.engine.experiments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

import blog.Main;
import blog.common.Util;
import blog.engine.ParticleFilter;
import blog.engine.onlinePF.absyn.PolicyModel;
import blog.engine.onlinePF.inverseBucket.InverseParticleFilterRunner;
import blog.engine.onlinePF.inverseBucket.UBT;
import blog.model.Evidence;
import blog.model.Model;

public class TigerExample2 {

	public static void main (String[] args){
		Collection linkStrings = Util.list();
		Collection queryStrings = Util.list("value(t)");
		setDefaultParticleFilterProperties();
	    setModel("ex_inprog//logistics//tigerExample2.txt");
	    
	    for (int i = 0; i<5; i++){
		    PolicyModel pm = PolicyModel.policyFromFile("ex_inprog//logistics//tigerPolicy2.txt");
		    InverseParticleFilterRunner runner = new InverseParticleFilterRunner(model, linkStrings, queryStrings, properties, pm);
		    UBT.runTimeTimer.startTimer();
		    //ParticleFilterRunnerOnlinePartitioned runner = new ParticleFilterRunnerOnlinePartitioned(model, linkStrings, queryStrings, properties, pm);
		    try {
				runner.run();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}
	    }
	}
	private static void setDefaultParticleFilterProperties() {
		properties = new Properties();
		properties.setProperty("numParticles", "300");
		properties.setProperty("useDecayedMCMC", "false");
		properties.setProperty("numMoves", "1");
		Util.initRandom(true);
		Util.setVerbose(true);
	}	
	private static Properties properties;
	private static ParticleFilter particleFilter;
	private static Model model;
	private static void setModel(String newModelString) {
		model = new Model();
		Evidence evidence = new Evidence();
		LinkedList queries = new LinkedList();
		ArrayList fn = new ArrayList();
		fn.add( newModelString);
		
		Main.setupFromFiles(model, evidence, queries,fn, new ArrayList(), false, false);
	}
	
}
