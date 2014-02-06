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

package blog.distrib;

import java.io.Serializable;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import blog.common.numerical.JamaMatrixLib;
import blog.common.numerical.MatrixLib;
import blog.common.Util;
import blog.distrib.AbstractCondProbDistrib;
import blog.model.MatrixSpec;
import blog.model.Type;

/**
 * Multinomial distribution.
 *
 * See https://en.wikipedia.org/wiki/Multinomial_distribution
 */
public class Multinomial extends AbstractCondProbDistrib {

    private void init(int numTrials, double[] pi) {
        this.numTrials = numTrials;
        this.pi = (double[]) pi.clone();
        double sum = 0;
        for (int i = 0; i < pi.length; i++) {
            if (pi[i] < 0) {
                throw new IllegalArgumentException("Probability " + pi[i]
                        + " for element " + i + " is negative.");
            }
            sum += pi[i];
        }
        if (sum < 1e-9) {
            throw new IllegalArgumentException("Probabilities sum to approx zero");
        }
        for (int i = 0; i < pi.length; i++) {
            this.pi[i] /= sum;
        }
    }

    /**
     * Creates a Multinomial object with probabilities specified by the given
     * array.
     *
     * @throws IllegalArgumentException
     *           if pi does not define a probability distribution
     */
    public Multinomial(int numTrials, double[] pi) {
        init(numTrials, pi);
    }

	public Multinomial(List params) {
        if (params.size() != 2) {
            throw new IllegalArgumentException("expected numTrials and pi");
        }

        if (!(params.get(0) instanceof Integer)) {
            throw new IllegalArgumentException("expected first arg to be integer numTrials");
        }
        int numTrials = (Integer) params.get(0);

        Object objectPi = params.get(1);
		if (objectPi instanceof MatrixSpec) {
			objectPi = ((MatrixSpec) objectPi).getValueIfNonRandom();
		}
        if (!(objectPi instanceof MatrixLib)) {
            throw new IllegalArgumentException(
                "expected second arg to be array of reals; got " + objectPi +
                " instead, which is of type " + objectPi.getClass().getName());
        }
        MatrixLib pi = (MatrixLib) objectPi;
        if (pi.colLen() != 1) {
            throw new IllegalArgumentException("expected second arg to be column vector");
        }
        double[] nativePi = new double[pi.rowLen()];
        for (int i = 0; i < pi.rowLen(); i++) {
            nativePi[i] = pi.elementAt(i, 0);
        }

        init(numTrials, nativePi);
    }

	/**
	 * Returns the probability of given vector.
	 */
	public double getProb(List args, Object value) {
		if (!(value instanceof MatrixLib)) {
			throw new IllegalArgumentException("expected vector value");
		}
        final int numBuckets = pi.length;
        MatrixLib valueVector = (MatrixLib) value;
        if (valueVector.rowLen() != numBuckets || valueVector.colLen() != 1) {
            throw new IllegalArgumentException("value has wrong dimension");
        }
        int sum = 0;
        for (int i = 0; i < numBuckets; i++) {
            sum += valueVector.elementAt(i, 0);
        }
        if (sum != numTrials) {
            return 0;
        }
        double prob = Util.factorial(numTrials);
        for (int i = 0; i < numBuckets; i++) {
            prob *= Math.pow(pi[i], valueVector.elementAt(i, 0));
            prob /= Util.factorial((int)Math.round(valueVector.elementAt(i, 0)));
            // FIXME: It would be better if we could take the param as an array
            // of ints, so we don't have to worry about rounding.
        }
        return prob;
	}

    /**
	 * Returns the log probability of given vector.
     */
    public double getLogProb(List args, Object value) {
		if (!(value instanceof MatrixLib)) {
			throw new IllegalArgumentException("expected vector value");
		}
        MatrixLib valueVector = (MatrixLib) value;
        if (valueVector.rowLen() != numTrials) {
            throw new IllegalArgumentException("value has wrong dimension");
        }
        int sum = 0;
        for (int i = 0; i < numTrials; i++) {
            sum += valueVector.elementAt(i, 0);
        }
        if (sum != numTrials) {
            return 0;
        }
        double logProb = Util.logFactorial(numTrials);
        for (int i = 0; i < numTrials; i++) {
            logProb += valueVector.elementAt(i, 0) * Math.log(pi[i]);
            logProb -= Util.logFactorial((int)Math.round(valueVector.elementAt(i, 0)));
        }
        return logProb;
    }

    /**
     * Returns a vector chosen at random according to this distribution.
     */
    public MatrixLib sampleVal(List args, Type childType) {
        final int numBuckets = pi.length;
        double[] cdf = new double[numBuckets];
        cdf[0] = pi[0];
        for (int i = 1; i < numBuckets; i++) {
            cdf[i] = cdf[i - 1] + pi[i];
        }

        int[] result = new int[numBuckets];
        for (int i = 0; i < numBuckets; i++) {
            result[i] = 0;
        }

        Random rng = new java.util.Random();
        for (int trial = 0; trial < numTrials; trial++) {
            double val = rng.nextDouble();
            int bucket;
            for (bucket = 0; bucket < numBuckets; bucket++) {
                if (val <= cdf[bucket]) {
                    break;
                }
            }
            result[bucket] += 1;
        }

        // Convert to Jama (nasty).
        double[][] doubleResult = new double[numBuckets][1];
        for (int i = 0; i < numBuckets; i++) {
            doubleResult[i][0] = result[i];
        }
        return new JamaMatrixLib(doubleResult);
    }

    private int numTrials;
    private double[] pi;
}
