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

package blog.sample;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import blog.BLOGUtil;
import blog.bn.BayesNetVar;
import blog.common.Util;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.Query;
import blog.model.Type;
import blog.world.DefaultPartialWorld;
import blog.world.PartialWorld;

/**
 * A likelihood weighting sampler. Instantiates the context-specifically active
 * ancestors of the query and evidence variables. Rather than sampling the
 * evidence variables, it just instantiates them to their observed values. The
 * weight of a sample is the product of the probabilities of the evidence
 * variables given their parents.
 * 
 * <p>
 * The LWSampler constructor looks at the following properties in the properties
 * table that is passed in:
 * <dl>
 * <dt>idTypes
 * <dd>Comma-separated list of names of types. Non-guaranteed objects of these
 * types will be represented by interchangeable identifiers rather than by
 * tuples. The value can also be "none", indicating that no objects should be
 * represented by identifiers, or "all", indicating that all non-guaranteed
 * objects should be represented by identifiers. Default: "none".
 * </dl>
 */
public class LWSampler extends Sampler {
  /**
   * Creates a new sampler for the given BLOG model. The properties table
   * specifies configuration parameters.
   */
  public LWSampler(Model model, Properties properties) {
    super(model);

    String idTypesString = properties.getProperty("idTypes", "none");
    idTypes = model.getListedTypes(idTypesString);
    if (idTypes == null) {
      Util.fatalErrorWithoutStack("Invalid idTypes list.");
    }
  }

  @Override
  public void initialize(Evidence evidence, List<Query> queries) {
    super.initialize(evidence, queries);
    for (Query query : queries) {
      queryVars.addAll(query.getVariables());
    }

    numSamplesThisTrial = 0;
    numConsistentThisTrial = 0;
    logSumWeightsThisTrial = Double.NEGATIVE_INFINITY;

    curWorld = null;
    latestSampleLogWeight = Double.NEGATIVE_INFINITY;
  }

  public void setBaseWorld(PartialWorld world) {
    baseWorld = world;
  }

  public PartialWorld getBaseWorld() {
    return baseWorld;
  }

  /**
   * Generates the next partial world and computes its weight.
   */
  public void nextSample() {
    if (baseWorld != null)
      curWorld = baseWorld;
    else
      curWorld = new DefaultPartialWorld(idTypes);

    latestSampleLogWeight = supportEvidenceAndCalculateLogWeight();
    BLOGUtil.ensureDetAndSupportedWithListener(queryVars, curWorld,
        afterSamplingListener);
    // if (Util.verbose()) {
    // System.out.println("Generated world:");
    // curWorld.print(System.out);
    // System.out.println("Log weight: " + latestSampleLogWeight);
    // }

    ++totalNumSamples;
    ++numSamplesThisTrial;
    if (latestSampleLogWeight > NEGLIGIBLE_LOG_WEIGHT) {
      ++totalNumConsistent;
      ++numConsistentThisTrial;
    }
    logSumWeightsThisTrial = Util.logSum(logSumWeightsThisTrial,
        latestSampleLogWeight);
  }

  protected double supportEvidenceAndCalculateLogWeight() {
    evidence.setEvidenceAndEnsureSupported(curWorld);
    return evidence.getEvidenceLogProb(curWorld);
  }

  public PartialWorld getLatestWorld() {
    if (curWorld == null) {
      throw new IllegalStateException("LWSampler has no latest sample.");
    }
    return curWorld;
  }

  public double getLatestLogWeight() {
    return latestSampleLogWeight;
  }

  /**
   * Print statistics gathered during sampling to standard out. These figures
   * are gathered during each call to sample(). This method should be called
   * once at the end of each trial.
   */
  public void printStats() {
    this.printStats("");
  }

  public void printStats(String samplerType) {
    System.out.println("======== " + samplerType + " LW Trial Stats =========");

    if (numSamplesThisTrial > 0) {
      double logAvgWeight = logSumWeightsThisTrial
          - java.lang.Math.log(numSamplesThisTrial);
      System.out.println("Log of average likelihood weight (this trial): "
          + logAvgWeight);
      System.out.println("Average likelihood weight (this trial): "
          + java.lang.Math.exp(logAvgWeight));
      System.out.println("Fraction of consistent worlds (this trial): "
          + (numConsistentThisTrial / (double) numSamplesThisTrial));
    }

    if (totalNumSamples > 0) {
      System.out
          .println("Fraction of consistent worlds (running avg, all trials): "
              + (totalNumConsistent / (double) totalNumSamples));
    } else {
      System.out.println("No samples yet.");
    }
  }

  protected Set<Type> idTypes; // of Type
  protected List<BayesNetVar> queryVars = new ArrayList<BayesNetVar>();

  protected PartialWorld curWorld = null;
  private PartialWorld baseWorld = null;
  protected double latestSampleLogWeight = Double.NEGATIVE_INFINITY;

  // overall statistics
  protected int totalNumSamples = 0;
  protected int totalNumConsistent = 0;

  // statistics since last call to initialize()
  protected int numSamplesThisTrial = 0;
  protected int numConsistentThisTrial = 0;
  protected double logSumWeightsThisTrial = 0;
}
