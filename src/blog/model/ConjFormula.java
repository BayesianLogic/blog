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
import blog.common.Util;
import blog.sample.EvalContext;

/**
 * Represents a conjuction of expressions, each of which is a Formula.
 * 
 * @see blog.model.Formula
 */
public class ConjFormula extends Formula {

	/**
	 * Creates a new conjunction with just one conjunct, namely the given formula.
	 */
	public ConjFormula(Formula conj) {
		conjuncts = new ArrayList();
		conjuncts.add(conj);
	}

	public ConjFormula(Formula conj1, Formula conj2) {
		conjuncts = new ArrayList();
		conjuncts.add(conj1);
		conjuncts.add(conj2);
	}

	/**
	 * Creates a new conjunction formula with the given conjuncts.
	 * 
	 * @param conjuncts
	 *          a List of Formula objects
	 */
	public ConjFormula(List conjuncts) {
		this.conjuncts = new ArrayList(conjuncts);
	}

	/**
	 * Returns a List of Formula objects representing the conjuncts in this
	 * conjunction formula.
	 */
	public List getConjuncts() {
		return Collections.unmodifiableList(conjuncts);
	}

	public Object evaluate(EvalContext context) {
		for (Iterator iter = conjuncts.iterator(); iter.hasNext();) {
			Formula conj = (Formula) iter.next();
			Boolean conjValue = (Boolean) conj.evaluate(context);
			if (conjValue == null) {
				return null;
			}

			if (!conjValue.booleanValue()) {
				// short-circuit; formula is false
				return Boolean.FALSE;
			}
		}

		// formula is true
		return Boolean.TRUE;
	}

	/**
	 * The standard form of a conjunction formula is just the conjunction of the
	 * standard forms of its conjuncts.
	 */
	public Formula getStandardForm() {
		List stdConjuncts = new ArrayList();
		for (Iterator iter = conjuncts.iterator(); iter.hasNext();) {
			Formula conj = (Formula) iter.next();
			stdConjuncts.add(conj.getStandardForm());
		}
		return new ConjFormula(stdConjuncts);
	}

	/**
	 * The formula equivalent to the negation of psi_1 & ... & psi_N is !psi_1 |
	 * ... | !psi_N (this is one of De Morgan's laws).
	 */
	protected Formula getEquivToNegationInternal() {
		List negs = new ArrayList();
		for (Iterator iter = conjuncts.iterator(); iter.hasNext();) {
			Formula conj = (Formula) iter.next();
			negs.add(new NegFormula(conj));
		}
		return new DisjFormula(negs);
	}

	public List getSubformulas() {
		return Collections.unmodifiableList(conjuncts);
	}

	/**
	 * To get the CNF form of a conjunction, we first convert its conjuncts into
	 * CNF, then agglomerate all the conjuncts of the resulting CNF forms together
	 * in a big conjunction.
	 */
	public ConjFormula getPropCNF() {
		List outputConjuncts = new ArrayList();
		for (Iterator iter = conjuncts.iterator(); iter.hasNext();) {
			Formula conj = (Formula) iter.next();
			ConjFormula cnfForm = conj.getPropCNF();
			outputConjuncts.addAll(cnfForm.getConjuncts());
		}
		return new ConjFormula(outputConjuncts);
	}

	/**
	 * To get the DNF form of a conjunction formula, we convert each conjunct to
	 * DNF, then use the distributive property of AND over OR. In its basic form,
	 * this says ((a | b) & c) is equivalent to ((a & c) | (b & c)). More
	 * generally, in order to satisfy the conjunction, we need to satisfy one
	 * disjunct from every conjunct. We get an OR over ways to choose one disjunct
	 * from each conjuct.
	 * <p>
	 * We begin by converting each conjunct to DNF, yielding something like:
	 * ((psi_{1,1} | ... | psi_{1,k1}) & ... & (psi_{n,1} | ... | psi_{n,kn}))
	 * where each psi is a conjunction of literals. Then we form a conjunction for
	 * each tuple (chi_1, ..., chi_n) in the cross product {psi_{1,1}, ...,
	 * psi_{1,k1}} x ... x {psi_{n,1}, ..., psi_{n,kn}}, then put all these
	 * conjunctions together in a big disjunction.
	 * <p>
	 * Special cases: if this is an empty conjunction, the output is a disjunction
	 * consisting of one empty conjunction, which is always true. If this formula
	 * contains a conjunct whose DNF form is an empty disjunction, then the output
	 * is an empty disjunction, which is always false.
	 */
	public DisjFormula getPropDNF() {
		// Handle easy case first
		if (isElementary()) {
			return new DisjFormula(this);
		}

		List disjunctLists = new ArrayList();
		for (Iterator conjIter = conjuncts.iterator(); conjIter.hasNext();) {
			Formula conj = (Formula) conjIter.next();
			disjunctLists.add(conj.getPropDNF().getDisjuncts());
		}

		List outputDisjuncts = new ArrayList();
		for (Iterator tupleIter = new TupleIterator(disjunctLists); tupleIter
				.hasNext();) {
			List tuple = (List) tupleIter.next();

			// We have a tuple of ConjFormulas. Flatten them into
			// one ConjFormula and add it as an output disjunct.
			List conjunctsFromTuple = new ArrayList();
			for (Iterator conjunctionIter = tuple.iterator(); conjunctionIter
					.hasNext();) {
				ConjFormula conjunction = (ConjFormula) conjunctionIter.next();
				conjunctsFromTuple.addAll(conjunction.getConjuncts());
			}
			outputDisjuncts.add(new ConjFormula(conjunctsFromTuple));
		}

		return new DisjFormula(outputDisjuncts);
	}

