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

/**
 * Operation that sums all the random variables covered by a particular 
 * term, subject to a constraint on the free variables in that term.  
 * An object of this class stores pointers to all the terms in all the 
 * parfactors (in a given set) whose random variable sets are equal to the 
 * set being summed out.  
 */
public class SummingOut extends LiftedInfOperator {

    private SummingOut(Set<Parfactor> parfactors, 
		       List<Parfactor.TermPtr> targetPtrs) {
	this.parfactors = parfactors;
	this.targetPtrs = targetPtrs;
    }

    public double logCost() {
	// The cost is the number of entries in the potential obtained
	// after multiplication.
	initPfsToMultiply();
	
	Set<Term> termsInProduct = new HashSet<Term>();
	for (Parfactor pf : pfsToMultiply) {
	    termsInProduct.addAll(pf.dimTerms());
	}

	double logSize = 0;
	for (Term term : termsInProduct) {
	    logSize += Math.log(term.getType().range().size());
	}
	return logSize;
    }

    public void operate() {
	// Remove the affected parfactors from the parfactor set
	for (Parfactor.TermPtr targetPtr : targetPtrs) {
	    parfactors.remove(targetPtr.parfactor());
	}

	// Get versions of the affected parfactors where the logical 
	// variables are all the same.  Then multiply them together.
	initPfsToMultiply();
	Parfactor product = Parfactor.multiply(pfsToMultiply);

	// Figure out what the target term looks like after substitution, 
	// and find it in the product parfactor's term list
	Term target = pfsToMultiply.get(0).dimTerms()
	    .get(targetPtrs.get(0).index());
	int targetIndex = product.dimTerms().indexOf(target);
	if (targetIndex == -1) {
	    throw new IllegalStateException
		("Target term " + target + " is not in product " + product);
	}
	
	// Actually sum out that dimension from the potential
	Parfactor result = product.sumOut(targetIndex);

	// Exponentiate on any logical variables that are now unused
	for (LogicalVar var : result.getUnusedVars()) {
	    result = result.exponentiate(var);
	}

	parfactors.add(result);
    }

    public String toString() {
	StringBuffer buf = new StringBuffer();
	buf.append("SummingOut(");

	Parfactor.TermPtr firstPtr = targetPtrs.get(0);
	buf.append(firstPtr.term());
	buf.append(" : ");
	buf.append(firstPtr.parfactor().constraint());

	buf.append(")");
	return buf.toString();
    }

    private void initPfsToMultiply() {
	if (pfsToMultiply != null) {
	    return;
	}

	// Create a new list of logical variables of the appropriate types
	Parfactor.TermPtr firstPtr = targetPtrs.get(0);
	List<LogicalVar> firstArgVars = getArgVars(firstPtr.term());
	List<LogicalVar> newVars 
	    = new ArrayList<LogicalVar>(firstArgVars.size());
	for (LogicalVar orig : firstArgVars) {
	    newVars.add(orig.makeNew());
	}

	// For each affected parfactor, create an appropriate
	// substitution.  Then apply that substitution and store
	// resulting parfactor in list.
	pfsToMultiply = new ArrayList<Parfactor>(targetPtrs.size());
	for (Parfactor.TermPtr termPtr : targetPtrs) {
	    List<LogicalVar> argVars = getArgVars(termPtr.term());
	    Substitution subst = new Substitution(argVars, newVars);
	    pfsToMultiply.add(termPtr.parfactor().applySubstitution(subst));
	}	
    }

    public static Collection<LiftedInfOperator> opFactory
	    (Set<Parfactor> parfactors, ElimTester query) 
    {
	List ops = new ArrayList<LiftedInfOperator>();

	// Iterate over all parfactors, and all terms in each parfactor.  
	// For each term, see if the set of random variables it covers 
	// can be summed out of all the parfactors at a lifted level.  
	// 
	// Maintain a set of pointers to terms that are known to overlap 
	// (in random variables) with a term that has already been 
	// examined.  We can skip such terms, because they're either covered 
	// by an earlier operation or ineligible to be summed out.
	Set<Parfactor.TermPtr> overlappers = new HashSet<Parfactor.TermPtr>();
	for (Parfactor pf : parfactors) {
	    List<? extends Term> terms = pf.dimTerms();
	    Constraint constraint = pf.constraint();
	    for (int i = 0; i < terms.size(); ++i) {
		Parfactor.TermPtr termPtr = pf.termPtr(i);
		if (!overlappers.contains(termPtr)
		        && query.shouldEliminate(termPtr.term(),constraint)) {
		    SummingOut op = tryMakeOpForTerm(parfactors, termPtr, 
						     overlappers);
		    if (op != null) {
			ops.add(op);
		    }
		}
	    }
	}

	return ops;
    }

