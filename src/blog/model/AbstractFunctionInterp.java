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

package blog.model;

import java.util.*;


/**
 * Abstract implementation of FunctionInterp including a getInverseTuples method
 * that always returns null.
 */
public abstract class AbstractFunctionInterp implements FunctionInterp {
	/**
	 * Returns the set of argument tuples that yield the given value, if this set
	 * is finite and can be computed easily. Otherwise returns null.
	 * 
	 * <p>
	 * This default implementation just returns null.
	 * 
	 * @return Set of List of objects
	 */
	public Set getInverseTuples(Object value) {
		return null;
	}

	/**
	 * Returns the set of values for argument <code>argIndex</code> that, in
	 * combination with the given values for the other arguments, yield the given
	 * function value. If this set cannot be computed straightforwardly, returns
	 * null.
	 * 
	 * <p>
	 * This default implementation calls getInverseTuples(value), and if it gets a
	 * non-null result, it searches the resulting set for tuples that match
	 * <code>args</code> except on <code>argIndex</code>. It returns a set
	 * consisting of the values at index <code>argIndex</code> in those tuples.
	 * 
	 * @param args
	 *          tuple of arguments; the entry at <code>argIndex</code> is ignored
	 * 
	 * @param argIndex
	 *          index of argument whose possible values are to be returned
	 * 
	 * @param argType
	 *          type of the argument at index argIndex
	 * 
	 * @param value
	 *          value of this function
	 */
	public Set getInverseArgs(List args, int argIndex, Type argType, Object value) {
		Set inverseTuples = getInverseTuples(value);
		if (inverseTuples == null) {
			return null;
		}

		Set inverseArgs = new HashSet();
		for (Iterator iter = inverseTuples.iterator(); iter.hasNext();) {
			List tuple = (List) iter.next();
			if (tuple.subList(0, argIndex).equals(args.subList(0, argIndex))
					&& (tuple.subList(argIndex + 1, tuple.size()).equals(args.subList(
							argIndex + 1, args.size())))) {
				inverseArgs.add(tuple.get(argIndex));
			}
		}
		return inverseArgs;
	}
}
