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
package expmt.rodrigoexperiments.gibbssamplingexperiment;

import java.util.*;

import blog.BLOGUtil;
import blog.bn.RandFuncAppVar;
import blog.common.Util;
import blog.model.Evidence;
import blog.model.Model;
import blog.sample.MHSampler;
import blog.world.DefaultPartialWorld;
import blog.world.PartialWorld;
import blog.world.PartialWorldDiff;

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
