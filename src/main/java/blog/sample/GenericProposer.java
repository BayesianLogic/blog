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

package blog.sample;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Set;

import blog.bn.BasicVar;
import blog.bn.BayesNetVar;
import blog.bn.DerivedVar;
import blog.bn.VarWithDistrib;
import blog.common.Util;
import blog.distrib.CondProbDistrib;
import blog.model.DependencyModel;
import blog.model.Model;
import blog.world.PartialWorld;
import blog.world.PartialWorldDiff;

/**
 * Implements a generic MCMC proposal algorithm. Each step changes the value of
 * exactly one instantiated RV (selected uniformly at random from the
 * instantiated RVs), then samples values for previously uninstantiated RVs to
 * make the resulting instantiation self-supporting. It also uninstantiates any
 * RVs that become barren.
 * 
 * <p>
 * Each variable is sampled from its prior distribution given its parents. The
 * forward proposal probability is just the probability of choosing the selected
 * variable to change, times the product of the probabilities of values sampled
 * (for that variable and any other variables that must be instantiated to make
 * the instantiation self-supporting). The backward probability is the
 * probability of choosing the same variable again, choosing its old value, and
 * re-instantiating all the newly uninstantiated variables to their old values.
 * No other moves can yield the same proposed world because each move changes
 * the value of exactly one instantiated variable (it just instantiates and
 * uninstantiates other variables).
 * 
 * <p>
 * GenericProposer does not use identifiers in the worlds it constructs.
 */
public class GenericProposer extends AbstractProposer {

  /**
   * Creates a new GenericProposer that proposes possible worlds for the given
   * model. The properties table, specifying configuration parameters, is passed
   * to the LWSampler that is used to generate the initial world.
   */
  public GenericProposer(Model model, Properties properties) {
    super(model, properties);
  }

  protected class PickVarToSampleResult {
    public VarWithDistrib varToSample;
    public int numberOfChoices;

    public PickVarToSampleResult(VarWithDistrib varToSample, int numberOfChoices) {
      this.varToSample = varToSample;
      this.numberOfChoices = numberOfChoices;
    }
  }

  protected PickVarToSampleResult pickVarToSample(PartialWorld world) {
    Set eligibleVars = new HashSet(world.getInstantiatedVars());
    eligibleVars.removeAll(evidenceVars);

    // Uniformly sample a random variable from the support network.
    VarWithDistrib varToSample = (VarWithDistrib) Util
        .uniformSample(eligibleVars);

    return new PickVarToSampleResult(varToSample, eligibleVars.size());
  }

  /**
   * Proposes a next state for the Markov chain given the current state. The
   * proposedWorld argument is a PartialWorldDiff that the proposer can modify
   * to create the proposal; the saved version of this PartialWorldDiff is the
   * state before the proposal. Returns the log proposal ratio: log (q(x | x') /
   * q(x' | x))
   */
  public double proposeNextState(PartialWorldDiff world) {
    if (evidence == null) {
      throw new IllegalStateException(
          "initialize() has not been called on proposer.");
    }

    logProbForward = 0;
    logProbBackward = 0;

    PickVarToSampleResult result = pickVarToSample(world);
    chosenVar = result.varToSample;

    if (result.varToSample == null)
      return 1.0;

    if (Util.verbose())
      System.out.println("  sampling " + result.varToSample);

    // Multiply in the probability of this uniform sample.
    logProbForward += (-Math.log(result.numberOfChoices));

    if (Util.verbose()) {
      System.out.println("GenericProposer: world right before sampling"
          + " new value for " + result.varToSample + ".\n");
      System.out.println(world);
    }

    // Sample value for variable and update forward and backward probs
    sampleValue(result.varToSample, world);

    if (Util.verbose()) {
      System.out.println("GenericProposer: world right before getting"
          + " newly barren vars.\n");
      System.out.println(world);
    }

    // Remove barren variables
    Set evidenceAndQueryVars = new HashSet();
    for (Iterator iter = evidenceVars.iterator(); iter.hasNext();) {
      BayesNetVar curVar = (BayesNetVar) iter.next();
      if (curVar instanceof BasicVar) {
        evidenceAndQueryVars.add(curVar);
      } else if (curVar instanceof DerivedVar) {
        evidenceAndQueryVars.addAll(curVar.getParents(world));
      }
    }
    for (Iterator iter = queryVars.iterator(); iter.hasNext();) {
      BayesNetVar curVar = (BayesNetVar) iter.next();
      if (curVar instanceof BasicVar) {
        evidenceAndQueryVars.add(curVar);
      } else if (curVar instanceof DerivedVar) {
        evidenceAndQueryVars.addAll(curVar.getParents(world));
      }
    }
    for (Iterator iter = world.getVarsWithValue(Model.NULL).iterator(); iter
        .hasNext();) {
      BasicVar curVar = (BasicVar) iter.next();
      world.setValue(curVar, null);
    }
    boolean OK;
    do {
      OK = true;
      for (Iterator iter = world.getInstantiatedVars().iterator(); iter
          .hasNext();) {
        BasicVar curVar = (BasicVar) iter.next();
        PartialWorldDiff tmpWorld = new PartialWorldDiff(world);
        tmpWorld.setValue(curVar, null);
        if (evidenceAndQueriesAreSupported(evidenceAndQueryVars, tmpWorld)) {
          world.setValue(curVar, null);
          OK = false;
          logProbBackward += world.getSaved().getLogProbOfValue(curVar);
        }
      }
    } while (!OK);

    // Uniform sampling from new world.
    logProbBackward += (-Math.log(world.getInstantiatedVars().size()
        - numBasicEvidenceVars));
    return (logProbBackward - logProbForward);
  }

