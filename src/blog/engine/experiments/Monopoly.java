package blog.engine.experiments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

import blog.Main;
import blog.common.Util;
import blog.engine.ParticleFilter;
import blog.engine.onlinePF.inverseBucket.InverseParticleFilterRunner;
import blog.engine.onlinePF.inverseBucket.UBT;
import blog.model.Evidence;
import blog.model.Model;

public class Monopoly {

	public static void main (String[] args){
		Collection linkStrings = Util.list();
		Collection queryStrings = Util.list("value(t)");
		setDefaultParticleFilterProperties();
	    setModel("ex_inprog//logistics//monopoly.mblog");
	    
	    for (int i = 0; i<5; i++){
		    
		    
		    UBT.runTimeTimer.startTimer();
		    //System.exit(1);
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
