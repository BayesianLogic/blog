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

import blog.EvalContext;
import blog.GenericObject;
import blog.common.UnaryProcedure;
import blog.common.Util;
import blog.world.PartialWorld;


/**
 * Formula is an abstract class from which all classes denoting particular kinds
 * of formulas inherit. A formula can be "wrapped" in the class Wrapper that
 * facilitates error-checking.
 */
public abstract class Formula extends ArgSpec {

	/**
	 * Returns the logical value of this formula in the given partial world. If
	 * the given world is not complete enough to determine the value of this
	 * formula, this method yields a fatal error.
	 */
	public boolean isTrue(PartialWorld w) {
		return ((Boolean) evaluate(w)).booleanValue();
	}

	/**
	 * Compiles all sub-formulas and top-level terms of this formula.
	 * 
	 * @param callStack
	 *          Set of objects whose compile methods are parents of this method
	 *          invocation. Ordered by invocation order. Used to detect cycles.
	 */
	public int compile(LinkedHashSet callStack) {
		compiled = true;

		callStack.add(this);
		int errors = 0;

		for (Iterator iter = getSubformulas().iterator(); iter.hasNext();) {
			errors += ((Formula) iter.next()).compile(callStack);
		}

		for (Iterator iter = getTopLevelTerms().iterator(); iter.hasNext();) {
			errors += ((Term) iter.next()).compile(callStack);
		}

		callStack.remove(this);
		return errors;
	}

	/**
	 * Returns the proper sub-expressions of this formula. This default
	 * implementation returns the set of sub-formulas of this formula. This
	 * implementation is overridden by AtomicFormula and EqualityFormula, whose
	 * sub-expressions are terms.
	 */
	public Collection getSubExprs() {
		return getSubformulas();
	}

	/**
	 * Returns a new formula in which:
	 * <ul>
	 * <li>occurrences of the formula "true" have been converted to empty
	 * conjunctions;
	 * <li>implications have been converted to disjunctions;
	 * <li>only literals (atomic and equality formulas) are negated.
	 * </ul>
	 * The default implementation just returns this formula.
	 */
	public Formula getStandardForm() {
		return this;
	}

	/**
	 * Returns a formula that is equivalent to the negation of this formula, but
	 * is not simply a NegFormula constructed with this formula as its argument.
	 * If no such formula can be constructed easily, this method returns null.
	 * 
	 * <p>
	 * This method calls <code>getEquivToNegationInternal</code> and caches the
	 * formula that is returned (if it is non-null).
	 */
	public Formula getEquivToNegation() {
		if (equivToNegation == null) {
			// Note that the following call happens even if we previously
			// called getEquivToNegationInternal on this object and it
			// returned null. In this case the method call is unnecessary,
			// but should be very fast.
			equivToNegation = getEquivToNegationInternal();
		}
		return equivToNegation;
	}

	/**
	 * Returns a formula that is equivalent to the negation of this formula, but
	 * is not simply a NegFormula constructed with this formula as its argument.
	 * If no such formula can be constructed easily, this method returns null.
	 * 
	 * <p>
	 * The default implementation returns null.
	 */
	protected Formula getEquivToNegationInternal() {
		return null;
	}

	/**
	 * Returns the proper subformulas of this formula. The default implementation
	 * returns an empty list.
	 * 
	 * @return unmodifiable List of Formula objects
	 */
	public List getSubformulas() {
		return Collections.EMPTY_LIST;
	}

	/**
	 * Returns the terms that are part of this formula, but not part of any other
	 * term or formula within this formula. The default implementation returns an
	 * empty list.
	 * 
	 * @return unmodifiable List of Term objects
	 */
	public List getTopLevelTerms() {
		return Collections.EMPTY_LIST;
	}