  /**
   * Proposes a next state for the Markov chain given the current state. The
   * proposedWorld argument is a PartialWorldDiff that the proposer can modify
   * to create the proposal; the saved version of this PartialWorldDiff is the
   * state before the proposal. Returns the log proposal ratio: log (q(x | x') /
   * q(x' | x))
   */
  public double proposeNextState(PartialWorldDiff world,
      VarWithDistrib varToSample) {
    if (evidence == null) {
      throw new IllegalStateException(
          "initialize() has not been called on proposer.");
    }

    logProbForward = 0;
    logProbBackward = 0;

    PickVarToSampleResult result = pickVarToSample(world);
    chosenVar = varToSample;

    if (result.varToSample == null)
      return 1.0;

    if (Util.verbose())
      System.out.println("  sampling " + varToSample);

    // Multiply in the probability of this uniform sample.
    logProbForward += (-Math.log(result.numberOfChoices));

    if (Util.verbose()) {
      System.out.println("GenericProposer: world right before sampling"
          + " new value for " + varToSample + ".\n");
      System.out.println(world);
    }

    // Sample value for variable and update forward and backward probs
    sampleValue(varToSample, world);

    if (Util.verbose()) {
      System.out.println("GenericProposer: world right before getting"
          + " newly barren vars.\n");
      System.out.println(world);
    }

    // Remove barren variables
    Set evidenceAndQueryVars = new HashSet();
    for (Iterator iter = evidenceVars.iterator(); iter.hasNext();) {
      BayesNetVar curVar = (BayesNetVar) iter.next();
      if (curVar instanceof BasicVar) {
        evidenceAndQueryVars.add(curVar);
      } else if (curVar instanceof DerivedVar) {
        evidenceAndQueryVars.addAll(curVar.getParents(world));
      }
    }
    for (Iterator iter = queryVars.iterator(); iter.hasNext();) {
      BayesNetVar curVar = (BayesNetVar) iter.next();
      if (curVar instanceof BasicVar) {
        evidenceAndQueryVars.add(curVar);
      } else if (curVar instanceof DerivedVar) {
        evidenceAndQueryVars.addAll(curVar.getParents(world));
      }
    }
    for (Iterator iter = world.getVarsWithValue(Model.NULL).iterator(); iter
        .hasNext();) {
      BasicVar curVar = (BasicVar) iter.next();
      world.setValue(curVar, null);
    }
    boolean OK;
    do {
      OK = true;
      for (Iterator iter = world.getInstantiatedVars().iterator(); iter
          .hasNext();) {
        BasicVar curVar = (BasicVar) iter.next();
        PartialWorldDiff tmpWorld = new PartialWorldDiff(world);
        tmpWorld.setValue(curVar, null);
        if (evidenceAndQueriesAreSupported(evidenceAndQueryVars, tmpWorld)) {
          world.setValue(curVar, null);
          OK = false;
          logProbBackward += world.getSaved().getLogProbOfValue(curVar);
        }
      }
    } while (!OK);

    // Uniform sampling from new world.
    logProbBackward += (-Math.log(world.getInstantiatedVars().size()
        - numBasicEvidenceVars));
    return (logProbBackward - logProbForward);
  }

