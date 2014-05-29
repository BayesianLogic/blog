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

package blog.old_distrib;

import java.util.*;
import blog.*;
import blog.common.Util;
import blog.model.Model;

/**
 * CPD that takes a set of size one as an argument, and defines a probability
 * distribution concentrated on the single element of that set. If the argument
 * set is empty, the distribution is concentrated on Model.NULL. If the set has
 * size greater than one, the <code>getProb</code> and <code>sampleVal</code>
 * methods throw IllegalArgumentExceptions.
 * 
 * <p>
 * The name comes from the iota operator in logic.
 */
public class Iota extends DetCondProbDistrib {
	/**
	 * Creates an Iota CPD.
	 */
	public Iota() {
	}

	/**
	 * Creates an Iota CPD. The CPD takes no parameters.
	 * 
	 * @throws IllegalArgumentException
	 *           if <code>params</code> is non-empty
	 */
	public Iota(List params) {
		if (!params.isEmpty()) {
			throw new IllegalArgumentException(
					"Iota CPD does not take any parameters.");
		}
	}

	/**
	 * Takes a single argument, namely a set S. If S is empty, returns Model.NULL.
	 * If S has a single element, returns that element. If S has more than one
	 * element, throws an IllegalArgumentException.
	 * 
	 * @throws IllegalArgumentException
	 *           if <code>args</code> contains anything other than a single
	 *           argument of class Set.
	 */
	public Object getChildValue(List args) {
		Set s = processArgs(args); // throws exception if size > 1
		if (s.isEmpty()) {
			return Model.NULL;
		}
		return s.iterator().next();
	}

	private Set processArgs(List args) {
		if (args.size() != 1) {
			throw new IllegalArgumentException("Iota CPD takes exactly one argument.");
		}
		if (!(args.get(0) instanceof Set)) {
			throw new IllegalArgumentException(
					"Iota CPD takes an argument of class Set, not one of "
							+ args.get(0).getClass() + ".");
		}

		Set s = (Set) args.get(0);
		if (s.size() > 1) {
			throw new IllegalArgumentException(
					"Set passed to Iota CPD has more than one element: " + s);
		}

		return s;
	}
}
