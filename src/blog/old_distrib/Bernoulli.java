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

package blog.old_distrib;

import java.util.List;

import blog.common.Util;

/**
 * A distribution over {0,1}. It takes one parameter, which is the probability
 * of <code>true</code>.
 */
public class Bernoulli extends AbstractCondProbDistrib {

  public Bernoulli(List params) {
    if (params.size() != 1) {
      throw new IllegalArgumentException(
          "Binary Bernoulli distribution requires exactly one parameter, "
              + "not " + params.size() + ".");
    }
    Object param_obj = params.get(0);
    if (!(param_obj instanceof Number)) {
      throw new IllegalArgumentException(
          "Parameter to Binary Bernoulli distrib must be of class Number, "
              + "not " + param_obj.getClass() + ".");
    }

    pi = ((Number) param_obj).doubleValue();
    if ((pi < 0) || (pi > 1)) {
      throw new IllegalArgumentException(
          "Parameter to Binary Bernoulli must be in interval [0, 1], not " + pi
              + ".");
    }
  }

  public double getProb(List args, Object value) {
    if (args.size() != 0) {
      throw new IllegalArgumentException(
          "Binary Bernoulli distribution takes zero arguments, not "
              + args.size() + ".");
    }
    if (!(value instanceof Integer)) {
      throw new IllegalArgumentException(
          "Binary Bernoulli distribution is over objects of class Integer, "
              + "not " + value.getClass() + ".");
    }

    int int_value = ((Integer) value).intValue();

    if (!((int_value == 0) || (int_value == 1))) {
      throw new IllegalArgumentException(
          "Binary Bernoulli distribution is over the set {0,1}; passed value: "
              + value.getClass() + ".");

    }

    if (int_value == 1) {
      return pi;
    }
    return (1 - pi);
  }

  public Object sampleVal(List args) {
    if (args.size() != 0) {
      throw new IllegalArgumentException(
          "Binary Bernoulli distribution takes zero arguments, not "
              + args.size() + ".");
    }

    if (Util.random() < pi) {
      return new Integer(1);
    }
    return new Integer(0);
  }

  private double pi;

}
