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

package common;

import java.util.*;

/**
 * Multiset that allows sampling, but not iteration or removals.  Uses 
 * a sorted map (from elements to integers) so the sampling is 
 * reproducible.  Null elements cannot be added.    
 */
public class SamplingMultiset implements SetWithDistrib {
    /**
     * Creates a new, empty multiset.
     */
    public SamplingMultiset() {
    }

    /**
     * Increments the count for the given element.
     */
    public void incrementCount(Object o) {
	Integer count = (Integer) map.get(o);
	if (count == null) {
	    count = new Integer(1);
	} else {
	    count = new Integer(count.intValue() + 1);
	}
	map.put(o, count);
	++totalCount;
    }

    /**
     * Returns the count for the given element.
     */
    public int getCount(Object o) {
	if (o == null) {
	    return 0;
	}

	Integer count = (Integer) map.get(o);
	if (count == null) {
	    return 0;
	}
	return count.intValue();
    }

    /**
     * Returns the total count of all elements.
     */
    public int totalCount() {
	return totalCount;
    }
	
    public double getProb(Object o) {
	if (o == null) {
	    return 0;
	}

	Integer count = (Integer) map.get(o);
	if (count == null) {
	    return 0;
	}
	return count.intValue() / (double) totalCount;
    }

    public double getLogProb(Object o) {
	return Math.log(getProb(o));
    }

    /**
     * Returns an object sampled uniformly from this multiset.  If the 
     * multiset is empty, returns null.  
     */
    public Object sample() {
	int remaining = Util.randInt(totalCount);
	for (Iterator iter = map.entrySet().iterator(); iter.hasNext(); ) {
	    Map.Entry entry = (Map.Entry) iter.next();
	    int count = ((Integer) entry.getValue()).intValue();
	    if (remaining < count) {
		return entry.getKey();
	    }
	    remaining -= count;
	}
	return null;
    }

    public String toString() {
	return map.toString();
    }

    private Map map = new TreeMap();
    private int totalCount = 0;
}
