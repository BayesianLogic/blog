/*
 * Copyright (c) 2005, 2006, Regents of the University of California
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

import common.UnaryProcedure;


/** Represents a Bayesian atom. All specific kinds of terms are expected to
 * implement this interface.
 */
public abstract class Term extends ArgSpec {
    /**
     * Returns an object representing this term in the given scope.  This 
     * method exists to handle cases where a term's class depends on the 
     * scope where it occurs, such as when a term consists of a single 
     * symbol that may be either a logical variable or a function symbol.  
     * If calling checkTypesAndScope on this term would return false, then 
     * this method returns null.
     *
     * <p>The default implementation simply returns this object if 
     * checkTypesAndScope returns true, and null otherwise.
     *
     * @param model a BLOG model
     * @param scope a map from String to LogicalVar
     */
    public Term getTermInScope(Model model, Map scope) {
	return (checkTypesAndScope(model, scope) ? this : null);
    }

    /**
     * Returns the type of this term.  Throws an IllegalStateException 
     * if this term has not been compiled successfully (e.g., if this 
     * term is a variable that is not in scope).  
     */
    public abstract Type getType();

    /**
     * Returns true if the given term occurs in this term (or if the given 
     * term is equal to this term).  
     */
    public boolean containsTerm(Term target) {
	if (equals(target)) {
	    return true;
	}
	for (Iterator iter = getSubExprs().iterator(); iter.hasNext(); ) {
	    if (((Term) iter.next()).containsTerm(target)) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Returns the set of generating functions that are applied to the
     * term <code>subject</code> by this term or any of its subterms.
     *
     * <p>The default implementation returns the union of the generating 
     * functions applied in this term's sub-expressions.  This is 
     * overridden by FuncAppTerm.
     *
     * @return unmodifiable set of OriginFunc
     */
    public Set getGenFuncsApplied(Term subject) {
	Set genFuncs = new HashSet();
	for (Iterator iter = getSubExprs().iterator(); iter.hasNext(); ) {
	    genFuncs.addAll(((Term) iter.next()).getGenFuncsApplied(subject));
	}
	return Collections.unmodifiableSet(genFuncs);
    }

    public boolean isNumeric() {
	return getType().isSubtypeOf(BuiltInTypes.REAL);
    }

    /**
     * Returns true if this term is the constant term that always denotes 
     * Model.NULL.  The default implementation returns false.  
     */
    public boolean isConstantNull() {
	return false;
    }
    
    public Term find(Term t) {
	if (equals(t))
	    return this;
	for (Iterator it = getSubExprs().iterator(); it.hasNext(); ) {
	    Term sub = (Term) it.next();
	    Term result;
	    if ((result = sub.find(t)) != null) {
		return result;
	    }
	}
	return null;
    }

    public void applyToTerms(UnaryProcedure procedure) {
	procedure.evaluate(this);

	for (Iterator it = getSubExprs().iterator(); it.hasNext(); ) {
	    Term sub = (Term) it.next();
	    sub.applyToTerms(procedure);
	}
    }


    /**
     * Function for computing a substitution that will make the two
     * terms overlap.  Returns <code>null</code> if there is no such 
     * substitution.
     */
    public Substitution makeOverlapSubst(Term t){
	Substitution s = new Substitution();
	if (makeOverlapSubst(t, s))
	    return s;
	else
	    return null;
    }
    
    /**
     * If this term represents a number, returns it as a Double; otherwise, returns <code>null</code>.
     */
    public Double asDouble() {
	try {
	    FuncAppTerm faTerm = (FuncAppTerm) this;
	    NonRandomFunction nrFunction = (NonRandomFunction) faTerm.getFunction();
	    ConstantInterp interp = (ConstantInterp) nrFunction.getInterpretation();
	    Double d = (Double) interp.getValue(new LinkedList());
	    return d;
	}
	catch (ClassCastException e) {
	    return null;
	}
    }
    
    /**
     * Function for computing a substitution that will make the two
     * terms overlap.  The necessary mappings are added to the given 
     * substitution <code>theta</code>.  If there is no extension of 
     * <code>theta</code> that makes this term and <code>t</code> overlap, 
     * this method returns false.
     */
    public abstract boolean makeOverlapSubst(Term t, Substitution theta);

    /**
     * Returns a version of this term that uses canonical terms as
     * much as possible.  The canonical term for a guaranteed object
     * <code>obj</code> of type <code>type</code> is returned by
     * <code>type.getCanonicalTerm(obj)</code>.  A term can be
     * converted to canonical form only if it is non-random and has no
     * free variables (note that if the term is non-random, its value
     * must be a guaranteed object).  This method returns a canonical
     * version of this term if possible; otherwise, it returns a
     * version where the subterms have been converted to canonical
     * form as much as possible.
     */
    public abstract Term getCanonicalVersion();
}
