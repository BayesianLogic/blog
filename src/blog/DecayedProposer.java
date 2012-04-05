/*
 * Copyright (c) 2005, Regents of the University of California
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

package blog;

import java.util.*;

import blog.common.Util;
import blog.model.Model;

/**
 * A DecayedProposer as defined by Bhaskara Marthi, Hanna Pasula, Stuart
 * Russell, Yuval Peres, "Decayed MCMC Filtering" (2002). It follows the method
 * used by {@link GenericProposer}, but differs in how it chooses the variable
 * to be sampled. Whereas GenericProposer chooses a variable uniformly from the
 * instantiated ones, DecayedProposer first decides whether it will choose a
 * temporal or an atemporal variable. If it decides for an atemporal variable,
 * then it chooses uniformly among them. If it decides for a temporal variable,
 * then it first chooses a timestep from which to sample, with probability
 * inverse-polynomially proportional to the distance between this timestep and
 * the current timestep. Then it samples uniformly from the temporal variables
 * with that timestep. The probability of choosing to pick an atemporal variable
 * is <code>atemporalVarFactor/(atemporalVarFactor + current timestep)</code>,
 * where <code>atemporalVarFactor</code> is provided in the Properties passed to
 * the constructor.
 * 
 * <p>
 * The DecayedProposer also accepts a property <code>maxRecall</code>, with
 * default <code>30</code>, that limits how many timesteps it looks back.
 */
public class DecayedProposer extends GenericProposer {

	/**
	 * Constructs a DecayedProposer based on the given model and properties.
	 * Besides those properties used by the super class {@link GenericProposer},
	 * DecayedProposer used the properties <code>maxRecall</code>, an integer
	 * determining how many timesteps back the proposer uses at most (default
	 * <code>30</code>), and <code>atemporalVarFactor</code>, a double indicating
	 * the weight of atemporal variables, relative to each timestep, in the choice
	 * of a variable to be sampled (with default <code>1.0</code>).
	 */
	public DecayedProposer(Model model, Properties properties) {
		super(model, properties);

		String maxRecallStr = properties.getProperty("maxRecall", "30");
		try {
			maxRecall = Integer.parseInt(maxRecallStr);
			inversePolynomialSampler = new Util.InversePolynomialSampler(maxRecall);
		} catch (NumberFormatException e) {
			Util.fatalErrorWithoutStack("Invalid maximum recall: " + maxRecallStr);
		}

		String atemporalVarFactorStr = properties.getProperty("atemporalVarFactor",
				"1.0");

		try {
			atemporalVarFactor = Double.parseDouble(atemporalVarFactorStr);
		} catch (NumberFormatException e) {
			Util.fatalErrorWithoutStack("Invalid atemporal variable factor: "
					+ atemporalVarFactorStr);
		}
	}

	protected PickVarToSampleResult pickVarToSample(PartialWorldDiff world) {

		int maxTime = getMaxTime(world);

		getTemporalAndMetaVariables(world, maxTime);

		int temporalSize = 0;
		for (int i = 0; i < temporalVars.length; i++) {
			if (temporalVars[i] != null) {
				temporalVars[i].removeAll(evidenceVars);
				temporalSize += temporalVars[i].size();
			}
		}
		metaVars.removeAll(evidenceVars);

		// Uniformly sample a random variable from the support network.
		VarWithDistrib varToSample;
		double roll = Util.random();
		if (temporalSize + metaVars.size() == 0) // nothing instantiated
			varToSample = null;
		else if (roll < atemporalVarFactor / (atemporalVarFactor + maxTime)
				&& metaVars.size() > 0)
			varToSample = (VarWithDistrib) Util.uniformSample(metaVars);
		else
			varToSample = pickTemporalVariableToSample(maxTime, temporalVars);

		if (varToSample instanceof NumberVar)
			return null;

		return new PickVarToSampleResult(varToSample, temporalSize
				+ metaVars.size());
	}

	private void getTemporalAndMetaVariables(PartialWorldDiff world, int maxTime) {
		metaVars = new HashSet();
		int numberOfSlices = numberOfTimeSlicesToSampleFrom(maxTime);
		temporalVars = new Set[numberOfSlices];

		for (Iterator it = world.getInstantiatedVars().iterator(); it.hasNext();) {
			VarWithDistrib var = (VarWithDistrib) it.next();
			if (var.timestep() == null)
				metaVars.add(var);
			else {
				int varTimestep = var.timestep().getValue();

				int indexInArray = numberOfSlices - 1 - (maxTime - varTimestep);

				if (indexInArray >= 0) {
					if (temporalVars[indexInArray] == null)
						temporalVars[indexInArray] = new HashSet();
					temporalVars[indexInArray].add(var);
				}
			}
		}
		// TODO: This is wasteful in several ways. We can avoid
		// recomputing much of this, maybe by placing listeners in the
		// PartialWorld.
	}

	/**
	 * Picks a temporal variable uniformly from a time slice, the index of which
	 * is chosen proportionally to a backward inverse polynomial decay from
	 * <code>maxTime</code>.
	 * 
	 * @param maxTime
	 *          the latest timestep index.
	 * @param temporalVars
	 *          a circular queue with the temporal random variables in each
	 *          timestep;
	 *          <code>temporalVars[t % maxRecall]<code> contains the random variables indexed by t.
	 * @return the <code>BasicVar</code> chosen to be sampled.
	 */
	private VarWithDistrib pickTemporalVariableToSample(int maxTime,
			Set[] temporalVars) {

		int numberOfTimestepsBack = inversePolynomialSampler.nextSample()
				% numberOfTimeSlicesToSampleFrom(maxTime);
		int timestepIndex = maxTime - numberOfTimestepsBack;
		int temporalVarsIndex = timestepIndex;

		if (temporalVars[temporalVarsIndex] == null)
			return null;
		VarWithDistrib varToSample = (VarWithDistrib) Util
				.uniformSample(temporalVars[temporalVarsIndex]);
		return varToSample;
	}

	private int getMaxTime(PartialWorld world) {
		int maxTime = -1;
		for (Iterator it = world.getInstantiatedVars().iterator(); it.hasNext();) {
			VarWithDistrib var = (VarWithDistrib) it.next();
			if (var.timestep() != null) {
				int varTimestep = var.timestep().getValue();
				if (varTimestep > maxTime)
					maxTime = varTimestep;
			}
		}
		return maxTime;
		// TODO: seems like we ought to do better than finding this value every
		// time.
	}

	private int numberOfTimeSlicesToSampleFrom(int maxTime) {
		return Math.min(maxRecall, maxTime + 1);
	}

	public int getMaxRecall() {
		return maxRecall;
	}

	private Set[] temporalVars;
	private Set metaVars;

	private static double atemporalVarFactor;
	protected static int maxRecall;
	private static Util.InversePolynomialSampler inversePolynomialSampler;
}
