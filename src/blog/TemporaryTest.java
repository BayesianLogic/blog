package blog;

import java.util.*;

import blog.common.Util;
import blog.model.Model;

public class TemporaryTest {
	public static void main(String[] args) {
		Util.initRandom(true);
		Model model = BLOGUtil.parseModel_NE("random Real X ~ UniformReal[0,3]();"
				+ "random Real Y ~ Gaussian(X,1);");
		RandFuncAppVar x = (RandFuncAppVar) BLOGUtil.parseVariable_NE("X", model);
		RandFuncAppVar y = (RandFuncAppVar) BLOGUtil.parseVariable_NE("Y", model);
		PartialWorld world = new DefaultPartialWorld();
		y.ensureDetAndSupported(world);

		System.out.println("World: " + world);

		double average = 0;
		double totalWeight = 0;

		VariableGibbsProposer proposer = new VariableGibbsProposer(model,
				new Properties());
		proposer.initialize(new Evidence(), new LinkedList());
		PartialWorldDiff proposedWorld = new PartialWorldDiff(world);
		average = 0;
		totalWeight = 0;
		for (int i = 0; i != 10000; i++) {
			proposer.proposeNextState(proposedWorld);
			average = Util.incrementalWeightedAverage(
					((Double) proposedWorld.getValue(x)).doubleValue(), 1, average,
					totalWeight);
			totalWeight += 1;
		}
		System.out.println("Average of samples: " + average);

		MHSampler mh = new MHSampler(model, Util.properties("proposerClass",
				"blog.VariableGibbsProposer"));
		mh.initialize(BLOGUtil.parseEvidence_NE("obs Y = 1.0;", model),
				new LinkedList());
		average = 0;
		totalWeight = 0;
		for (int i = 0; i != 10000; i++) {
			mh.nextSample();
			average = Util.incrementalWeightedAverage(((Double) mh.getLatestWorld()
					.getValue(x)).doubleValue(), 1, average, totalWeight);
			totalWeight += 1;
		}
		System.out.println("Average of samples: " + average);

		TruncatedUniformAndGaussianMCMCSampler sampler = new TruncatedUniformAndGaussianMCMCSampler();
		TruncatedUniformAndGaussianMCMCSampler.SampleIterator it = sampler.sampler(
				x, world);
		System.out.println("Eligible: " + (it != null));
		System.out.println("From children: " + it.messageFromChildren);
		System.out.println("Last prior: " + it.uniformRealPrior);
		average = 0;
		totalWeight = 0;
		for (int i = 0; i != 10000; i++) {
			WeightedValue sample = (WeightedValue) it.next();
			average = Util.incrementalWeightedAverage(
					((Double) sample.value).doubleValue(), sample.weight, average,
					totalWeight);
			totalWeight += sample.weight;
		}
		System.out.println("Average of samples: " + average);
	}
}
