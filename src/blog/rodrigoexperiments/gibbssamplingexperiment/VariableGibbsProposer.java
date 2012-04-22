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
package blog.rodrigoexperiments.gibbssamplingexperiment;

import java.util.*;

import blog.AbstractProposer;
import blog.bn.VarWithDistrib;
import blog.common.Util;
import blog.model.Model;
import blog.world.PartialWorldDiff;


/**
 * Proposer that uses {@link VariableImportanceSampler}s assumed to be Gibbs and
 * uses them at random.
 * 
 * @author Rodrigo
 */
public class VariableGibbsProposer extends AbstractProposer {

	public VariableGibbsProposer(Model model, Properties properties) {
		super(model, properties);
		this.sampler = new TruncatedUniformAndGaussianMCMCSampler();
	}

	@Override
	public double proposeNextState(PartialWorldDiff proposedWorld) {
		List basicVars = new LinkedList(proposedWorld.basicVarToValueMap().keySet());
		List variables = Util.sampleWithoutReplacement(basicVars, basicVars.size());
		// System.out.println("VarGibbsProp: all vars: " +
		// proposedWorld.basicVarToValueMap());
		removeVariablesWithoutDistribution(variables);
		// System.out.println("VarGibbsProp: vars: " + variables);

		Iterator sampleIt = null;
		VarWithDistrib var = null;
		ListIterator it = variables.listIterator();
		while (it.hasNext() && sampleIt == null) {
			var = (VarWithDistrib) it.next();
			// System.out.println("VarGibbsProp: var: " + var);
			// System.out.println("VarGibbsProp: world: " + proposedWorld);
			sampleIt = sampler.sampler(var, proposedWorld);
		}

		if (sampleIt == null)
			Util.fatalError("No variable is eligible for TruncatedUniformAndGaussianMCMCSampler");

		WeightedValue weightedValue = (WeightedValue) sampleIt.next();
		proposedWorld.setValue(var, weightedValue.value);
		proposedWorld.save();
		return 1.0; // sampler is Gibbs, so proposal ratio is 1.
	}

	private void removeVariablesWithoutDistribution(List variables) {
		ListIterator it = variables.listIterator();
		while (it.hasNext())
			if (!(it.next() instanceof VarWithDistrib))
				it.remove();
	}

	public VariableImportanceSampler sampler;
}
