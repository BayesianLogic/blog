package blog.engine.experiments;

import blog.engine.onlinePF.FileCommunicator;
import blog.engine.onlinePF.SampledParticleFilterRunner;
import blog.engine.onlinePF.inverseBucket.UBT;

public class DepthFirstPolicyEvaluationRunner {
	
	public static void main (String[] args){
                
		SUU suu = new SUU();
		suu.setNumParticle(Integer.parseInt(args[0])); 
               
		SampledParticleFilterRunner runner = suu.makeRunner
                        ("ex_inprog//logistics//policies//monopoly_markov.mblog", 
                        "ex_inprog//logistics//policies//donothingpolicy", 
                        "ex_inprog//logistics//policies//forced_query");
                UBT.valueOutput = new FileCommunicator("randomstuff//log"+args[1]+".log");
                UBT.worldOutput = new FileCommunicator("randomstuff//world"+args[1]+".log");
                UBT.numtstep = Integer.parseInt(args[2]);
		//SampledParticleFilterRunner runner = suu.makeRunner("ex_inprog//logistics//policies//monopoly_markov.mblog", "ex_inprog//logistics//policies//donothingpolicy");
		//SampledParticleFilterRunner runner = suu.makeRunner("ex_inprog//logistics//policies//monopoly_color_wp.mblog", "ex_inprog//logistics//policies//monopoly_policy");
		runner.run();
                
                    
	}
	
	/*
	public static void main (String[] args){
		SUU suu = new SUU();
		suu.setNumParticle(100);
		SampledParticleFilterRunner runner = suu.makeRunner("ex_inprog//logistics//policies//test.mblog", "ex_inprog//logistics//policies//test_policy");
		runner.run();
	}
	*/
}
