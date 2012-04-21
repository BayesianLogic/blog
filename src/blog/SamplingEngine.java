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

package blog;

import java.util.*;
import java.lang.reflect.*;

import blog.common.Timer;
import blog.common.Util;
import blog.model.Model;
import blog.model.Query;


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
 * 10,000.
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
				"blog.LWSampler");
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

		String numSamplesStr = properties.getProperty("numSamples", "10000");
		try {
			numSamples = Integer.parseInt(numSamplesStr);
		} catch (NumberFormatException e) {
			Util.fatalError("Invalid number of samples: " + numSamplesStr, false);
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

	public void answerQueries() {
		sampler.initialize(evidence, queries);

		System.out.println("Evidence: " + evidence);
		System.out.println("Query: " + queries);
		System.out.println("Running for " + numSamples + " samples...");
		if (numBurnIn != 0) {
			System.out.println("(Burn-in samples: " + numBurnIn + ")");
		}
		Timer timer = new Timer();
		timer.start();

		// Map statisticsOnWeather = new HashMap(); // debugging -- see below

		for (int i = 0; i < numSamples; ++i) {
			if (Util.verbose()) {
				System.out.println();
				System.out.println("Iteration " + i + ":");
			}
			sampler.nextSample();
			// System.out.println("SamplingEngine: right after nextSample, world is "
			// + System.identityHashCode(sampler.getLatestWorld()));
			double weight = sampler.getLatestWeight();

			// I wrote this to debug at a low level. Leaving it for a while. --
			// Rodrigo
			// System.out.println("World : " + sampler.getLatestWorld());
			// System.out.println("Weight: " + weight);
			// Object weather1 =
			// sampler.getLatestWorld().getValue(BLOGUtil.parseVariable_NE("Weather(@1)",
			// model));
			// Double accumulatedWeight = (Double) statisticsOnWeather.get(weather1);
			// if (accumulatedWeight == null)
			// accumulatedWeight = new Double(0);
			// statisticsOnWeather.put(weather1, accumulatedWeight.doubleValue() +
			// weight);
			// System.out.println("Distribution: ");
			// double sum = 0;
			// for (Iterator it = statisticsOnWeather.entrySet().iterator();
			// it.hasNext(); ) {
			// Map.Entry entry = (Map.Entry) it.next();
			// sum += ((Double)entry.getValue()).doubleValue();
			// }
			// if (sum > 0) {
			// for (Iterator it = statisticsOnWeather.entrySet().iterator();
			// it.hasNext(); ) {
			// Map.Entry entry = (Map.Entry) it.next();
			// System.out.println(entry.getKey() + ": " +
			// ((Double)entry.getValue()).doubleValue()/sum);
			// }
			// }

			if (i >= numBurnIn) {
				if (weight > 0) {
					// Update statistics to reflect this sample.
					for (Iterator iter = queries.iterator(); iter.hasNext();) {
						Query query = ((Query) iter.next());
						// System.out.println("SamplingEngine: Latest world: " +
						// sampler.getLatestWorld() + " weight " + weight);
						// System.out.println("SamplingEngine: Query: " + query);
						// Make sure the new world supports the query variables
						BLOGUtil.ensureDetAndSupported(query.getVariables(),
								sampler.getLatestWorld()); // this is not part of the sampler's
																						// sampling, but sampling done on
																						// top of it. Since this sampling is
																						// done according to the model's
																						// distribution, it still converges
																						// to it.
						query.updateStats(sampler.getLatestWorld(), weight);
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
		}

		sampler.printStats();
		// sampler.getLatestWorld().print(System.out);
	}

	private Sampler sampler;
	private int numSamples;
	private int numBurnIn;
	private int reportInterval;
}
