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

package blog.distrib;

import java.util.Collection;
import java.util.Iterator;

import blog.common.Util;
import blog.model.Model;

/**
 * CPD that takes <code>S</code>, a set of objects (an instance of the
 * ObjectSet interface), and defines a uniform distribution over this set.
 * 
 * @since June 17, 2014
 */
public class UniformChoice implements CondProbDistrib {

  /**
   * set parameters for UniformChoice
   * 
   * @param params
   *          an array of the form [ObjectSet]
   *          <ul>
   *          <li>params[0]: <code>S</code></li>
   *          </ul>
   * 
   * @see blog.distrib.CondProbDistrib#setParams(java.util.List)
   */
  @Override
  public void setParams(Object[] params) {
    if (params.length != 1) {
      throw new IllegalArgumentException("expected one parameter");
    }
    setParams((Collection<?>) params[0]);
  }

  /**
   * If the method parameter <code>set</code> is non-null, sets the distribution
   * parameter <code>S</code> to <code>set</code>
   * 
   */
  public void setParams(Collection<?> set) {
    if (set != null) {
      this.s = set;
      this.hasS = true;
    }
  }

  private void checkHasParams() {
    if (!this.hasS) {
      throw new IllegalArgumentException("parameter S not provided");
    }
  }

  /**
   * If <code>S</code> is non-empty, return 1 / |S| if <code>value</code> is in
   * <code>S</code>, and otherwise 0. If <code>S</code> is empty, returns 1 if
   * the value is Model.NULL, and 0 otherwise.
   * 
   * @see blog.distrib.CondProbDistrib#getProb(java.lang.Object)
   */
  @Override
  public double getProb(Object value) {
    checkHasParams();
    if (s.isEmpty()) {
      return (value == Model.NULL) ? 1 : 0;
    }
    return (s.contains(value) ? (1.0 / s.size()) : 0);
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#getLogProb(java.lang.Object)
   */
  @Override
  public double getLogProb(Object value) {
    return Math.log(getProb(value));
  }

  /**
   * Returns an element of <code>S</code> selected uniformly at random. If
   * <code>S</code> is empty, returns Model.NULL.
   * 
   * @see blog.distrib.CondProbDistrib#sampleVal()
   */
  @Override
  public Object sampleVal() {
    return sample_value();
  }

  /** Samples uniformly from <code>S</code>, a set of object. */
  public Object sample_value() {
    checkHasParams();
    if (s.isEmpty()) {
      return Model.NULL;
    }
    int n = Util.randInt(s.size());
    Iterator<?> it = s.iterator();
    while (it.hasNext()) {
      if (n == 0)
        return (Object) it.next();
      --n;
      it.next();
    }
    return null;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("UniformChoice(");
    for (Object item : s) {
      builder.append(item.toString() + ", ");
    }
    builder.replace(builder.length() - 2, builder.length(), "");
    builder.append(")");
    return builder.toString();
  }

  private Collection<?> s;
  private boolean hasS;
}