  /**
   * Proposes a next state for the Markov chain given the current state and the
   * current value. This method is written for Gibbs Sampler and therefore we
   * need to pass the objective value for the sampled variable. The method
   * returns the log probability of \log\frac{\Pr[var|\mathcal
   * \sigma_{T_{var}^{var=value}]}}{|V(var)|}, which is the part of Gibbs weight
   * no related to the core variable set. The other part will be computed in the
   * Gibbs Sampler itself.
   * 
   */
  public double proposeNextState(PartialWorldDiff world, BayesNetVar var,
      Object value) {
    if (evidence == null) {
      throw new IllegalStateException(
          "initialize() has not been called on proposer.");
    }

    logProbGibbs = 0;

    if (!(var instanceof VarWithDistrib)) {
      throw new IllegalStateException(
          "Try to sample a non-distribution variable in Gibbs Sampling.");
    }

    // Sample value for variable and update forward and backward probs
    sampleValue((VarWithDistrib) var, value, world);

    if (Util.verbose()) {
      System.out.println("GenericProposer: world right before getting"
          + " newly barren vars.\n");
      System.out.println(world);
    }

    Set evidenceAndQueryVars = new HashSet();
    for (Iterator iter = evidenceVars.iterator(); iter.hasNext();) {
      BayesNetVar curVar = (BayesNetVar) iter.next();
      if (curVar instanceof BasicVar) {
        evidenceAndQueryVars.add(curVar);
      } else if (curVar instanceof DerivedVar) {
        evidenceAndQueryVars.addAll(curVar.getParents(world));
      }
    }
    for (Iterator iter = queryVars.iterator(); iter.hasNext();) {
      BayesNetVar curVar = (BayesNetVar) iter.next();
      if (curVar instanceof BasicVar) {
        evidenceAndQueryVars.add(curVar);
      } else if (curVar instanceof DerivedVar) {
        evidenceAndQueryVars.addAll(curVar.getParents(world));
      }
    }
    for (Iterator iter = world.getVarsWithValue(Model.NULL).iterator(); iter
        .hasNext();) {
      BasicVar curVar = (BasicVar) iter.next();
      world.setValue(curVar, null);
    }
    boolean OK;
    do {
      OK = true;
      for (Iterator iter = world.getInstantiatedVars().iterator(); iter
          .hasNext();) {
        BasicVar curVar = (BasicVar) iter.next();
        PartialWorldDiff tmpWorld = new PartialWorldDiff(world);
        tmpWorld.setValue(curVar, null);
        if (evidenceAndQueriesAreSupported(evidenceAndQueryVars, tmpWorld)) {
          world.setValue(curVar, null);
          OK = false;
          logProbBackward += world.getSaved().getLogProbOfValue(curVar);
        }
      }
    } while (!OK);

    // Uniform sampling from new world.
    logProbGibbs += (-Math.log(world.getInstantiatedVars().size()
        - numBasicEvidenceVars));
    return logProbGibbs;
  }

  /**
   * Check whether the set of evidence and queries are all supported or not in
   * the given partial world. Also check whether the world is self-supported or
   * not. Returns true if all the evidence and queries are supported and also
   * the world is self-supported.
   * 
   */
  protected boolean evidenceAndQueriesAreSupported(Set evidenceAndQueries,
      PartialWorld world) {
    PartialWorldDiff tmpWorld = new PartialWorldDiff(world);
    for (Iterator iter = evidenceAndQueries.iterator(); iter.hasNext();) {
      BasicVar var = (BasicVar) iter.next();
      if (!tmpWorld.isInstantiated(var)) {
        return false;
      }
      TraceParentRecEvalContext context = new TraceParentRecEvalContext(
          tmpWorld);
      if (var instanceof VarWithDistrib) {
        ((VarWithDistrib) var).getDistrib(context);
        if (context.getNumCalculateNewVars() > 0) {
          return false;
        }
      }
    }
    for (Iterator iter = tmpWorld.getInstantiatedVars().iterator(); iter
        .hasNext();) {
      BasicVar var = (BasicVar) iter.next();
      if (!tmpWorld.isInstantiated(var)) {
        return false;
      }
      TraceParentRecEvalContext context = new TraceParentRecEvalContext(
          tmpWorld);
      if (var instanceof VarWithDistrib) {
        ((VarWithDistrib) var).getDistrib(context);
        if (context.getNumCalculateNewVars() > 0) {
          return false;
        }
      }
    }
    return true;
  }

