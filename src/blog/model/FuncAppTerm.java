/*
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import blog.bn.BayesNetVar;
import blog.bn.DerivedVar;
import blog.bn.RandFuncAppVar;
import blog.sample.EvalContext;

/**
 * Represents a function invocation.
 * 
 * @see blog.model.Term
 */
public class FuncAppTerm extends Term {
	/**
	 * Creates a new function application term with the given function and no
	 * arguments.
	 */
	public FuncAppTerm(Function f) {
		this.f = f;
		funcName = f.getName();
		args = new ArgSpec[0];
	}

	/**
	 * Creates a new function application term with the given function and a
	 * single given argument.
	 */
	public FuncAppTerm(Function f, ArgSpec arg) {
		this.f = f;
		funcName = f.getName();
		args = new ArgSpec[1];
		args[0] = arg;
	}

	/**
	 * Creates a new function application term with the given function and two
	 * given arguments.
	 */
	public FuncAppTerm(Function f, ArgSpec arg1, ArgSpec arg2) {
		this.f = f;
		funcName = f.getName();
		args = new Term[2];
		args[0] = arg1;
		args[1] = arg2;
	}

	/**
	 * Creates a new function application term with the given function and
	 * argument list.
	 * 
	 * @param f
	 *          a Function
	 * @param argList
	 *          a List of Term objects representing arguments
	 */
	public FuncAppTerm(Function f, List argList) {
		this.f = f;
		funcName = f.getName();
		args = new ArgSpec[argList.size()];
		argList.toArray(args);
	}

	/**
	 * Creates a new function application term with the given function name and
	 * argument list. The function itself will be found in the compilation phase
	 * based on the function name and the types of the arguments.
	 * 
	 * @param funcName
	 *          the name of a function
	 * @param args
	 *          a List of Term objects representing arguments
	 */
	public FuncAppTerm(String funcName, ArgSpec... args) {
		this.funcName = funcName;
		if (funcName.equals("Position"))
			position = this;
		// this.args = new ArgSpec[args.length];
		// argList.toArray(args);
		this.args = args;
	}

	public static FuncAppTerm position;

	/**
	 * Returns the function in this function application term.
	 */
	public Function getFunction() {
		return f;
	}

	/**
	 * Returns the arguments in this function application term.
	 */
	public ArgSpec[] getArgs() {
		return args;
	}

	/**
	 * Ensures that all the functions used in this term are compiled.
	 * 
	 * @param callStack
	 *          Set of objects whose compile methods are parents of this method
	 *          invocation. Ordered by invocation order. Used to detect cycles.
	 */
	public int compile(LinkedHashSet callStack) {
		compiled = true;
		callStack.add(this);
		int errors = 0;

		for (int i = 0; i < args.length; ++i) {
			errors += args[i].compile(callStack);
		}
		// errors += f.compile(callStack);

		callStack.remove(this);
		return errors;
	}

	public Object evaluate(EvalContext context) {
		// if (argValues == null) { // Not reusing anymore since this array was
		// being used for being argument arrays for RandFuncAppVars and had to be
		// cloned anyway.
		Object[] oldArgValues = argValues;
		argValues = new Object[args.length];
		// }

		for (int i = 0; i < args.length; ++i) {
			argValues[i] = args[i].evaluate(context);
			if (argValues[i] == null) {
				return null;
			}

			if (argValues[i] == Model.NULL) {
				// short-circuit, don't evaluate other args
				argValues = oldArgValues;
				return Model.NULL;
			}
		}

		Object result = f.getValueInContext(argValues, context, false);
		argValues = oldArgValues;
		return result;
	}

	/**
	 * Returns the (basic or derived) random variable that this function
	 * application term corresponds to. If all the arguments in this function
	 * application are non-random and the function itself is random, then this is
	 * a BasicVar (specifically, a RandFuncAppVar). Otherwise, it's a DerivedVar.
	 */
	public BayesNetVar getVariable() {
		Object[] oldArgValues = argValues;
		if (f instanceof RandomFunction) {
			if (loadArgValuesIfNonRandom()) {
				RandFuncAppVar randFuncAppVar = new RandFuncAppVar((RandomFunction) f,
						argValues, true);
				argValues = oldArgValues;
				return randFuncAppVar;
			}
		}
		argValues = oldArgValues;
		return new DerivedVar(this);
	}

	private boolean loadArgValuesIfNonRandom() {
		// if (argValues == null) { // Not reusing anymore since this array was
		// being used for being argument arrays for RandFuncAppVars and had to be
		// cloned anyway.
		argValues = new Object[args.length];
		// }

		for (int i = 0; i < args.length; ++i) {
			argValues[i] = args[i].getValueIfNonRandom();
			if (argValues[i] == null) {
				return false;
			}
		}
		return true;
	}

	public Collection getSubExprs() {
		return Arrays.asList(args);
	}

	public boolean containsRandomSymbol() {
		if (f instanceof RandomFunction) {
			return true;
		}

		for (int i = 0; i < args.length; ++i) {
			if (args[i].containsRandomSymbol()) {
				return true;
			}
		}

		return false;
	}

	public Set getGenFuncsApplied(Term subject) {
		Set genFuncsApplied = new HashSet();

		for (int i = 0; i < args.length; ++i) {
			if (args[i] instanceof Term) {
				Term arg = (Term) args[i];
				if (arg.equals(subject) && (f instanceof OriginFunction)) {
					genFuncsApplied.add(f);
				} else {
					genFuncsApplied.addAll(arg.getGenFuncsApplied(subject));
				}
			}
		}

		return Collections.unmodifiableSet(genFuncsApplied);
	}

