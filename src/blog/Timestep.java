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

import blog.model.ArgSpec;
import blog.model.BuiltInFunctions;
import blog.model.FuncAppTerm;
import blog.model.LogicalVar;

/**
 * Represents a time step, and is used for DBLOG (Dynamic BLOG) to indicate
 * temporal random variables.
 */
public class Timestep extends Number implements Comparable {

	private Timestep(int index) {
		this.index = index;
		if (index > max)
			max = index;
	}

	public Timestep next() {
		return at(index + 1);
	}

	public Timestep prev() {
		return at(index - 1);
	}

	public int getValue() {
		return index;
	}

	public static int getMax() {
		return max;
	}

	// implement abstract methods of Number

	public int intValue() {
		return index;
	}

	public long longValue() {
		return (long) index;
	}

	public double doubleValue() {
		return (double) index;
	}

	public float floatValue() {
		return (float) index;
	}

	public int compareTo(Object o) {
		return index - ((Number) o).intValue();
	}

	public String toString() {
		return "@" + index;
	}

	/**
	 * After this object is constructed in deserialization, either add it to the
	 * <code>generatedTimesteps</code> map or return its existing equivalent from
	 * that map.
	 */
	private Object readResolve() {
		Integer t = new Integer(index);
		Timestep existing = (Timestep) generatedTimesteps.get(t);
		if (existing == null) {
			generatedTimesteps.put(t, this);
			return this;
		}
		return existing;
	}

	public static Timestep at(int t) {
		Timestep ts = (Timestep) generatedTimesteps.get(new Integer(t));
		if (ts == null) {
			ts = new Timestep(t);
			generatedTimesteps.put(new Integer(t), ts);
		}
		return ts;
	}

	/**
	 * Returns true if <code>term</code> is exactly the logical variable
	 * <code>timeVar</code>.
	 */
	public static boolean isGivenTime(LogicalVar timeVar, ArgSpec term) {
		return (term == timeVar);
	}

	/**
	 * Returns true if <code>term</code> is an application of the built-in
	 * function <code>BuiltInFunctions.PREV</code> to a term for which
	 * <code>isGivenTime</code> returns true, or has the form <code>t - 1</code>
	 * where <code>isGivenTime(t)</code> returns true.
	 */
	public static boolean isPrevTime(LogicalVar timeVar, ArgSpec term) {
		if (term instanceof FuncAppTerm) {
			FuncAppTerm funcApp = (FuncAppTerm) term;
			if (funcApp.getFunction() == BuiltInFunctions.PREV) {
				return isGivenTime(timeVar, funcApp.getArgs()[0]);
			}
			if ((funcApp.getFunction() == BuiltInFunctions.MINUS)
					&& isGivenTime(timeVar, funcApp.getArgs()[0])) {
				Object sub = funcApp.getArgs()[1].getValueIfNonRandom();
				if ((sub instanceof Number) && (((Number) sub).intValue() == 1)) {
					return true;
				}
			}
		}
		return false;
	}

	private int index;
	private static int max = 0;
	private static Map generatedTimesteps = new HashMap(); // from Integer to
																													// Timestep
}
