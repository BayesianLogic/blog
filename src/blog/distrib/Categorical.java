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

package blog.distrib;

import blog.*;
import java.util.*;
import Jama.Matrix;
import common.Util;

/**
 * Distribution over a finite set of possible values numbered 0, ..., k-1, 
 * parameterized by a vector of probabilities pi<sub>0</sub>, ..., 
 * pi<sub>k-1</sub> that sum to one.  The possible values can be the 
 * user-defined guaranteed objects of some type, or a finite prefix of the 
 * natural numbers.   The probability vector can be specified 
 * when the distribution is constructed, or as an argument to the 
 * getProb and sampleVal methods (in the form of a column vector).  
 */
public class Categorical extends AbstractCondProbDistrib {
    /**
     * Value for constructor's <code>flag</code> argument indicating 
     * that all flags are false.
     */
    public static final int NO_FLAGS = 0;

    /**
     * Value that can be bitwise or'ed with a <code>flag</code> argument 
     * to indicate that the given weights are normalized (sum to 1).
     */
    public static final int NORMALIZED = 1 << 0;

    /**
     * Value that can be bitwise or'ed with a <code>flag</code> argument 
     * to indicate that the weights are given as (natural) logs.
     */
    public static final int LOG = 1 << 1;

    /**
     * Value that can be bitwise or'ed with a <code>flag</code> argument 
     * to indicate that the weights are sorted in non-increasing order 
     * (largest first).  This allows log weights to be normalized slightly 
     * faster (the code divides each weight by the largest one before 
     * exponentiating, to avoid underflow).  
     */
    public static final int SORTED = 1 << 2;

    /**
     * Creates a new Categorical distribution representing the uniform 
     * distribution over the given number of values.
     */
    public Categorical(int numValues) {
	probs = new double[numValues];
	Arrays.fill(probs, 1.0 / numValues);
	expectProbsAsArg = false;
    }

    /**
     * Creates a new Categorical distribution with the given probability 
     * vector.  The number of values is the length of this vector.
     */
    public Categorical(double[] probs) {
	this.probs = (double[]) probs.clone();
	expectProbsAsArg = false;
    }

    /**
     * Creates a new Categorical distribution with the given probability 
     * weights.  The <code>flags</code> argument is treated as a bit 
     * vector indicating what assumptions can be made about the given 
     * weights.  The possible flags are public static fields of the 
     * Categorical class.    
     */
    public Categorical(double[] weights, int flags) {
	this(weights); // sets probs to be a copy of weights

	if ((flags & NORMALIZED) == 0) {
	    // have to normalize

	    if ((flags & LOG) != 0) {
		// Normalize weights and take them out of the log domain.  
		// To avoid underflow when exponentiating, we subtract 
		// the maximum weight from each weight.
		double maxWeight = (weights.length == 0) ? 1 : weights[0];
		if ((flags & SORTED) == 0) {
		    for (int i = 1; i < weights.length; ++i) {
			if (weights[i] > maxWeight) {
			    maxWeight = weights[i];
			}
		    }
		}

		double scaledSum = 0; // scaledSum = sum / exp(maxWeight)
		for (int i = 0; i < weights.length; ++i) {
		    scaledSum += Math.exp(weights[i] - maxWeight);
		}

		// sum = scaledSum * exp(maxWeight)
		double logNormFactor = Math.log(scaledSum) + maxWeight;

		for (int i = 0; i < weights.length; ++i) {
		    probs[i] = Math.exp(weights[i] - logNormFactor);
		}
	    } else {
		// Normalize weights, no logs.  
		double sum = 0;
		for (int i = 0; i < probs.length; ++i) {
		    sum += probs[i];
		}

		for (int i = 0; i < probs.length; ++i) {
		    probs[i] /= sum;
		}
	    }
	} else {
	    // Weights are already normalized; just see if we have to 
	    // take them out of log domain.
	    if ((flags & LOG) != 0) {
		for (int i = 0; i < probs.length; ++i) {
		    probs[i] = Math.exp(probs[i]);
		}
	    }
	}
    }

    /**
     * Creates a new Categorical distribution from the given parameter 
     * list.  If the parameter list is empty, then the probability vector 
     * must be passed as an argument to this CPD.  Otherwise, the parameter 
     * list should contain the probability vector as a sequence of Number 
     * objects.
     */
    public Categorical(List params) {
	if (!params.isEmpty()) {
	    probs = new double[params.size()];
	    for (int i = 0; i < params.size(); ++i) {
		probs[i] = ((Number) params.get(i)).doubleValue();
	    }
	    expectProbsAsArg = false;
	}
    }

    /**
     * Returns the number of values to which this distribution explicitly 
     * assigns a probability.
     *
     * @throws IllegalStateException    if the probability vector was not 
     *                                  specified when this distribution was 
     *                                  constructed
     */
    public int getNumValues() {
	if (expectProbsAsArg) {
	    throw new IllegalStateException
		("Categorical distribution was constructed without a "
		 + "probability vector.");
	}

	return probs.length;
    }

