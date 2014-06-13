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

package blog.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import blog.DBLOGUtil;
import blog.common.Util;
import blog.io.TableWriter;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.Query;
import blog.sample.AfterSamplingListener;
import blog.sample.DMHSampler;
import blog.sample.Sampler;
import blog.type.Timestep;

/**
 * A Particle Filter. It works by keeping a set of {@link Particles}, each
 * representing a partial world, weighted by the
 * evidence. It uses the following properties: <code>numParticles</code> or
 * <code>numSamples</code>: number of particles (default is <code>1000</code>).
 * <code>useDecayedMCMC</code>: takes values <code>true</code> or
 * <code>false</code> (the default). Whether to use rejuvenation, presented by
 * W. R. Gilks and C. Berzuini.
 * "Following a moving target --- Monte Carlo inference for dynamic Bayesian models."
 * Journal of the Royal Statistical Society, Series B, 63:127--146, 2001.
 * <code>numMoves</code>: the number of moves used in specified by the property
 * given at construction time (default is <code>1</code>).
 * 
 * The ParticleFilter is an unusual {@link InferenceEngine} in that it takes
 * evidence and queries additional to the ones taken by
 * {@link #setEvidence(Evidence)} and {@link #setQueries(List)}. The evidence
 * set by {@link #setEvidence(Evidence)} is used in the very beginning of
 * inference (thus keeping the general InferenceEngine semantics for it) and the
 * queries set by {@link #setQueries(List)} are used by {@link #answerQueries()}
 * only (again keeping the original InferenceEngine semantics).
 */
public class ParticleFilter extends InferenceEngine {

  /**
   * Creates a new particle filter for the given BLOG model, with configuration
   * parameters specified by the given properties table.
   */
  public ParticleFilter(Model model, Properties properties) {
    super(model);

    String numParticlesStr = properties.getProperty("numParticles");
    String numSamplesStr = properties.getProperty("numSamples");
    if (numParticlesStr != null && numSamplesStr != null
        && !numParticlesStr.equals(numSamplesStr))
      Util.fatalError("ParticleFilter received both numParticles and numSamples properties with distinct values.");
    if (numParticlesStr == null)
      numParticlesStr = numSamplesStr;
    if (numParticlesStr == null)
      numParticlesStr = "1000";
    try {
      numParticles = Integer.parseInt(numParticlesStr);
    } catch (NumberFormatException e) {
      Util.fatalErrorWithoutStack("Invalid number of particles: "
          + numParticlesStr); // do not dump stack.
    }

    String useDecayedMCMCStr = properties
        .getProperty("useDecayedMCMC", "false");
    useDecayedMCMC = Boolean.parseBoolean(useDecayedMCMCStr);
    // if (useDecayedMCMC) numParticles = 1;

    String numMovesStr = properties.getProperty("numMoves", "1");
    try {
      numMoves = Integer.parseInt(numMovesStr);
    } catch (NumberFormatException e) {
      Util.fatalErrorWithoutStack("Invalid number of moves: " + numMovesStr);
    }

    String idTypesString = properties.getProperty("idTypes", "none");
    idTypes = model.getListedTypes(idTypesString);
    if (idTypes == null) {
      Util.fatalErrorWithoutStack("Fatal error: invalid idTypes list.");
    }

    String samplerClassName = properties.getProperty("samplerClass",
        "blog.sample.LWSampler");
    System.out.println("Constructing sampler of class " + samplerClassName);
    particleSampler = Sampler.make(samplerClassName, model, properties);

    String queryReportIntervalStr = properties.getProperty(
        "queryReportInterval", "10");
    try {
      queryReportInterval = Integer.parseInt(queryReportIntervalStr);
    } catch (NumberFormatException e) {
      Util.fatalError("Invalid reporting interval: " + queryReportIntervalStr,
          false);
    }

    if (useDecayedMCMC)
      dmhSampler = new DMHSampler(model, properties);

    dataLogLik = 0;
  }

  /** Answers the queries provided at construction time. */
  public void answerQueries() {
    if (Util.verbose()) {
      System.out.println("Evidence: " + evidence);
      System.out.println("Query: " + queries);
    }
    System.out.println("Report every: " + queryReportInterval + " timesteps");
    resetAndTakeInitialEvidence();
    answer(queries);
    System.out.println("Log likelihood of data: " + dataLogLik);
  }

  /**
   * Prepares particle filter for a new sequence of evidence and queries by
   * generating a new set of particles from scratch, which are consistent with
   * evidence set by {@link #setEvidence(Evidence)} (if it has not been invoked,
   * empty evidence is assumed), ensuring behavior consistent with other
   * {@link InferenceEngine}s.
   */
  public void resetAndTakeInitialEvidence() {
    reset();
    takeEvidenceAndAnswerQuery();
  }

