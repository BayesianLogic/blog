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
import blog.*;
import blog.common.Util;
import blog.model.Model;
import blog.objgen.ObjectSet;

/**
 * CPD that takes a set of objects (an instance of the ObjectSet interface) as
 * an argument, and defines a uniform distribution over this set.
 */
public class UniformChoice extends AbstractCondProbDistrib {
	/**
	 * Creates a UniformChoice CPD. This constructor is not used by the parser,
	 * which looks for a constructor taking a List as an argument. Instead, it's
	 * used when we create a UniformChoice CPD in the program.
	 */
	public UniformChoice() {
	}

	/**
	 * Creates a UniformChoice CPD. The CPD takes no parameters.
	 * 
	 * @throws IllegalArgumentException
	 *           if <code>params</code> is non-empty
	 */
	public UniformChoice(List params) {
		if (!params.isEmpty()) {
			throw new IllegalArgumentException(
					"UniformChoice CPD does not take any parameters.");
		}
	}

	/**
	 * Takes a single argument, namely a set S. If S is non-empty, returns 1 / |S|
	 * if <code>value</code> is in S, and otherwise 0. If S is empty, returns 1 if
	 * the value is Model.NULL, and 0 otherwise.
	 * 
	 * @throws IllegalArgumentException
	 *           if <code>args</code> contains anything other than a single
	 *           argument of class ObjectSet.
	 */
	public double getProb(List args, Object value) {
		ObjectSet s = processArgs(args);
		// if (!s.contains(value)) {
		// System.out.println("UniformChoice: " + value + " is not in " + s);
		// System.out.println("Explicit version of set is: "
		// + new ArrayList(s));
		// }
		if (s.isEmpty()) {
			return (value == Model.NULL) ? 1 : 0;
		}
		return (s.contains(value) ? (1.0 / s.size()) : 0);
	}

	/**
	 * Takes a single argument, namely a finite set S. Returns an element of S
	 * selected uniformly at random. If S is empty, returns Model.NULL.
	 * 
	 * @throws IllegalArgumentException
	 *           if <code>args</code> contains anything other than a single
	 *           argument of class ObjectSet.
	 */
	public Object sampleVal(List args) {
		ObjectSet s = processArgs(args);
		if (s.isEmpty()) {
			return Model.NULL;
		}
		int n = Util.randInt(s.size());
		return s.sample(n);
	}

	private ObjectSet processArgs(List args) {
		if (args.size() != 1) {
			throw new IllegalArgumentException(
					"UniformChoice CPD takes exactly one argument.");
		}
		if (!(args.get(0) instanceof ObjectSet)) {
			throw new IllegalArgumentException(
					"UniformChoice CPD takes an argument of class ObjectSet, "
							+ "not one of " + args.get(0).getClass() + ".");
		}

		return (ObjectSet) args.get(0);
	}
}
