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

/**
 * A distribution over {0,1}. It takes one parameter p, which is the probability
 * of <code>true</code>.
 */
public class Bernoulli extends BooleanDistrib {

  @Override
  public double getProb(Object value) {
    return super.getProb(getBooleanValue(value));
  }

  public static Boolean getBooleanValue(Object value) {
    Integer val = (Integer) value;
    if (val.equals(1)) {
      return true;
    } else if (val.equals(0)) {
      return false;
    } else {
      return null;
    }
  }

  public static Integer getIntegerValue(Object value) {
    Boolean val = (Boolean) value;
    if (Boolean.TRUE.equals(val)) {
      return 1;
    } else if (Boolean.FALSE.equals(val)) {
      return 0;
    } else {
      return null;
    }
  }

  @Override
  public double getLogProb(Object value) {
    return super.getLogProb(getBooleanValue(value));
  }

  @Override
  public Object sampleVal() {
    return getIntegerValue(super.sampleVal());
  }

  public String toString() {
    return "Bernoulli(" + p + ")";
  }

  private double p;
  private double logP; // log p
  private double log1_P; // log (1 - p)
  private boolean hasP;
}
