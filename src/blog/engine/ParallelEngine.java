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

import java.util.Properties;

import blog.model.Model;
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
public class ParallelEngine extends InferenceEngine {
  /**
   * Creates a new sampling engine for the given BLOG model, with configuration
   * parameters specified by the given properties table.
   */
  public ParallelEngine(Model model, Properties properties) {
    super(model);
    this.properties = properties;
  }

  public ParallelEngine(Model model) {
    this(model, new Properties());
  }

  public void printGeneratedWorld(Sampler sampler, double logWeight) {
    PartialWorld curWorld = sampler.getLatestWorld();
    System.out.println("======== Generated World: ========");
    curWorld.print(System.out);
    System.out.println("======== Log weight: " + logWeight + " ========");
  }

  public void answerQueries() {

    // Create a number of threads here. Use ParallelSamplingEngine as the
    // runnable

    int maxThreads = 4;
    Thread[] threads = new Thread[maxThreads];

    // Start the threads
    for (int i = 0; i < maxThreads; i++) {
      ParallelSamplingEngine engine = new ParallelSamplingEngine(this.model,
          this.properties);
      engine.setEvidence(evidence);
      engine.setQueries(queries);
      threads[i] = new Thread(engine, "Thread-" + i);
      threads[i].start();
    }

    // Join the threads. Requires a try catch
    for (int i = 0; i < maxThreads; i++) {
      try {
        threads[i].join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

  }

  private Properties properties;
}
