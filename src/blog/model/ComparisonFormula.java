/*
 * Copyright (c) 2006, Regents of the University of California
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

import blog.Substitution;
import blog.absyn.OpExpr;
import blog.bn.BayesNetVar;
import blog.bn.DerivedVar;
import blog.bn.RandFuncAppVar;
import blog.common.Util;
import blog.sample.EvalContext;

/**
 * @author rbharath
 *
 * A term consisting of two terms and a comparison between them. Comparison terms
 * are inserted in the model by transExpr.
 */
public class ComparisonFormula extends Formula {
    /**
     * Creates a Comparison Term with the two given left and right terms and 
     * comparison type. The possible comparison type values are given as static
     * constants in OpExpr.
     */
    public ComparisonFormula(Term l, Term r, int t) {
        left = l;
        right = r;
        compType = t;
    }
    
    public Object evaluate(EvalContext context) {
		Object t1Value = left.evaluate(context);
		if (t1Value == null) {
			return null;
		}

		Object t2Value = right.evaluate(context);
		if (t2Value == null) {
			return null;
		}

//		if (t1Value == t2Value) {
//			return Boolean.TRUE; // even if they're both a GenericObject
//		}
		if ((t1Value instanceof GenericObject)
				|| (t2Value instanceof GenericObject)) {
			// Can't tell what GenericObject is equal to, besides itself
			return null;
		}

		// TODO: complete comparisons for all operators
		if (compType == OpExpr.LT) {
			if (!(t1Value instanceof Comparable)) {
				return null;
			}
			Comparable leftVal = (Comparable) t1Value;
			return Boolean.valueOf(leftVal.compareTo(t2Value) < 0);
		}
		else if (compType == OpExpr.GT) {
			if (!(t1Value instanceof Comparable)) {
				return null;
			}
			Comparable leftVal = (Comparable) t1Value;
			return Boolean.valueOf(leftVal.compareTo(t2Value) > 0);
		}
		
		return null;
    }

	public Set getSatisfiersIfExplicit(EvalContext context,
			LogicalVar subject, GenericObject genericObj) {
		Set result = null;
		context.assign(subject, genericObj);

		Term other = getCompareTerm(subject);
		if (other != null) {
			// Subject is one of the terms in this formula. If the
			// other term can be evaluated for genericObj, then the
			// only possible satisifer is the value of that other term.
			Object otherValue = other.evaluate(context);
			if (otherValue != null) {
				if (genericObj.isConsistentInContext(context, otherValue)) {
					result = Collections.singleton(otherValue);
				} else {
					result = Collections.EMPTY_SET;
				}
			}
		} else {
			// If this formula can be evaluated for genericObj, we can still
			// determine the satisfier set.
			Boolean value = (Boolean) evaluate(context);
			if (value != null) {
				result = (value.booleanValue() == true ? Formula.ALL_OBJECTS
						: Collections.EMPTY_SET);
			}
		}

		context.unassign(subject);
		return result;

    }

    public boolean checkTypesAndScope(Model model, Map scope) {
        if ((left == null) || (right == null)) {
            return false;
        }

		Term eq1InScope = left.getTermInScope(model, scope);
		Term eq2InScope = right.getTermInScope(model, scope);
		if ((eq1InScope == null) || (eq2InScope == null)) {
			return false;
		}
		left = eq1InScope;
		right = eq2InScope;

		Type t1 = left.getType();
		Type t2 = right.getType();
		if ((t1 == null) || (t2 == null) || t1.isSubtypeOf(t2)
				|| t2.isSubtypeOf(t1)) {
			return true;
		} else {
			System.err.println(getLocation() + ": Terms in comparison "
					+ "formula are of disjoint types");
			return false;
		}
    }
    
    public ArgSpec getSubstResult(Substitution subst, Set<LogicalVar> boundVars) {
		return new ComparisonFormula((Term) left.getSubstResult(subst, boundVars),
				(Term) right.getSubstResult(subst, boundVars), compType);
    }

    public ArgSpec replace(Term t, ArgSpec another) {
		Term newEq1 = (Term) left.replace(t, another);
		Term newEq2 = (Term) right.replace(t, another);
		if (newEq1 != left || newEq2 != right)
			return compileAnotherIfCompiled(new ComparisonFormula(newEq1, newEq2, compType));
		return this;
    }

    public boolean containsRandomSymbol() {
        return false;
    }
    
	/**
	 * Returns the term that, according to this comparison formula, is the basis for
	 * comparison of <code>subject</code>. Returns null if <code>subject</code> is
	 * not one of the terms in this comparison.
	 */
	public Term getCompareTerm(Term subject) {
		if (left.equals(subject)) {
			return right;
		}
		if (right.equals(subject)) {
			return left;
		}
		return null;
	}
	
	// TODO: convert the integer code of the comparison to a string
	public String toString() {
		return "(" + left.toString() + " " + compType + " " + right.toString() + ")";
	}

    private Term left;
    private Term right;
    private int compType;
}

