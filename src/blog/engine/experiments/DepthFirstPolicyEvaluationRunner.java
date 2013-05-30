package blog.engine.experiments;

import blog.engine.onlinePF.SampledParticleFilterRunner;

public class DepthFirstPolicyEvaluationRunner {
	
	public static void main (String[] args){
                
		SUU suu = new SUU();
		suu.setNumParticle(1000);
                if (args.length == 0){
		SampledParticleFilterRunner runner = suu.makeRunner("ex_inprog//logistics//policies//monopoly_markov.mblog", "ex_inprog//logistics//policies//donothingpolicy");
		//SampledParticleFilterRunner runner = suu.makeRunner("ex_inprog//logistics//policies//monopoly_color_wp.mblog", "ex_inprog//logistics//policies//monopoly_policy");
		runner.run();
                }
                else{
                    SampledParticleFilterRunner runner = suu.makeRunner
                            (args[0], args[1]);
                    runner.run();
                    
                }
                
                    
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
