/*
 * Copyright (c) 2005, Regents of the University of California
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

package blog.distrib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import blog.common.Util;

/**
 * A distribution over Boolean values. It is defined by one parameter p, which
 * is the probability of <code>true</code>.
 * 
 * @author cgioia
 * @since June 17, 2014
 */
public class BooleanDistrib implements CondProbDistrib {

  private void checkHasParams() {
    if (!this.hasP) {
      throw new IllegalArgumentException("parameter p not provided");
    }
  }

  /**
   * Returns the log probability of the given Boolean value under this
   * distribution.
   */
  public double getLogProb(boolean value) {
    checkHasParams();
    if (value) {
      return logP;
    }
    return log1_P;
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#getLogProb(java.lang.Object)
   */
  @Override
  public double getLogProb(Object value) {
    return getLogProb(((Boolean) value).booleanValue());
  }

  /**
   * Returns the probability of the given Boolean value under this distribution.
   **/
  public double getProb(boolean value) {
    checkHasParams();
    if (value) {
      return p;
    }
    return 1 - p;
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#getProb(java.lang.Object)
   */
  @Override
  public double getProb(Object value) {
    return getProb(((Boolean) value).booleanValue());
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#sampleVal()
   */
  @Override
  public Object sampleVal() {
    checkHasParams();
    if (Util.random() < p) {
      return true;
    }
    return false;
  }

  /**
   * @param p
   *          probability of <code>true</code>
   */
  public void setParams(Number p) {
    if (p != null) {
      double pDouble = p.doubleValue();
      if (pDouble > 1 || pDouble < 0) {
        throw new IllegalArgumentException(
            "Parameter to Bernoulli must be in interval [0, 1], not " + p + ".");
      }
      this.p = pDouble;
      this.logP = Math.log(pDouble);
      this.log1_P = Math.log(1 - pDouble);
      this.hasP = true;
    }
    finiteSupport.clear();
    finiteSupport.add(true);
    finiteSupport.add(false);
  }

  /**
   * set parameters for BooleanDistrib distribution
   * 
   * @param params
   *          An array of a single double.
   *          params[0]: <code>p</code>
   * @see blog.distrib.CondProbDistrib#setParams(java.lang.Object[])
   */
  @Override
  public void setParams(Object[] params) {
    if (params.length != 1) {
      throw new IllegalArgumentException(
          "expected one parameter: probability of success");
    }
    setParams((Number) params[0]);
  }

  @Override
  public String toString() {
    return "BooleanDistrib(" + p + ")";
  }

  @Override
  public List getFiniteSupport() {
    checkHasParams();
    return Collections.unmodifiableList(finiteSupport);
  }

  private double p;

  private double logP; // log p

  private double log1_P; // log (1 - p)

  private boolean hasP;

  private List finiteSupport = new ArrayList();
}
