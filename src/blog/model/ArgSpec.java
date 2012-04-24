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

import java.util.*;

import blog.Substitution;
import blog.bn.BayesNetVar;
import blog.bn.DerivedVar;
import blog.common.UnaryFunction;
import blog.common.UnaryPredicate;
import blog.common.UnaryProcedure;
import blog.sample.DefaultEvalContext;
import blog.sample.EvalContext;
import blog.world.PartialWorld;


/**
 * Represents an abstract argument specification. In general, an argument can
 * be:
 * <UL>
 * <LI>a Term;
 * <LI>an explicitly specified set of terms;
 * <LI>an implicitly specified set of terms;
 * <LI>an implicitly specified set of tuples of terms;
 * </UL>
 * Functions are only allowed to take Terms as arguments, whereas CPDs are are
 * allowed to take all argument types.
 */
public abstract class ArgSpec {
	/**
	 * Returns a List consisting of the objects obtained by evaluating each
	 * element of <code>argSpecs</code> in the given context.
	 */
	public static List evaluate(EvalContext context, List argSpecs) {
		List results = new ArrayList();
		for (Iterator iter = argSpecs.iterator(); iter.hasNext();) {
			Object value = ((ArgSpec) iter.next()).evaluate(context);
			results.add(value);
		}
		return results;
	}

	/**
	 * Returns the value of this argument specification in the context. This
	 * method raises a fatal error if the partial world in the given context is
	 * not complete enough to evaluate this ArgSpec.
	 */
	public Object evaluate(PartialWorld w) {
		return evaluate(new DefaultEvalContext(w, true));
	}

	/**
	 * Returns the value of this argument specification in the given context.
	 * Returns null if the partial world in this context is not complete enough to
	 * evaluate this ArgSpec, or if this ArgSpec contains a free variable that is
	 * not assigned a value in the given context.
	 */
	public abstract Object evaluate(EvalContext context);

	/**
	 * Returns true if this ArgSpec contains any random function symbols or any
	 * type symbols (any type might have a number statement, and thus could be
	 * random).
	 */
	public abstract boolean containsRandomSymbol();

	/**
	 * Returns true if the given partial world is complete enough to determine the
	 * value of this ArgSpec.
	 */
	public boolean isDetermined(PartialWorld w) {
		return (evaluate(new DefaultEvalContext(w, false)) != null);
	}

	/**
	 * Returns the value of this ArgSpec if it is non-random. Otherwise returns
	 * null.
	 */
	public Object getValueIfNonRandom() {
		return evaluate(new DefaultEvalContext(PartialWorld.EMPTY_INST, false));
	}

	/**
	 * Returns the (basic or derived) random variable that this argument
	 * specification corresponds to. Assumes this argument specification contains
	 * no free variables. The default implementation just returns a DerivedVar
	 * with this ArgSpec. However, some argument specifications (such as function
	 * application terms and atomic formulas) may correspond to BasicVars.
	 */
	public BayesNetVar getVariable() {
		return new DerivedVar(this);
	}

	/**
	 * Returns true if, within the given scope, all the variables used in this
	 * ArgSpec are in scope and all type constraints are satisfied. If there is a
	 * type or scope error, prints an appropriate message to standard error and
	 * returns false.
	 * 
	 * @param scope
	 *          a Map from variable names (Strings) to LogicalVar objects
	 */
	public abstract boolean checkTypesAndScope(Model model, Map scope);

	/**
	 * Does compilation steps that can only be done correctly once the model is
	 * complete. Prints messages to standard error if any errors are encountered.
	 * Returns the number of errors encountered.
	 * 
	 * <p>
	 * This default implementation just returns 0.
	 * 
	 * @param callStack
	 *          Set of objects whose compile methods are parents of this method
	 *          invocation. Ordered by invocation order. Used to detect cycles.
	 */
	public int compile(LinkedHashSet callStack) {
		return 0;
	}

	/**
	 * Returns true if the value of this ArgSpec is always an instance of Number
	 * (regardless of scope). The default implementation returns false.
	 */
	public boolean isNumeric() {
		return false;
	}

	/**
	 * Sets the location of this ArgSpec, for instance, the file name and line
	 * number where it appears. The location can be any object whose toString
	 * method returns an identifying string that can be used in error messages.
	 */
	public void setLocation(Object loc) {
		location = loc;
	}

	/**
	 * Returns the object specified by the last call to setLocation. If
	 * setLocation has not been called, returns the string "(no location)".
	 */
	public Object getLocation() {
		return location;
	}

