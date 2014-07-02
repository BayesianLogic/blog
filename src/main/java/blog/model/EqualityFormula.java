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

package blog.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import blog.sample.EvalContext;

/**
 * Represents an equality test on 2 expressions, each of which is a
 * non-boolean-valued Term.
 * 
 * @see blog.model.Formula
 */
public class EqualityFormula extends Formula {

  public EqualityFormula(Term eq1, Term eq2) {

    this.eq1 = eq1;
    this.eq2 = eq2;

  }

  public Term getTerm1() {

    return eq1;

  }

  public Term getTerm2() {

    return eq2;

  }

  public Object evaluate(EvalContext context) {
    Object t1Value = eq1.evaluate(context);
    if (t1Value == null) {
      return null;
    }

    Object t2Value = eq2.evaluate(context);
    if (t2Value == null) {
      return null;
    }

    if (t1Value == t2Value) {
      return Boolean.TRUE; // even if they're both a GenericObject
    }
    if ((t1Value instanceof GenericObject)
        || (t2Value instanceof GenericObject)) {
      // Can't tell what GenericObject is equal to, besides itself
      return null;
    }

    return Boolean.valueOf(t1Value.equals(t2Value));
  }

  public Collection getSubExprs() {
    List terms = new ArrayList();
    terms.add(eq1);
    terms.add(eq2);
    return Collections.unmodifiableList(terms);
  }

  /**
   * Returns true.
   */
  public boolean isLiteral() {
    return true;
  }

  public List getTopLevelTerms() {
    List terms = new ArrayList();
    terms.add(eq1);
    terms.add(eq2);
    return Collections.unmodifiableList(terms);
  }

  /**
   * Returns the term that, according to this equality formula, has the same
   * denotation as <code>subject</code>. Returns null if <code>subject</code> is
   * not one of the terms in this equality.
   */
  public Term getEqualTerm(Term subject) {
    if (eq1.equals(subject)) {
      return eq2;
    }
    if (eq2.equals(subject)) {
      return eq1;
    }
    return null;
  }

  /**
   * Returns true if this equality formula asserts that <code>subject</code> has
   * the same denotation as the constant "null".
   */
  public boolean assertsNull(Term subject) {
    return ((eq1.equals(subject) && eq2.isConstantNull()) || (eq2
        .equals(subject) && eq1.isConstantNull()));
  }

  public Set getSatisfiersIfExplicit(EvalContext context, LogicalVar subject,
      GenericObject genericObj) {
    Set result = null;
    context.assign(subject, genericObj);

    Term other = getEqualTerm(subject);
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

  public Set getNonSatisfiersIfExplicit(EvalContext context,
      LogicalVar subject, GenericObject genericObj) {
    Set result = null;
    context.assign(subject, genericObj);

    // If this formula can be evaluated for genericObj, we can determine
    // the non-satisfier set.
    Boolean value = (Boolean) evaluate(context);
    if (value != null) {
      result = (value.booleanValue() == false ? Formula.ALL_OBJECTS
          : Collections.EMPTY_SET);
    }
    // Otherwise, nothing we can do

    context.unassign(subject);
    return result;
  }

  /**
   * Two EqualityFormulas are equal if they have the same pair of terms.
   */
  public boolean equals(Object o) {
    if (o instanceof EqualityFormula) {
      EqualityFormula other = (EqualityFormula) o;
      return (eq1.equals(other.getTerm1()) && eq2.equals(other.getTerm2()));
    }
    return false;
  }

  public int hashCode() {
    return (getClass().hashCode() ^ eq1.hashCode() ^ eq2.hashCode());
  }

  /**
   * Returns a string of the form (t1 = t2) where t1 and t2 are the terms being
   * compared in this EqualityFormula.
   */
  public String toString() {
    return ("(" + eq1 + " = " + eq2 + ")");
  }

  /**
   * An equality formula is properly typed if the type of one term is a subtype
   * of the type of the other term.
   */
  public boolean checkTypesAndScope(Model model, Map scope) {

    if ((eq1 == null) || (eq2 == null)) {
      return false;
    }

    Term eq1InScope = eq1.getTermInScope(model, scope);
    Term eq2InScope = eq2.getTermInScope(model, scope);
    if ((eq1InScope == null) || (eq2InScope == null)) {
      return false;
    }
    eq1 = eq1InScope;
    eq2 = eq2InScope;

    Type t1 = eq1.getType();
    Type t2 = eq2.getType();
    if ((t1 == null) || (t2 == null) || t1.isSubtypeOf(t2)
        || t2.isSubtypeOf(t1)) {
      return true;
    } else {
      System.err.println(getLocation() + ": Terms in equality/inequality "
          + "formula are of disjoint types " + t1.toString() + " and "
          + t2.toString());
      return false;
    }
  }

  public ArgSpec replace(Term t, ArgSpec another) {
    Term newEq1 = (Term) eq1.replace(t, another);
    Term newEq2 = (Term) eq2.replace(t, another);
    if (newEq1 != eq1 || newEq2 != eq2)
      return compileAnotherIfCompiled(new EqualityFormula(newEq1, newEq2));
    return this;
  }

  public ArgSpec getSubstResult(Substitution subst, Set<LogicalVar> boundVars) {
    return new EqualityFormula((Term) eq1.getSubstResult(subst, boundVars),
        (Term) eq2.getSubstResult(subst, boundVars));
  }

  private Term eq1;
  private Term eq2;
}
