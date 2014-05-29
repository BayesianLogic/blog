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

import java.util.LinkedList;
import java.util.List;

import blog.model.Type;

/**
 * A Beta distribution with shape parameters a and b, defined by f(x) =(x^(a-1)
 * * (1-x)^(b-1)) / B(a,b) where B(a,b) is a normalization constant equal to
 * integral from 0 to 1 of x^(a-1) * (1-x)^(b-1) dx
 */
public class Beta extends AbstractCondProbDistrib {
  /**
   * Returns a new Beta with shape parameters a and b.
   */
  public Beta(List params) {
    if (!((params.get(0) instanceof Number) && (params.get(1) instanceof Number))) {
      throw new IllegalArgumentException(
          "Beta expects two numerical arguments "
              + "{a, b} where both are Numbers. Got: " + params);
    }
    a = ((Number) params.get(0)).doubleValue();
    b = ((Number) params.get(1)).doubleValue();
    gammaA = new Gamma(a, 1);
    gammaB = new Gamma(b, 1);
  }

  /**
   * Returns the probability of value under this distribution.
   */
  public double getProb(List args, Object value) {
    if (!(value instanceof Number)) {
      throw new IllegalArgumentException(
          "Beta CPD defines a distribution over objects"
              + " of class Number, not " + value.getClass() + ".");
    } else {
      double x = ((Number) value).doubleValue();
      return ((Math.pow(x, (a - 1)) * Math.pow((1 - x), (b - 1))) / beta(a, b));
    }
  }

  /**
   * Returns the log of the probability of value under this distribution.
   */
  public double getLogProb(List args, Object value) {
    if (!(value instanceof Number)) {
      throw new IllegalArgumentException(
          "Beta CPD defines a distribution over objects"
              + " of class Number, not " + value.getClass() + ".");
    } else {
      double x = ((Number) value).doubleValue();
      double t1 = 0;
      double t2 = 0;
      if (a != 1) {
        t1 = (a - 1) * Math.log(x);
      }
      if (b != 1) {
        t2 = (b - 1) * Math.log(1 - x);
      }
      return t1 + t2 - Math.log(beta(a, b));
    }
  }

  /**
   * Returns a double sampled according to this distribution. Takes time
   * equivalent to the distrib.Gamma sampling function. (Reference: A Guide To
   * Simulation, 2nd Ed. Bratley, Paul, Bennett L. Fox and Linus E. Schrage.)
   */
  public Object sampleVal(List args, Type childType) {
    LinkedList l = new LinkedList();
    double y = ((Double) gammaA.sampleVal(l, childType)).doubleValue();
    double z = ((Double) gammaB.sampleVal(l, childType)).doubleValue();
    return new Double(y / (y + z));
  }

  /**
   * Returns the Beta function of reals a and b B(a,b) = Gamma(a)Gamma(b) /
   * Gamma(a+b) Reference: Numerical Recipes in C
   * http://www.library.cornell.edu/nr/cbookcpdf.html
   */
  public static double beta(double a, double b) {
    return ((Gamma.gamma(a) * Gamma.gamma(b)) / Gamma.gamma(a + b));
  }

  public String toString() {
    return getClass().getName();
  }

  private double a;
  private double b;
  private Gamma gammaA;
  private Gamma gammaB;

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#setParams(java.util.List)
   */
  @Override
  public void setParams(List<Object> params) {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#getProb(java.lang.Object)
   */
  @Override
  public double getProb(Object value) {
    // TODO Auto-generated method stub
    return 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#getLogProb(java.lang.Object)
   */
  @Override
  public double getLogProb(Object value) {
    // TODO Auto-generated method stub
    return 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#sampleVal()
   */
  @Override
  public Object sampleVal() {
    // TODO Auto-generated method stub
    return null;
  }
}