	/**
	 * Returns true if any of the subformulas or top-level terms of this term
	 * contain a random symbol. Note that this is overridden by UniversalFormula
	 * and ExistentialFormula.
	 */
	public boolean containsRandomSymbol() {
		for (Iterator iter = getSubformulas().iterator(); iter.hasNext();) {
			if (((Formula) iter.next()).containsRandomSymbol()) {
				return true;
			}
		}

		for (Iterator iter = getTopLevelTerms().iterator(); iter.hasNext();) {
			if (((Term) iter.next()).containsRandomSymbol()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns true if this formula contains the given term.
	 */
	public boolean containsTerm(Term target) {
		for (Iterator iter = getSubformulas().iterator(); iter.hasNext();) {
			if (((Formula) iter.next()).containsTerm(target)) {
				return true;
			}
		}

		for (Iterator iter = getTopLevelTerms().iterator(); iter.hasNext();) {
			if (((Term) iter.next()).containsTerm(target)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns true if this formula contains any term in the given collection.
	 */
	public boolean containsAnyTerm(Collection terms) {
		for (Iterator iter = terms.iterator(); iter.hasNext();) {
			if (containsTerm((Term) iter.next())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the set of generating functions that are applied to the term
	 * <code>subject</code> anywhere in this formula.
	 * 
	 * @return unmodifiable Set of GeneratingFunction
	 */
	public Set getGenFuncsApplied(Term subject) {
		Set genFuncs = new HashSet();

		for (Iterator iter = getSubformulas().iterator(); iter.hasNext();) {
			Formula sub = (Formula) iter.next();
			genFuncs.addAll(sub.getGenFuncsApplied(subject));
		}

		for (Iterator iter = getTopLevelTerms().iterator(); iter.hasNext();) {
			Term t = (Term) iter.next();
			genFuncs.addAll(t.getGenFuncsApplied(subject));
		}

		return Collections.unmodifiableSet(genFuncs);
	}

	/**
	 * Returns an equivalent formula that consists of a conjunction where each
	 * conjunct is an elementary disjunction. An elementary formula is one in
	 * which all the subformulas are atomic or equality formulas, or negations of
	 * those formulas, or unnegated quantified formulas. This is "propositional"
	 * CNF in that quantified formulas are treated as atomic.
	 * 
	 * <p>
	 * The default implementation just returns a conjunction consisting of one
	 * disjunction, whose sole disjunct is this formula. This is the correct
	 * behavior for literals and quantified formulas.
	 */
	public ConjFormula getPropCNF() {
		return new ConjFormula(new DisjFormula(this));
	}

	/**
	 * Returns an equivalent formula that consists of a disjunction where each
	 * disjunct is an elementary conjunction. An elementary formula is one in
	 * which all the subformulas are atomic or equality formulas, or negations of
	 * those formulas, or unnegated quantified formulas. This is "propositional"
	 * DNF in that quantified formulas are treated as atomic.
	 * 
	 * <p>
	 * The default implementation just returns a disjunction consisting of one
	 * conjunction, whose sole conjunct is this formula. This is the correct
	 * behavior for literals and quantified formulas.
	 */
	public DisjFormula getPropDNF() {
		return new DisjFormula(new ConjFormula(this));
	}

	/**
	 * Returns true if this formula is a literal, that is, an atomic/equality
	 * formula or the negation thereof.
	 * 
	 * <p>
	 * The default implementation returns false.
	 */
	public boolean isLiteral() {
		return false;
	}

	/**
	 * Returns true if this is a quantified formula. The default implementation
	 * returns false.
	 */
	public boolean isQuantified() {
		return false;
	}

	/**
	 * Returns true if this formula is elementary, that is, all its subformulas
	 * are literals or unnegated quantified formulas.
	 */
	public boolean isElementary() {
		for (Iterator iter = getSubformulas().iterator(); iter.hasNext();) {
			Formula sub = (Formula) iter.next();
			if (!(sub.isLiteral() || sub.isQuantified())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the set of values for the logical variable <code>subject</code>
	 * that are consistent with the generating function values of
	 * <code>genericObj</code> and that make this formula true in the given
	 * context, if this set can be determined without enumerating possible values
	 * for <code>subject</code>. Returns the special value Formula.NOT_EXPLICIT if
	 * determining the desired set would requiring enumerating possible values for
	 * <code>subject</code>. Also, returns the special value Formula.ALL_OBJECTS
	 * if this formula is true in the given context for all objects consistent
	 * with <code>genericObj</code>. Finally, returns null if it tries to access
	 * an uninstantiated random variable.
	 * 
	 * @param context
	 *          an evaluation context that does not assign a value to the logical
	 *          variable <code>subject</code>
	 * 
	 * @param subject
	 *          a logical variable
	 * 
	 * @param genericObj
	 *          a GenericObject instance, which can stand for any object of a
	 *          given type or include values for certain generating functions
	 */
	public abstract Set getSatisfiersIfExplicit(EvalContext context,
			LogicalVar subject, GenericObject genericObj);

	/**
	 * Returns the set of values for the logical variable <code>subject</code>
	 * that are consistent with the generating function values of
	 * <code>genericObj</code> and that make this formula false in the given
	 * context, if this set can be determined without enumerating possible values
	 * for <code>subject</code>. Returns the special value Formula.NOT_EXPLICIT if
	 * determining the desired set would requiring enumerating possible values for
	 * <code>subject</code>. Also, returns the special value Formula.ALL_OBJECTS
	 * if this formula is false in the given context for all objects consistent
	 * with <code>genericObj</code>. Finally, returns null if it tries to access
	 * an uninstantiated random variable.
	 * 
	 * <p>
	 * This default implementation calls <code>getEquivToNegation</code>, then
	 * calls <code>getSatisfiersIfExplicit</code> on the resulting formula.
	 * Warning: subclasses must override either this method or
	 * <code>getEquivToNegationInternal</code> to avoid an
	 * UnsupportedOperationException.
	 * 
	 * @param context
	 *          an evaluation context that does not assign a value to the logical
	 *          variable <code>subject</code>
	 * 
	 * @param subject
	 *          a logical variable
	 * 
	 * @param genericObj
	 *          a GenericObject instance, which can stand for any object of a
	 *          given type or include values for certain generating functions
	 */
	public Set getNonSatisfiersIfExplicit(EvalContext context,
			LogicalVar subject, GenericObject genericObj) {
		Formula neg = getEquivToNegation();
		if (neg == null) {
			throw new UnsupportedOperationException(
					"Can't get simplified equivalent to negation of " + this);
		}
		return neg.getSatisfiersIfExplicit(context, subject, genericObj);
	}

	public ArgSpec find(Term t) {
		Collection subExprs = getSubExprs();
		for (Iterator it = subExprs.iterator(); it.hasNext();) {
			ArgSpec sub = (ArgSpec) it.next();
			ArgSpec result = sub.find(t);
			if (result != null)
				return result;
		}
		return null;
	}

	public void applyToTerms(UnaryProcedure procedure) {
		Collection subExprs = getSubExprs();
		for (Iterator it = subExprs.iterator(); it.hasNext();) {
			ArgSpec sub = (ArgSpec) it.next();
			sub.applyToTerms(procedure);
		}
	}

	/** Compile a given formula f if this is compiled, and return f. */
	protected Formula compileAnotherIfCompiled(Formula another) {
		if (compiled)
			another.compile(new LinkedHashSet());
		return another;
	}

	/**
	 * Special value returned by <code>getSatisfiersIfExplicit</code> and
	 * <code>GetNonSatisfiersIfExplicit</code> to indicate that the desired set
	 * cannot be determined without iterating over possible values of the subject
	 * variable.
	 */
	public static final Set NOT_EXPLICIT = Collections.singleton("NOT_EXPLICIT");

	/**
	 * Special value returned by <code>getSatisfiersIfExplicit</code> and
	 * <code>getNonSatisfiersIfExplicit</code> to indicate that the set of
	 * satisfiers or non-satisfiers consists of all objects of the appropriate
	 * type. This means that the truth value of the formula does not depend on the
	 * value of the subject variable.
	 */
	public static final Set ALL_OBJECTS = Collections.singleton("ALL_OBJECTS");

	private Formula equivToNegation = null;

	private boolean compiled = false;
}
