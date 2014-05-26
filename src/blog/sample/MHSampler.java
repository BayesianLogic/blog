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

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import blog.bn.BayesNetVar;
import blog.bn.NumberVar;
import blog.common.Timer;
import blog.common.Util;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.Query;
import blog.world.PartialWorld;
import blog.world.PartialWorldDiff;

/**
 * Generates samples from a Markov chain over possible worlds using a
 * Metropolis-Hastings algorithm. The proposal distribution for this algorithm
 * is represented by an object that implements the Proposer interface.
 * 
 * <p>
 * The MHSampler constructor looks at the following properties in the properties
 * table that is passed in:
 * <dl>
 * <dt>proposerClass
 * <dd>Name of the proposer class to use. This class must implement the Proposer
 * interface. Default: blog.GenericProposer.
 * </dl>
 * The property table is also passed to the proposer's constructor.
 */
public class MHSampler extends Sampler {
  /**
   * Creates a new sampler for the given BLOG model.
   **/
  public MHSampler(Model model) {
    super(model);
  }

  /**
   * Creates a new sampler for the given BLOG model. The properties table
   * specifies configuration parameters.
   */
  public MHSampler(Model model, Properties properties) {
    super(model);
    constructProposer(properties);
  }

  /** Method responsible for initializing the proposer field. */
  protected void constructProposer(Properties properties) {
    String proposerClassName = properties.getProperty("proposerClass",
        "blog.GenericProposer");
    System.out.println("Constructing M-H proposer of class "
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

  @Override
  public void initialize(Evidence evidence, List<Query> queries) {
    super.initialize(evidence, queries);

    ++numTrials;
    numSamplesThisTrial = 0;
    numAcceptedThisTrial = 0;

    if (Util.verbose())
      System.out.println("Creating initial world...");
    curWorld = proposer.initialize(evidence, queries);
    if (Util.verbose())
      System.out.println("Saving initial world...");
    Timer timer = new Timer();
    timer.start();
    curWorld.save();
    if (Util.verbose())
      System.out.println("Saving initial world took " + timer.elapsedTime()
          + " s");

    if (Util.verbose())
      System.out.println("Validating initial world...");
    if (!validateIdentifiers(curWorld)) {
      Util.fatalError("Fatal identifier errors in initial world.", false);
    }

    if (!evidence.isTrue(curWorld)) {
      throw new IllegalStateException(
          "Error: evidence is not true in initial world.");
    }
  }

  public void setWorld(PartialWorld w) {
    if (w instanceof PartialWorldDiff)
      curWorld = (PartialWorldDiff) w;
    else
      curWorld = new PartialWorldDiff(w);
  }

  /** For MH samplers, same as {@link #setWorld(PartialWorld)}. */
  public void setBaseWorld(PartialWorld world) {
    setWorld(world);
  }

  /**
   * Generates the next world in the Markov chain. This method gets a proposed
   * world from the Proposer. If the proposal is accepted, then the next world
   * is the proposed world; otherwise the next world is the same as the previous
   * world.
   */
  public void nextSample() {
    curWorld.save(); // make sure we start with saved world.

    ++totalNumSamples;
    ++numSamplesThisTrial;

    // Propose new world and get log proposal ratio, which is:
    // log (q(x | x') / q(x' | x))
    // where x is current world, x' is proposed new world, and
    // q is proposal distribution. The proposer changes curWorld,
    // but the saved world is unchanged, and we can revert curWorld
    // to this saved version if we reject the proposal.
    if (Util.verbose()) {
      System.out.println("Proposing world...");
    }
    double logProposalRatio = proposer.proposeNextState(curWorld);
    if (Util.verbose()) {
      System.out.println();
      System.out.println("\tlog proposal ratio: " + logProposalRatio);
    }

    if (!validateIdentifiers(curWorld)) {
      Util.fatalError("Fatal identifier errors in proposed world.", false);
    }

    // Compute the acceptance probability
    acceptProbTimer.start();
    double logProbRatio = computeLogProbRatio(curWorld.getSaved(), curWorld);
    if (Util.verbose()) {
      System.out.println("\tlog probability ratio: " + logProbRatio);
    }
    double logAcceptRatio = logProbRatio + logProposalRatio;
    if (Util.verbose()) {
      System.out.println("\tlog acceptance ratio: " + logAcceptRatio);
    }
    acceptProbTimer.stop();

    // Accept or reject proposal
    if ((logAcceptRatio >= 0) || (Util.random() < Math.exp(logAcceptRatio))) {
      worldUpdateTimer.start();
      // ensureQueriesAreDetAndSupported(); // this is not part of the MH
      // algorithm, but sampling done on top of it. Since this sampling is done
      // according to the model's distribution, it still converges to it.
      // I moved this to SamplingEngine to keep the MHSampler's "purity".
      curWorld.save();
      worldUpdateTimer.stop();
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

  /**
   * Samples from some given world, leaving the current world in sampler
   * undisturbed.
   */
  public PartialWorld nextSample(PartialWorld world) {
    PartialWorldDiff previousCurrentWorld = curWorld;
    setWorld(world);
    nextSample();
    PartialWorld result = getLatestWorld();
    curWorld = previousCurrentWorld;
    return result;
  }

  public PartialWorld getLatestWorld() {
    return curWorld; // for debugging
    // return curWorld.getSaved(); // return saved version for speed
  }

  /**
   * Checks whether identifiers are used properly in the given world. For every
   * identifier, the world should determine that it satisfies a particular POP
   * application, and the number variable for this POP application should be
   * instantiated. Also, every identifier should be the value of some basic
   * variable (specifically a random function application variable, since the
   * values of number variables are integers). Prints error messages if it finds
   * errors.
   * 
   * @return true if the world is valid, false otherwise
   */
  private boolean validateIdentifiers(PartialWorldDiff world) {
    boolean valid = true;

    Set newlyMissing = world.getNewlyOverloadedNumberVars();
    if (!newlyMissing.isEmpty()) {
      valid = false;
    }
    for (Iterator iter = newlyMissing.iterator(); iter.hasNext();) {
      NumberVar nv = (NumberVar) iter.next();
      System.err.println("Error: Number variable " + nv
          + " is satisfied by too many identifiers.");
    }

    Set newlyFloating = world.getNewlyFloatingIds();
    if (!newlyFloating.isEmpty()) {
      valid = false;
    }
    for (Iterator iter = newlyFloating.iterator(); iter.hasNext();) {
      System.err.println("Error: Identifier " + iter.next()
          + " is not the value of any basic variable.");
    }

    return valid;
  }

  /**
   * Computes the log probability ratio: log (p(x') / p(x)) where x is the
   * current world, x' is the proposed new world, and p is the posterior
   * distribution defined by the model and the evidence. This method treats
   * worlds that do not satisfy the evidence as having probability zero. This
   * means that the acceptance probability is 0 if the proposed new world does
   * not satisfy the evidence, and undefined if the current world does not
   * satisfy the evidence.
   */
  public double computeLogProbRatio(PartialWorld savedWorld,
      PartialWorldDiff proposedWorld) {
    double logProbRatio = 0;
    if (!evidence.isTrue(proposedWorld)) {
      logProbRatio = Double.NEGATIVE_INFINITY;
      return logProbRatio;
    }
    logProbRatio += computeLogMultRatio(savedWorld, proposedWorld);

    Set factorVars = proposedWorld.getVarsWithChangedProbs();
    for (Iterator iter = factorVars.iterator(); iter.hasNext();) {
      BayesNetVar v = (BayesNetVar) iter.next();
      double logProbInCurWorld = savedWorld.getLogProbOfValue(v);
      if (Util.verbose() && (logProbInCurWorld == Double.NEGATIVE_INFINITY)) {
        System.out.println("Zero probability in old world: " + v + " = "
            + savedWorld.getValue(v));
      }
      double logProbInProposedWorld = proposedWorld.getLogProbOfValue(v);
      if (Util.verbose()
          && (logProbInProposedWorld == Double.NEGATIVE_INFINITY)) {
        System.out.println("Zero probability in proposed world: " + v + " = "
            + proposedWorld.getValue(v));
      }

      // This is just in case the proposal changes the values of some
      // evidence variables.
      if (evidence.getEvidenceVars().contains(v)) {
        Object obsVal = evidence.getObservedValue(v);
        if (!savedWorld.getValue(v).equals(obsVal)) {
          logProbInCurWorld = Double.NEGATIVE_INFINITY;
        }
        if (!proposedWorld.getValue(v).equals(obsVal)) {
          logProbInProposedWorld = Double.NEGATIVE_INFINITY;
        }
      }

      if (Util.verbose()) {
        System.out.println("Variable " + v + " going from log prob "
            + logProbInCurWorld + " to log prob " + logProbInProposedWorld);
      }

      if (logProbInCurWorld != logProbInProposedWorld) {
        logProbRatio -= logProbInCurWorld;
        logProbRatio += logProbInProposedWorld;
      }
    }
    return logProbRatio;
  }

  private double computeLogMultRatio(PartialWorld savedWorld,
      PartialWorldDiff proposedWorld) {
    double logMultRatio = 0;

    Set varsWithChangedMultipliers = proposedWorld
        .getVarsWithChangedMultipliers();
    for (Iterator iter = varsWithChangedMultipliers.iterator(); iter.hasNext();) {
      NumberVar v = (NumberVar) iter.next();

      int oldNumSat = (savedWorld.getValue(v) == null) ? 0 : savedWorld
          .getSatisfiers(v).size();
      int oldNumIds = savedWorld.getAssertedIdsForPOPApp(v).size();

      int newNumSat = (proposedWorld.getValue(v) == null) ? 0 : proposedWorld
          .getSatisfiers(v).size();
      int newNumIds = proposedWorld.getAssertedIdsForPOPApp(v).size();

      if (Util.verbose()) {
        System.out.println("For " + v + ":");
        System.out.println("\tcurrently " + oldNumSat + " satisfiers, "
            + oldNumIds + " IDs");
        System.out.println("\tproposed " + newNumSat + " satisfiers, "
            + newNumIds + " IDs");
      }

      // The multiplier is n * (n-1) * ... * (n-k+1) where n is
      // the value of the number variable, and k is the number
      // of identifiers that satisfy the number variable. Note:
      // this multiplier is only correct if distinct assignments
      // of objects to identifiers yield concrete partial worlds
      // that represent disjoint events. One way to guarantee
      // this is to ensure that each identifier used in a
      // partial world is the value of some term in that partial
      // world.
      int afterLastInNumerator = newNumSat - newNumIds;
      int afterLastInDenominator = oldNumSat - oldNumIds;
      if ((afterLastInNumerator >= oldNumSat)
          || (afterLastInDenominator >= newNumSat)) {
        // no cancellation between numerator and denominator
        logMultRatio += Util.logPartialFactorial(newNumSat, newNumIds);
        logMultRatio -= Util.logPartialFactorial(oldNumSat, oldNumIds);
      } else {
        if (newNumSat > oldNumSat) {
          // numerator has uncanceled factors at beginning
          logMultRatio += Util.logPartialFactorial(newNumSat, newNumSat
              - oldNumSat);
        } else if (oldNumSat > newNumSat) {
          // denominator has uncanceled factors at beginning
          logMultRatio -= Util.logPartialFactorial(oldNumSat, oldNumSat
              - newNumSat);
        }

        if (afterLastInNumerator < afterLastInDenominator) {
          // numerator has uncanceled factors at end
          logMultRatio += Util.logPartialFactorial(afterLastInDenominator,
              afterLastInDenominator - afterLastInNumerator);
        } else if (afterLastInDenominator < afterLastInNumerator) {
          // denominator has uncanceled factors at end
          logMultRatio -= Util.logPartialFactorial(afterLastInNumerator,
              afterLastInNumerator - afterLastInDenominator);
        }
      }
    }

    return logMultRatio;
  }

  /**
   * Print statistics gathered during sampling to standard out. These figures
   * are gathered during each call to sample() and is called once at the end of
   * each trial.
   */
  public void printStats() {
    System.out.println("======== MH Trial Stats ========");
    if (totalNumSamples > 0) {
      if (numSamplesThisTrial > 0) {
        System.out.println("Fraction of proposals accepted (this trial): "
            + (numAcceptedThisTrial / (double) numSamplesThisTrial));
      }
      System.out
          .println("Fraction of proposals accepted (running avg, all trials): "
              + (totalNumAccepted / (double) totalNumSamples));
      System.out.println("Time spent computing acceptance probs: "
          + acceptProbTimer.elapsedTime() + " s");
      System.out.println("Time spent updating world: "
          + worldUpdateTimer.elapsedTime() + " s");
    } else {
      System.out.println("No samples yet.");
    }

    proposer.printStats();
  }

  protected Timer acceptProbTimer = new Timer();
  protected Timer worldUpdateTimer = new Timer();

  protected Proposer proposer;

  protected PartialWorldDiff curWorld;
  protected PartialWorld baseWorld = null;

  protected int numTrials = 0;
  protected int totalNumSamples = 0;
  protected int totalNumAccepted = 0;

  protected int numSamplesThisTrial = 0;
  protected int numAcceptedThisTrial = 0;
}
