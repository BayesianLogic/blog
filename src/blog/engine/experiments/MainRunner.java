package blog.engine.experiments;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import blog.common.cmdline.AbstractOption;
import blog.common.cmdline.BooleanOption;
import blog.common.cmdline.IntOption;
import blog.common.cmdline.StringOption;
import blog.engine.onlinePF.Util.FileCommunicator;
import blog.engine.onlinePF.evidenceGenerator.EvidenceGeneratorOnline;
import blog.engine.onlinePF.inverseBucket.UBT;
import blog.engine.onlinePF.runner.PFRunnerPartitioned;
import blog.engine.onlinePF.runner.PFRunnerSampled;

public class MainRunner {
	public static void main(String[] args) {
		blog.common.cmdline.Parser
				.setProgramDesc("Bayesian Logic (BLOG) inference engine");
		blog.common.cmdline.Parser
				.setUsageLine("Usage: runblog <file1> ... <fileN>");

		SUU suu = new SUU();
		
		/**
		 * The following options specify the mode,
		 * they significantly affect how the particle filter will be used
		 */
		StringOption mode = new StringOption("m", "mode",
				"policyeval",
				"offline, online or policyeval");
		OptionsCollection.mode = mode;
		
		BooleanOption breadthFirst = new BooleanOption("b", "breadthfirst", false,
				"Breadth first policy eval?");
		OptionsCollection.breadthFirst = breadthFirst;
		
		BooleanOption inverseBucket = new BooleanOption("i", "inversebucket", false,
				"Whether to use inverse bucketing");
		OptionsCollection.inverseBucket = inverseBucket;
		
		BooleanOption userInput = new BooleanOption("v", "userinput", false,
				"Whether history should be dropped");
		OptionsCollection.userInput = userInput;
		
		
		/**
		 * The following options do not affect the main mode of operation
		 */
		BooleanOption dropHistory = new BooleanOption("d", "drophistory", false,
				"Whether history should be dropped");
		OptionsCollection.dropHistory = dropHistory;
		
		IntOption numParticles = new IntOption("n", "num_particles", 1000,
				"Use n particles");
		OptionsCollection.numParticles = numParticles;
		
		IntOption numTimesteps = new IntOption("t", "num_timesteps", 100,
				"Run for t timesteps");
		OptionsCollection.numTimesteps = numTimesteps;
		
		
		/**
		 * The following inputs specify the file names
		 * the model file does not require a flag
		 */
		StringOption policyFile = new StringOption("p", "policy_file",
				"ex_inprog//logistics//policies//donothingpolicy",
				"Path of policy file <s>");
		OptionsCollection.policyFile = policyFile;
		
		StringOption queryFile = new StringOption("q", "query_file",
				"ex_inprog//logistics//policies//forced_query",
				"Path of query file <s>");
		OptionsCollection.queryFile = queryFile;
		
		StringOption logName = new StringOption("s", "logName", "0",
				"Name that identifies the output files");
		OptionsCollection.logName = logName;
		
		List<String> filenames = filenames = blog.common.cmdline.Parser.parse(args);
		OptionsCollection.filenames = filenames;
		
		
		suu.setNumParticle( OptionsCollection.numParticles.getValue());
		UBT.dropHistory = OptionsCollection.dropHistory.getValue();
		
		if(OptionsCollection.mode.getValue().equals("offline")){
			filenames.add("-e");
			filenames.add("blog.engine.ParticleFilter");
			filenames.add("-n");
			filenames.add("" + OptionsCollection.numParticles.getValue());
			filenames.add("-r");
			String[] arguments = Arrays.copyOf((filenames.toArray()), (filenames.toArray()).length, String[].class);
			blog.Main.main(arguments);
			System.exit(0);
		}
		else if(OptionsCollection.mode.getValue().equals("policyeval")){
			if (!breadthFirst.getValue())
				depthFirstMain();
			else {
				if (!inverseBucket.getValue()){
					breadthFirstMain();
				}
			}
			System.exit(0);
		}
		else if(OptionsCollection.mode.getValue().equals("online")){
			System.err.println("error in iteractive mode: corrupt pipe");
			System.exit(1);
		}
	}


	public static void depthFirstMain() {

		SUU suu = new SUU();
		suu.setNumParticle(OptionsCollection.numParticles.getValue());

		PFRunnerSampled runner = suu.makeSampledRunner(
				OptionsCollection.filenames,
				OptionsCollection.policyFile.getValue(),
				OptionsCollection.queryFile.getValue());
		runner.numtstep = (OptionsCollection.numTimesteps).getValue();
		UBT.valueOutput = new FileCommunicator("randomstuff//" + (OptionsCollection.logName.getValue()) + "val.log"
				+ "0.log");
		UBT.valueOutput2 = new FileCommunicator("randomstuff//" + (OptionsCollection.logName.getValue()) + "val.log"
				+ "1.log");
		UBT.valueOutput3 = new FileCommunicator("randomstuff//" + (OptionsCollection.logName.getValue()) + "val.log"
				+ "2.log");
		UBT.worldOutput = new FileCommunicator("randomstuff//" + (OptionsCollection.logName.getValue()) + "world.log"
				+ ".log");
		runner.run();
	}
	public static void breadthFirstMain() {

		SUU suu = new SUU();
		suu.setNumParticle(OptionsCollection.numParticles.getValue());

		PFRunnerPartitioned runner = suu.makeBFRunner(
				OptionsCollection.filenames,
				OptionsCollection.policyFile.getValue(),
				OptionsCollection.queryFile.getValue());
		runner.numtstep = (OptionsCollection.numTimesteps).getValue();
		UBT.valueOutput = new FileCommunicator("randomstuff//" + (OptionsCollection.logName.getValue()) + "val.log"
				+ "0.log");
		UBT.valueOutput2 = new FileCommunicator("randomstuff//" + (OptionsCollection.logName.getValue()) + "val.log"
				+ "1.log");
		UBT.valueOutput3 = new FileCommunicator("randomstuff//" + (OptionsCollection.logName.getValue()) + "val.log"
				+ "2.log");
		UBT.worldOutput = new FileCommunicator("randomstuff//" + (OptionsCollection.logName.getValue()) + "world.log"
				+ ".log");
		runner.run();
	}
	public static void interactivetMain() {

		SUU suu = new SUU();
		suu.setNumParticle(OptionsCollection.numParticles.getValue());

		PFRunnerPartitioned runner = suu.makeBFRunner(
				OptionsCollection.filenames,
				OptionsCollection.policyFile.getValue(),
				OptionsCollection.queryFile.getValue());
		runner.numtstep = (OptionsCollection.numTimesteps).getValue();
		UBT.valueOutput = new FileCommunicator("randomstuff//" + (OptionsCollection.logName.getValue()) + "val.log"
				+ "0.log");
		UBT.valueOutput2 = new FileCommunicator("randomstuff//" + (OptionsCollection.logName.getValue()) + "val.log"
				+ "1.log");
		UBT.valueOutput3 = new FileCommunicator("randomstuff//" + (OptionsCollection.logName.getValue()) + "val.log"
				+ "2.log");
		UBT.worldOutput = new FileCommunicator("randomstuff//" + (OptionsCollection.logName.getValue()) + "world.log"
				+ ".log");
		runner.run();
	}
}
