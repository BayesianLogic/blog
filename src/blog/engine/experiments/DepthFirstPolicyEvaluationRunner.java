package blog.engine.experiments;

import blog.engine.onlinePF.SampledParticleFilterRunner;

public class DepthFirstPolicyEvaluationRunner {
	public static void main (String[] args){
		SUU suu = new SUU();
		suu.setNumParticle(1000);
		SampledParticleFilterRunner runner = suu.makeRunner("ex_inprog//logistics//policies//monopoly_color_wp.mblog", "ex_inprog//logistics//policies//monopoly_policy");
		runner.run();
	}

}
