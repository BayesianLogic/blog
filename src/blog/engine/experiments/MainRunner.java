package blog.engine.experiments;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import blog.common.cmdline.AbstractOption;
import blog.common.cmdline.BooleanOption;
import blog.common.cmdline.IntOption;
import blog.common.cmdline.StringOption;
import blog.engine.onlinePF.FileCommunicator;
import blog.engine.onlinePF.OPFevidenceGenerator;
import blog.engine.onlinePF.PFRunnerSampled;
import blog.engine.onlinePF.inverseBucket.UBT;

public class MainRunner {
	public static HashMap<String, AbstractOption> runtimeOptions = new HashMap<String, AbstractOption>();

	public static void main(String[] args) {
		blog.common.cmdline.Parser
				.setProgramDesc("Bayesian Logic (BLOG) inference engine");
		blog.common.cmdline.Parser
				.setUsageLine("Usage: runblog <file1> ... <fileN>");

		SUU suu = new SUU();
		StringOption mode = new StringOption("o", "mode",
				"policyeval",
				"offline, online or policyeval");
		runtimeOptions.put("mode", mode);
		BooleanOption dropHistory = new BooleanOption("d", "drophistory", false,
				"Whether history should be dropped");
		runtimeOptions.put("drop history", dropHistory);
		IntOption numParticles = new IntOption("n", "num_particles", 1000,
				"Use n particles");
		runtimeOptions.put("numparticles", numParticles);
		IntOption numTimesteps = new IntOption("t", "num_timesteps", 100,
				"Run for t timesteps");
		runtimeOptions.put("numtimesteps", numTimesteps);
		StringOption policyFile = new StringOption("p", "policy_file",
				"ex_inprog//logistics//policies//donothingpolicy",
				"Path of policy file <s>");
		runtimeOptions.put("policyfile", policyFile);
		BooleanOption breadthFirst = new BooleanOption("b", "breadthfirst", false,
				"Breadth first policy eval?");
		runtimeOptions.put("breadthFirst", breadthFirst);
		StringOption queryFile = new StringOption("q", "query_file",
				"ex_inprog//logistics//policies//forced_query",
				"Path of query file <s>");
		runtimeOptions.put("queryfile", queryFile);
		StringOption logName = new StringOption("s", "logName", "0",
				"Name that identifies the output files");
		runtimeOptions.put("logname", logName);
		BooleanOption inverseBucket = new BooleanOption("i", "inverseBucket", false,
				"Whether history should be dropped");
		runtimeOptions.put("inverseBucket", inverseBucket);
		BooleanOption userInput = new BooleanOption("v", "userInput", false,
				"Whether history should be dropped");
		runtimeOptions.put("userInput", userInput);
		List<String> filenames = blog.common.cmdline.Parser.parse(args);
		
		suu.setNumParticle(((IntOption) runtimeOptions.get("numparticles")).getValue());
		UBT.dropHistory = dropHistory.getValue();
		
		if(((StringOption) runtimeOptions.get("mode")).getValue().equals("offline")){
			filenames.add("-e");
			filenames.add("blog.engine.ParticleFilter");
			filenames.add("-n");
			filenames.add("" + ((IntOption) runtimeOptions.get("numparticles")).getValue());
			filenames.add("-r");
			String[] arguments = Arrays.copyOf((filenames.toArray()), (filenames.toArray()).length, String[].class);
			blog.Main.main(arguments);
			System.exit(0);
		}
		else if(((StringOption) runtimeOptions.get("mode")).getValue().equals("policyeval")){
			if (!breadthFirst.getValue())
				DepthFirstPolicyEvaluationRunner.main(args);
			else {
				if (!inverseBucket.getValue()){
					BreadthFirstPolicyEvaluationRunner.main(args);
				}
			}
			System.exit(0);
		}
		else if(((StringOption) runtimeOptions.get("mode")).getValue().equals("online")){
			PFRunnerSampled.vanilla = true;
			OPFevidenceGenerator.userInput = userInput.getValue();
			DepthFirstPolicyEvaluationRunner.main(args);
		}
		PFRunnerSampled runner = suu.makeSampledRunner(
				filenames,
				((StringOption) runtimeOptions.get("policyfile")).getValue(),
				((StringOption) runtimeOptions.get("queryfile")).getValue());
		runner.numtstep = ((IntOption) runtimeOptions.get("numtimesteps")).getValue();
		UBT.valueOutput = new FileCommunicator("randomstuff//log" + (((StringOption) runtimeOptions.get("logname")).getValue())
				+ ".log");
		UBT.worldOutput = new FileCommunicator("randomstuff//world" + (((StringOption) runtimeOptions.get("logname")).getValue())
				+ ".log");
		UBT.numtstep = ((IntOption) runtimeOptions.get("numtimesteps")).getValue();
		// SampledParticleFilterRunner runner =
		// suu.makeRunner("ex_inprog//logistics//policies//monopoly_markov.mblog",
		// "ex_inprog//logistics//policies//donothingpolicy");
		// SampledParticleFilterRunner runner =
		// suu.makeRunner("ex_inprog//logistics//policies//monopoly_color_wp.mblog",
		// "ex_inprog//logistics//policies//monopoly_policy");
		runner.run();

	}

	/*
	 * public static void main (String[] args){ SUU suu = new SUU();
	 * suu.setNumParticle(100); SampledParticleFilterRunner runner =
	 * suu.makeRunner("ex_inprog//logistics//policies//test.mblog",
	 * "ex_inprog//logistics//policies//test_policy"); runner.run(); }
	 */
}
