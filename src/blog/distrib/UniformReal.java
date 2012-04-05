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

import blog.*;
import blog.common.Util;

import java.util.*;

/**
 * Uniform distribution over a range of real numbers [lower, upper). The range
 * is open at the upper end for consistency with Random.nextDouble().
 */
public class UniformReal extends AbstractCondProbDistrib {
	/**
	 * Interprets the parameters as a pair of real numbers (lower, upper) and
	 * creates a uniform distribution over the range [lower, upper).
	 * 
	 * @throws IllegalArgumentException
	 *           if params does not consist of exactly two Number objects, or if
	 *           lower &gt;= upper
	 */
	public UniformReal(List params) {
		try {
			lower = ((Number) params.get(0)).doubleValue();
			upper = ((Number) params.get(1)).doubleValue();
			if ((lower >= upper) || (params.size() > 2)) {
				throw new IllegalArgumentException();
			}
		} catch (RuntimeException e) {
			throw new IllegalArgumentException(
					"UniformReal CPD expects two numeric arguments "
							+ "[lower, upper) with lower < upper.  Got: " + params);
		}
	}

	/**
	 * Returns 1 / (upper - lower) if the given number is in the range of this
	 * distribution, otherwise returns zero. Takes no arguments.
	 * 
	 * @throws IllegalArgumentException
	 *           if <code>args</code> is non-empty or <code>value</code> is not a
	 *           Number
	 */
	public double getProb(List args, Object value) {
		if (!args.isEmpty()) {
			throw new IllegalArgumentException(
					"UniformReal CPD does not take any arguments.");
		}
		if (!(value instanceof Number)) {
			throw new IllegalArgumentException(
					"UniformReal CPD defines distribution over objects of class "
							+ "Number, not " + value.getClass() + " (value is " + value
							+ ").");
		}
		double x = ((Number) value).doubleValue();

		if ((x >= lower) && (x < upper)) {
			return 1.0 / (upper - lower);
		}
		return 0;
	}

	/**
	 * Returns a sample from this distribution.
	 * 
	 * @throws IllegalArgumentException
	 *           if <code>args</code> is non-empty
	 */
	public Object sampleVal(List args, Type childType) {
		if (!args.isEmpty()) {
			throw new IllegalArgumentException(
					"UniformReal CPD does not take any arguments.");
		}

		// rely on the fact that Util.random() returns a value in [0, 1)
		double x = lower + (Util.random() * (upper - lower));
		return new Double(x);
	}

	private double lower;
	private double upper;

	public double getLower() {
		return lower;
	}

	public void setLower(double lower) {
		this.lower = lower;
	}

	public double getUpper() {
		return upper;
	}

	public void setUpper(double upper) {
		this.upper = upper;
	}

	public String toString() {
		return "U[" + lower + ", " + upper + "]";
	}
}
