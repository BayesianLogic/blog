package blog.engine.onlinePF;

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

public class TigerExample {

	public static String tigerModelFile =
			"type Move;"
		+	"type State;"
		+	"type Sound;"
		+	"distinct Sound noiseLeft, noiseRight;"
		+	"distinct Move openLeft, openRight, Listen;"
		+	"distinct State sl, sr;"
		+	"decision Boolean chosen_Move(Move m, Timestep t);"
		+	"random Boolean observable(getSound(Timestep t)){"
		+	"	if (t == @0) then "
		+	"		= false"
		+	"	else if (chosen_Move(Listen, Prev(t)) == true) then"
		+	"		= true"
		+	"	else"
		+	"		= false};"
		+	"random Sound getSound(Timestep t){"
		+	"	if (getState(t) == sl) then"
		+	"		~ Categorical({noiseLeft -> 0.85, noiseRight -> 0.15})"
		+	"	else"
		+	"		~ Categorical({noiseLeft -> 0.15, noiseRight -> 0.85})};"
		+	"random State getState (Timestep t){"
		+	"	if (t == @0) then" 
		+	"		~ UniformChoice({State s})"
		+	"	else if (chosen_Move(openLeft, Prev(t)) == true) then"
		+	"		~ UniformChoice({State s})"
		+	"	else if (chosen_Move(openRight, Prev(t)) == true) then"
		+	"		~ UniformChoice({State s})"
		+	"	else"
		+	"		= getState(Prev(t))};"
		+	"random Integer reward (Timestep t){"
		+	"	if (t == @0) then" 
		+	"		= 0"
		+	"	else if (chosen_Move(Listen,Prev(t)) == true) then"
		+	"		= -1"
		+	"	else if ((getState(Prev(t)) == sl & chosen_Move(openLeft, Prev(t))) | (getState(Prev(t)) == sr & chosen_Move(openRight, Prev(t)))) then"
		+	"		= -100"
		+	"	else"
		+	"		= 10};"
		+	"random Integer value(Timestep t){"
		+	"	if (t == @0) then"
		+	"		= reward(t)"
		+	"	else"
		+	"		= value(Prev(t)) + reward(t)};"
			;
			
	public static String tigerPolicyFile =
			"if (\"getState(t)==sl\" >= 0.95)"
		+	"	{\"chosen_Move(openRight,t)\"}"
		+	"elseif (\"getState(t)==sr\" >= 0.95)"
		+	"	{\"chosen_Move(openLeft,t)\"}"
		+	"else"
		+	"	{\"chosen_Move(Listen,t)\"};"
			;
	public static void main (String[] args) throws Exception {
		Collection linkStrings = Util.list();
		Collection queryStrings = Util.list("value(t)");
		setDefaultParticleFilterProperties();
	    setModel(tigerModelFile);
	    PolicyModel pm = PolicyModel.policyFromString(tigerPolicyFile);

	    for (int i = 1; i<4; i++){
		    //ParticleFilterRunnerOnlinePartitioned runner = new ParticleFilterRunnerOnlinePartitioned(model, linkStrings, queryStrings, properties, pm);
		    //InverseParticleFilterRunner runner = new InverseParticleFilterRunner(model, linkStrings, queryStrings, properties, pm);
		    SampledParticleFilterRunnerOnlinePartitioned runner = new SampledParticleFilterRunnerOnlinePartitioned(model, linkStrings, queryStrings, properties, pm);
		    properties.setProperty("numParticles", ""+i*10000);
		    
		    UBT.runTimeTimer.startTimer();
		    
		    UBT.dataOutput.printInput("#Particles: " + i*10000);
		    //UBT.runTimeTimer.startTimer();
		    runner.run();
	    }
	}
	private static void setDefaultParticleFilterProperties() {
		properties = new Properties();
		properties.setProperty("numParticles", "10000");
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
		Main.stringSetup(model, evidence, queries, newModelString);
	}
	
}