	/**
	 * Returns the proper sub-expressions of this ArgSpec. This is an empty
	 * collection if this ArgSpec has no proper sub-expressions.
	 * 
	 * <p>
	 * This default implementation returns an empty collection.
	 * 
	 * @return unmodifiable Collection of ArgSpec
	 */
	public Collection getSubExprs() {
		return Collections.EMPTY_LIST;
	}

	/**
	 * Returns the logical variables that occur free in this expression. If this
	 * expression was loaded from a file, then this method should be called only
	 * after <code>checkTypesAndScope</code>, which converts SymbolTerms to
	 * LogicalVars.
	 * 
	 * <p>
	 * The default implementation returns the union of the sets of free variables
	 * in this expression's sub-expressions.
	 * 
	 * @return unmodifiable Set of LogicalVar
	 * 
	 * @throws IllegalStateException
	 *           if this expression contains a SymbolTerm
	 */
	public Set getFreeVars() {
		Set freeVars = new HashSet();
		for (Iterator iter = getSubExprs().iterator(); iter.hasNext();) {
			ArgSpec subExpr = (ArgSpec) iter.next();
			freeVars.addAll(subExpr.getFreeVars());
		}
		return Collections.unmodifiableSet(freeVars);
	}

	/**
	 * Returns term in ArgSpec equal to a given term t, or null if there isn't
	 * any.
	 */
	public abstract ArgSpec find(Term t);

	/**
	 * Applies a procedure to all terms in this ArgSpec which satisfy a given
	 * predicate to a given collection.
	 */
	public abstract void applyToTerms(UnaryProcedure procedure);

	/**
	 * Adds all terms in this ArgSpec which satisfy a predicate to a given
	 * collection.
	 */
	public void selectTerms(final UnaryPredicate predicate,
			final Collection selected) {
		applyToTerms(new UnaryProcedure() {
			public void evaluate(Object x) {
				if (predicate.evaluate(x))
					selected.add(x);
			}
		});
	}

	/**
	 * Returns an ArgSpec resulting from the replacement of all occurrences of a
	 * term by another, if there is any, or self. A new ArgSpec is compiled if
	 * this is compiled.
	 */
	public abstract ArgSpec replace(Term t, ArgSpec another);

	/**
	 * Returns an ArgSpec obtained by successively replacing terms and
	 * corresponding values (given in two arrays of same dimension) in this
	 * ArgSpec.
	 */
	public ArgSpec replace(Term[] terms, Object[] values) {
		ArgSpec result = this;
		for (int termIndex = 0; termIndex < terms.length; termIndex++) {
			result = (ArgSpec) result.replace(terms[termIndex],
					(ArgSpec) values[termIndex]);
		}
		return result;
	}

	/**
	 * Returns the result of applying the given substitution to this expression.
	 * In the result, every free occurrence of a logical variable <code>x</code>
	 * is replaced with <code>subst.getReplacement(x)</code> (which is just
	 * <code>x</code> itself if <code>subst</code> does not explicitly specify a
	 * replacement).
	 */
	public ArgSpec getSubstResult(Substitution subst) {
		Set<LogicalVar> boundVars = Collections.emptySet();
		return getSubstResult(subst, boundVars);
	}

	/**
	 * Returns the result of applying the substitution <code>subst</code> to this
	 * expression, excluding the logical variables in <code>boundVars</code>. This
	 * method is used for recursive calls. The set <code>boundVars</code> should
	 * contain those variables that are bound in the syntax tree between this
	 * sub-expression and the top-level expression to which the substitution is
	 * being applied.
	 */
	public abstract ArgSpec getSubstResult(Substitution subst,
			Set<LogicalVar> boundVars);

	/**
	 * Returns an object representing this argspec in the given scope. This method
	 * exists to handle cases where an argspec's class depends on the scope where
	 * it occurs, such as when a term consists of a single symbol that may be
	 * either a logical variable or a function symbol. If calling
	 * checkTypesAndScope on this term would return false, then this method
	 * returns null.
	 * 
	 * <p>
	 * The default implementation simply returns this object if checkTypesAndScope
	 * returns true, and null otherwise.
	 * 
	 * @param model
	 *          a BLOG model
	 * @param scope
	 *          a map from String to LogicalVar
	 */
	public ArgSpec getArgSpecInScope(Model model, Map scope) {
		return (checkTypesAndScope(model, scope) ? this : null);
	}

	private static String DEFAULT_LOCATION = "(no location)";

	protected Object location = DEFAULT_LOCATION;
}
