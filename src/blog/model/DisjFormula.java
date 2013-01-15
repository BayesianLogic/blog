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

import blog.common.TupleIterator;
import blog.sample.EvalContext;


/**
 * Represents a disjunction of expressions, each of which is a Formula.
 * 
 * @see blog.model.Formula
 */
public class DisjFormula extends Formula {

	/**
	 * Creates a new disjunction with just one disjunct, namely the given formula.
	 */
	public DisjFormula(Formula disj) {
		disjuncts = new ArrayList();
		disjuncts.add(disj);
	}

	public DisjFormula(Formula disj1, Formula disj2) {
		disjuncts = new ArrayList();
		disjuncts.add(disj1);
		disjuncts.add(disj2);
	}

	/**
	 * Creates a new disjunction formula with the given disjuncts.
	 * 
	 * @param disjuncts
	 *          a List of Formula objects
	 */
	public DisjFormula(List disjuncts) {
		this.disjuncts = new ArrayList(disjuncts);
	}

	/**
	 * Returns a List of Formula objects representing the disjuncts in this
	 * disjunction formula.
	 */
	public List getDisjuncts() {
		return Collections.unmodifiableList(disjuncts);
	}

	public Object evaluate(EvalContext context) {
		for (Iterator iter = disjuncts.iterator(); iter.hasNext();) {
			Formula disj = (Formula) iter.next();
			Boolean disjValue = (Boolean) disj.evaluate(context);
			if (disjValue == null) {
				return null;
			}

			if (disjValue.booleanValue()) {
				// short-circuit; formula is true
				return Boolean.TRUE;
			}
		}

		// formula is false
		return Boolean.FALSE;
	}

	/**
	 * The standard form of a disjunction formula is just the disjunction of the
	 * standard forms of its disjuncts.
	 */
	public Formula getStandardForm() {
		List stdDisjuncts = new ArrayList();
		for (Iterator iter = disjuncts.iterator(); iter.hasNext();) {
			Formula disj = (Formula) iter.next();
			stdDisjuncts.add(disj.getStandardForm());
		}
		return new DisjFormula(stdDisjuncts);
	}

	/**
	 * The formula equivalent to the negation of psi_1 | ... | psi_N is !psi_1 &
	 * ... & !psi_N (this is one of De Morgan's laws).
	 */
	protected Formula getEquivToNegationInternal() {
		List negs = new ArrayList();
		for (Iterator iter = disjuncts.iterator(); iter.hasNext();) {
			Formula disj = (Formula) iter.next();
			negs.add(new NegFormula(disj));
		}
		return new ConjFormula(negs);
	}

	public List getSubformulas() {
		return Collections.unmodifiableList(disjuncts);
	}

	/**
	 * To get the CNF form of a disjunction formula, we convert each disjunct to
	 * CNF, then use the distributive property of OR over AND. In its basic form,
	 * this says ((a & b) | c) is equivalent to ((a | c) & (b | c)). More
	 * generally, if we choose one conjunct from each disjunct, then at least one
	 * of these conjuncts must be satisfied. We get an AND over ways of choosing
	 * one conjunct from each disjunct.
	 * <p>
	 * We begin by converting each disjunct to CNF, yielding something like:
	 * ((psi_{1,1} & ... & psi_{1,k1}) | ... | (psi_{n,1} & ... & psi_{n,kn}))
	 * where each psi is a disjunction of literals. Then we form a disjunction for
	 * each tuple (chi_1, ..., chi_n) in the cross product {psi_{1,1}, ...,
	 * psi_{1,k1}} x ... x {psi_{n,1}, ..., psi_{n,kn}}, then put all these
	 * disjunctions together in a big conjunction.
	 * <p>
	 * Special cases: if this is an empty disjunction, then the output is a
	 * conjunction consisting of one empty disjunction, which is always false. If
	 * this formula contains a disjunct whose CNF form is an empty conjunction,
	 * then the output is an empty conjunction, which is always true.
	 */
	public ConjFormula getPropCNF() {
		// Handle easy case first
		if (isElementary()) {
			return new ConjFormula(this);
		}

		List conjunctLists = new ArrayList();
		for (Iterator disjIter = disjuncts.iterator(); disjIter.hasNext();) {
			Formula disj = (Formula) disjIter.next();
			conjunctLists.add(disj.getPropCNF().getConjuncts());
		}

		List outputConjuncts = new ArrayList();
		for (Iterator tupleIter = new TupleIterator(conjunctLists); tupleIter
				.hasNext();) {
			List tuple = (List) tupleIter.next();

			// We have a tuple of DisjFormulas. Flatten them into one
			// DisjFormula and add it as an output conjunct.
			List disjunctsFromTuple = new ArrayList();
			for (Iterator disjunctionIter = tuple.iterator(); disjunctionIter
					.hasNext();) {
				DisjFormula disjunction = (DisjFormula) disjunctionIter.next();
				disjunctsFromTuple.addAll(disjunction.getDisjuncts());
			}
			outputConjuncts.add(new DisjFormula(disjunctsFromTuple));
		}

		return new ConjFormula(outputConjuncts);
	}

