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

package blog.common;

import java.util.*;

/**
 * A histogram that stores the logs of the weights of its elements, rather than
 * the weights themselves.
 */
public class LogHistogram implements SetWithDistrib {
	/**
	 * Creates a new LogHistogram with no elements.
	 */
	public LogHistogram() {
		initMap();
	}

	/**
	 * Creates a new LogHistogram with no elements.
	 * 
	 * @param sorted
	 *          if true, use a sorted map from elements to log weights
	 */
	public LogHistogram(boolean sorted) {
		this.sorted = sorted;
		initMap();
	}

	public double getProb(Object o) {
		return Math.exp(getLogProb(o));
	}

	public double getLogProb(Object o) {
		if (o == null) {
			return Double.NEGATIVE_INFINITY;
		}

		Double logWeight = (Double) map.get(o);
		if (logWeight == null) {
			return Double.NEGATIVE_INFINITY;
		}
		return (logWeight.doubleValue() - logTotalWeight);
	}

	public Object sample() {
		double remaining = Util.random();
		Object o = null;
		for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			o = entry.getKey();
			double logWeight = ((Double) entry.getValue()).doubleValue();
			remaining -= Math.exp(logWeight - logTotalWeight);
			if (remaining < 0) {
				break;
			}
		}
		return o;
	}

	/**
	 * Increases the weight of the given object by an amount whose logarithm is
	 * the given value.
	 */
	public void increaseWeight(Object o, double logIncrement) {
		Double logWeight = (Double) map.get(o);
		if (logWeight == null) {
			logWeight = new Double(logIncrement);
		} else {
			logWeight = new Double(Util.logSum(logWeight.doubleValue(), logIncrement));
		}
		map.put(o, logWeight);
		logTotalWeight = Util.logSum(logTotalWeight, logIncrement);
	}

	private void initMap() {
		if (sorted) {
			map = new TreeMap();
		} else {
			map = new HashMap();
		}
	}

	private boolean sorted = false;
	private Map map;
	private double logTotalWeight = Double.NEGATIVE_INFINITY;
}
