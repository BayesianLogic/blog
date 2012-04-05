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

import blog.bn.BayesNetVar;
import blog.model.ArgSpec;
import blog.model.BuiltInTypes;
import blog.model.Formula;
import blog.model.LogicalVar;
import blog.model.Model;
import blog.model.Term;

/**
 * A Formula consisting of a single boolean-valued term.
 * 
 * @see blog.model.Term
 * @see blog.model.Formula
 */
public class AtomicFormula extends Formula {

	public AtomicFormula(Term sent) {

		this.sent = sent;

	}

	public Term getTerm() {

		return sent;

	}

	public Object evaluate(EvalContext context) {
		Object value = sent.evaluate(context);
		if (value == null) {
			return null;
		}

		if (!(value instanceof Boolean)) {
			throw new IllegalStateException("Sentence " + sent
					+ " has non-Boolean value " + value);
		}
		return (value.equals(Boolean.TRUE) ? Boolean.TRUE : Boolean.FALSE);
	}

	/**
	 * Returns the (basic or derived) random variable that this atomic formula
	 * corresponds to under the given assignment. This is just the random variable
	 * corresponding to underlying Boolean term.
	 */
	public BayesNetVar getVariable() {
		return sent.getVariable();
	}

	/**
	 * Returns a singleton collection containing the term in this atomic formula.
	 */
	public Collection getSubExprs() {
		return Collections.singletonList(sent);
	}

	/**
	 * Returns true.
	 */
	public boolean isLiteral() {
		return true;
	}

	public List getTopLevelTerms() {
		return Collections.singletonList(sent);
	}

	public Set getSatisfiersIfExplicit(EvalContext context, LogicalVar subject,
			GenericObject genericObj) {
		Set result = null;
		context.assign(subject, genericObj);

		// The only time we can determine the satisfiers is if this
		// formula can be evaluated on genericObj.
		Boolean value = (Boolean) evaluate(context);
		if (value != null) {
			result = (value.booleanValue() == true ? Formula.ALL_OBJECTS
					: Collections.EMPTY_SET);
		}

		context.unassign(subject);
		return result;
	}

	public Set getNonSatisfiersIfExplicit(EvalContext context,
			LogicalVar subject, GenericObject genericObj) {
		Set result = null;
		context.assign(subject, genericObj);

		// The only time we can determine the non-satisfiers is if
		// this formula can be evaluated on genericObj.
		Boolean value = (Boolean) evaluate(context);
		if (value != null) {
			result = (value.booleanValue() == false ? Formula.ALL_OBJECTS
					: Collections.EMPTY_SET);
		}

		context.unassign(subject);
		return result;
	}

	/**
	 * Two atomic formulas are equal if their underlying terms are equal.
	 */
	public boolean equals(Object o) {
		if (o instanceof AtomicFormula) {
			AtomicFormula other = (AtomicFormula) o;
			return sent.equals(other.getTerm());
		}
		return false;
	}

	public int hashCode() {
		return sent.hashCode();
	}

	/**
	 * Returns the string representation of the underlying term.
	 */
	public String toString() {
		return sent.toString();
	}

	/**
	 * Returns true if the underlying term satisfies the type/scope constraints
	 * and has a Boolean type.
	 */
	public boolean checkTypesAndScope(Model model, Map scope) {
		Term sentInScope = sent.getTermInScope(model, scope);
		if (sentInScope == null) {
			return false;
		}
		sent = sentInScope;
		if (!sent.getType().isSubtypeOf(BuiltInTypes.BOOLEAN)) {
			System.err.println("Error: Non-Boolean term treated as "
					+ "atomic formula: " + sent);
			return false;
		}
		return true;
	}

	public ArgSpec replace(Term t, ArgSpec another) {
		Term newSent = (Term) sent.replace(t, another);
		if (newSent != sent)
			return compileAnotherIfCompiled(new AtomicFormula(newSent));
		return this;
	}

	public ArgSpec getSubstResult(Substitution subst, Set<LogicalVar> boundVars) {
		return new AtomicFormula((Term) sent.getSubstResult(subst, boundVars));
	}

	/** The Term instance, assumed to be boolean-valued */
	private Term sent;

}
