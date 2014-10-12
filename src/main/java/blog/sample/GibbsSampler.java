/*
 * Copyright (c) 2012, Regents of the University of California
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

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import blog.bn.BayesNetVar;
import blog.bn.VarWithDistrib;
import blog.common.Util;
import blog.model.Model;
import blog.world.PartialWorldDiff;

/**
 * An implementation of the open universe Gibbs Sampler described by
 * Arora et. al. This sampler differs from a standard Gibbs sampler
 * in the fact that it shrinks and expands the CBN during sampling steps
 * to account for the changes in CBN structure as the values of random
 * variables change.
 * 
 * This implementation is built as a modification of the MH sampler since
 * many of the CBN manipulations are the same for Gibbs as for MH and since this
 * Gibbs sampler reverts to MH sampling for variables of infinite domain.
 * 
 * 
 * @author Da Tang
 * @date Sep 7, 2014
 */

public class GibbsSampler extends MHSampler {

  /**
   * Creates a new Gibbs Sampler for a given BLOG model.
   */
  public GibbsSampler(Model model) {
    super(model);
  }

  /**
   * Creates a new Gibbs Sampler for a given BLOG model.
   */
  public GibbsSampler(Model model, Properties properties) {
    super(model);
    // For now, only the generic proposer is allowed
    // properties.setProperty("proposerClass", "blog.VariableGibbsProposer");
    constructProposer(properties);
  }

  /** Method responsible for initializing the proposer field. */
  protected void constructProposer(Properties properties) {
    String proposerClassName = properties.getProperty("proposerClass",
        "blog.sample.GenericProposer");
    System.out.println("Constructing Gibbs Sampling proposer of class "
        + proposerClassName);

    try {
      Class proposerClass = Class.forName(proposerClassName);
      Class[] paramTypes = { Model.class, Properties.class };
      Constructor constructor = proposerClass.getConstructor(paramTypes);

      Object[] args = { model, properties };
      proposer = (Proposer) constructor.newInstance(args);
    } catch (Exception e) {
      Util.fatalError(e);
    }
  }

  /**
   * Generates the next partial world by Gibbs sampling: Randomly selects a
   * non-evidence variable X. Reduces the current instantiation
   * to its core, and resamples X from this core. Ensures that the resulting
   * world is minimal and self supported.
   */
  public void nextSample() {
    // Find Nonevidence Variables in Current World
    Set eligibleVars = new HashSet(curWorld.getInstantiatedVars());
    eligibleVars.removeAll(evidence.getEvidenceVars());
    ++totalNumSamples;
    ++numSamplesThisTrial;

    // Return if no vars to sample
    if (eligibleVars.isEmpty())
      return;

    // Find Variable to Sample
    VarWithDistrib varToSample = (VarWithDistrib) Util
        .uniformSample(eligibleVars);

    if (Util.verbose())
      System.out.println("Sampling " + varToSample);

    Object[] finiteSupport = varToSample
        .getDistrib(new DefaultEvalContext(curWorld)).getCPD()
        .getFiniteSupport();

    // If domain size is finite
    if (finiteSupport != null) {
      // Calculate possible transitions and their weights
      curWorld.save();
      int supportSize = finiteSupport.length;
      double[] weights = new double[supportSize];
      PartialWorldDiff[] diffs = new PartialWorldDiff[supportSize];

      PartialWorldDiff reducedWorld = proposer.reduceToCore(curWorld,
          varToSample);

      for (int i = 0; i < supportSize; i++) {
        // Set varToSample to i-th value in domain
        // Ensure Minimal Self-Supported Instantiation
        PartialWorldDiff proposedWorld;
        if (finiteSupport[i].equals(curWorld.getValue(varToSample))) {
          proposedWorld = new PartialWorldDiff(curWorld);
        } else {
          proposedWorld = new PartialWorldDiff(curWorld, reducedWorld);
        }
        double logProposalRatio = proposer.proposeNextState(proposedWorld,
            varToSample, finiteSupport[i]);
        // double weight = evidence.getEvidenceProb(curWorld);
        for (Iterator iter = reducedWorld.getInstantiatedVars().iterator(); iter
            .hasNext();) {
          BayesNetVar curVar = (BayesNetVar) iter.next();
          if (curVar.getParents(proposedWorld).contains(varToSample)) {
            logProposalRatio += proposedWorld.getLogProbOfValue(curVar);
          }
        }
        if (!proposedWorld.getVarsWithValue(Model.NULL).isEmpty()
            || !evidence.isTrue(proposedWorld))
          logProposalRatio = Double.NEGATIVE_INFINITY;
        weights[i] = Math.exp(logProposalRatio
            + computeLogMultRatio(curWorld, proposedWorld));
        diffs[i] = proposedWorld;
      }

      int idx = Util.sampleWithProbs(Util.normalize(weights));
      if (idx < 0) {
        int mt = 250;
        mt = mt + mt;
      }
      PartialWorldDiff selected = diffs[idx];

      // Save the selected world
      selected.save();
    } else {
      // Infinite Domain Size so we fall back to MH Sampling
      curWorld.save(); // make sure we start with saved world.
      double logProposalRatio = ((GenericProposer) proposer).proposeNextState(
          curWorld, varToSample);

      if (Util.verbose()) {
        System.out.println();
        System.out.println("\tlog proposal ratio: " + logProposalRatio);
      }

      double logProbRatio = computeLogProbRatio(curWorld.getSaved(), curWorld);
      if (Util.verbose()) {
        System.out.println("\tlog probability ratio: " + logProbRatio);
      }
      double logAcceptRatio = logProbRatio + logProposalRatio;
      if (Util.verbose()) {
        System.out.println("\tlog acceptance ratio: " + logAcceptRatio);
      }

      // Accept or reject proposal
      if ((logAcceptRatio >= 0) || (Util.random() < Math.exp(logAcceptRatio))) {
        curWorld.save();
        if (Util.verbose()) {
          System.out.println("\taccepted");
        }
        ++totalNumAccepted;
        ++numAcceptedThisTrial;
        proposer.updateStats(true);
      } else {
        curWorld.revert(); // clean slate for next proposal
        if (Util.verbose()) {
          System.out.println("\trejected");
        }
        proposer.updateStats(false);
      }

    }
  }

  // Num Samples Drawn Thus Far
  protected int totalNumSamples = 0;
  protected int totalNumAccepted = 0;
  protected int numSamplesThisTrial = 0;
  protected int numAcceptedThisTrial = 0;
}