    private static SummingOut tryMakeOpForTerm(Set<Parfactor> parfactors, 
					       Parfactor.TermPtr candPtr, 
					       Set<Parfactor.TermPtr> 
					           overlappers) {
	Parfactor candParfactor = candPtr.parfactor();
	int candIndex = candPtr.index();
	Term candTerm = candPtr.term();
	Constraint candConstraint = candParfactor.constraint();
	//System.out.println("Considering " + candTerm + " : " + candConstraint);

	// First make sure all the candidate parfactor's variables occur 
	// as arguments in the candidate term.
	List<LogicalVar> candArgVars = getArgVars(candTerm);
	if (!candArgVars.containsAll(candParfactor.logicalVars())) {
	    //System.out.println("\tDoesn't have all logical vars.");
	    return null;
	}
	
	// Make sure the candidate term's random variables don't overlap 
	// with those of any other term in the same parfactor.
	List<? extends Term> siblings = candParfactor.dimTerms();
	for (int i = 0; i < siblings.size(); ++i) {
	    if ((i != candIndex) 
		&& (Constraint.getOverlap(candTerm, candConstraint, 
					  siblings.get(i), candConstraint)
		    != null)) {
		overlappers.add(candParfactor.termPtr(i));
		//System.out.println("\tOverlaps with sibling " 
		//		   + siblings.get(i));
		return null;
	    }
	}

	List<Parfactor.TermPtr> targetPtrs 
	    = new ArrayList<Parfactor.TermPtr>();
	targetPtrs.add(candPtr);

	// Iterate over all other parfactors.  In each one, find the first 
	// term that overlaps with candTerm.  If that term overlaps fully and 
	// has all the logical variables in its parfactor as arguments, 
	// then store a pointer to it in targetPtrs, and make sure none of 
	// the remaining terms overlap with candTerm at all.  Otherwise, 
	// return null.
	for (Parfactor curParfactor : parfactors) {
	    if (curParfactor == candParfactor) {
		continue;
	    }

	    Constraint curConstraint = curParfactor.constraint();
	    List<? extends Term> terms = curParfactor.dimTerms();
	    boolean gotTarget = false;
	    for (int i = 0; i < terms.size(); ++i) {
		Constraint.Overlap overlap = Constraint.getOverlap
		    (candTerm, candConstraint, terms.get(i), curConstraint);
		//System.out.println("\toverlap with (" + terms.get(i) + " : "
		//		   + curConstraint + "): " + overlap);
		if (overlap != null) {
		    Parfactor.TermPtr termPtr = curParfactor.termPtr(i);
		    overlappers.add(termPtr);

		    if ((!gotTarget) 
			    && (overlap.isFull())
			    && (getArgVars(termPtr.term())
				.containsAll(curParfactor.logicalVars()))
			    && (termPtr.term().getSubstResult(overlap.theta())
			        .equals(candTerm.getSubstResult
					(overlap.theta())))) {
			gotTarget = true;
			targetPtrs.add(termPtr);
			//System.out.println("\t\tAdded target.");
		    } else {
			//System.out.println("\t\tCan't sum out.");
			return null;
		    }
		}
	    }
	}

	return new SummingOut(parfactors, targetPtrs);
    }

    /**
     * Returns the logical variables that occur as arguments in the given 
     * term, in order of occurrence.
     */
    private static List<LogicalVar> getArgVars(Term term) {
	if (term instanceof FuncAppTerm) {
	    List<LogicalVar> argVars = new ArrayList<LogicalVar>();
	    ArgSpec[] args = ((FuncAppTerm) term).getArgs();
	    for (int i = 0; i < args.length; ++i) {
		if (args[i] instanceof LogicalVar) {
		    argVars.add((LogicalVar) args[i]);
		}
	    }
	    return argVars;
	} else if (term instanceof CountingTerm) {
	    // assume the counting term has just a single sub-term
	    return getArgVars(((CountingTerm) term).singleSubTerm());
	} 

	throw new IllegalArgumentException
	    ("Can't get argument variables in term of class " 
	     + term.getClass());
    }
	
    private Set<Parfactor> parfactors;
    private List<Parfactor.TermPtr> targetPtrs;
    
    // cached by initPfsToMultiply
    private List<Parfactor> pfsToMultiply = null; 
}