	/**
	 * To get the DNF form of a disjunction formula, we get the DNF forms of all
	 * its disjuncts, then agglomerate all the disjuncts of the resulting DNF
	 * forms in one big disjunction.
	 */
	public DisjFormula getPropDNF() {
		List outputDisjuncts = new ArrayList();
		for (Iterator iter = disjuncts.iterator(); iter.hasNext();) {
			Formula disj = (Formula) iter.next();
			DisjFormula dnfForm = disj.getPropDNF();
			outputDisjuncts.addAll(dnfForm.getDisjuncts());
		}
		return new DisjFormula(outputDisjuncts);
	}

	public Set getSatisfiersIfExplicit(EvalContext context, LogicalVar subject,
			GenericObject genericObj) {
		Set satisfiers = null;
		boolean createdNewSet = false;

		for (Iterator iter = disjuncts.iterator(); iter.hasNext();) {
			Formula disj = (Formula) iter.next();
			Set disjSat = disj.getSatisfiersIfExplicit(context, subject, genericObj);
			if (disjSat == Formula.ALL_OBJECTS) {
				// Short-circuit: all objects make this disjunct true,
				// so no point in looking at other disjuncts.
				return Formula.ALL_OBJECTS;
			}
			if (disjSat == null) {
				// Any objects could satisfy this disjunct and thereby
				// satisfy the formula.
				return null;
			} else {
				// Objects that make this disjunct true make formula true.
				if (satisfiers == null) {
					satisfiers = disjSat;
				} else {
					if (!createdNewSet) {
						// in case satisfiers was unmodifiable...
						satisfiers = new LinkedHashSet(satisfiers);
						createdNewSet = true;
					}
					satisfiers.addAll(disjSat);
				}
			}
		}

		return satisfiers;
	}

	/**
	 * Two DisjFormulas are equal if they have the same list of subformulas.
	 */
	public boolean equals(Object o) {
		if (o instanceof DisjFormula) {
			DisjFormula other = (DisjFormula) o;
			return disjuncts.equals(other.getDisjuncts());
		}
		return false;
	}

	public int hashCode() {
		return (getClass().hashCode() ^ disjuncts.hashCode());
	}

	/**
	 * Returns a string of the form (psi_1 | ... | psi_N) where psi_1, ..., psi_N
	 * are the subformulas of this DisjFormula.
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("(");
		if (!disjuncts.isEmpty()) {
			Iterator iter = disjuncts.iterator();
			buf.append(iter.next());
			while (iter.hasNext()) {
				buf.append(" | ");
				buf.append(iter.next());
			}
		}
		buf.append(")");
		return buf.toString();
	}

	public boolean checkTypesAndScope(Model model, Map scope) {
		boolean result = true; // disjunction of no fomulas is true
		Iterator disjIter = disjuncts.iterator();
		while (disjIter.hasNext()) {
			Formula form = (Formula) disjIter.next();
			if (!form.checkTypesAndScope(model, scope)) {
				return false;
			}
		}
		return true;
	}

	public ArgSpec replace(Term t, ArgSpec another) {
		List newDisjuncts = new LinkedList();
		boolean replacement = false;
		for (Iterator it = disjuncts.iterator(); it.hasNext();) {
			Formula disjunct = (Formula) it.next();
			Formula newDisjunct = (Formula) disjunct.replace(t, another);
			if (newDisjunct != disjunct)
				replacement = true;
			newDisjuncts.add(newDisjunct);
		}
		if (replacement)
			return compileAnotherIfCompiled(new DisjFormula(newDisjuncts));
		return this;
	}

	public ArgSpec getSubstResult(Substitution subst, Set<LogicalVar> boundVars) {
		List<Formula> newDisjuncts = new ArrayList<Formula>(disjuncts.size());
		for (Iterator iter = disjuncts.iterator(); iter.hasNext();) {
			Formula disj = (Formula) iter.next();
			newDisjuncts.add((Formula) disj.getSubstResult(subst, boundVars));
		}
		return new DisjFormula(newDisjuncts);
	}

	private List disjuncts; // of Formula
}
