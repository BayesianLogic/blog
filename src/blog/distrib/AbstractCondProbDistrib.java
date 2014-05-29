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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

/**
 * Backwards-compatibility layer.
 */
public abstract class AbstractCondProbDistrib implements CondProbDistrib {

  // Old interface:

  abstract double getProb(List args, Object childValue);

  public double getLogProb(List args, Object childValue) {
    return Math.log(getProb(args, childValue));
  }

  abstract Object sampleVal(List args);

  // New interface:

  public void setParams(List<Object> params_) {
    if (params == null) {
      params = params_;
    } else {
      for (int i = 0; i < params.size(); i++) {
        if (params_.get(i) != null) {
          params.set(i, params_.get(i));
        }
      }
    }
  }

  public double getProb(Object value) {
    return makeActualDistrib().getProb(Collections.emptyList(), value);
  }

  public double getLogProb(Object value) {
    return makeActualDistrib().getLogProb(Collections.emptyList(), value);
  }

  public Object sampleVal() {
    return makeActualDistrib().sampleVal(Collections.emptyList());
  }

  private AbstractCondProbDistrib makeActualDistrib() {
    // Construct the distribution with fixed params. This is inefficient in
    // general; we do this for backwards compatibility only.
    AbstractCondProbDistrib result = null;
    try {
      Class[] constrArgTypes = { List.class };
      Constructor constructor = getClass().getConstructor(constrArgTypes);
      Object[] constrArgs = { params };
      result = (AbstractCondProbDistrib) constructor.newInstance(constrArgs);
    } catch (NoSuchMethodException | InvocationTargetException
        | SecurityException | InstantiationException | IllegalAccessException
        | IllegalArgumentException e) {
      System.out.println("CPD init fail");
    }
    return result;
  }

  private List<Object> params;
}
