package blog.rodrigoexperiments.gibbssamplingexperiment;

import java.util.*;
import blog.PartialWorld;
import blog.bn.VarWithDistrib;
import blog.common.EZIterator;
import blog.distrib.*;

/*
 * Copyright (c) 2005, 2007, 2008, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.  
 *
 * * Neither the name of the University of California, Berkeley nor
 *   the names of its contributors may be used to endorse or promote
 *   products derived from this software without specific prior 
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
