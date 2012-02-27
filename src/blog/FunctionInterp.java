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

/**
 * Interface for classes that define the interpretation of a non-random 
 * function symbol.  An interpretation is just a function from argument 
 * tuples to values.  Implementations of this class should have a constructor 
 * that takes a List of Objects as its sole argument; these objects are 
 * parameters that define the interpretation.
 */
public interface FunctionInterp {
    /**
     * Returns the value of this function on the given tuple of arguments.  
     * Implementations can assume that the arguments are of the expected 
     * types and are not Model.NULL.
     */
    Object getValue(List args);

    /**
     * Returns the set of argument tuples that yield the given value, if 
     * this set is finite and can be computed easily.  Otherwise returns 
     * null.
     *
     * @return Set of List of objects
     */
    Set getInverseTuples(Object value);

    /**
     * Returns the set of values for argument <code>argIndex</code> that, 
     * in combination with the given values for the other arguments, 
     * yield the given function value.  If this set cannot be computed 
     * straightforwardly, returns null.
     *
     * @param args   tuple of arguments; the entry at <code>argIndex</code> 
     *               is ignored
     *
     * @param argIndex index of argument whose possible values are to 
     *                 be returned
     *
     * @param argType  type of the argument at index argIndex
     *
     * @param value    value of this function
     */
    Set getInverseArgs(List args, int argIndex, Type argType, Object value);
}
