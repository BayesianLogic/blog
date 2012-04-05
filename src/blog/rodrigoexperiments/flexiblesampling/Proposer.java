package blog.rodrigoexperiments.flexiblesampling;

import java.util.List;

import blog.BLOGUtil;
import blog.DefaultPartialWorld;
import blog.Evidence;
import blog.InstantiatingEvalContext;
import blog.PartialWorld;
import blog.PartialWorldDiff;
import blog.VarWithDistrib;
import blog.common.Util;


public class Proposer implements blog.Proposer {

	public Proposer(SamplerFactory flexibleImportanceSamplerFactory) {
		this.flexibleImportanceSamplerFactory = flexibleImportanceSamplerFactory;
	}

	public PartialWorldDiff initialize(Evidence evidence, List queries) {
		PartialWorld world = new DefaultPartialWorld();
		InstantiatingEvalContext context = new EvalContext(world, Util.set(),
				flexibleImportanceSamplerFactory);
		BLOGUtil.ensureDetAndSupported(queries, world);
		BLOGUtil.ensureDetAndSupported(evidence.getEvidenceVars(), world);
		return new PartialWorldDiff(world);
	}

	public double proposeNextState(PartialWorldDiff world) {
		VarWithDistrib seedVar = (VarWithDistrib) Util.uniformSample(world
				.getInstantiatedVars());
		Sampler blockSampler = flexibleImportanceSamplerFactory.make(seedVar,
				world, Util.set());
		double unsamplingWeight = blockSampler.unsample().getWeight();
		double samplingWeight = blockSampler.sample().getWeight();
		double minimizingWeight = makeWorldMinimal(world);
		return unsamplingWeight * samplingWeight;
	}

	private double makeWorldMinimal(PartialWorldDiff world) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void printStats() {
		// TODO Auto-generated method stub
	}

	public void updateStats(boolean accepted) {
		// TODO Auto-generated method stub
	}

	private SamplerFactory flexibleImportanceSamplerFactory;
}
