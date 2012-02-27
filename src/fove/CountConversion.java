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
import ve.*;
import common.TupleIterator;

public class CountConversion extends LiftedInfOperator {

    private Set<Parfactor> parfactors;
    private Parfactor phi;
    private LogicalVar X;
    private FuncAppTerm termToCount;

    private List<Term> newDimTerms; // cached by initNewDimTerms
    private int countingTermIndex; // cached by initNewDimTerms
    private List<Integer> oldToNewIndices; // cached by initNewDimTerms

    private CountConversion(Set<Parfactor> parfactors, Parfactor phi, 
			    LogicalVar X, FuncAppTerm termToCount) {
	this.parfactors = parfactors;
	this.phi = phi;
	this.X = X;
	this.termToCount = termToCount;
    }

    public String toString() {
	return "CountConversion(" + X + " in " + termToCount + " in " + phi + ")";
    }

    public double logCost() {
	// The cost is the size of the resulting potential.

	initNewDimTerms();
	double logSize = 0;
	for (Term a : newDimTerms) {
	    logSize += Math.log(a.getType().range().size());
	}
	
	return logSize;
    }

    public void operate() {
	int countIndex = phi.dimTerms().indexOf(termToCount);

	// make the new list of terms and their types
	initNewDimTerms();
	List<Type> types = new ArrayList<Type>(phi.dimTerms().size());
	for (Term a : newDimTerms) {
	    types.add(a.getType());
	}

	// make the new potential
	Potential newPotential = new MultiArrayPotential(types);

	// fill the entries
	List<Collection<Object> > newDimTermRanges 
	    = new ArrayList<Collection<Object> >();
	for (Term f : newDimTerms) {
	    newDimTermRanges.add(f.getType().range());
	}

	TupleIterator outerIter = new TupleIterator(newDimTermRanges);
	while (outerIter.hasNext()){
	    List vals = (List)outerIter.next();
	    List fullInd = new ArrayList(oldToNewIndices.size());
	    for (int i = 0; i < oldToNewIndices.size(); ++i) {
		fullInd.add(i == countIndex ? 
			    null : vals.get(oldToNewIndices.get(i)));
	    }

	    Histogram h = (Histogram) vals.get(countingTermIndex);
	    // compute the product by iterating over buckets
	    double prod = 1.0;
	    for (int i=0; i<h.numBuckets(); i++){
		fullInd.set(countIndex,termToCount.getType().range().get(i));
		prod*=Math.pow(phi.potential().getValue(fullInd),h.getCount(i));		
	    }
	    
	    newPotential.setValue(vals,prod);
	}

	// update constraints
	Constraint newConstraint = phi.constraint().dropVar(X);

	// update logical variables
	List<LogicalVar> newLvs = new LinkedList<LogicalVar>(phi.logicalVars());
	newLvs.remove(X);

	parfactors.remove(phi);
	Parfactor newPhi = new Parfactor(newLvs,newConstraint,newDimTerms,
					 newPotential);
	parfactors.add(newPhi);

    }

    private void initNewDimTerms() {
	if (newDimTerms != null) {
	    return;
	}

	// get constraint for count atom
	Constraint countConstraint = phi.constraint().keepVar(X);

	// make a new variable name for the variable we are counting
	LogicalVar newVar = X.makeNew();
	Substitution theta = new Substitution();
	theta.add(X,newVar);

	// rename X in the constraint and termToCount
	countConstraint = countConstraint.getSubstResult(theta);
	Term newCountAtom = (Term) termToCount.getSubstResult(theta);
	
	CountingTerm ct 
	    = new CountingTerm(Collections.singletonList(newVar),
			       countConstraint,
			       Collections.singletonList(newCountAtom));
	
	List oldDimTerms = phi.dimTerms();
	int termToCountIndex = oldDimTerms.indexOf(termToCount);
	countingTermIndex = oldDimTerms.indexOf(ct);
	oldToNewIndices = new ArrayList<Integer>(oldDimTerms.size());
	newDimTerms = new ArrayList<Term>(oldDimTerms);
	if (countingTermIndex == -1) {
	    // No equivalent counting term exists already.  
	    // Just put the counting term at termToCountIndex.
	    countingTermIndex = termToCountIndex;
	    newDimTerms.set(countingTermIndex, ct);
	    for (int i = 0; i < oldDimTerms.size(); ++i) {
		oldToNewIndices.add(i);
	    }
	} else {
	    newDimTerms.remove(termToCountIndex);
	    for (int i = 0; i < oldDimTerms.size(); ++i) {
		oldToNewIndices.add(i < termToCountIndex ? i : i - 1);
	    }
	    countingTermIndex = oldToNewIndices.get(countingTermIndex);
	}
    }

    public static Collection<LiftedInfOperator> opFactory
	(Set<Parfactor> parfactors, ElimTester query) 
    {
	HashSet<LiftedInfOperator> valid_ops = new HashSet<LiftedInfOperator>();

	for(Parfactor phi : parfactors) {
	    for (LogicalVar x : phi.logicalVars()) {
		FuncAppTerm singleFuncApp = null;
		for (ArgSpec term : phi.dimTerms()) {
		    if (term.getFreeVars().contains(x)) {
			if ((singleFuncApp == null) 
			        && (term instanceof FuncAppTerm)) {
			    singleFuncApp = (FuncAppTerm) term;
			} else {
			    singleFuncApp = null;
			    break;
			}
		    }
		}

		if (singleFuncApp != null) {
		    valid_ops.add(new CountConversion(parfactors, phi, x, 
						      singleFuncApp));
		}
	    }
	}

	return valid_ops;
    }
}
			    
