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

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.Properties;

import blog.BLOGUtil;
import blog.Main;
import blog.MyILoop;
import blog.common.Timer;
import blog.common.Util;
import blog.model.Model;
import blog.model.Query;
import blog.sample.Sampler;
import blog.world.PartialWorld;

/**
 * Inference engine that answers queries by sampling partial worlds (possibly
 * with associated weights) from some sampling distribution, and collecting
 * statistics over these samples.
 * 
 * <p>
 * The SamplingEngine constructor looks for the following properties in the
 * properties table that is passed in:
 * <dl>
 * <dt>samplerClass
 * <dd>Name of the Sampler subclass to use. Default is blog.LWSampler.
 * 
 * <dt>numSamples
 * <dd>Number of samples that <code>answerQueries</code> will run. Default is
 * 50,000.
 * 
 * <dt>queryReportInterval
 * <dd>Number of samples after which queries are reported
 * <code>answerQueries</code> will run. Default is 10,000.
 * 
 * <dt>burnIn
 * <dd>Number of initial samples to treat as a burn-in period. Samples during
 * this period will not be used to compute answers to queries. Default is 0.
 * 
 * <dt>reportInterval
 * <dd>Number of samples between progress reports (-1 for no report). A progress
 * report just involves printing how many samples have been done and how long
 * the sampler has been running in this trial. Default is 500.
 * </dl>
 * The property list is also passed to the sampler's constructor.
 */
public class SamplingEngine extends InferenceEngine {
  /**
   * Creates a new sampling engine for the given BLOG model, with configuration
   * parameters specified by the given properties table.
   */
  public SamplingEngine(Model model, Properties properties) {
    super(model);

    String samplerClassName = properties.getProperty("samplerClass",
        "blog.sample.LWSampler");
    System.out.println("Constructing sampler of class " + samplerClassName);

    try {
      Class samplerClass = Class.forName(samplerClassName);
      Class[] paramTypes = { Model.class, Properties.class };
      Constructor constructor = samplerClass.getConstructor(paramTypes);

      Object[] args = { model, properties };
      sampler = (Sampler) constructor.newInstance(args);
    } catch (Exception e) {
      Util.fatalError(e);
    }

    String numSamplesStr = properties.getProperty("numSamples", "50000");
    try {
      numSamples = Integer.parseInt(numSamplesStr);
    } catch (NumberFormatException e) {
      Util.fatalError("Invalid number of samples: " + numSamplesStr, false);
    }

    String queryReportIntervalStr = properties.getProperty(
        "queryReportInterval", "10000");
    try {
      queryReportInterval = Integer.parseInt(queryReportIntervalStr);
    } catch (NumberFormatException e) {
      Util.fatalError("Invalid reporting interval: " + queryReportIntervalStr,
          false);
    }

    String burnInStr = properties.getProperty("burnIn", "0");
    try {
      numBurnIn = Integer.parseInt(burnInStr);
    } catch (NumberFormatException e) {
      Util.fatalError("Invalid number of burn-in samples: " + burnInStr, false);
    }

    String reportIntervalStr = properties.getProperty("reportInterval", "500");
    try {
      reportInterval = Integer.parseInt(reportIntervalStr);
    } catch (NumberFormatException e) {
      Util.fatalError("Invalid report interval: " + reportIntervalStr, false);
    }
  }

  public SamplingEngine(Model model) {
    this(model, new Properties());
  }

  public void printGeneratedWorld(Sampler sampler, double logWeight) {
    PartialWorld curWorld = sampler.getLatestWorld();
    System.out.println("======== Generated World: ========");
    curWorld.print(System.out);
    System.out.println("======== Log weight: " + logWeight + " ========");
  }

  public void answerQueries() {
    boolean printed = false;
    sampler.initialize(evidence, queries);

    System.out.println("Evidence: " + evidence);
    System.out.println("Query: " + queries);
    System.out.println("Running for " + numSamples + " samples...");
    System.out.println("Query Reporting interval is " + queryReportInterval);
    if (numBurnIn != 0) {
      System.out.println("(Burn-in samples: " + numBurnIn + ")");
    }
    Timer timer = new Timer();
    timer.start();

    for (int i = 0; i < numSamples; ++i) {
      Util.debug("\nIteration ", i, ":");
      sampler.nextSample();
      double logWeight = sampler.getLatestLogWeight();

      if (Util.verbose()) {
        printGeneratedWorld(sampler, logWeight);
      }

      if (i != 0 && (i) % queryReportInterval == 0) {
        // Print query results
        System.out.println("======== Query Results ========");
        System.out.println("Iteration " + i + ":");
        for (Iterator iter = queries.iterator(); iter.hasNext();) {
          Query q = (Query) iter.next();
          q.printResults(System.out);
        }
        System.out.println("======== Done ========");
      }

      if (i >= numBurnIn) {
        if (logWeight > Sampler.NEGLIGIBLE_LOG_WEIGHT) {
          // Update statistics to reflect this sample.
          for (Iterator iter = queries.iterator(); iter.hasNext();) {
            Query query = ((Query) iter.next());

            // Make sure the new world supports the query variables
            BLOGUtil.ensureDetAndSupported(query.getVariables(),
            // this is not part of the sampler's
            // sampling, but sampling done on
            // top of it. Since this sampling is
            // done according to the model's
            // distribution, it still converges
            // to it.
                sampler.getLatestWorld());
            query.updateStats(sampler.getLatestWorld(), logWeight);
          }
        }

        if ((Main.outputPath() != null)
            && ((i + 1) % Main.outputInterval() == 0)) {
          for (Iterator iter = queries.iterator(); iter.hasNext();) {
            ((Query) iter.next()).logResults(i + 1);
          }
        }
      }

      if (reportInterval != -1 && (i + 1) % reportInterval == 0) {
        System.out.println("Samples done: " + (i + 1) + ".  \tTime elapsed: "
            + timer.elapsedTime() + " s.");
      }

      if (Util.print() && logWeight > Sampler.NEGLIGIBLE_LOG_WEIGHT && !printed) {
        printGeneratedWorld(sampler, logWeight);
        printed = true;
      }

      MyILoop repl = new MyILoop();
      repl.bind("i", "Int", i);
      repl.bind("world", "blog.world.DefaultPartialWorld", sampler.getLatestWorld());
      repl.bind("logWeight", "Double", sampler.getLatestLogWeight());
      repl.run();
    }

    sampler.printStats();
  }

  private Sampler sampler;
  private int numSamples;
  private int reportInterval;
  private int queryReportInterval;
  private int numBurnIn;
}
