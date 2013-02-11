/*
 * Copyright (c) 2005, 2006, Regents of the University of California
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

package blog.sample;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import blog.bn.BayesNetVar;
import blog.bn.VarWithDistrib;
import blog.common.Util;
import blog.model.DependencyModel;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.Query;
import blog.world.PartialWorld;
import blog.world.UninstVarIterator;
import blog.world.WorldInProgress;

/**
 * Generates a partial world by sampling basic random variables in an order that
 * does not depend on the evidence or queries, but only on which basic RVs are
 * supported by the instantiation created so far. If it was initialized with the
 * usual <code>initialize</code> method, the RejectionSampler stops when all the
 * query and evidence variables have been instantiated. If it was initialized
 * with <code>initializeCompleteSampling</code>, however, it keeps going until
 * it has instantiated all the basic RVs whose arguments exist in the generated
 * world.
 * 
 * <p>
 * The RejectionSampler constructor looks for the following properties in the
 * properties table:
 * 
 * <dl>
 * <dt>intBound
 * <dd>The sampler will not instantiate any random variables with integer (or
 * natural number) arguments greater in magnitude then the specified integer.
 * The bound is ignored if it is negative (so 0 is always allowed as an
 * argument).
 * 
 * <dt>depthBound
 * <dd>The sampler will not instantiate any random variables whose arguments
 * include non-guaranteed objects of depth greater than the specified integer.
 * The depth of an object generated by the empty tuple is 0; for other
 * non-guaranteed objects, the depth is one greater than the maximum depth of
 * their generating objects. This bound is ignored if it is negative (so objects
 * of depth 0 are always allowed as arguments).
 * </dl>
 * 
 * These bounding flags may prevent the sampler from instantiating evidence or
 * query variables, if those variables depend on variables with "large"
 * arguments. In such cases, the sampler will print an error message and exit
 * the program.
 */
public class RejectionSampler extends Sampler {
	/**
	 * Creates a new RejectionSampler for the given model, with configuration
	 * parameters specified by the given properties table.
	 */
	public RejectionSampler(Model model, Properties properties) {
		super(model);

		String intBoundStr = properties.getProperty("intBound");
		if (intBoundStr != null) {
			try {
				intBound = Integer.parseInt(intBoundStr);
			} catch (NumberFormatException e) {
				Util.fatalError("Invalid intBound: " + intBoundStr);
			}
		}

		String depthBoundStr = properties.getProperty("depthBound");
		if (depthBoundStr != null) {
			try {
				depthBound = Integer.parseInt(depthBoundStr);
			} catch (NumberFormatException e) {
				Util.fatalError("Invalid depthBound: " + depthBoundStr);
			}
		}
	}

	public void initialize(Evidence evidence, List queries) {
		super.initialize(evidence, queries);
		requireComplete = false;

		numSamplesThisTrial = 0;
		numAcceptedThisTrial = 0;

		curWorld = null;
	}

	/**
	 * Alternative initialization method that does not specify any evidence or
	 * queries, but tells this object to generate complete worlds (possibly up to
	 * the bounds specified by the intBound and depthBound properties).
	 */
	public void initializeCompleteSampling() {
		super.initialize(new Evidence(), Collections.EMPTY_LIST);
		requireComplete = true;

		numSamplesThisTrial = 0;
		numAcceptedThisTrial = 0;

		curWorld = null;
	}

	public void setBaseWorld(PartialWorld world) {
		Util.fatalError("setBaseWorld not implemented for RejectionSampler.");
	}

	public void nextSample() {
		if (Util.verbose()) {
			System.out.println();
			System.out.println("Sampling world...");
		}
		curWorld = new WorldInProgress(model, evidence, intBound, depthBound);

		// Find the first variable that is supported but not instantiated.
		// Instantiate it. Repeat until all evidence and query variables
		// are determined.
		while (!isCurWorldSufficient()) {
			boolean varInstantiated = false;

			if (curWorld.isComplete()) {
				Util.fatalError("World is complete (up to specified integer "
						+ "and depth bounds) but does not determine "
						+ "values for evidence and queries.", false);
			}

			for (UninstVarIterator iter = curWorld.uninstVarIterator(); iter
					.hasNext();) {
				VarWithDistrib var = (VarWithDistrib) iter.next();
				DependencyModel.Distrib distrib = var
						.getDistrib(new DefaultEvalContext(curWorld, false));
				if (distrib == null) {
					if (Util.verbose()) {
						System.out.println("Not supported yet: " + var);
					}
				} else {
					// var is supported
					if (Util.verbose()) {
						System.out.println("Instantiating: " + var);
					}
					iter.setValue(distrib.getCPD().sampleVal(distrib.getArgValues(),
							var.getType()));

					varInstantiated = true;
					break; // start again from first uninstantiated var
				}
			}

			if (!varInstantiated) {
				String msg = ("World is not complete, but no basic random "
						+ "variable is supported.  Please check for "
						+ "a possible cycle in your model.");
				if ((intBound >= 0) || (depthBound >= 0)) {
					msg = (msg + "  This problem could also be caused by " + "the intBound and depthBound flags.");
				}
				Util.fatalError(msg, false);
			}
		}

		// See if world happens to satisfy the evidence
		curWorldAccepted = evidence.isTrue(curWorld);

		++numSamplesThisTrial;
		if (curWorldAccepted) {
			++numAcceptedThisTrial;
		}
	}

	public PartialWorld getLatestWorld() {
		if (curWorld == null) {
			throw new IllegalStateException("Sampler has no latest sample.");
		}
		return curWorld;
	}

	public double getLatestWeight() {
		if (curWorld == null) {
			throw new IllegalStateException("Sampler has no latest sample.");
		}
		return (curWorldAccepted ? 1.0 : 0.0);
	}

	public void printStats() {
		System.out.println("=== Rejection Sampler Trial Stats ===");
		if (numSamplesThisTrial > 0) {
			System.out.println("Fraction of worlds accepted (this trial): "
					+ (numAcceptedThisTrial / (double) numSamplesThisTrial));
		} else {
			System.out.println("No samples this trial.");
		}
	}

	private boolean isCurWorldSufficient() {
		if (requireComplete) {
			return curWorld.isComplete();
		}

		if (!evidence.isDetermined(curWorld)) {
			return false;
		}

		for (Iterator iter = queries.iterator(); iter.hasNext();) {
			Query q = (Query) iter.next();
			for (BayesNetVar var : q.getVariables()) {
				if (!var.isDetermined(curWorld)) {
					return false;
				}
			}
		}

		return true;
	}

	private int intBound = -1;
	private int depthBound = -1;
	private boolean requireComplete = false;

	private WorldInProgress curWorld;
	private boolean curWorldAccepted;

	private int numSamplesThisTrial;
	private int numAcceptedThisTrial;
}