	public Set getSatisfiersIfExplicit(EvalContext context, LogicalVar subject,
			GenericObject genericObj) {
		boolean[] satByAllObjs = new boolean[conjuncts.size()];

		for (int i = 0; i < conjuncts.size(); ++i) {
			Formula conj = (Formula) conjuncts.get(i);
			Set conjSat = conj.getSatisfiersIfExplicit(context, subject, genericObj);
			satByAllObjs[i] = (conjSat == Formula.ALL_OBJECTS);

			if ((conjSat != null) && (conjSat != Formula.ALL_OBJECTS)) {
				// We have explicit set of objects satisfying this conjunct.
				// Formula satisfiers are those that satisfy all other
				// conjuncts too.
				Set satisfiers = new LinkedHashSet();
				for (Iterator iter = conjSat.iterator(); iter.hasNext();) {
					Object subjectVal = iter.next();
					context.assign(subject, subjectVal);
					if (allOtherConjunctsTrue(context, satByAllObjs, i)) {
						satisfiers.add(subjectVal);
					}
				}
				context.unassign(subject);
				return satisfiers;
			}
		}

		// We did not get an explicit satisfier set for any conjunct.
		// We have to return null unless all conjuncts are satisfied
		// by all objects, in which case we return Formula.ALL_OBJECTS.
		for (int i = 0; i < satByAllObjs.length; ++i) {
			if (!satByAllObjs[i]) {
				return null;
			}
		}
		return Formula.ALL_OBJECTS;
	}

	/**
	 * Two ConjFormulas are equal if they have the same list of subformulas.
	 */
	public boolean equals(Object o) {
		if (o instanceof ConjFormula) {
			ConjFormula other = (ConjFormula) o;
			return conjuncts.equals(other.getConjuncts());
		}
		return false;
	}

	public int hashCode() {
		return (getClass().hashCode() ^ conjuncts.hashCode());
	}

	/**
	 * Returns a string of the form (psi_1 & ... & psi_N) where psi_1, ..., psi_N
	 * are the subformulas of this ConjFormula.
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("(");
		if (!conjuncts.isEmpty()) {
			Iterator iter = conjuncts.iterator();
			buf.append(iter.next());
			while (iter.hasNext()) {
				buf.append(" & ");
				buf.append(iter.next());
			}
		}
		buf.append(")");
		return buf.toString();
	}

	public boolean checkTypesAndScope(Model model, Map scope) {
		boolean result = true; // conjunction of no formulas is true
		Iterator conjIter = conjuncts.iterator();
		while (conjIter.hasNext()) {
			Formula form = (Formula) conjIter.next();
			if (!form.checkTypesAndScope(model, scope)) {
				return false;
			}
		}
		return true;
	}

	public ArgSpec getSubstResult(Substitution subst, Set<LogicalVar> boundVars) {
		List<Formula> newConjuncts = new ArrayList<Formula>(conjuncts.size());
		for (Iterator iter = conjuncts.iterator(); iter.hasNext();) {
			Formula conj = (Formula) iter.next();
			newConjuncts.add((Formula) conj.getSubstResult(subst, boundVars));
		}
		return new ConjFormula(newConjuncts);
	}

	private boolean allOtherConjunctsTrue(EvalContext context,
			boolean[] satByAllObjs, int nominatingIndex) {
		for (int i = 0; i < conjuncts.size(); ++i) {
			if ((i > nominatingIndex) || ((i < nominatingIndex) && !satByAllObjs[i])) {
				Formula conj = (Formula) conjuncts.get(i);
				Boolean conjValue = (Boolean) conj.evaluate(context);
				if ((conjValue == null) || !conjValue.booleanValue()) {
					return false;
				}
			}
		}

		return true;
	}

	public ArgSpec replace(Term t, ArgSpec another) {
		List newConjuncts = new LinkedList();
		boolean replacement = false;
		for (Iterator it = conjuncts.iterator(); it.hasNext();) {
			Formula conjunct = (Formula) it.next();
			Formula newConjunct = (Formula) conjunct.replace(t, another);
			if (newConjunct != conjunct)
				replacement = true;
			newConjuncts.add(newConjunct);
		}
		if (replacement)
			return compileAnotherIfCompiled(new ConjFormula(newConjuncts));
		return this;
	}

	private List conjuncts; // of Formula
}