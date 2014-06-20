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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import blog.common.TupleIterator;
import blog.common.Util;
import blog.sample.DefaultEvalContext;
import blog.sample.EvalContext;
import blog.world.PartialWorld;

/**
 * Represents a BLOG function. A function is specified by its name, return type,
 * and argument types.
 */

public abstract class Function {
  public Function(String fname, List<Type> argTypeList, Type retType) {
    sig = new FunctionSignature(fname, argTypeList);
    this.retType = retType;
  }

  public String getName() {
    return sig.getName();
  }

  public Type[] getArgTypes() {
    return sig.getArgTypes();
  }

  public Type getRetType() {
    return retType;
  }

  public FunctionSignature getSig() {
    return sig;
  }

  /**
   * Returns true if this function applies to all tuples of objects with the
   * given types. This is true if each type in the given tuple is a subtype of
   * the corresponding type in this function's argument type tuple (and the
   * tuples have the same length).
   */
  public boolean appliesTo(Type[] types) {
    if (types.length != sig.getArgTypes().length) {
      return false;
    }

    for (int i = 0; i < types.length; ++i) {
      if (!types[i].isSubtypeOf(sig.getArgTypes()[i])) {
        return false;
      }
    }

    return true;
  }

  /**
   * Returns true if this function could apply to some tuple of non-null objects
   * with the given types. Since we have single inheritance (except for
   * NULL_TYPE), this is true if every type in the given tuple is either a
   * subtype or a supertype of the corresponding type in this function's
   * argument type tuple (and the tuples have the same length).
   */
  public boolean overlapsWith(Type[] types) {
    if (types.length != sig.getArgTypes().length) {
      return false;
    }

    for (int i = 0; i < types.length; ++i) {
      if (!(types[i].isSubtypeOf(sig.getArgTypes()[i]) || sig.getArgTypes()[i]
          .isSubtypeOf(types[i]))) {
        return false;
      }
    }

    return true;
  }

  /**
   * Returns true if the dependency statement or interpretation statement for
   * this function (if any) satisfies type and scope constraints. If there is a
   * type or scope error, prints a message to standard error and returns false.
   * 
   * <p>
   * This default implementation just returns true.
   */
  public boolean checkTypesAndScope(Model model) {
    return true;
  }

  /**
   * Ensures that this function is ready to be used by an inference engine. If
   * any errors occur, prints a message to standard error. Returns the number of
   * errors that occurred.
   * 
   * <p>
   * This default implementation just returns 0.
   * 
   * @param callStack
   *          Set of objects whose compile methods are parents of this method
   *          invocation. Ordered by invocation order. Used to detect cycles.
   */
  public int compile(LinkedHashSet callStack) {
    return 0;
  }

  /**
   * Returns the value of this function applied to the given tuple of arguments
   * in the given partial world. If the given partial world is not complete
   * enough to determine the function value, this method returns null.
   */
  public Object getValue(Object[] args, PartialWorld w) {
    return getValueInContext(args, new DefaultEvalContext(w), false);
  }

  /**
   * Returns the value of this function applied to an empty tuple of arguments
   * in the given partial world (this method should be used only for zero-ary
   * functions). If the given partial world is not complete enough to determine
   * the function value, this method returns null.
   */
  public Object getValue(PartialWorld w) {
    return getValueInContext(new Object[0], new DefaultEvalContext(w), true);
  }

  /**
   * Returns the value of this function applied to the given single argument in
   * the given partial world (this method should be used only for unary
   * functions). If the given partial world is not complete enough to determine
   * the function value, this method returns null.
   */
  public Object getValueSingleArg(Object arg, PartialWorld w) {
    Object[] args = { arg };
    return getValueInContext(args, new DefaultEvalContext(w), true);
  }

  /**
   * Returns the value of this function applied to the given pair of arguments
   * in the given partial world (this method should be used only for binary
   * functions). If the given partial world is not complete enough to determine
   * the function value, this method returns null.
   */
  public Object getValue(Object arg1, Object arg2, PartialWorld w) {
    Object[] args = { arg1, arg2 };
    return getValueInContext(args, new DefaultEvalContext(w), true);
  }

  /**
   * Returns the value of this function applied to the given tuple of arguments
   * in the given context. If the partial world in the given context is not
   * complete enough to determine the function value, this method returns null.
   * 
   * @param stable
   *          if true, the caller guarantees that the <code>args</code> array
   *          will not be modified
   */
  public abstract Object getValueInContext(Object[] args, EvalContext context,
      boolean stable);

  /**
   * Returns the value of this function on arguments that are not in its domain.
   * This is Boolean.FALSE for Boolean functions, and Model.NULL for other
   * functions.
   */
  public Object getDefaultValue() {
    return retType.getDefaultValue();
  }

  /**
   * Returns true if this function is indexed by time, that is, it has a final
   * argument of type Timestep.
   */
  public boolean isTimeIndexed() {
    return ((sig.getArgTypes().length > 0) && (sig.getArgTypes()[sig
        .getArgTypes().length - 1] == BuiltInTypes.TIMESTEP));
  }

  /**
   * Returns an iterator over the tuples of objects that can serve as arguments
   * to this function. Raises a fatal error if this would require enumerating a
   * random or infinite set.
   * 
   * @return Iterator over Lists
   */
  public Iterator domainIterator() {
    return domainIterator(false);
  }

  /**
   * Returns an iterator over the tuples of objects that can serve as arguments
   * to this function. Raises a fatal error if this would require enumerating a
   * random or infinite set.
   * 
   * @param skipLastArg
   *          if true, exclude the last argument from the tuples (useful for
   *          time-indexed functions)
   * 
   * @return Iterator over Lists
   */
  public Iterator domainIterator(boolean skipLastArg) {
    List argDomains = new ArrayList();
    int numDomains = (skipLastArg ? sig.getArgTypes().length - 1 : sig
        .getArgTypes().length);
    for (Type t : sig.getArgTypes()) {
      if (t.hasFiniteGuaranteed() && t.getPOPs().isEmpty()) {
        argDomains.add(t.getGuaranteedObjects());
      } else {
        Util.fatalError("Can't enumerate set of objects of type " + t
            + " because this set is random or infinite");
      }
    }

    return new TupleIterator(argDomains);
  }

  /**
   * Returns an index indicating when this function was declared (or otherwise
   * created).
   */
  public int getCreationIndex() {
    return creationIndex;
  }

  /**
   * Returns this function's name.
   */
  public String toString() {
    return sig.getName();
  }

  public int hashCode() {
    return sig.hashCode();
  }

  private FunctionSignature sig;
  private Type retType;

  private int creationIndex = Model.nextCreationIndex();
}
