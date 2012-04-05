/*
 * Copyright (c) 2007 Massachusetts Institute of Technology
 * Copyright (c) 2005, 2006, Regents of the University of California
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

import blog.BayesNetVar;
import blog.DerivedVar;
import blog.EvalContext;
import blog.Substitution;

/**
 * A logical variable. Specifically, a single LogicalVar object is used for the
 * introduction of a logical variable in a particular scope, and for all uses of
 * the variable in that scope. Unlike other terms, LogicalVar objects can be
 * compared safely using ==.
 * 
 * <p>
 * Note that some LogicalVar objects are not created directly by the parser, but
 * are returned by the <code>getTermInScope</code> method on SymbolTerm. This is
 * because, in some contexts, the parser cannot distinguish a logical variable
 * from a constant symbol based on local syntax alone.
 */
public class LogicalVar extends Term {
	/**
	 * Creates a new LogicalVar with the given name and type.
	 */
	public LogicalVar(String name, Type type) {
		this.name = name;
		this.type = type;
	}

	/**
	 * Returns the name of this variable.
	 */
	public String getName() {
		return name;
	}

	public boolean checkTypesAndScope(Model model, Map scope) {
		if (scope.get(name) != this) {
			System.err.println(getLocation() + ": LogicalVar " + name
					+ " is not in scope.");
			return false;
		}
		return true;
	}

	public int compile(LinkedHashSet callStack) {
		return 0; // no compilation necessary for logical variables
	}

	/**
	 * Returns the type of this variable.
	 */
	public Type getType() {
		return type;
	}

	public Object evaluate(EvalContext context) {
		return context.getLogicalVarValue(this);
	}

	public BayesNetVar getVariable() {
		return new DerivedVar(this);
	}

	public boolean containsRandomSymbol() {
		return false;
	}

	public Set getFreeVars() {
		return Collections.singleton(this);
	}

	public ArgSpec getSubstResult(Substitution subst, Set<LogicalVar> boundVars) {
		if (boundVars.contains(this)) {
			return this;
		}
		return subst.getReplacement(this);
	}

	public boolean makeOverlapSubst(Term t, Substitution theta) {
		boolean ret = theta.makeEqual(this, t);
		return ret;
	}

	public Term getCanonicalVersion() {
		return this; // can't canonicalize because this is a free variable
	}

	public LogicalVar makeNew() {
		return LogicalVar.createVar(type);
	}

	/**
	 * Returns the name of this variable.
	 */
	public String toString() {
		return name;
	}

	public ArgSpec replace(Term t, ArgSpec another) {
		if (t.equals(this))
			return another;
		return this;
	}

	public static LogicalVar createVar(Type type) {
		counter++;
		return new LogicalVar("$" + counter, type);
	}

	private String name;
	private Type type;

	private static int counter = 0;
}
