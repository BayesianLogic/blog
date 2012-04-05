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

package blog.model;

import java.util.*;

import blog.BayesNetVar;
import blog.DerivedVar;
import blog.EvalContext;
import blog.FuncAppTerm;
import blog.RandFuncAppVar;
import blog.RandomFunction;
import blog.Substitution;
import blog.common.Util;


/**
 * A term consisting of a single symbol. This may be either a zero-ary function
 * application or a use of a logical variable. SymbolTerms occur in the abstract
 * syntax tree for a model only before checkTypesAndScope is called on the
 * expressions in the model. After that point, a SymbolTerm is replaced with a
 * LogicalVar if its symbol is the name of a logical variable in the current
 * scope; otherwise it's replaced with a zero-argument FuncAppTerm.
 * 
 * <p>
 * If you're creating a term or formula programmatically, you should not use
 * SymbolTerm; you should use either LogicalVar or FuncAppTerm.
 */
public class SymbolTerm extends Term {
	/**
	 * Creates a new SymbolTerm with the given symbol. The
	 * <code>checkTypesAndScope</code> method will determine if this is a zero-ary
	 * function or a logical variable.
	 */
	public SymbolTerm(String symbol) {
		name = symbol;
	}

	/**
	 * Returns true if this SymbolTerm is an occurrence of a logical variable.
	 */
	public boolean isLogicalVar() {
		checkCompiled();
		return (var != null);
	}

	/**
	 * Returns the logical variable used in this SymbolTerm, or null if this is a
	 * zero-ary function application.
	 */
	public LogicalVar getLogicalVar() {
		checkCompiled();
		return var;
	}

	/**
	 * Returns the zero-ary function used in this SymbolTerm, or null if this is
	 * an occurrence of a logical variable.
	 */
	public Function getFunc() {
		checkCompiled();
		return func;
	}

	public boolean checkTypesAndScope(Model model, Map scope) {
		if ((var == null) && (func == null)) {
			var = (LogicalVar) scope.get(name);
			if (var == null) {
				func = model.getFunction(new Function.Sig(name));
				if (func == null) {
					System.err.println(getLocation() + ": Symbol \"" + name
							+ "\" is neither a variable in the current scope "
							+ "nor a zero-ary function.");
					return false;
				}
			}
		} else if (var != null) {
			if (var != scope.get(name)) {
				System.err.println(getLocation() + ": LogicalVar " + var
						+ " is not in scope.");
				return false;
			}
		}

		return true;
	}

	public Term getTermInScope(Model model, Map scope) {
		Term result = null;
		if (checkTypesAndScope(model, scope)) {
			if (func != null) {
				result = new FuncAppTerm(func);
			} else {
				result = var;
			}
			result.setLocation(getLocation());
		}
		return result;
	}

	public int compile(LinkedHashSet callStack) {
		if (func != null) {
			callStack.add(this);
			int errors = func.compile(callStack);
			callStack.remove(this);
			return errors;
		}
		return 0; // no compilation necessary for logical variables
	}

	public Type getType() {
		checkCompiled();
		return ((var != null) ? var.getType() : func.getRetType());
	}

	public Object evaluate(EvalContext context) {
		if (var != null) {
			return context.getLogicalVarValue(var);
		}
		return func.getValueInContext(NO_ARGS, context, true);
	}

	/**
	 * Returns the random variable that this term corresponds to. If this
	 * SymbolTerm is a random function, then the variable returned is a BasicVar.
	 * Otherwise, it's a DerivedVar.
	 */
	public BayesNetVar getVariable() {
		if (func instanceof RandomFunction) {
			return new RandFuncAppVar((RandomFunction) func, NO_ARGS, true);
		}
		return new DerivedVar(this);
	}

	public boolean containsRandomSymbol() {
		checkCompiled();
		return (func instanceof RandomFunction);
	}

	public Set getFreeVars() {
		checkCompiled();
		return ((var != null) ? Collections.singleton(var) : Collections.EMPTY_SET);
	}

	public ArgSpec getSubstResult(Substitution subst, Set<LogicalVar> boundVars) {
		throw new UnsupportedOperationException(
				"Can't apply substitution to SymbolTerm.  Make sure "
						+ "checkTypesAndScope is called to eliminate SymbolTerms "
						+ "before a substitutions is applied.");
	}

	public boolean isConstantNull() {
		checkCompiled();
		return (func == BuiltInFunctions.NULL);
	}

	public boolean makeOverlapSubst(Term t, Substitution theta) {
		// this shouldn't ever get called...
		return false;
	}

	public Term getCanonicalVersion() {
		throw new UnsupportedOperationException(
				"Can't get canonical version of SymbolTerm.  Make sure "
						+ "checkTypesAndScope is called to eliminate SymbolTerms "
						+ "before getCanonicalVersion is called.");
	}

	public boolean equals(Object o) {
		checkCompiled();
		if (o instanceof SymbolTerm) {
			SymbolTerm other = (SymbolTerm) o;
			if (var != null) {
				return (var == other.getLogicalVar());
			}
			return (func.equals(other.getFunc()));
		}
		return false;
	}

	public int hashCode() {
		checkCompiled();
		return ((var != null) ? var.hashCode() : func.hashCode());
	}

	public String toString() {
		return name;
	}

	private void checkCompiled() {
		if ((func == null) && (var == null)) {
			throw new IllegalStateException("SymbolTerm \"" + this
					+ "\" has not been succesfully " + "compiled.");
		}
	}

	public ArgSpec replace(Term t, ArgSpec another) {
		Util.fatalError("replace not supported for SymbolTerm.");
		return null;
	}

	private String name;
	private Function func;
	private LogicalVar var;

	private static Object[] NO_ARGS = new Object[0];
}
