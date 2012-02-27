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
import blog.Model;
import common.MapWithPreimages;
import common.HashMapWithPreimages;

/**
 * An interpretation for a function symbol, specified by listing argument 
 * tuples and the value that the function returns on each tuple.  The 
 * first parameter to TabularInterp is the number of arguments to the 
 * function.  If the number of arguments is <i>k</i>, the remaining 
 * parameters are interpreted in groups of <i>k</i>+1, as a tuple of 
 * <i>k</i> arguments followed by the function value on those arguments.  
 * If an argument tuple is not listed, then the function returns Model.NULL 
 * on that tuple.  
 */
public class TabularInterp extends AbstractFunctionInterp {
    /**
     * Creates a new TabularInterp with an empty argument-value mapping.
     */
    public TabularInterp(int arity) {
	this.arity = arity;
    }

    /**
     * Creates a new TabularInterp object with a specified argument-value 
     * mapping.
     *
     * @param params List whose first element is an Integer <i>k</i>, and 
     *               whose remaining elements are interpreted in groups of 
     *               <i>k</i>+1 as a tuple of arguments and a function value.  
     *
     * @throws IllegalArgumentException  if <code>params</code> includes more 
     *                                   than one value for the same 
     *                                   argument tuple
     */
    public TabularInterp(List params) {
	if (params.isEmpty() || !(params.get(0) instanceof Integer)) {
	    throw new IllegalArgumentException
		("First parameter to TabularInterp must be an integer "
		 + "specifying the number of arguments to the function.");
	}
	arity = ((Integer) params.get(0)).intValue();

	if (arity < 0) {
	    throw new IllegalArgumentException
		("Function specified by TabularInterp cannot have a "
		 + "negative number of arguments " + arity);
	}

	if ((params.size() - 1) % (arity + 1) != 0) {
	    throw new IllegalArgumentException
		("TabularInterp initialized with arity " + arity 
		 + ", but number of remaining parameters is not a "
		 + "multiple of " + arity + "+1.");
	}

	for (int i = 1; i < params.size(); i += (arity+1)) {
	    List args = params.subList(i, i + arity);
	    Object value = params.get(i + arity);
	    //System.out.println(args + " -> " + value);

	    if (values.containsKey(args)) {
		throw new IllegalArgumentException
		    ("TabularInterp parameters include duplicate entries for "
		     + "the arguments " + args);
	    }
	    values.put(args, value);
	}
    }

    /**
     * Adds the given argument-value mapping to this interpretation.  
     * Replaces any existing value for the given arguments.  
     *
     * @throws IllegalArgumentException if the length of <code>args</code> 
     *                                  is not equal to the arity specified 
     *                                  in this object's constructor
     */
    public void put(List args, Object value) {
	if (args.size() != arity) {
	    throw new IllegalArgumentException
		("Illegal argument list for function of arity " + arity
		 + ": " + args);
	}
	values.put(args, value);
    }
	

    public Object getValue(List args) {
	if (args.size() != arity) {
	    throw new IllegalArgumentException
		("TabularInterp expected argument tuples of arity " + arity 
		 + ", got one of arity " + args.size());
	}

	Object value = values.get(args);
	return (value == null) ? Model.NULL : value;
    }

    public Set getInverseTuples(Object value) {
	return Collections.unmodifiableSet(values.getPreimage(value));
    }

    private int arity;
    private MapWithPreimages values 
	= new HashMapWithPreimages(); // from List to Object
}