  // Samples a new value for the given variable (which must be
  // supported in <code>world</code>) and sets this new value as the
  // value of the variable in <code>world</code>. Then ensures that
  // <code>world</code> is self-supporting by calling
  // ensureDetAndSupported on each of the variable's children. Also
  // updates the logProbForward and logProbBackward variables.
  protected void sampleValue(VarWithDistrib varToSample, PartialWorld world) {
    // Save child set before graph becomes out of date
    Set children = new HashSet();
    children.addAll(world.getCBN().getChildren(varToSample));
    children.addAll(evidenceVars);
    children.addAll(queryVars);
    DependencyModel.Distrib distrib = varToSample
        .getDistrib(new DefaultEvalContext(world, true));
    CondProbDistrib cpd = distrib.getCPD();
    cpd.setParams(distrib.getArgValues());
    Object oldValue = world.getValue(varToSample);
    chosenVarOldValue = oldValue;
    logProbBackward += Math.log(cpd.getProb(oldValue));

    Object newValue = cpd.sampleVal();
    chosenVarNewValue = newValue;
    world.setValue(varToSample, newValue);
    logProbForward += Math.log(cpd.getProb(newValue));

    // Make the world self-supporting. The only variables whose active
    // parent sets could have changed are the children of varToSample.
    ClassicInstantiatingEvalContext instantiator = new ClassicInstantiatingEvalContext(
        world);

    for (Iterator childrenIter = children.iterator(); childrenIter.hasNext();) {
      BayesNetVar child = (BayesNetVar) childrenIter.next();
      if (!world.isInstantiated(child) && !(child instanceof DerivedVar))
        continue;
      child.ensureDetAndSupported(instantiator);
    }

    logProbForward += instantiator.getLogProbability();
  }

  protected void sampleValue(VarWithDistrib varToSample, Object value,
      PartialWorld world) {
    // Save child set before graph becomes out of date
    Set children = new HashSet();
    children.addAll(world.getCBN().getChildren(varToSample));
    children.addAll(evidenceVars);
    children.addAll(queryVars);
    world.setValue(varToSample, value);
    logProbGibbs += varToSample.getDistrib(new DefaultEvalContext(world, true))
        .getCPD().getLogProb(value);

    // Make the world self-supporting. The only variables whose active
    // parent sets could have changed are the children of varToSample.
    ClassicInstantiatingEvalContext instantiator = new ClassicInstantiatingEvalContext(
        world);

    for (Iterator childrenIter = children.iterator(); childrenIter.hasNext();) {
      BayesNetVar child = (BayesNetVar) childrenIter.next();

      if (!world.isInstantiated(child) && !(child instanceof DerivedVar))
        continue;

      child.ensureDetAndSupported(instantiator);
    }
  }

  /**
   * Reduce the current partial world to the core. That is, only leave the core
   * variables and the sampled variable instantiated and uninstantiate the other
   * variables.
   */
  public PartialWorldDiff reduceToCore(PartialWorld curWorld, BayesNetVar var) {
    if (!(var instanceof BasicVar) || !curWorld.isInstantiated(var)) {
      throw new IllegalStateException(
          "Sampled variable is not an instantiated BasicVar.");
    }
    LinkedList coreBFS = new LinkedList();
    Set core = new HashSet();
    for (Iterator iter = evidenceVars.iterator(); iter.hasNext();) {
      BayesNetVar curVar = (BayesNetVar) iter.next();
      if (!coreBFS.contains(curVar))
        coreBFS.addLast(curVar);
    }
    for (Iterator iter = queryVars.iterator(); iter.hasNext();) {
      BayesNetVar curVar = (BayesNetVar) iter.next();
      if (!coreBFS.contains(curVar))
        coreBFS.addLast(curVar);
    }
    while (!coreBFS.isEmpty()) {
      BayesNetVar curVar = (BayesNetVar) coreBFS.removeFirst();
      if (curVar instanceof BasicVar) {
        if (curWorld.isInstantiated(curVar)) {
          core.add(curVar);
        }
      } else if (!(curVar instanceof DerivedVar)) {
        continue;
      }
      Set parentVars = curVar.getParents(curWorld);
      for (Iterator iter = parentVars.iterator(); iter.hasNext();) {
        BayesNetVar parVar = (BayesNetVar) iter.next();
        if (!curWorld.getCBN().isContingentOn(curWorld, var, parVar, curVar)) {
          coreBFS.addLast(parVar);
        }
      }
    }
    Set varsToUninstantiate = new HashSet();
    varsToUninstantiate.addAll(curWorld.getInstantiatedVars());
    varsToUninstantiate.removeAll(core);
    varsToUninstantiate.remove(var);
    PartialWorldDiff newWorld = new PartialWorldDiff(curWorld);
    for (Iterator iter = varsToUninstantiate.iterator(); iter.hasNext();) {
      BasicVar curVar = (BasicVar) iter.next();
      newWorld.setValue(curVar, null);
    }
    return newWorld;
  }

  private double logProbGibbs;

  // The following are for debugger use only!

  private VarWithDistrib chosenVar = null;
  private Object chosenVarOldValue = null;
  private Object chosenVarNewValue = null;

  public VarWithDistrib latestChosenVar() {
    return chosenVar;
  }

  public Object latestChosenVarOldValue() {
    return chosenVarOldValue;
  }

  public Object latestChosenVarNewValue() {
    return chosenVarNewValue;
  }

  public double latestLogProbBackward() {
    return logProbBackward;
  }

  public double latestLogProbForward() {
    return logProbForward;
  }

  // End of debugger-only members.
}
