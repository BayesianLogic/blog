package blog.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import blog.common.UnaryProcedure;
import blog.sample.EvalContext;

/**
 * ArgSpec that represents an array list. For now supports only Integer array
 * and Real array.
 * 
 * @author leili
 * @date 2014/10/04
 */

public class ArraySpec<T> extends ArgSpec {

  List<T> elements;
  Type arraytype;
  boolean compiled;

  public ArraySpec() {
    elements = new ArrayList<T>();
    compiled = true;
  }

  public ArraySpec(List<T> args) {
    this();
    elements.addAll(args);
  }

  public ArraySpec(List<T> args, Type typeOfContents) {
    this(args);
    this.arraytype = typeOfContents;
  }

  /**
   * To compile a list, it is only necessary to compile
   * each of its possible values.
   */
  public int compile(LinkedHashSet callStack) {
    return 0;
  }

  @Override
  public Object evaluate(EvalContext context) {
    return elements;
  }

  /**
   * Lists only handle distinct objects and built-in types for now.
   */
  @Override
  public boolean containsRandomSymbol() {
    return false;
  }

  @Override
  public boolean checkTypesAndScope(Model model, Map scope, Type childType) {
    return true;
  }

  /**
   * Returns term in ArgSpec equal to a given term t, or null if there isn't
   * any.
   */
  @Override
  public ArgSpec find(Term t) {
    return null;
  }

  /**
   * Applies a procedure to all terms in this ArgSpec which satisfy a given
   * predicate to a given collection.
   */
  @Override
  public void applyToTerms(UnaryProcedure procedure) {
  }

  /**
   * Returns an ArgSpec resulting from the replacement of all occurrences of a
   * term by another, if there is any, or self. A new ArgSpec is compiled if
   * this is compiled.
   */
  @Override
  public ArgSpec replace(Term t, ArgSpec another) {
    return null;
  }

  /**
   * Returns the result of applying the substitution <code>subst</code> to this
   * expression, excluding the logical variables in <code>boundVars</code>. This
   * method is used for recursive calls. The set <code>boundVars</code> should
   * contain those variables that are bound in the syntax tree between this
   * sub-expression and the top-level expression to which the substitution is
   * being applied.
   */
  @Override
  public ArgSpec getSubstResult(Substitution subst, Set<LogicalVar> boundVars) {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.model.ArgSpec#getSubExprs()
   */
  @Override
  public Collection getSubExprs() {
    // TODO Auto-generated method stub
    return Collections.emptyList();
  }

  public String toString() {
    return elements.toString();
  }

  public int hashCode() {
    return elements.hashCode();
  }

  public boolean equals(Object o) {
    return o.hashCode() == this.hashCode();
  }
}
