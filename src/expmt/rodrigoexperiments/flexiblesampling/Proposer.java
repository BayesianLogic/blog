package expmt.rodrigoexperiments.flexiblesampling;

import java.util.List;

import blog.BLOGUtil;
import blog.bn.VarWithDistrib;
import blog.bn.BayesNetVar;
import blog.common.Util;
import blog.model.Evidence;
import blog.sample.InstantiatingEvalContext;
import blog.world.DefaultPartialWorld;
import blog.world.PartialWorld;
import blog.world.PartialWorldDiff;


public class Proposer implements blog.sample.Proposer {

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

    public PartialWorldDiff reduceToCore(PartialWorld curWorld, BayesNetVar var) {
        return null;
    }

    public double proposeNextState(PartialWorldDiff proposedWorld, 
                            BayesNetVar var, int i) {
        return 0;
    }

    public double proposeNextState(PartialWorldDiff proposedWorld, BayesNetVar var) {
        return 0;
    }

	private SamplerFactory flexibleImportanceSamplerFactory;
}
