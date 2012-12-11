package blog;

import java.util.*;

import blog.bn.VarWithDistrib;
import blog.common.EZIterator;
import blog.distrib.*;
import blog.sample.VariableImportanceSampler;
import blog.world.PartialWorld;

/**
 * Implements an MCMC sampler for variables with truncated uniform priors and
 * which are the means of Gaussian distributions on their children.
 * 
 * @author Rodrigo
 * 
 */
public class TruncatedUniformAndGaussianMCMCSampler implements
		VariableImportanceSampler {

	public SampleIterator sampler(VarWithDistrib var, PartialWorld world) {
		blog.distrib.UniformReal uniformRealPrior;
		UnivarGaussian messageFromChildren;

		CondProbDistrib prior = var.getDistrib(world).getCPD();
		boolean priorIsUniformReal = prior instanceof blog.distrib.UniformReal;
		if (!priorIsUniformReal)
			return null;
		uniformRealPrior = (blog.distrib.UniformReal) prior;

		messageFromChildren = blog.distrib.Util
				.posteriorOnVarGivenChildrenWithGaussingDistributionWithVarAsMean(var,
						world);
		if (messageFromChildren == null)
			return null;

		return new SampleIterator(uniformRealPrior, messageFromChildren);
	}

	public static class SampleIterator extends EZIterator {
		public SampleIterator(UniformReal lastUniformRealPrior,
				UnivarGaussian lastMessageFromChildren) {
			this.uniformRealPrior = lastUniformRealPrior;
			this.messageFromChildren = lastMessageFromChildren;
		}

		public Object calculateNext() {
			double sample;
			do {
				sample = messageFromChildren.sampleVal();
			} while (sample < uniformRealPrior.getLower()
					|| sample > uniformRealPrior.getUpper());

			// this can be very inefficient if there is a large misalignment between
			// the truncated uniform
			// and the gaussian.

			return new WeightedValue(sample, 1.0);
		}

		public blog.distrib.UniformReal uniformRealPrior;
		public UnivarGaussian messageFromChildren;
	}
}
