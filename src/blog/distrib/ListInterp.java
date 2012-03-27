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

import java.util.*;
import blog.AbstractFunctionInterp;

/**
 * An interpretation for a Boolean function symbol, specified by a list of
 * tuples for which the function returns true. The first parameter to ListInterp
 * is the arity of the tuples, i.e., the number of arguments to the function. If
 * the arity is <i>k</i>, then the remaining parameters are interpreted in
 * groups of <i>k</i>, as <i>k</i>-tuples for which the function returns true.
 * The function returns false for all other tuples.
 */
public class ListInterp extends AbstractFunctionInterp {
	/**
	 * Creates a new ListInterp object with an empty list of tuples on which the
	 * function returns true.
	 */
	public ListInterp(int arity) {
		this.arity = arity;
	}

	/**
	 * Creates a new ListInterp object with a specified list of tuples on which
	 * the function returns true.
	 * 
	 * @param params
	 *          List whose first element is an Integer <i>k</i>, and whose
	 *          remaining elements are interpreted in groups of <i>k</i>, as
	 *          tuples of function arguments.
	 */
	public ListInterp(List params) {
		if (params.isEmpty() || !(params.get(0) instanceof Integer)) {
			throw new IllegalArgumentException(
					"First parameter to ListInterp must be an integer "
							+ "specifying the number of elements in each tuple.");
		}
		arity = ((Integer) params.get(0)).intValue();

		if (arity <= 0) {
			throw new IllegalArgumentException(
					"Function specified by ListInterp must take at least 1 "
							+ "argument, not " + arity + ".  (For zero-ary functions, "
							+ "just use, e.g., \"nonrandom Integer C = 17;\", with no "
							+ "parentheses after C.)");
		}

		if ((params.size() - 1) % arity != 0) {
			throw new IllegalArgumentException("ListInterp initialized with arity "
					+ arity + ", but number of remaining parameters is not a "
					+ "multiple of " + arity);
		}

		for (int i = 1; i < params.size(); i += arity) {
			List args = params.subList(i, i + arity);
			// System.out.println(args);
			tuples.add(args);
		}
	}

	/**
	 * Adds the given argument tuple to the list of tuples on which the function
	 * returns true. Does nothing if the tuple is already in the list.
	 * 
	 * @throws IllegalArgumentException
	 *           if the size of <code>args</code> does not equal the arity passed
	 *           to this object's constructor
	 */
	public void add(List args) {
		if (args.size() != arity) {
			throw new IllegalArgumentException("Extension of relation with arity "
					+ arity + " can't include argument tuple: " + args);
		}
		tuples.add(args);
	}

	public Object getValue(List args) {
		if (args.size() != arity) {
			throw new IllegalArgumentException(
					"ListInterp expected argument tuples of arity " + arity
							+ ", got one of arity " + args.size());
		}
		return Boolean.valueOf(tuples.contains(args));
	}

	public Set getInverseTuples(Object value) {
		if ((value instanceof Boolean) && ((Boolean) value).booleanValue()) {
			return Collections.unmodifiableSet(tuples);
		}
		return null;
	}

	private int arity;
	private Set tuples = new HashSet(); // of List
}