    /**
     * Returns the probability of the value with the given index under 
     * this distribution.  If the given index is greater than the length 
     * of this distribution's probability vector, then the return value is 
     * zero.  
     *
     * @throws IllegalStateException    if the probability vector was not 
     *                                  specified when this distribution was 
     *                                  constructed
     *
     * @throws IllegalArgumentException if <code>index</code> is negative
     */
    public double getProb(int index) {
	if (expectProbsAsArg) {
	    throw new IllegalStateException
		("Categorical distribution was constructed without a "
		 + "probability vector.");
	}

	if (index < 0) {
	    throw new IllegalArgumentException
		("Negative index passed to Categorical.getProb: " + index);
	}
	if (index >= probs.length) {
	    return 0.0;
	}
	return probs[index];
    }

    /**
     * Returns the log probability of the value with the given index under 
     * this distribution.  If the given index is greater than the length 
     * of this distribution's probability vector, then the return value is 
     * Double.NEGATIVE_INFINITY.
     *
     * @throws IllegalStateException    if the probability vector was not 
     *                                  specified when this distribution was 
     *                                  constructed
     *
     * @throws IllegalArgumentException if <code>index</code> is negative
     */
    public double getLogProb(int index) {
	if (expectProbsAsArg) {
	    throw new IllegalStateException
		("Categorical distribution was constructed without a "
		 + "probability vector.");
	}

	if (index < 0) {
	    throw new IllegalArgumentException
		("Negative index passed to Categorical.getLogProb: " + index);
	}
	if (index >= probs.length) {
	    return Double.NEGATIVE_INFINITY;
	}
	return Math.log(probs[index]);
    }

    /**
     * Returns the probability of the given child value conditioned on 
     * the given argument values.  If this distribution was constructed with 
     * a probability vector, then no arguments are expected.  Otherwise, 
     * there should be one argument, namely a column vector of probabilities.
     * 
     * @return the probability associated with <code>childValue</code>'s 
     *         index, or 0 if <code>childValue</code> does not have an index 
     *         or its index is beyond the end of the probability vector
     *
     * @throws IllegalArgumentException  if the probability vector was 
     *                                   specified at construction and 
     *                                   <code>args</code> is non-empty, or 
     *                                   if the probability vector was not 
     *                                   specified and <code>args</code> is 
     *                                   empty
     */
    public double getProb(List args, Object childValue) {
	ensureProbsInited(args);

	int index = Model.getObjectIndex(childValue);
	if ((index == -1) || (index >= probs.length)) {
	    return 0;
	}
	return probs[index];
    }

    /**
     * Returns an index sampled from this distribution.
     *
     * @throws IllegalStateException    if the probability vector was not 
     *                                  specified when this distribution was 
     *                                  constructed
     */
    public int sampleVal() {
	if (expectProbsAsArg) {
	    throw new IllegalStateException
		("Categorical distribution was constructed without a "
		 + "probability vector.");
	}

	return Util.sampleWithProbs(probs);
    }

    /**
     * Returns a value sampled from the given child type according to this 
     * distribution.  If this distribution was constructed with 
     * a probability vector, then no arguments are expected.  Otherwise, 
     * there should be one argument, namely a column vector of probabilities.
     * 
     * @return a guaranteed object of the given type whose index is sampled 
     *         according to this distribution, or Model.NULL if the index 
     *         sampled is beyond the end of the guaranteed object list for 
     *         the given type
     *
     * @throws IllegalArgumentException  if the probability vector was 
     *                                   specified at construction and 
     *                                   <code>args</code> is non-empty, or 
     *                                   if the probability vector was not 
     *                                   specified and <code>args</code> is 
     *                                   empty
     */
    public Object sampleVal(List args, Type childType) {
	ensureProbsInited(args);

	int index = Util.sampleWithProbs(probs);
	Object value = childType.getGuaranteedObject(index);
	if (value == null) {
	    // make list so we can print the probabilities easily
	    List probList = new ArrayList();
	    for (int i = 0; i < probs.length; ++i) {
		probList.add(new Double(probs[i]));
	    }
	    System.err.println("Warning: distribution does not sum to 1 over "
			       + "the guaranteed objects of type " + childType
			       + ": " + probList);
	    value = Model.NULL;
	}
	return value;
    }

    private void ensureProbsInited(List args) {
	if (expectProbsAsArg) {
	    if (args.isEmpty()) {
		throw new IllegalArgumentException
		    ("Arguments to Categorical CPD should consist of a "
		     + "probability vector, since the probabilities were not "
		     + "specified as CPD parameters.");
	    }
	    
	    if ((!(args.get(0) instanceof Matrix))
		    || (((Matrix) args.get(0)).getColumnDimension() != 1)) {
		throw new IllegalArgumentException
		    ("Argument to Categorical CPD should be a column "
		     + "vector of probabilities, not: " + args.get(0));
	    }

	    Matrix m = (Matrix) args.get(0);
	    probs = new double[m.getRowDimension()];
	    for (int i = 0; i < probs.length; ++i) {
		probs[i] = m.get(i, 0);
	    }
	} else if (!args.isEmpty()) {
	    throw new IllegalArgumentException
		("Categorical CPD expects no arguments (probabilities were "
		 + "specified as CPD parameters).");
	}
    }

    boolean expectProbsAsArg = true;
    private double[] probs;
}
