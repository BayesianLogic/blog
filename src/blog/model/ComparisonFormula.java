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

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import blog.sample.EvalContext;

/**
 * @see BuiltInFunction.java
 *
 *      A term consisting of two terms and a comparison between them.
 *      Comparison terms
 *      are inserted in the model by transExpr.
 * @author rbharath
 * @author leili
 * @date 2013/1/1
 */
public class ComparisonFormula extends Formula {
  
  public enum Operator {
    LT("<"), LEQ("<="), GT(">"), GEQ(">=");
    private final String name;

    Operator(String name) {
      this.name = name;
    }

    public String toString() {
      return name;
    }
  }

  /**
   * Creates a Comparison Term with the two given left and right terms and
   * comparison type. The possible comparison type values are given as static
   * constants in OpExpr.
   */
  public ComparisonFormula(Term l, Term r, Operator t) {
    left = l;
    right = r;
    compator = t;
  }
  
  protected Formula getEquivToNegationInternal() {
    Operator oppositeOp = null;
    switch (compator) {
      case LT:
        oppositeOp = Operator.GEQ;
      case LEQ:
        oppositeOp = Operator.GT;
      case GT:
        oppositeOp = Operator.LEQ;
      case GEQ:
        oppositeOp = Operator.LT;
    }
    return new ComparisonFormula(left, right, oppositeOp);
  }

  public Object evaluate(EvalContext context) {
    Object t1Value = left.evaluate(context);
    if (t1Value == null || (!(t1Value instanceof Comparable))) {
      return null;
    }

    Object t2Value = right.evaluate(context);
    if (t2Value == null) {
      return null;
    }

    if ((t1Value instanceof GenericObject)
        || (t2Value instanceof GenericObject)) {
      // Can't tell what GenericObject is equal to, besides itself
      return null;
    }

    switch (compator) {
    case LT:
      return Boolean.valueOf(((Comparable) t1Value).compareTo(t2Value) < 0);
    case LEQ:
      return Boolean.valueOf(((Comparable) t1Value).compareTo(t2Value) <= 0);
    case GT:
      return Boolean.valueOf(((Comparable) t1Value).compareTo(t2Value) > 0);
    case GEQ:
      return Boolean.valueOf(((Comparable) t1Value).compareTo(t2Value) >= 0);
    }
    return null;
  }

  public Set getSatisfiersIfExplicit(EvalContext context, LogicalVar subject,
      GenericObject genericObj) {
    Term otherOperand = (Term)getCompareTerm(subject);
    
    if (genericObj.getType().equals(BuiltInTypes.INTEGER)) {
      Set vals = new HashSet();
      switch(compator) {
        case LT:
          for (int i = 1; i < (Integer)otherOperand.evaluate(context); i++) {
            vals.add(i);
          }
          return vals;
        case LEQ:
          for (int i = 1; i <= (Integer)otherOperand.evaluate(context); i++) {
            vals.add(i);
          }
          return vals;
        default:
          return Formula.NOT_EXPLICIT;
      }
    }
    else {
      return Formula.NOT_EXPLICIT;
    }
  }
  
  @Override
  public Set getNonSatisfiersIfExplicit(EvalContext context, LogicalVar subject,
  			GenericObject genericObj) {
   Term otherOperand = (Term)getCompareTerm(subject);
    
    if (genericObj.getType().equals(BuiltInTypes.INTEGER)) {
      Set vals = new HashSet();
      switch(compator) {
        case GT:
          for (int i = 1; i <= (Integer)otherOperand.evaluate(context); i++) {
            vals.add(i);
          }
          return vals;
        case GEQ:
          for (int i = 1; i < (Integer)otherOperand.evaluate(context); i++) {
            vals.add(i);
          }
          return vals;
        default:
          return Formula.NOT_EXPLICIT;
      }
    }
    else {
      return Formula.NOT_EXPLICIT;
    }
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
        (Term) right.getSubstResult(subst, boundVars), compator);
  }

  public ArgSpec replace(Term t, ArgSpec another) {
    Term newEq1 = (Term) left.replace(t, another);
    Term newEq2 = (Term) right.replace(t, another);
    if (newEq1 != left || newEq2 != right)
      return compileAnotherIfCompiled(new ComparisonFormula(newEq1, newEq2,
          compator));
    return this;
  }

  public boolean containsRandomSymbol() {
    return false;
  }

  /**
   * Returns the term that, according to this comparison formula, is the basis
   * for
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
  
  public ComparisonFormula.Operator getCompareOp() {
    return compator;
  }
  
  public boolean isSubjectFirst(Term subject) {
    return left.equals(subject);
  }

  // TODO: convert the integer code of the comparison to a string
  public String toString() {
    return "(" + left.toString() + " " + compator + " " + right.toString()
        + ")";
  }

  private Term left;
  private Term right;
  private Operator compator;
}
