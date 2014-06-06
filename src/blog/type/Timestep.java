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
package blog.type;

import java.util.HashMap;
import java.util.Map;

import blog.common.UnaryProcedure;

/**
 * Represents a time step, and is used for DBLOG (Dynamic BLOG) to indicate
 * temporal random variables.
 */
public class Timestep extends Number implements Comparable<Timestep> {

  private Timestep(int index) {
    this.index = index;
    if (index > max)
      max = index;
  }

  public Timestep next() {
    return at(index + 1);
  }

  public Timestep prev() {
    return at(index - 1);
  }

  public int getValue() {
    return index;
  }

  public static int getMax() {
    return max;
  }

  // implement abstract methods of Number

  public int intValue() {
    return index;
  }

  public long longValue() {
    return (long) index;
  }

  public double doubleValue() {
    return (double) index;
  }

  public float floatValue() {
    return (float) index;
  }

  public int compareTo(Timestep o) {
    return index - o.intValue();
  }

  public String toString() {
    return "@" + index;
  }

  /**
   * After this object is constructed in deserialization, either add it to the
   * <code>generatedTimesteps</code> map or return its existing equivalent from
   * that map.
   */
  private Object readResolve() {
    Integer t = new Integer(index);
    Timestep existing = (Timestep) generatedTimesteps.get(t);
    if (existing == null) {
      generatedTimesteps.put(t, this);
      return this;
    }
    return existing;
  }

  public static Timestep at(int t) {
    Timestep ts = generatedTimesteps.get(t);
    if (ts == null) {
      ts = new Timestep(t);
      generatedTimesteps.put(new Integer(t), ts);
    }
    return ts;
  }

  /**
   * Use this to compute the maximum Timestep from a sequence of objects.
   * 
   * Override extractTimestep() to extract the Timestep from an object.
   * Call evaluate() on all the objects you are considering.
   * Then read the max Timestep from the <code>result</code> property.
   * (The result is null if no Timesteps were found.)
   * 
   * @author cberzan
   * @since Jun 6, 2014
   */
  public static class MaxReduce implements UnaryProcedure {
    public Timestep result = null;

    /**
     * Return Timestep that the object refers to, otherwise return null.
     * The default implementation tries to cast the object to a Timestep.
     */
    public Timestep extractTimestep(Object x) {
      if (x instanceof Timestep) {
        return (Timestep) x;
      }
      return null;
    }

    public void evaluate(Object x) {
      Timestep cand = extractTimestep(x);
      if (result == null || (cand != null && cand.compareTo(result) > 0)) {
        result = cand;
      }
    }
  }

  private int index;
  private static int max = 0;
  private static Map<Integer, Timestep> generatedTimesteps = new HashMap<Integer, Timestep>();
}
