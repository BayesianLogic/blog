/*
 * Copyright (c) 2007 Massachusetts Institute of Technology
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

package fove;

import java.util.*;
import java.io.PrintStream;

import common.Util;

public class Histogram {

	public Histogram(int[] cs) {
		counts = new int[cs.length];
		for (int i = 0; i < counts.length; i++)
			counts[i] = cs[i];
	}

	public int numBuckets() {
		return counts.length;
	}

	public int getCount(int bucket) {
		return counts[bucket];
	}

	/**
	 * Returns the multinomial coefficient for this histogram, given that it has
	 * the specified total count. (The total is provided just to save
	 * computation.) The multinomial coefficient for a histogram h is:
	 * <blockquote> total! / (h[0]! * ... * h[k]!) <blockquote> where k is the
	 * number of buckets in the histogram.
	 * 
	 * <p>
	 * This method returns a double because the result might overflow an int.
	 */
	public double multinomialCoefficient(int total) {
		double logDenom = 0;
		for (int i = 0; i < counts.length; ++i) {
			logDenom += Util.logFactorial(counts[i]);
		}
		return Math.exp(Util.logFactorial(total) - logDenom);
	}

	public String toString() {
		return Arrays.toString(counts);
	}

	public boolean equals(Object o) {
		if (o instanceof Histogram) {
			Histogram other = (Histogram) o;
			return Arrays.equals(counts, other.counts);
		}
		return false;
	}

	public int hashCode() {
		return Arrays.hashCode(counts);
	}

	private void setCount(int index, int val) {
		counts[index] = val;
	}

	public Histogram incBucket(int index) {
		Histogram ret = new Histogram(counts);
		ret.setCount(index, getCount(index) + 1);
		return ret;
	}

	private int[] counts;

}