  private void reset() {
    System.out.println("Using " + numParticles + " particles...");
    int numTimeSlicesInMemory = useDecayedMCMC ? dmhSampler.getMaxRecall() : 1;
    if (evidence == null)
      evidence = new Evidence();
    if (queries == null)
      queries = new LinkedList();
    if (useDecayedMCMC)
      dmhSampler.initialize(evidence, queries);
    particles = new ArrayList();
    for (int i = 0; i < numParticles; i++) {
      Particle newParticle = makeParticle(idTypes, numTimeSlicesInMemory);
      particles.add(newParticle);
    }
    needsToBeResampledBeforeFurtherSampling = false;
  }

  private void takeEvidenceAndAnswerQuery() {
    // Split evidence and queries according to the timestep it occurs in.
    Map<Timestep, Evidence> slicedEvidence = DBLOGUtil
        .splitEvidenceInTime(evidence);
    Map<Timestep, List<Query>> slicedQueries = DBLOGUtil
        .splitQueriesInTime((List<Query>) queries);

    // Process atemporal evidence (if any) before everything else.
    if (slicedEvidence.containsKey(null)) {
      take(slicedEvidence.get(null));
    }

    // Process temporal evidence and queries in lockstep.
    List<Timestep> nonNullTimesteps = new ArrayList<Timestep>();
    nonNullTimesteps.addAll(slicedEvidence.keySet());
    nonNullTimesteps.addAll(slicedQueries.keySet());
    nonNullTimesteps.remove(null);
    // We use a TreeSet to remove duplicates and to sort the timesteps.
    // (We can't construct a TreeSet directly because it doesn't accept nulls.)
    TreeSet<Timestep> sortedTimesteps = new TreeSet<Timestep>(nonNullTimesteps);
    for (Timestep timestep : sortedTimesteps) {
      if (slicedEvidence.containsKey(timestep)) {
        take(slicedEvidence.get(timestep));
      }
      if (slicedQueries.containsKey(timestep)) {
        List<Query> currentQueries = slicedQueries.get(timestep);
        for (Particle particle : particles) {
          particle.answer(currentQueries);
        }
        if (timestep.intValue() % queryReportInterval == 0) {
          TableWriter tableWriter = new TableWriter(queries);
          tableWriter.setHeader("After timestep " + timestep.intValue());
          tableWriter.writeResults(System.out);
        }
      }
      removePriorTimeSlice(timestep);
    }

    // Process atemporal queries (if any) after all the evidence.
    if (slicedQueries.containsKey(null)) {
      List<Query> currentQueries = slicedQueries.get(null);
      for (Particle particle : particles) {
        particle.answer(currentQueries);
      }
    }
  }

  /**
   * A method making a particle (by default, {@link Particle}). Useful for
   * extensions using specialized particles (don't forget to specialize
   * {@link Particle#copy()} for it to return an object of its own class).
   */
  protected Particle makeParticle(Set idTypes, int numTimeSlicesInMemory) {
    return new Particle(idTypes, numTimeSlicesInMemory, particleSampler);
  }

  /**
   * remove all the temporal variables prior to the specified timestep
   * 
   * @param timestep
   *          Timestep before which the vars should be removed
   */
  public void removePriorTimeSlice(Timestep timestep) {
    // For now we assume numTimeSlicesInMemory = 1.
    for (Particle p : particles) {
      p.removePriorTimeSlice(timestep);
    }
  }

  /** Takes more evidence. */
  public void take(Evidence evidence) {
    if (particles == null)
      resetAndTakeInitialEvidence();

    if (!evidence.isEmpty()) { // must be placed after check on particles ==
                               // null because after this method the filter
                               // should be ready to take queries.

      if (needsToBeResampledBeforeFurtherSampling) {
        move();
        resample();
      }

      if (beforeTakesEvidence != null)
        beforeTakesEvidence.evaluate(evidence, this);

      for (Particle p : particles) {
        if (beforeParticleTakesEvidence != null)
          beforeParticleTakesEvidence.evaluate(p, evidence, this);
        p.take(evidence);
        if (afterParticleTakesEvidence != null)
          afterParticleTakesEvidence.evaluate(p, evidence, this);

        // if (!useDecayedMCMC) {
        // p.uninstantiatePreviousTimeslices();
        // p.removeAllDerivedVars();
        // }

      }

      double logSumWeights = Double.NEGATIVE_INFINITY;
      ListIterator particleIt = particles.listIterator();
      while (particleIt.hasNext()) {
        Particle particle = (Particle) particleIt.next();
        if (particle.getLatestLogWeight() < Sampler.NEGLIGIBLE_LOG_WEIGHT) {
          particleIt.remove();
        } else {
          logSumWeights = Util.logSum(logSumWeights,
              particle.getLatestLogWeight());
        }
      }

      if (particles.size() == 0)
        throw new IllegalArgumentException("All particles have zero weight");

      dataLogLik += logSumWeights;

      needsToBeResampledBeforeFurtherSampling = true;

      if (useDecayedMCMC)
        dmhSampler.add(evidence);

      if (afterTakesEvidence != null)
        afterTakesEvidence.evaluate(evidence, this);
    }
  }

