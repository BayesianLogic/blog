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
import blog.model.Formula;
import blog.model.LogicalVar;
import blog.model.Model;
import blog.model.Term;

/**
 * A Formula consisting of just the zero-ary logical operator "true", which is
 * true in every world under every assignment.
 */
public class TrueFormula extends Formula {
	/**
	 * A canonical instance of TrueFormula.
	 */
	public static final TrueFormula TRUE = new TrueFormula();

	/**
	 * Creates a new TrueFormula.
	 */
	public TrueFormula() {
	}

	public Object evaluate(EvalContext context) {
		return Boolean.TRUE;
	}

	/**
	 * The standard form of the formula "true" is an empty conjunction, which is
	 * always true.
	 */
	public Formula getStandardForm() {
		return new ConjFormula(Collections.EMPTY_LIST);
	}

	/**
	 * A formula equivalent to the negation of "true" is the empty disjunction,
	 * which is always false.
	 */
	protected Formula getEquivToNegationInternal() {
		return new DisjFormula(Collections.EMPTY_LIST);
	}

	/**
	 * The CNF form of TrueFormula has no conjuncts.
	 */
	public ConjFormula getPropCNF() {
		return new ConjFormula(Collections.EMPTY_LIST);
	}

	/**
	 * The DNF form of TrueFormula has just one disjunct, which is an empty
	 * conjunction.
	 */
	public DisjFormula getPropDNF() {
		return new DisjFormula(new ConjFormula(Collections.EMPTY_LIST));
	}

	public Set getSatisfiersIfExplicit(EvalContext context, LogicalVar subject,
			GenericObject genericObj) {
		return Formula.ALL_OBJECTS;
	}

	public Set getNonSatisfiersIfExplicit(EvalContext context,
			LogicalVar subject, GenericObject genericObj) {
		return Collections.EMPTY_SET;
	}

	/**
	 * Any two instances of TrueFormula are equal.
	 */
	public boolean equals(Object o) {
		return (o instanceof TrueFormula);
	}

	public int hashCode() {
		return Boolean.TRUE.hashCode();
	}

	/**
	 * Returns the string "true".
	 */
	public String toString() {
		return "true";
	}

	public boolean checkTypesAndScope(Model model, Map scope) {
		return true;
	}

	public ArgSpec replace(Term t, ArgSpec another) {
		return this;
	}

	public ArgSpec getSubstResult(Substitution subst, Set<LogicalVar> boundVars) {
		return this;
	}
}
