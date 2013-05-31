package blog.engine.experiments;

import java.util.HashMap;

import blog.common.cmdline.AbstractOption;
import blog.common.cmdline.IntOption;
import blog.common.cmdline.StringOption;
import blog.engine.onlinePF.FileCommunicator;
import blog.engine.onlinePF.SampledParticleFilterRunner;
import blog.engine.onlinePF.inverseBucket.UBT;

public class DepthFirstPolicyEvaluationRunner {
	public static HashMap<String, AbstractOption> runtimeOptions = new HashMap<String, AbstractOption>();

	public static void main(String[] args) {
		blog.common.cmdline.Parser
				.setProgramDesc("Bayesian Logic (BLOG) inference engine");
		blog.common.cmdline.Parser
				.setUsageLine("Usage: runblog <file1> ... <fileN>");

		SUU suu = new SUU();
		IntOption numParticles = new IntOption("n", "num_particles", 1000,
				"Use n particles");
		runtimeOptions.put("numparticles", numParticles);
		IntOption numTimesteps = new IntOption("t", "num_timesteps", 100,
				"Run for t timesteps");
		runtimeOptions.put("numtimesteps", numTimesteps);
		StringOption modelFile = new StringOption("m", "model_file",
				"ex_inprog//logistics//policies//monopoly_markov.mblog",
				"Path of model file <s>");
		runtimeOptions.put("modelfile", modelFile);
		StringOption policyFile = new StringOption("p", "policy_file",
				"ex_inprog//logistics//policies//donothingpolicy",
				"Path of policy file <s>");
		runtimeOptions.put("policyfile", policyFile);
		StringOption queryFile = new StringOption("q", "query_file",
				"ex_inprog//logistics//policies//forced_query",
				"Path of query file <s>");
		runtimeOptions.put("queryfile", queryFile);
		StringOption logName = new StringOption("s", "logName", "0",
				"Name that identifies the output files");
		runtimeOptions.put("logname", logName);
		blog.common.cmdline.Parser.parse(args);
		suu.setNumParticle(((IntOption) runtimeOptions.get("numparticles")).getValue());
		SampledParticleFilterRunner runner = suu.makeRunner(
				((StringOption) runtimeOptions.get("modelfile")).getValue(),
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
