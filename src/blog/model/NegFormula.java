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

import blog.sample.EvalContext;

/**
 * Represents a logical negation of an expression of type Formula.
 * 
 * @see blog.model.Formula
 */
public class NegFormula extends Formula {

	public NegFormula(Formula neg) {

		this.neg = neg;

	}

	public Formula getNeg() {

		return neg;

	}

	public Object evaluate(EvalContext context) {
		Boolean negValue = (Boolean) neg.evaluate(context);
		if (negValue == null) {
			return null;
		}

		return Boolean.valueOf(!negValue.booleanValue());
	}

	/**
	 * The standard form of a negation formula !psi is determined as follows. If
	 * there is a formula equivalent to !psi that is not a negation formula, we
	 * return the standard form of that formula. Otherwise, we just return !psi',
	 * where psi' is the standard form of psi.
	 */
	public Formula getStandardForm() {
		Formula equiv = neg.getEquivToNegation();
		if (equiv == null) {
			return new NegFormula(neg.getStandardForm());
		}
		return equiv.getStandardForm();
	}

	/**
	 * A formula equivalent to the negation of !psi is psi itself.
	 */
	protected Formula getEquivToNegationInternal() {
		return neg;
	}

	public List getSubformulas() {
		return Collections.singletonList(neg);
	}

	/**
	 * If this is a literal, then its CNF form is just a conjunction consisting of
	 * one disjunction, whose sole disjunct is this formula. Otherwise, its CNF
	 * form is the CNF form of the equivalent formula that is not a NegFormula.
	 */
	public ConjFormula getPropCNF() {
		Formula equiv = neg.getEquivToNegation();
		if (equiv == null) {
			// can't push negation inside, so this is a literal
			return new ConjFormula(new DisjFormula(this));
		}
		return equiv.getPropCNF();
	}

	/**
	 * If this is a literal, then its DNF form is just a disjunction consisting of
	 * one conjunction, whose sole conjunct is this formula. Otherwise, its DNF
	 * form is the DNF form of the equivalent formula that is not a NegFormula.
	 */
	public DisjFormula getPropDNF() {
		Formula equiv = neg.getEquivToNegation();
		if (equiv == null) {
			// can't push negation inside, so this is a literal
			return new DisjFormula(new ConjFormula(this));
		}
		return equiv.getPropDNF();
	}

	/**
	 * Returns true if the negated formula is an atomic formula or an equality
	 * formula.
	 */
	public boolean isLiteral() {
		return ((neg instanceof AtomicFormula) || (neg instanceof EqualityFormula));
	}

	public Set getSatisfiersIfExplicit(EvalContext context, LogicalVar subject,
			GenericObject genericObj) {
		return neg.getNonSatisfiersIfExplicit(context, subject, genericObj);
	}

	public Set getNonSatisfiersIfExplicit(EvalContext context,
			LogicalVar subject, GenericObject genericObj) {
		return neg.getSatisfiersIfExplicit(context, subject, genericObj);
	}

	/**
	 * Two NegFormulas are equal if they have the same subformula.
	 */
	public boolean equals(Object o) {
		if (o instanceof NegFormula) {
			NegFormula other = (NegFormula) o;
			return neg.equals(other.getNeg());
		}
		return false;
	}

	public int hashCode() {
		return (getClass().hashCode() ^ neg.hashCode());
	}

	/**
	 * Returns a string of the form !psi where psi is the negated formula.
	 */
	public String toString() {
		return ("!" + neg);
	}

	public boolean checkTypesAndScope(Model model, Map scope) {
		return neg.checkTypesAndScope(model, scope);
	}

	public ArgSpec replace(Term t, ArgSpec another) {
		Formula newNeg = (Formula) neg.replace(t, another);
		if (newNeg != neg)
			return compileAnotherIfCompiled(new NegFormula(newNeg));
		return this;
	}

	public ArgSpec getSubstResult(Substitution subst, Set<LogicalVar> boundVars) {
		return new NegFormula((Formula) neg.getSubstResult(subst, boundVars));
	}

	private Formula neg;

}