	/**
	 * Two function application terms are equal if all their arguments are equal
	 * and their functions are equal.
	 */
	public boolean equals(Object o) {
		if (o instanceof FuncAppTerm) {
			FuncAppTerm other = (FuncAppTerm) o;
			return ((f.equals(other.getFunction())) && Arrays.equals(args,
					other.getArgs()));
		}
		return false;
	}

	public int hashCode() {
		int code = funcName.hashCode();
		for (int i = 0; i < args.length; ++i) {
			code ^= args[i].hashCode();
		}
		return code;
	}

	/**
	 * If this function application terms involves a non-zero number of arguments,
	 * returns a string of the form f(t1, ..., tK) where f is the string
	 * representation of the function and t1, ..., tK are string representations
	 * of the argument terms. If this function application term involves zero
	 * arguments, just returns the string representation of the function.
	 */
	public String toString() {
		if (args.length == 0) {
			return funcName;
		}

		StringBuffer buf = new StringBuffer();
		buf.append(funcName);
		buf.append("(");
		buf.append(args[0]);
		for (int i = 1; i < args.length; ++i) {
			buf.append(", ");
			buf.append(args[i]);
		}
		buf.append(")");
		return buf.toString();
	}

	/**
	 * Type checks this function application.
	 */
	public boolean checkTypesAndScope(Model model, Map scope) {
		boolean correct = true;

		Type[] argTypes = new Type[args.length];
		for (int i = 0; i < args.length; ++i) {
			if (args[i] instanceof Term) {
				Term arg = (Term) args[i];
				Term argInScope = arg.getTermInScope(model, scope);
				if (argInScope == null) {
					correct = false;
				} else {
					args[i] = argInScope;
					argTypes[i] = argInScope.getType();
				}
			} else if (args[i] instanceof ImplicitSetSpec) {
				argTypes[i] = BuiltInTypes.SET;
				ImplicitSetSpec setDef = (ImplicitSetSpec) args[i];
				correct = setDef.checkTypesAndScope(model, scope);
      } else if (args[i] instanceof ListSpec) {
        argTypes[i] = BuiltInTypes.REAL_ARRAY;
        // FIXME: handle arrays of other types?
			} else {
        throw new RuntimeException(
          "don't know how to process ArgSpec of type " +
          args[i].getClass().getName());
			}
		}

		if (correct && (f == null)) {
			f = model.getApplicableFunc(funcName, argTypes);
			if (f == null) {
				System.err
						.println(getLocation() + ": No function named " + funcName
								+ " is applicable to arguments of types "
								+ Arrays.asList(argTypes));
				return false;
			}
		}

		return correct;
	}

	public Type getType() {
		return f.getRetType();
	}

	public ArgSpec getSubstResult(Substitution subst, Set<LogicalVar> boundVars) {
		List<Term> newArgs = new ArrayList<Term>(args.length);
		for (int i = 0; i < args.length; ++i) {
			newArgs.add((Term) args[i].getSubstResult(subst, boundVars));
		}
		return new FuncAppTerm(f, newArgs);
	}

	/**
	 * If the top-level function in this term is indexed by time, returns the
	 * argument that serves as the time index. Otherwise, returns null.
	 */
	public ArgSpec getTimeArg() {
		if (f.isTimeIndexed()) {
			return args[args.length - 1];
		}
		return null;
	}

	public ArgSpec replace(Term t, ArgSpec another) {
		if (t.equals(this))
			return another;

		Term[] newArgs = new Term[args.length];
		boolean replacement = false;
		for (int i = 0; i != args.length; i++) {
			Term newArg = (Term) args[i].replace(t, another);
			replacement = replacement || newArg != args[i];
			newArgs[i] = newArg;
		}

		if (replacement) {
			Term result = new FuncAppTerm(f, Arrays.asList(newArgs));
			if (compiled)
				result.compile(new LinkedHashSet());
			return result;
		}

		return this;
	}

	public boolean makeOverlapSubst(Term t, Substitution theta) {
		if (equals(t))
			return true; // check for the same constant

		if (t instanceof fove.CountingTerm) {
			return t.makeOverlapSubst(this, theta);
		}

		if (args.length == 0) { // we are a constant
			if (t instanceof LogicalVar) { // they are a variable
				// can try to unify the other way
				return t.makeOverlapSubst(this, theta);
			}
			return false;
		}
		if (t instanceof FuncAppTerm) {
			FuncAppTerm ft = (FuncAppTerm) t;
			if (f != ft.f || args.length != ft.args.length)
				return false;
			for (int i = 0; i < args.length; i++) {
				if ((args[i] instanceof Term) && (ft.args[i] instanceof Term)) {
					Term arg = (Term) args[i];
					if (!arg.makeOverlapSubst((Term) ft.args[i], theta)) {
						return false;
					}
				}
			}
			return true;
		}
		return false;
	}

	public Term getCanonicalVersion() {
		if ((f instanceof NonRandomFunction) && getFreeVars().isEmpty()) {
			Object value = getValueIfNonRandom();
			if (value != null) {
				Term canonical = getType().getCanonicalTerm(value);
				if (canonical != null) {
					return canonical;
				}
			}
		}

		List<Term> newArgs = new ArrayList<Term>();
		for (int i = 0; i < args.length; ++i) {
			if (args[i] instanceof Term) {
				Term arg = (Term) args[i];
				newArgs.add(arg.getCanonicalVersion());
			}
		}

		FuncAppTerm canonical = new FuncAppTerm(f, newArgs);
		canonical.setLocation(location);
		return canonical;
	}

	private String funcName;
	private Function f;
	private ArgSpec[] args;
	private Object[] argValues; // scratch space for storing arg values
	private boolean compiled = false;
}
