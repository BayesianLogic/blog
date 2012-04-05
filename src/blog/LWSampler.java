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
import blog.common.Util;
import blog.model.Model;

/**
 * A likelihood weighting sampler. Instantiates the context-specifically active
 * ancestors of the query and evidence variables. Rather than sampling the
 * evidence variables, it just instantiates them to their observed values. The
 * weight of a sample is the product of the probabilities of the evidence
 * variables given their parents.
 * 
 * <p>
 * The LWSampler constructor looks at the following properties in the properties
 * table that is passed in:
 * <dl>
 * <dt>idTypes
 * <dd>Comma-separated list of names of types. Non-guaranteed objects of these
 * types will be represented by interchangeable identifiers rather than by
 * tuples. The value can also be "none", indicating that no objects should be
 * represented by identifiers, or "all", indicating that all non-guaranteed
 * objects should be represented by identifiers. Default: "none".
 * </dl>
 */
public class LWSampler extends Sampler {
	/**
	 * Creates a new sampler for the given BLOG model. The properties table
	 * specifies configuration parameters.
	 */
	public LWSampler(Model model, Properties properties) {
		super(model);

		String idTypesString = properties.getProperty("idTypes", "none");
		idTypes = model.getListedTypes(idTypesString);
		if (idTypes == null) {
			Util.fatalErrorWithoutStack("Invalid idTypes list.");
		}
	}

	public void initialize(Evidence evidence, List queries) {
		super.initialize(evidence, queries);
		for (Iterator iter = queries.iterator(); iter.hasNext();) {
			queryVars.addAll(((Query) iter.next()).getVariables());
		}

		numSamplesThisTrial = 0;
		numConsistentThisTrial = 0;
		sumWeightsThisTrial = 0;

		curWorld = null;
		weight = -1;
	}

	public void setBaseWorld(PartialWorld world) {
		baseWorld = world;
	}

	/**
	 * Generates the next partial world and computes its weight.
	 */
	public void nextSample() {
		double numerator;
		double denominator;
		int first_consistent = 0;
		int total_consistent = 0;
		boolean notYetConsistent = true;

		if (baseWorld != null)
			curWorld = baseWorld;
		else
			curWorld = new DefaultPartialWorld(idTypes);

		weight = calculateWeight();
		BLOGUtil.ensureDetAndSupportedWithListener(queryVars, curWorld,
				afterSamplingListener);

		if (Util.verbose()) {
			System.out.println("Generated world:");
			curWorld.print(System.out);
			System.out.println("Weight: " + weight);
		}

		++totalNumSamples;
		++numSamplesThisTrial;
		if (weight > 0) {
			++totalNumConsistent;
			++numConsistentThisTrial;
		}
		sumWeightsThisTrial += weight;
	}

	/**
	 * Calculates weight for evidence and current world.
	 */
	protected double calculateWeight() {
		return evidence.setEvidenceEnsureSupportedAndReturnLikelihood(curWorld);
	}

	public PartialWorld getLatestWorld() {
		if (curWorld == null) {
			throw new IllegalStateException("LWSampler has no latest sample.");
		}
		return curWorld;
	}

	public double getLatestWeight() {
		if (weight == -1) {
			throw new IllegalStateException("LWSampler has no latest sample.");
		}
		return weight;
	}

	/**
	 * Print statistics gathered during sampling to standard out. These figures
	 * are gathered during each call to sample(). This method should be called
	 * once at the end of each trial.
	 */
	public void printStats() {
		System.out.println("======== LW Trial Stats =========");

		if (numSamplesThisTrial > 0) {
			System.out.println("Average likelihood weight (this trial): "
					+ (sumWeightsThisTrial / (double) numSamplesThisTrial));
			System.out.println("Fraction of consistent worlds (this trial): "
					+ (numConsistentThisTrial / (double) numSamplesThisTrial));
		}

		if (totalNumSamples > 0) {
			System.out
					.println("Fraction of consistent worlds (running avg, all trials): "
							+ (totalNumConsistent / (double) totalNumSamples));
		} else {
			System.out.println("No samples yet.");
		}
	}

	private Set idTypes; // of Type
	private List<BayesNetVar> queryVars = new ArrayList<BayesNetVar>();

	protected PartialWorld curWorld = null;
	private PartialWorld baseWorld = null;
	private double weight = -1;

	// overall statistics
	private int totalNumSamples = 0;
	private int totalNumConsistent = 0;

	// statistics since last call to initialize()
	private int numSamplesThisTrial = 0;
	private int numConsistentThisTrial = 0;
	private double sumWeightsThisTrial = 0;
}
