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

import common.Multiset;
import common.HashMultiset;
import common.UnaryProcedure;
import common.Util;


/** Represents an argument - set with explicit listing of its elements. The 
 * elements are assumed to be Terms.
 */
public class ExplicitSetSpec extends ArgSpec{

    /**
     * Creates a new explicit set specification.
     *
     * @param terms List of Term objects
     */
    public ExplicitSetSpec(List terms) {

	this.terms = new ArrayList( terms );

    }


    public List getElts( ){

	return Collections.unmodifiableList(terms);

    }
    
    public Object evaluate(EvalContext context) {
	Multiset values = new HashMultiset();
	for (Iterator iter = terms.iterator(); iter.hasNext(); ) {
	    Term term = (Term) iter.next();
	    Object termValue = term.evaluate(context);
	    if (termValue == null) {
		return null;
	    }
	    values.add(termValue);
	}
	return new DefaultObjectSet(values);
    }

    public boolean containsRandomSymbol() {
	for (Iterator iter = terms.iterator(); iter.hasNext(); ) {
	    if (((Term) iter.next()).containsRandomSymbol()) {
		return true;
	    }
	}
	
	return false;
    }

    public boolean checkTypesAndScope(Model model, Map scope) {
	boolean correct = true;
	for (int i = 0; i < terms.size(); ++i) {
	    Term termInScope 
		= ((Term) terms.get(i)).getTermInScope(model, scope);
	    if (termInScope == null) {
		correct = false;
	    } else {
		terms.set(i, termInScope);
	    }
	}
	return correct;
    }

    public Collection getSubExprs() {
	return Collections.unmodifiableList(terms);
    }

    public ArgSpec getSubstResult(Substitution subst, 
				  Set<LogicalVar> boundVars) {
	List<Term> newTerms = new ArrayList<Term>(terms.size());
	for (Iterator iter = terms.iterator(); iter.hasNext(); ) {
	    Term term = (Term) iter.next();
	    newTerms.add((Term) term.getSubstResult(subst, boundVars));
	}
	return new ExplicitSetSpec(newTerms);
    }

    /**
     * Two explicit set specifications are equal if they have the same
     * list of terms (in the same order).  Explicit set specifications
     * with the same terms in different orders are equivalent, but we
     * do not consider them equal, just as we do not consider the
     * conjunctive formula (alpha & beta) equal to (beta & alpha).
     */
    public boolean equals(Object o) {
	if (o instanceof ExplicitSetSpec) {
	    ExplicitSetSpec other = (ExplicitSetSpec) o;
	    return terms.equals(other.getElts());
	}
	return false;
    }

    public int hashCode() {
	return terms.hashCode();
    }

    /**
     * Returns a string of the form {t1, ..., tK} where t1, ..., tK are 
     * the terms in this explicit set specification.
     */
    public String toString() {
	StringBuffer buf = new StringBuffer();
	buf.append("{");
	if (!terms.isEmpty()) {
	    buf.append(terms.get(0));
	    for (int i = 1; i < terms.size(); ++i) {
		buf.append(", ");
		buf.append(terms.get(i));
	    }
	}
	buf.append("}");
	return buf.toString();
    }
    
    public ArgSpec find(Term t){
	return (ArgSpec) Util.findFirstEquals(terms, t);
    }

    public void applyToTerms(UnaryProcedure procedure) {
	for (Iterator it = terms.iterator(); it.hasNext(); ) {
	    Term term = (Term) it.next();
	    term.applyToTerms(procedure);
	}
    }

    public ArgSpec replace(Term t, ArgSpec another) {
	List newTerms = new LinkedList();
	boolean replacement = false;
	for (Iterator it = terms.iterator(); it.hasNext(); ) {
	    Term term = (Term) it.next();
	    Term newTerm = (Term) term.replace(t, another);
	    replacement = replacement || newTerm != term;
	    newTerms.add(newTerm);
	}
	if (replacement)
	    return new ExplicitSetSpec(newTerms);
	return this;
    }

    // use List rather than Set for terms so we can print the terms in the 
    // same order the user listed them
    private List terms;


}
