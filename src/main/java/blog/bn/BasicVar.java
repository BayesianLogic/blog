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
package blog.bn;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import blog.common.MaxReduce;
import blog.model.FuncAppTerm;
import blog.model.Model;
import blog.model.Type;
import blog.type.Timestep;
import blog.world.PartialWorld;

/**
 * A basic random variable in a BLOG model. A BasicVar is a BayesNetVar whose
 * value is stored explicitly in a PossibleWorld.
 * 
 * The set of basic variables is defined implicitly by a BLOG model. BasicVar
 * objects are just a convenience for bundling together a function and a tuple
 * of arguments, or a POP and a tuple of generating objects.
 */
public abstract class BasicVar extends AbstractBayesNetVar implements
    Comparable<BasicVar>, Cloneable {

  /**
   * Creates a new BasicVar with the given tuple of arguments or generating
   * objects.
   */
  protected BasicVar(Object[] args) {
    this.args = args;
  }

  /**
   * Creates a new BasicVar with the given tuple of arguments or generating
   * objects. If <code>stable</code> is true, then the caller guarantees that
   * the given <code>args</code> array will not be modified externally.
   */
  protected BasicVar(Object[] args, boolean stable) {
    this.args = args;
    this.stable = stable;
  }

  /**
   * Creates a new BasicVar with the given tuple of arguments or generating
   * objects.
   */
  protected BasicVar(List argList) {
    args = new Object[argList.size()];
    argList.toArray(args);
    stable = true;
  }

  /**
   * Returns the tuple of arguments if this is a function application variable,
   * or the tuple of generating objects if this is a number variable. The
   * returned array should not be modified.
   */
  public final Object[] args() {
    return args;
  }

  /**
   * Returns the type of object that can be a value for this variable.
   */
  public abstract Type getType();

  /**
   * Returns an index to be used for comparing this variable to others.
   * Variables for which this method returns the same index value will be
   * compared based on their arguments.
   */
  public abstract int getOrderingIndex();

  /**
   * A basic random variable is determined if and only if it is instantiated.
   */
  public boolean isDetermined(PartialWorld w) {
    return (w.getValue(this) != null);
  }

  /**
   * Returns the value of this basic variable in the given world.
   */
  public Object getValue(PartialWorld w) {
    return w.getValue(this);
  }

  /**
   * Returns a term whose value in any possible world is the same as this random
   * variable's value.
   */
  public FuncAppTerm getCanonicalTerm() {
    return getCanonicalTerm(Collections.EMPTY_MAP);
  }

  /**
   * Returns a term whose value in any possible world is the same as this random
   * variable's value, assuming that objects are bound to logical variables as
   * specified in <code>logicalVarForObj</code>.
   * 
   * @param logicalVarForObj
   *          map from Object to LogicalVar
   */
  public FuncAppTerm getCanonicalTerm(Map logicalVarForObj) {
    throw new UnsupportedOperationException(
        "Can't convert random variable to term: " + this);
  }

  /**
   * Ensures that this BasicVar's arguments are stored in an array that will not
   * be modified externally.
   */
  public void ensureStable() {
    if (!stable) {
      args = (Object[]) args.clone();
      stable = true;
    }
  }

  public Timestep maxTimestep() {
    MaxReduce<Timestep> reducer = new MaxReduce<Timestep>();
    for (Object arg : args) {
      if (arg instanceof Timestep) {
        reducer.evaluate(arg);
      }
    }
    return reducer.result;
  }

  /**
   * Compares this BasicVar to another one. The ordering is intended to be used
   * for printing basic variables. First, basic variables are compared based on
   * the order in which their dependency models were created; this corresponds
   * to the order of definition in the model file. For basic variables with the
   * same dependency model, we use a lexicographic ordering based on the
   * arguments.
   */
  @Override
  public int compareTo(BasicVar other) {
    int indexDiff = getOrderingIndex() - other.getOrderingIndex();
    if (indexDiff != 0) {
      return indexDiff;
    }

    return Model.compareArgTuples(args, other.args());
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 71 * hash + Arrays.deepHashCode(this.args);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final BasicVar other = (BasicVar) obj;
    if (!Arrays.deepEquals(this.args, other.args)) {
      return false;
    }
    return true;
  }

  public abstract Object clone();

  protected Object[] args; // of Object

  private boolean stable = false;
}
