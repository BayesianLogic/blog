/*
 * Copyright (c) 2006, Regents of the University of California
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

package blog.distrib;

import java.util.*;
import Jama.Matrix;
import blog.*;
import blog.common.Util;
import blog.model.Type;

/**
 * CPD that takes a sequence of probabilities as parameters, and uses those
 * probabilities to define a distribution over its arguments. The number of
 * arguments must equal the number of probability parameters. Alternatively, a
 * ChooseFromArgs CPD can be constructed with no parameters, in which case it
 * expects a column vector of probabilities as its first argument and chooses
 * from its remaining arguments.
 */
public class ChooseFromArgs extends AbstractCondProbDistrib {
	/**
	 * Creates a ChooseFromArgs CPD with the given parameter vector.
	 */
	public ChooseFromArgs(double[] probs) {
		this.probs = (double[]) probs.clone();
		expectProbsAsArg = false;
	}

	/**
	 * Creates a new ChooseFromArgs distribution from the given parameter list. If
	 * the parameter list is empty, then the probability vector must be passed as
	 * an argument to this CPD. Otherwise, the parameter list should contain the
	 * probability vector as a sequence of Number objects.
	 */
	public ChooseFromArgs(List params) {
		if (!params.isEmpty()) {
			probs = new double[params.size()];
			for (int i = 0; i < params.size(); ++i) {
				probs[i] = ((Number) params.get(i)).doubleValue();
			}
			expectProbsAsArg = false;
		}
	}

	/**
	 * Returns the probability of the given child value conditioned on the given
	 * argument values. If this distribution was constructed with a probability
	 * vector, then the arguments are the possible values. Otherwise, the first
	 * argument should be a column vector of probabilities and the remaining
	 * arguments should be the possible values.
	 * 
	 * @throws IllegalArgumentException
	 *           if the probability vector was not specified at construction and
	 *           the first argument is not a column vector of probabilities
	 */
	public double getProb(List args, Object childValue) {
		int firstPossibleValueIndex = ensureProbsInited(args);

		for (int i = 0; i < probs.length; ++i) {
			if (childValue.equals(args.get(firstPossibleValueIndex + i))) {
				return probs[i];
			}
		}
		return 0;
	}

	/**
	 * Returns a value sampled according to this distribution. If this
	 * distribution was constructed with a probability vector, then the arguments
	 * are the possible values. Otherwise, the first argument should be a column
	 * vector of probabilities and the remaining arguments should be the possible
	 * values.
	 * 
	 * @throws IllegalArgumentException
	 *           if the probability vector was not specified at construction and
	 *           the first argument is not a column vector of probabilities
	 */
	public Object sampleVal(List args, Type childType) {
		int firstPossibleValueIndex = ensureProbsInited(args);

		int i = Util.sampleWithProbs(probs);
		return args.get(firstPossibleValueIndex + i);
	}

	private int ensureProbsInited(List args) {
		int firstPossibleValueIndex = 0;
		if (expectProbsAsArg) {
			if (args.isEmpty()) {
				throw new IllegalArgumentException(
						"First argument to ChooseFromArgs CPD should be a "
								+ "column vector of probabilities, since the "
								+ "probabilities were not specified as CPD parameters.");
			}

			if ((!(args.get(0) instanceof Matrix))
					|| (((Matrix) args.get(0)).getColumnDimension() != 1)) {
				throw new IllegalArgumentException(
						"First argument to ChooseFromArgs CPD should be a column "
								+ "vector of probabilities, not: " + args.get(0));
			}

			Matrix m = (Matrix) args.get(0);
			probs = new double[m.getRowDimension()];
			for (int i = 0; i < probs.length; ++i) {
				probs[i] = m.get(i, 0);
			}
			firstPossibleValueIndex = 1;
		}

		if (args.size() - firstPossibleValueIndex != probs.length) {
			throw new IllegalArgumentException(
					"ChooseFromArgs CPD with probability vector of length "
							+ probs.length + " should be given " + probs.length
							+ " arguments to choose from, not "
							+ (args.size() - firstPossibleValueIndex));
		}

		return firstPossibleValueIndex;
	}

	private boolean expectProbsAsArg = true;
	private double[] probs = null;
}
