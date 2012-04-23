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

package blog.sample;

import java.util.*;
import java.io.PrintStream;

import blog.ObjectSet;
import blog.bn.BasicVar;
import blog.bn.NumberVar;
import blog.model.LogicalVar;
import blog.model.Type;

/**
 * Interface for objects that encapsulate a partial world and an assignment of
 * values to logical variables. To facilitate debugging, an EvalContext object
 * also maintains a separate stack of objects being evaluated.
 */
public interface EvalContext {
	/**
	 * Returns the value of the given basic random variable in this partial world,
	 * or null if the given variable is not instantiated. Rodrigo: Why BasicVars
	 * only? Sounds like it could be done for DerivedVars as well.
	 */
	Object getValue(BasicVar var);

	/**
	 * Returns the set of objects that satisfy the given POP in this world. The
	 * objects may be represented as actual objects or identifiers, depending on
	 * the way the underlying PartialWorld implementation handles objects of the
	 * relevant type. A null return value indicates that the relevant number
	 * variable is not instantiated.
	 * 
	 * <p>
	 * The set returned by this method will remain correct if new basic random
	 * variables are instantiated. It may not remain correct if new identifiers
	 * are added or already-instantiated random variables are changed.
	 */
	ObjectSet getSatisfiers(NumberVar popApp);

	/**
	 * Returns the NumberVar (i.e., POP and generating objects) such that the
	 * given object satisfies that POP applied to those generating objects in this
	 * world. Returns null if the given object does not satisfy any POP
	 * application.
	 * 
	 * @throws IllegalArgumentException
	 *           if the given object does not exist in this world
	 */
	NumberVar getPOPAppSatisfied(Object obj);

	/**
	 * Returns true if the world that underlies this context uses object
	 * identifiers for the given type.
	 */
	boolean usesIdentifiers(Type type);

	/**
	 * Returns Boolean.TRUE if <code>obj</code> exists in all worlds consistent
	 * with this context's partial world. This is true if <code>obj</code> is a
	 * guaranteed object, a concrete non-guaranteed object whose number variable
	 * is instantiated to a sufficiently large value, or an object identifier.
	 * Returns Boolean.FALSE if <code>obj</code> is a concrete non-guaranteed
	 * object whose number variable is instantiated to a value that is too small.
	 * If neither of the above cases holds, then this method returns null.
	 * 
	 * @throws IllegalArgumentException
	 *           if <code>obj</code> is an identifier that is not in the common
	 *           ground of this context's world
	 */
	Boolean objectExists(Object obj);

	/**
	 * Returns the value of the given logical variable in this context, or null if
	 * the given logical variable has no assigned value.
	 */
	Object getLogicalVarValue(LogicalVar var);

	/**
	 * Returns the set of objects that are the value of some logical variable in
	 * this context.
	 * 
	 * @return unmodifiable Set of Object
	 */
	Set getLogicalVarValues();

	/**
	 * Returns a string describing the assignment of values to variables in this
	 * context.
	 */
	String getAssignmentStr();

	/**
	 * Assigns the given value to the given logical variable, overwriting any
	 * previous value.
	 */
	void assign(LogicalVar var, Object value);

	/**
	 * Assigns values to a tuple of logical variables.
	 * 
	 * @param vars
	 *          array of LogicalVar objects
	 * 
	 * @param values
	 *          array of Objects, in one-to-one correspondence with the elements
	 *          of <code>vars</code>, representing values for those variables
	 * 
	 * @throws IllegalStateException
	 *           if the assignment stack is empty
	 */
	void assignTuple(LogicalVar[] vars, Object[] values);

	/**
	 * Erases any value currently assigned to the given logical variable.
	 */
	void unassign(LogicalVar var);

	/**
	 * Erases any values currently assigned to any of the given logical variables.
	 */
	void unassignTuple(LogicalVar[] vars);

	/**
	 * Pushes the given object onto the stack of objects being evaluated. This
	 * stack is maintained purely so that the EvalContext can print more
	 * informative error messages.
	 * 
	 */
	void pushEvaluee(Object evaluee);

	/**
	 * Pops the top object from the stack of objects being evaluated.
	 * 
	 * @throws IllegalStateException
	 *           if the stack of objects being evaluated is empty
	 */
	void popEvaluee();

	/**
	 * Prints the sequence of objects being evaluated, in the order they were
	 * added to the stack, to the given stream.
	 */
	void printEvalTrace(PrintStream s);
}
