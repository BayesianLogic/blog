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

import blog.bn.BayesNetVar;
import blog.bn.VarWithDistrib;
import blog.common.HashMultiMap;
import blog.common.MultiMap;
import blog.common.Util;
import blog.model.DependencyModel;
import blog.model.Model;
import blog.sample.ClassicInstantiatingEvalContext;
import blog.sample.DefaultEvalContext;
import blog.world.PartialWorld;
import blog.world.PartialWorldDiff;

/**
 * Implements a generic MCMC proposal algorithm. Each step changes the value of
 * exactly one instantiated RV (selected uniformly at random from the
 * instantiated RVs), then samples values for previously uninstantiated RVs to
 * make the resulting instantiation self-supporting. It also uninstantiates any
 * RVs that become barren.
 * 
 * <p>
 * Each variable is sampled from its prior distribution given its parents. The
 * forward proposal probability is just the probability of choosing the selected
 * variable to change, times the product of the probabilities of values sampled
 * (for that variable and any other variables that must be instantiated to make
 * the instantiation self-supporting). The backward probability is the
 * probability of choosing the same variable again, choosing its old value, and
 * re-instantiating all the newly uninstantiated variables to their old values.
 * No other moves can yield the same proposed world because each move changes
 * the value of exactly one instantiated variable (it just instantiates and
 * uninstantiates other variables).
 * 
 * <p>
 * GenericProposer does not use identifiers in the worlds it constructs.
 */
public class GenericProposer extends AbstractProposer {

	/**
	 * Creates a new GenericProposer that proposes possible worlds for the given
	 * model. The properties table, specifying configuration parameters, is passed
	 * to the LWSampler that is used to generate the initial world.
	 */
	public GenericProposer(Model model, Properties properties) {
		super(model, properties);
	}

	protected class PickVarToSampleResult {
		public VarWithDistrib varToSample;
		public int numberOfChoices;

		public PickVarToSampleResult(VarWithDistrib varToSample, int numberOfChoices) {
			this.varToSample = varToSample;
			this.numberOfChoices = numberOfChoices;
		}
	}

	protected PickVarToSampleResult pickVarToSample(PartialWorld world) {
		Set eligibleVars = new HashSet(world.getInstantiatedVars());
		eligibleVars.removeAll(evidenceVars);

		// Uniformly sample a random variable from the support network.
		VarWithDistrib varToSample = (VarWithDistrib) Util
				.uniformSample(eligibleVars);

		return new PickVarToSampleResult(varToSample, eligibleVars.size());
	}

	/**
	 * Proposes a next state for the Markov chain given the current state. The
	 * proposedWorld argument is a PartialWorldDiff that the proposer can modify
	 * to create the proposal; the saved version of this PartialWorldDiff is the
	 * state before the proposal. Returns the log proposal ratio: log (q(x | x') /
	 * q(x' | x))
	 */
	public double proposeNextState(PartialWorldDiff world) {
		if (evidence == null) {
			throw new IllegalStateException(
					"initialize() has not been called on proposer.");
		}

		logProbForward = 0;
		logProbBackward = 0;

		PickVarToSampleResult result = pickVarToSample(world);

		if (result.varToSample == null)
			return 1.0;

		if (Util.verbose())
			System.out.println("  sampling " + result.varToSample);

		// Multiply in the probability of this uniform sample.
		logProbForward += (-Math.log(result.numberOfChoices));

		// System.out.println("GenericProposer: world right before sampling new value for "
		// + result.varToSample + ".\n");
		// System.out.println(world);

		// Sample value for variable and update forward and backward probs
		sampleValue(result.varToSample, world);

		// System.out.println("GenericProposer: world right before getting newly barren vars.\n");
		// System.out.println(world);

		// Remove barren variables
		LinkedList newlyBarren = new LinkedList(world.getNewlyBarrenVars());
		while (!newlyBarren.isEmpty()) {
			BayesNetVar var = (BayesNetVar) newlyBarren.removeFirst();
			if (!evidenceVars.contains(var) && !queryVars.contains(var)) {

				// Remember its parents.
				Set parentSet = world.getBayesNet().getParents(var);

				if (var instanceof VarWithDistrib) {
					// Multiply in the probability of sampling this
					// variable again. Since the parent value may have
					// changed, must use the old world.
					logProbBackward += world.getSaved().getLogProbOfValue(var);

					// Uninstantiate
					world.setValue((VarWithDistrib) var, null);
				}

				// Check to see if its parents are now barren.
				for (Iterator parentIter = parentSet.iterator(); parentIter.hasNext();) {

					// If parent is barren, add to the end of this
					// linked list. Note that if a parent has two
					// barren children, it will only be added to the
					// end of the list once, when the last child is
					// considered.
					BayesNetVar parent = (BayesNetVar) parentIter.next();
					if (world.getBayesNet().getChildren(parent).isEmpty())
						newlyBarren.addLast(parent);
				}
			}
		}

		// Uniform sampling from new world.
		logProbBackward += (-Math.log(world.getInstantiatedVars().size()
				- numBasicEvidenceVars));
		return (logProbBackward - logProbForward);
	}

	// Samples a new value for the given variable (which must be
	// supported in <code>world</code>) and sets this new value as the
	// value of the variable in <code>world</code>. Then ensures that
	// <code>world</code> is self-supporting by calling
	// ensureDetAndSupported on each of the variable's children. Also
	// updates the logProbForward and logProbBackward variables.
	private void sampleValue(VarWithDistrib varToSample, PartialWorld world) {
		// Save child set before graph becomes out of date
		Set children = world.getBayesNet().getChildren(varToSample);

		DependencyModel.Distrib distrib = varToSample
				.getDistrib(new DefaultEvalContext(world, true));
		Object oldValue = world.getValue(varToSample);
		logProbBackward += Math.log(distrib.getCPD().getProb(
				distrib.getArgValues(), oldValue));

		Object newValue = distrib.getCPD().sampleVal(distrib.getArgValues(),
				varToSample.getType());
		world.setValue(varToSample, newValue);
		logProbForward += Math.log(distrib.getCPD().getProb(distrib.getArgValues(),
				newValue));

		// Make the world self-supporting. The only variables whose active
		// parent sets could have changed are the children of varToSample.
		ClassicInstantiatingEvalContext instantiator = new ClassicInstantiatingEvalContext(
				world);

		for (Iterator childrenIter = children.iterator(); childrenIter.hasNext();) {
			BayesNetVar child = (BayesNetVar) childrenIter.next();
			if (!world.isInstantiated(child)) // NOT SURE YET THIS IS THE RIGHT THING
																				// TO DO! CHECKING WITH BRIAN.
				continue;
			child.ensureDetAndSupported(instantiator);
		}

		logProbForward += instantiator.getLogProbability();
	}
}
