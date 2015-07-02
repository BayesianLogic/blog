package blog.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import blog.DBLOGUtil;
import blog.common.Util;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.Queries;
import blog.model.Type;
import blog.sample.AbstractProposer;
import blog.sample.MHSampler;
import blog.sample.Sampler;
import blog.type.Timestep;
import blog.world.DefaultPartialWorld;

/**
 * Resample-move particle filter with customizable proposal distribution for the
 * move step.
 * 
 * Does not forget past timesteps, since they are needed to compute the
 * acceptance ratio in the move step.
 * 
 * @author cberzan
 * @since May 26, 2015
 */
public class ResampleMovePF extends InferenceEngine {

  /**
   * Creates a new particle filter for the given BLOG model, with configuration
   * parameters specified by the given properties table.
   */
  public ResampleMovePF(Model model, Properties properties) {
    super(model);

    String numParticlesStr = properties.getProperty("numParticles");
    String numSamplesStr = properties.getProperty("numSamples");
    if (numParticlesStr != null && numSamplesStr != null
        && !numParticlesStr.equals(numSamplesStr))
      Util.fatalError("ResampleMovePF received both numParticles and numSamples properties with distinct values.");
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

    dataLogLik = 0;

    // TODO: make this a parameter
    numMHIters = 10;

    mhSampler = new MHSampler(model, new Properties());
    mhSampler.initialize(new Evidence(null), new Queries(null));
  }

  /** Answers the queries provided at construction time. */
  public void answerQueries() {
    if (Util.verbose()) {
      System.out.println("Evidence: " + evidence);
      System.out.println("Query: " + queries);
    }
    System.out.println("Report every: " + queryReportInterval + " timesteps");
    reset();
    takeEvidenceAndAnswerQuery();
    System.out.println("Log likelihood of data: " + dataLogLik);
  }

  private void reset() {
    System.out.println("Using " + numParticles + " particles...");
    if (evidence == null) {
      evidence = new Evidence(model);
    }
    if (queries == null) {
      queries = new Queries(model);
    }
    particles = new ArrayList<Particle>();
    for (int i = 0; i < numParticles; i++) {
      Particle newParticle = makeParticle(idTypes);
      particles.add(newParticle);
    }
    needsToBeResampledBeforeFurtherSampling = false;
  }

  private void takeEvidenceAndAnswerQuery() {
    // Split evidence and queries according to the timestep it occurs in.
    Map<Timestep, Evidence> slicedEvidence = DBLOGUtil
        .splitEvidenceInTime(evidence);
    Map<Timestep, Queries> slicedQueries = DBLOGUtil
        .splitQueriesInTime(queries);

    // Process atemporal evidence (if any) before everything else.
    if (slicedEvidence.containsKey(null)) {
      take(slicedEvidence.get(null));
    }

    // Process temporal evidence and queries in lockstep.
    List<Timestep> nonNullTimesteps = new ArrayList<Timestep>();
    nonNullTimesteps.addAll(slicedEvidence.keySet());
    nonNullTimesteps.addAll(slicedQueries.keySet());
    nonNullTimesteps.removeAll(Collections.singleton(null));
    // We use a TreeSet to remove duplicates and to sort the timesteps.
    // (We can't construct a TreeSet directly because it doesn't accept nulls.)
    TreeSet<Timestep> sortedTimesteps = new TreeSet<Timestep>(nonNullTimesteps);
    for (Timestep timestep : sortedTimesteps) {
      if (slicedEvidence.containsKey(timestep)) {
        take(slicedEvidence.get(timestep));
      }
      if (slicedQueries.containsKey(timestep)) {
        Queries currentQueries = slicedQueries.get(timestep);
        for (Particle particle : particles) {
          particle.answer(currentQueries);
        }
        if (currentQueries != null) {
          writer.writeAllResults(currentQueries);
          currentQueries.reset();
        }
      }
    }

    // Process atemporal queries (if any) after all the evidence.
    if (slicedQueries.containsKey(null)) {
      Queries currentQueries = slicedQueries.get(null);
      for (Particle particle : particles) {
        particle.answer(currentQueries);
      }
      writer.writeAllResults(currentQueries);
      currentQueries.reset();
    }
  }

