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

import blog.model.ArgSpec;
import blog.model.LogicalVar;
import blog.model.Model;
import blog.model.Term;

/**
 * An implication formula alpha -> beta. It is true if alpha is false or beta is
 * true.
 */
public class ImplicFormula extends Formula {

	/**
	 * Creates a new ImplicFormula of the form antecedent -> consequent.
	 */
	public ImplicFormula(Formula antecedent, Formula consequent) {
		this.antecedent = antecedent;
		this.consequent = consequent;
	}

	/**
	 * Returns the antecedent (lefthand side) of this formula.
	 */
	public Formula getAntecedent() {
		return antecedent;
	}

	/**
	 * Returns the consequent (righthand side) of this formula.
	 */
	public Formula getConsequent() {
		return consequent;
	}

	public Object evaluate(EvalContext context) {
		Boolean anteValue = (Boolean) antecedent.evaluate(context);
		if (anteValue == null) {
			return null;
		}
		if (anteValue.booleanValue()) {
			// antecedent is true
			// in this case, implication is true iff consequent is true
			Boolean consValue = (Boolean) consequent.evaluate(context);
			return consValue;
		}

		// antecedent is false
		// implication is vacuously true
		return Boolean.TRUE;
	}

	/**
	 * The standard form of an implication formula phi -> psi is the standard form
	 * of the equivalent formula !phi | psi.
	 */
	public Formula getStandardForm() {
		Formula equiv = new DisjFormula(new NegFormula(antecedent), consequent);
		return equiv.getStandardForm();
	}

	/**
	 * The formula equivalent to the negation of phi -> psi is phi & !psi.
	 */
	protected Formula getEquivToNegationInternal() {
		return new ConjFormula(antecedent, new NegFormula(consequent));
	}

	public List getSubFormulas() {
		List subFormulas = new ArrayList();
		subFormulas.add(antecedent);
		subFormulas.add(consequent);
		return Collections.unmodifiableList(subFormulas);
	}

	/**
	 * The CNF form of an implication phi -> psi is the CNF form of the equivalent
	 * formula !phi | psi.
	 */
	public ConjFormula getPropCNF() {
		Formula equiv = new DisjFormula(new NegFormula(antecedent), consequent);
		return equiv.getPropCNF();
	}

	/**
	 * The DNF form of an implication phi -> psi is the DNF form of the equivalent
	 * formula !phi | psi.
	 */
	public DisjFormula getPropDNF() {
		Formula equiv = new DisjFormula(new NegFormula(antecedent), consequent);
		return equiv.getPropDNF();
	}

	public Set getSatisfiersIfExplicit(EvalContext context, LogicalVar subject,
			GenericObject genericObj) {
		Set satisfiers = null;

		// A satisfier can make the antecedent false...

		Set anteNonSat = antecedent.getNonSatisfiersIfExplicit(context, subject,
				genericObj);
		if (anteNonSat == Formula.ALL_OBJECTS) {
			return Formula.ALL_OBJECTS; // no point in looking at consequent
		}
		if (anteNonSat == null) {
			// Any objects could fail to satisfy antecedent and thus
			// satisfy formula
			return null;
		}

		satisfiers = anteNonSat;

		// ...or make the consequent true

		Set conseqSat = consequent.getSatisfiersIfExplicit(context, subject,
				genericObj);
		if (conseqSat == Formula.ALL_OBJECTS) {
			return Formula.ALL_OBJECTS;
		}
		if (conseqSat == null) {
			// Any objects could satisfy consequent and thus satisfy formula
			return null;
		}

		if (satisfiers == null) {
			satisfiers = conseqSat;
		} else {
			// Some satisfiers make antecedent false, others make
			// consequent true.
			satisfiers = new LinkedHashSet(satisfiers); // in case unmodifiable
			satisfiers.addAll(conseqSat);
		}

		return satisfiers;
	}

	public Set getNonSatisfiersIfExplicit(EvalContext context,
			LogicalVar subject, GenericObject genericObj) {
		Set anteSat = antecedent.getSatisfiersIfExplicit(context, subject,
				genericObj);
		if ((anteSat != null) && (anteSat != Formula.ALL_OBJECTS)) {
			// We have explicit set of objects that satisfy antecendent.
			// Non-satisfiers are those that don't satisfy consequent.
			Set nonSatisfiers = new LinkedHashSet();
			for (Iterator iter = anteSat.iterator(); iter.hasNext();) {
				Object subjectValue = iter.next();
				context.assign(subject, subjectValue);
				Boolean conseqValue = (Boolean) consequent.evaluate(context);
				if ((conseqValue != null) && !conseqValue.booleanValue()) {
					nonSatisfiers.add(subjectValue);
				}
			}
			context.unassign(subject);
			return nonSatisfiers;
		}

		Set conseqNonSat = consequent.getNonSatisfiersIfExplicit(context, subject,
				genericObj);
		if (anteSat == Formula.ALL_OBJECTS) {
			// In this case, non-satisfiers of formula are exactly
			// those that don't satisfy consequent.
			return conseqNonSat;
		}
		if ((conseqNonSat != null) && (conseqNonSat != Formula.ALL_OBJECTS)) {
			// We have explicit set of objects that don't satisfy consequent.
			// Non-satisfiers are those that satisfy antecedent.
			Set nonSatisfiers = new LinkedHashSet();
			for (Iterator iter = conseqNonSat.iterator(); iter.hasNext();) {
				Object subjectValue = iter.next();
				context.assign(subject, subjectValue);
				Boolean anteValue = (Boolean) antecedent.evaluate(context);
				if ((anteValue != null) && anteValue.booleanValue()) {
					nonSatisfiers.add(subjectValue);
				}
			}
			context.unassign(subject);
			return nonSatisfiers;
		}

		return null;
	}

	public boolean equals(Object o) {
		if (o instanceof ImplicFormula) {
			ImplicFormula other = (ImplicFormula) o;
			return (antecedent.equals(other.getAntecedent()) && consequent
					.equals(other.getConsequent()));
		}
		return false;
	}

	public int hashCode() {
		return (getClass().hashCode() ^ antecedent.hashCode() ^ consequent
				.hashCode());
	}

	public String toString() {
		return (antecedent + " -> " + consequent);
	}

	public boolean checkTypesAndScope(Model model, Map scope) {
		return (antecedent.checkTypesAndScope(model, scope) && consequent
				.checkTypesAndScope(model, scope));
	}

	public ArgSpec replace(Term t, ArgSpec another) {
		Formula newAntecedent = (Formula) antecedent.replace(t, another);
		Formula newConsequent = (Formula) consequent.replace(t, another);
		if (newAntecedent != antecedent || newConsequent != consequent)
			return compileAnotherIfCompiled(new ImplicFormula(newAntecedent,
					newConsequent));
		return this;
	}

	public ArgSpec getSubstResult(Substitution subst, Set<LogicalVar> boundVars) {
		return new ImplicFormula((Formula) antecedent.getSubstResult(subst,
				boundVars), (Formula) consequent.getSubstResult(subst, boundVars));
	}

	private Formula antecedent;
	private Formula consequent;
}
