/*
 * Copyright (c) 2008 Massachusetts Institute of Technology
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
 * * Neither the name of the Massachusetts Institute of Technology nor
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

package fove;

import java.util.*;
import blog.*;
import common.CartesianProduct;
import ve.Potential;
import ve.MultiArrayPotential;

/**
 * Class of operators that expand a counting formula into a list of atomic
 * formulas, one for each possible grounding of the logical variable that the
 * counting formula ranges over.
 */
public class CountExpansion extends LiftedInfOperator {

	private Set<Parfactor> parfactors;
	private Parfactor phi;
	private Parfactor.TermPtr countTermPtr;
	private LogicalVar x;

	private CountExpansion(Set<Parfactor> parfactors, Parfactor phi,
			Parfactor.TermPtr countTermPtr, LogicalVar x) {
		this.parfactors = parfactors;
		this.phi = phi;
		this.countTermPtr = countTermPtr;
		this.x = x;
	}

	public double logCost() {
		int curSize = phi.potential().size();
		CountingTerm countingTerm = (CountingTerm) countTermPtr.term();
		int numHists = countingTerm.getType().range().size();

		countingTerm.checkSingletons();
		FuncAppTerm subTerm = countingTerm.singleSubTerm();
		int subTermRangeSize = subTerm.getType().range().size();

		int numGroundings = countingTerm.constraint().numAllowedConstants(x);

		double logCost = (Math.log(curSize) - Math.log(numHists) + (numGroundings * Math
				.log(subTermRangeSize)));

		return logCost;
	}

	public void operate() {
		parfactors.remove(phi);

		CountingTerm cTerm = (CountingTerm) countTermPtr.term();
		cTerm.checkSingletons();
		FuncAppTerm subTerm = cTerm.singleSubTerm();

		List<CountingTerm> newCountingTerms = Collections.emptyList();
		List<FuncAppTerm> newFuncApps = new ArrayList<FuncAppTerm>();

		Set<? extends Term> groundings = cTerm.constraint.allowedConstants(x);
		Substitution subst = new Substitution();
		for (Term c : groundings) {
			subst.add(x, c);
			FuncAppTerm newTerm = (FuncAppTerm) subTerm.getSubstResult(subst);
			newFuncApps.add(newTerm);
		}

		Parfactor result = phi.expandCountInto(countTermPtr.index(),
				newCountingTerms, newFuncApps);
		parfactors.add(result);

		LiftedVarElim.shatter(parfactors, Collections.EMPTY_LIST);
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Expansion(");
		buf.append(x);
		buf.append(" in formula ");
		buf.append(countTermPtr.index());
		buf.append(" of ");
		buf.append(phi);
		buf.append(")");
		return buf.toString();
	}

	public static Collection<LiftedInfOperator> opFactory(
			Set<Parfactor> parfactors, ElimTester query) {
		List<LiftedInfOperator> ops = new LinkedList<LiftedInfOperator>();

		for (Parfactor phi : parfactors) {
			List<? extends Term> terms = phi.dimTerms();
			for (int i = 0; i < terms.size(); ++i) {
				ArgSpec term = terms.get(i);
				if (term instanceof CountingTerm) {
					maybeAddOps(parfactors, phi, i, (CountingTerm) term, ops);
				}
			}
		}

		return ops;
	}

	private static void maybeAddOps(Set<Parfactor> parfactors, Parfactor phi,
			int index, CountingTerm countingTerm, List<LiftedInfOperator> ops) {
		Constraint constraint = countingTerm.constraint();
		for (LogicalVar x : countingTerm.logicalVars()) {
			// Make sure excluded set for x contains no logical variables.
			// If it does contain logical variables, then the set of
			// allowed groundings for x varies across groundings of the
			// parfactor phi, so we would need to propositionalize before
			// expanding.
			if (constraint.excluded(x).size() == constraint.excludedConstants(x)
					.size()) {
				ops.add(new CountExpansion(parfactors, phi, phi.termPtr(index), x));
			}
		}
	}
}