  /**
   * A method making a particle (by default, {@link Particle}). Useful for
   * extensions using specialized particles (don't forget to specialize
   * {@link Particle#copy()} for it to return an object of its own class).
   */
  protected Particle makeParticle(Set<? extends Type> idTypes) {
    DefaultPartialWorld world = new DefaultPartialWorld(idTypes, false, true);
    return new Particle(particleSampler, world);
  }

  /** Takes more evidence. */
  public void take(Evidence evidence) {
    if (evidence.isEmpty()) {
      return;
    }

    if (needsToBeResampledBeforeFurtherSampling) {
      resample();
      move();
    }

    for (Particle p : particles) {
      p.take(evidence);
    }

    double logSumWeights = Double.NEGATIVE_INFINITY;
    ListIterator<Particle> particleIt = particles.listIterator();
    while (particleIt.hasNext()) {
      Particle particle = particleIt.next();
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

    // Make the MHSampler aware of the new evidence.
    AbstractProposer proposer = (AbstractProposer) mhSampler.getProposer();
    proposer.add(evidence);
  }

  protected void resample() {
    double[] logWeights = new double[particles.size()];
    boolean[] alreadySampled = new boolean[particles.size()];
    double maxLogWeight = Double.NEGATIVE_INFINITY;
    double sumWeights = 0;
    double[] normalizedWeights = new double[particles.size()];
    List<Particle> newParticles = new ArrayList<Particle>();

    /*
     * Modified on Oct. 2. 2014 by yiwu
     * For normalization, we do not actually need to compute the sum of
     * the log weights.
     * Every logsum operator requires 2 expensive math function calls.
     * Only computing the maximum will be enough.
     */
    for (int i = 0; i < particles.size(); i++) {
      logWeights[i] = particles.get(i).getLatestLogWeight();
      maxLogWeight = Math.max(maxLogWeight, logWeights[i]);
    }

    if (maxLogWeight == Double.NEGATIVE_INFINITY) {
      throw new IllegalArgumentException("All particles have zero weight");
    }

    /*
     * Modified on Oct. 2. 2014 by yiwu
     * I replace the original super slow resample algorithm, which
     * runs in O(n^2) time, with a new algorithm, which only needs
     * a quick-sort and a linear scan.
     */
    for (int i = 0; i < particles.size(); i++) {
      normalizedWeights[i] = Math.exp(logWeights[i] - maxLogWeight);
      if (i > 0)
        normalizedWeights[i] += normalizedWeights[i - 1];
    }

    sumWeights = normalizedWeights[particles.size() - 1];

    /*
     * Modified by yiwu on Oct.8.2014
     * Use systematic resample scheme
     */
    double ratio = sumWeights / numParticles;
    double basis = 0;
    double sampleKey = 0;
    int selection = 0;
    for (int i = 0; i < numParticles; i++) {
      sampleKey = basis + Util.random() * ratio;
      basis += ratio;
      while (normalizedWeights[selection] < sampleKey)
        ++selection;
      if (!alreadySampled[selection]) {
        newParticles.add(particles.get(selection));
        alreadySampled[selection] = true;
      } else {
        newParticles.add(particles.get(selection).copy());
      }
    }

    particles = newParticles;
  }

  protected void move() {
    for (int i = 0; i < particles.size(); i++) {
      System.out.println("Moving particle " + i);
      Particle particle = particles.get(i);
      mhSampler.setBaseWorld(particle.getLatestWorld());
      for (int j = 0; j < numMHIters; j++) {
        mhSampler.nextSample();
      }
      particle.curWorld = mhSampler.getLatestWorld();
      particle.logWeight = 0.0;
    }
  }

  private Set<Type> idTypes; // of Type

  private int numParticles;
  private int numMHIters;
  private MHSampler mhSampler;
  protected List<Particle> particles;
  private boolean needsToBeResampledBeforeFurtherSampling = false;
  private Sampler particleSampler;
  private int queryReportInterval;
  private double dataLogLik; // log likelihood of the data
}
