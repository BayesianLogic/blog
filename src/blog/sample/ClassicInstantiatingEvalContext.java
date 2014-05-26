/*
 * Copyright (c) 2006, Regents of the University of California
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

package blog.sample;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import blog.ObjectIdentifier;
import blog.bn.BasicVar;
import blog.bn.VarWithDistrib;
import blog.common.Util;
import blog.distrib.CondProbDistrib;
import blog.model.DependencyModel;
import blog.world.PartialWorld;

/**
 * Implementation of the EvalContext interface that instantiates new basic
 * random variables as necessary in response to method calls. To instantiate a
 * variable, it first instantiates all that variable's active parents, then
 * samples a value for the variable using its CPD given those parent values.
 * 
 * <p>
 * When it becomes necessary to instantiate a random variable, a new
 * InstantiatingEvalContext is created to evaluate its dependency model and
 * determine its CPD. We say that the random variable to be instantiated is
 * <i>responsible</i> for the creation of this new InstantiatingEvalContext. For
 * any given InstantiatingEvalContext, there is a sequence of responsible
 * variables that led to its creation. This sequence is passed into the
 * InstantiatingEvalContext constructor (except in the default case where the
 * sequence is empty), in the form of an ordered map from responsible variables
 * to the evaluation contexts that led to those variables.
 * 
 * <p>
 * Storing this sequence of responsible variables is useful for debugging, but
 * it also allows us to detect cyclic dependencies. If an
 * InstantiatingEvalContext is about to try determining the CPD for a random
 * variable that's already among its responsible variables, then it raises a
 * fatal error rather than entering an infinite loop.
 * 
 * <p>
 * This used to be called <code>InstantiatingEvalContext</code>, but other
 * instantiating eval contexts came along and that became an interface. This
 * took the name "classic" since it was the one instantiating eval context in
 * the initial BLOG implementation.
 */
public class ClassicInstantiatingEvalContext extends ParentRecEvalContext
    implements InstantiatingEvalContext {
  /**
   * Creates a new InstantiatingEvalContext using the given world. Its sequence
   * of responsible variables is empty.
   */
  public ClassicInstantiatingEvalContext(PartialWorld world) {
    super(world);
    this.respVarsAndContexts = new LinkedHashMap<VarWithDistrib, ClassicInstantiatingEvalContext>();
  }

  /**
   * Creates a new InstantiatingEvalContext with the given sequence of
   * responsible variables.
   * 
   * @param respVarsAndContexts
   *          map from VarWithDistrib to EvalContext in which the order of the
   *          variables is the order in which they were reached in the recursive
   *          instantiation process, and each variable is mapped to the context
   *          that led to it
   */
  protected ClassicInstantiatingEvalContext(
      PartialWorld world,
      LinkedHashMap<VarWithDistrib, ClassicInstantiatingEvalContext> respVarsAndContexts) {
    super(world);
    this.respVarsAndContexts = respVarsAndContexts;
  }

  public boolean isInstantiated(BasicVar var) {
    return (world.getValue(var) != null);
  }

  protected Object getOrComputeValue(BasicVar var) {
    Object value = world.getValue(var);
    if (value == null) {
      if (var instanceof VarWithDistrib) {
        value = instantiate((VarWithDistrib) var);
      } else {
        throw new IllegalArgumentException("Don't know how to instantiate: "
            + var);
      }
    }
    return value;
  }

  // Note that we don't have to override getSatisfiers, because the
  // DefaultEvalContext implementation of getSatisfiers calls getValue
  // on the number variable.

  /**
   * Returns the log of the probability of this InstantiatingEvalContext
   * assigning the values it has assigned to the variables it has instantiated.
   */
  public double getLogProbability() {
    return logProb;
  }

  /**
   * A listener of the type {@link AfterSamplingListener} invoked after each
   * time a variable is instantiated (sampled).
   */
  public AfterSamplingListener afterSamplingListener;

  /**
   * A <b>static</b> listener of the type {@link AfterSamplingListener} invoked
   * after each time a variable is instantiated (sampled).
   */
  public static AfterSamplingListener staticAfterSamplingListener;

  protected Object instantiate(VarWithDistrib var) {
    var.ensureStable();

    /*
     * if (Util.verbose()) { System.out.println("Need to instantiate: " + var);
     * }
     */

    if (respVarsAndContexts.containsKey(var)) {
      cycleError(var);
    }

    // Create a new "child" context and get the distribution for
    // var in that context.
    respVarsAndContexts.put(var, this);
    ClassicInstantiatingEvalContext spawn = new ClassicInstantiatingEvalContext(
        world, respVarsAndContexts);
    spawn.afterSamplingListener = afterSamplingListener;
    DependencyModel.Distrib distrib = var.getDistrib(spawn);
    logProb += spawn.getLogProbability();
    //		respVarsAndContexts.remove(this); //this code in original implementation seems wrong (leili)
    respVarsAndContexts.remove(var);

    // Sample new value for var
    CondProbDistrib cpd = distrib.getCPD();
    List cpdArgs = distrib.getArgValues();
    Object newValue = cpd.sampleVal(cpdArgs, var.getType());
    double logProbForThisValue = cpd.getLogProb(cpdArgs, newValue);
    logProb += logProbForThisValue;

    // Assert any identifiers that are used by var
    Object[] args = var.args();
    for (int i = 0; i < args.length; ++i) {
      if (args[i] instanceof ObjectIdentifier) {
        world.assertIdentifier((ObjectIdentifier) args[i]);
      }
    }
    if (newValue instanceof ObjectIdentifier) {
      world.assertIdentifier((ObjectIdentifier) newValue);
    }

    // Actually set value
    world.setValue(var, newValue);

    if (afterSamplingListener != null) {
      afterSamplingListener.evaluate(var, newValue, logProbForThisValue);
    }

    if (staticAfterSamplingListener != null) {
      staticAfterSamplingListener.evaluate(var, newValue, logProbForThisValue);
    }

    /*
     * if (Util.verbose()) { System.out.println("Instantiated: " + var); }
     */

    return newValue;
  }

  protected void cycleError(VarWithDistrib var) {
    System.err.println("Encountered cycle in context-specific "
        + "dependency graph.  Evaluation sequence: ");
    for (Iterator iter = respVarsAndContexts.entrySet().iterator(); iter
        .hasNext();) {
      Map.Entry entry = (Map.Entry) iter.next();
      ((EvalContext) entry.getValue()).printEvalTrace(System.err);
      System.err.println(entry.getKey());
    }
    printEvalTrace(System.err);
    System.err.println(var);
    Util.fatalError("Stopping evaluation to avoid infinite loop.", false);
  }

  protected LinkedHashMap<VarWithDistrib, ClassicInstantiatingEvalContext> respVarsAndContexts; // VarWithDistrib to EvalContext    

  protected double logProb = 0;
}
