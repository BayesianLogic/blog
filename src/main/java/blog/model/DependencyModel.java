/*
 * Copyright (c) 2005, 2006, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the University of California, Berkeley nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior
 * written permission.
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

import java.io.PrintStream;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import blog.bn.BasicVar;
import blog.common.Util;
import blog.distrib.CondProbDistrib;
import blog.distrib.EqualsCPD;
import blog.sample.EvalContext;

/**
 * Represents dependency statements for functions and number statements for
 * potential object patterns. It consists of a list of clauses the
 * dependency/number statement consists of. Each DependencyModel also has a
 * default value: if none of the clauses are satisfied, then the child variable
 * has the default value with probability 1. The default value is Boolean.FALSE
 * for Boolean functions, null for all other functions, and Integer(0) for POPs.
 * 
 * @see blog.model.Function
 * @see blog.model.POP
 */
public class DependencyModel {

  /**
   * Nested class representing a distribution over child values, in the form of
   * a CPD and a list of values for the CPD's arguments.
   * 
   * TODO: This class is no longer necessary after the new distribution
   * interface is implemented. Instead of storing the args in this container, we
   * can just call setParams() on the distribution. So we only need to pass
   * around a CondProbDistrib, not a CondProbDistrib + args.
   */
  public static class Distrib {
    public Distrib(CondProbDistrib cpd, List argValues) {
      this.cpd = cpd;
      this.argValues = argValues;
    }

    public CondProbDistrib getCPD() {
      return cpd;
    }

    public Object[] getArgValues() {
      return argValues.toArray();
    }

    public String toString() {
      return (cpd + "(" + argValues + ")");
    }

    private CondProbDistrib cpd;
    private List argValues;
  }

  public DependencyModel(ArgSpec cl, Type childType, Object defaultVal) {

    clause = cl;
    this.childType = childType;
    this.defaultVal = defaultVal;

    Term defaultTerm = childType.getCanonicalTerm(defaultVal);
    if (defaultTerm == null) {
      Util.fatalError("No canonical term for default value " + defaultVal
          + " of type " + childType);
    }
    defaultClause = new DistribSpec(EqualsCPD.class,
        Collections.singletonList((ArgSpec) defaultTerm));
    defaultClause.initCPD();
  }

  public ArgSpec getClause() {
    return clause;
  }

  public Object getDefaultValue() {
    return defaultVal;
  }

  /**
   * Returns the CPD and argument values for the first satisfied clause in the
   * context obtained by binding the given variables to the given objects. If
   * any of the given objects does not exist, returns a distribution that is
   * deterministically equal to the given <code>valueWhenArgsDontExist</code>.
   * If the context is not complete enough to determine the first satisfied
   * clause and its argument values, this method returns null.
   */
  public Distrib getDistribWithBinding(EvalContext context, LogicalVar[] vars,
      Object[] objs, Object valueWhenArgsDontExist) {
    for (Object obj : objs) {
      Boolean exists = context.objectExists(obj);
      if (exists == null) {
        return null;
      }
      if (!exists.booleanValue()) {
        return new DependencyModel.Distrib(EqualsCPD.CPD,
            Collections.singletonList(valueWhenArgsDontExist));
      }
    }

    context.assignTuple(vars, objs);
    DependencyModel.Distrib distrib = getDistrib(context);
    context.unassignTuple(vars);
    return distrib;
  }

  /**
   * Returns the CPD and argument values for the first satisfied clause in the
   * given context. If the context is not complete enough to determine the first
   * satisfied clause and its argument values, then this method returns null.
   */
  public Distrib getDistrib(EvalContext context) {
    Object tmp = clause.evaluate(context);
    if (tmp == null)
      return null;
    if (tmp == Model.NULL)
      return defaultClause.getDistrib(context);
    if (tmp instanceof DistribSpec)
      return ((DistribSpec) tmp).getDistrib(context);
    else
      return null;
  }

  /**
   * If, in the given context, this dependency model specifies that the child is
   * equal to one of its parents, then this method returns that "equal parent".
   * Otherwise it returns null. This method also returns null if the given
   * context is not complete enough to determine the equal parent.
   */
  public BasicVar getEqualParent(EvalContext context) {
    Object tmp = clause.evaluate(context);
    if (tmp instanceof DistribSpec)
      return ((DistribSpec) tmp).getEqualParent(context);

    return null;
  }

  /**
   * Prints this dependency model to the given stream. Each clause is printed on
   * a separate line, and each line is indented 1 tab. The first clause begins
   * with "if"; all subsequent clauses begin with "elseif".
   */
  public void print(PrintStream s) {
    s.println(clause);
  }

  public boolean checkTypesAndScope(Model model, Map scope) {
    return clause.checkTypesAndScope(model, scope, childType);
  }

  /**
   * Creates CPD objects for this dependency model, and does any necessary
   * compilation on the conditions and CPD arguments. Prints messages to
   * standard error if any errors occur. Returns the number of errors
   * encountered.
   * 
   * @param callStack
   *          Set of objects whose compile methods are parents of this method
   *          invocation. Ordered by invocation order. Used to detect cycles.
   */
  public int compile(LinkedHashSet callStack) {
    callStack.add(this);
    int errors = clause.compile(callStack);
    callStack.remove(this);
    return errors;
  }

  /**
   * Returns an index indicating when this dependency model was defined.
   */
  public int getCreationIndex() {
    return creationIndex;
  }

  private ArgSpec clause; // of Clause, not including the default clause
  private DistribSpec defaultClause;
  private Type childType;
  private Object defaultVal;
  private int creationIndex = Model.nextCreationIndex();

  private static int numCreated = 0;
}