  /**
   * Answer queries according to current distribution represented by filter.
   */
  public void answer(Collection queries) {
    if (particles == null)
      resetAndTakeInitialEvidence();

    if (useDecayedMCMC)
      dmhSampler.addQueries(queries);
  }

  public void answer(Query query) {
    answer(Util.list(query));
  }

  protected void resample() {
    double[] logWeights = new double[particles.size()];
    boolean[] alreadySampled = new boolean[particles.size()];
    double logSumWeights = Double.NEGATIVE_INFINITY;
    double[] normalizedWeights = new double[particles.size()];
    List newParticles = new ArrayList();

    for (int i = 0; i < particles.size(); i++) {
      logWeights[i] = ((Particle) particles.get(i)).getLatestLogWeight();
      logSumWeights = Util.logSum(logSumWeights, logWeights[i]);
    }

    if (logSumWeights == Double.NEGATIVE_INFINITY) {
      throw new IllegalArgumentException("All particles have zero weight");
    }

    for (int i = 0; i < particles.size(); i++) {
      normalizedWeights[i] = Math.exp(logWeights[i] - logSumWeights);
    }

    for (int i = 0; i < numParticles; i++) {
      int selection = Util.sampleWithProbs(normalizedWeights);
      if (!alreadySampled[selection]) {
        newParticles.add(particles.get(selection));
        alreadySampled[selection] = true;
      } else {
        newParticles.add(((Particle) particles.get(selection)).copy());
      }
    }

    particles = newParticles;
  }

  private void printLogWeights() {
    for (int i = 0; i < particles.size(); i++) {
      System.out.println(i + ":"
          + ((Particle) particles.get(i)).getLatestLogWeight());
    }
    System.out.println();
  }

  private void move() {
    if (!useDecayedMCMC)
      return;

    for (int i = 0; i < numMoves; i++) {
      for (Iterator iter = particles.iterator(); iter.hasNext();) {
        Particle p = (Particle) iter.next();
        p.setWorld(dmhSampler.nextSample(p.getLatestWorld()));
      }
    }
  }

  // PARTICLE TAKES EVIDENCE EVENT HANDLING
  /**
   * An interface specifying handlers for before and after a particle takes
   * evidence.
   */
  public static interface ParticleTakesEvidenceHandler {
    public void evaluate(Particle particle, Evidence evidence,
        ParticleFilter particleFilter);
  }

  /**
   * The {@link ParticleTakesEvidenceHandler} invoked right before a particle
   * takes evidence.
   */
  public ParticleTakesEvidenceHandler beforeParticleTakesEvidence;

  /**
   * The {@link ParticleTakesEvidenceHandler} invoked right after a particle
   * takes evidence.
   */
  public ParticleTakesEvidenceHandler afterParticleTakesEvidence;

  // FILTER TAKES EVIDENCE EVENT HANDLING
  /**
   * An interface specifying handlers for before and after the particle filter
   * takes evidence.
   */
  public static interface TakesEvidenceHandler {
    public void evaluate(Evidence evidence, ParticleFilter particleFilter);
  }

  /**
   * The {@link TakesEvidenceHandler} invoked right before a particle takes
   * evidence.
   */
  public TakesEvidenceHandler beforeTakesEvidence;

  /**
   * The {@link TakesEvidenceHandler} invoked right after a particle takes
   * evidence.
   */
  public TakesEvidenceHandler afterTakesEvidence;

  // END OF EVENT HANDLING

  public AfterSamplingListener getAfterSamplingListener() {
    return afterSamplingListener;
  }

  public void setAfterSamplingListener(
      AfterSamplingListener afterSamplingListener) {
    this.afterSamplingListener = afterSamplingListener;
    particleSampler.afterSamplingListener = afterSamplingListener;
  }

  private Set idTypes; // of Type

  private int numParticles;
  private boolean useDecayedMCMC;
  public List<Particle> particles;
  private int numMoves;
  private boolean needsToBeResampledBeforeFurtherSampling = false;
  private Sampler particleSampler;
  private AfterSamplingListener afterSamplingListener;
  private DMHSampler dmhSampler;
  private int queryReportInterval;
  private double dataLogLik; // log likelihood of the data
}
