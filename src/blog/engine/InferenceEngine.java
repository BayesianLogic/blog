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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import blog.common.Util;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.ModelEvidenceQueries;
import blog.model.Query;

/**
 * Abstract class representing a BLOG inference engine. Its central method is
 * <code>answerQueries</code>, which records answers to a list of queries
 * (specified with the <code>setQueries</code> method) given some evidence
 * (specified with the <code>setEvidence</code> method). If the inference
 * algorithm is randomized, repeated calls to <code>answerQueries</code> may
 * yield different results.
 * 
 * <p>
 * A concrete subclass of InferenceEngine should have a constructor with two
 * arguments, of types blog.Model and java.util.Properties. The properties
 * argument specifies configuration parameters for the inference engine; it may
 * be ignored if the engine has no such parameters.
 */
public abstract class InferenceEngine {
	/**
	 * Returns a new InferenceEngine object for the given BLOG model, with
	 * properties specified by the given Properties table. In particular, the
	 * InferenceEngine subclass is specified by the <code>engineClass</code>
	 * property (the default is blog.SamplingEngine).
	 */
	public static InferenceEngine constructEngine(Model model,
			Properties properties) {
		String className = properties.getProperty("engineClass",
				"blog.engine.SamplingEngine");
		System.out.println("Constructing inference engine of class " + className);

		try {
			Class engineClass = Class.forName(className);
			Class[] paramTypes = { Model.class, Properties.class };
			Constructor constructor = engineClass.getConstructor(paramTypes);

			Object[] args = { model, properties };
			return (InferenceEngine) constructor.newInstance(args);
		} catch (Exception e) {
			Util.fatalError(e);
		}

		return null;
	}

	/**
	 * Returns an inference engine of the given class, for given model and
	 * properties.
	 */
	public static InferenceEngine constructEngine(String engineClassName,
			Model model, Properties properties) {
		Properties constructionProperties = new Properties();
		constructionProperties.putAll(properties);
		constructionProperties.setProperty("engineClass", engineClassName);
		return InferenceEngine.constructEngine(model, constructionProperties);
	}

	/**
	 * Creates a new inference engine for the given BLOG model.
	 */
	public InferenceEngine(Model model) {
		this.model = model;
	}

	/** Answer queries in <code>meq</code> using its evidence. */
	public void solve(ModelEvidenceQueries meq) {
		solve(meq.queries, meq.evidence);
	}

	/** Answer query given evidence. */
	public void solve(Query query, Evidence evidence) {
		solve(Util.list(query), evidence);
	}

	/** Answer query given no evidence. */
	public void solve(Query query) {
		solve(Util.list(query), new Evidence());
	}

	/** Answer queries given evidence. */
	public void solve(List queries, Evidence evidence) {
		setEvidence(evidence);
		setQueries(queries);
		answerQueries();
	}

	/**
	 * Sets the evidence to be conditioned on in the next call to
	 * <code>answerQueries</code>.
	 */
	public void setEvidence(Evidence evidence) {
		this.evidence = evidence;
	}

	/**
	 * Sets the queries to be answered in the next call to
	 * <code>answerQueries</code>.
	 * 
	 * @param queries
	 *          List of Query objects
	 */
	public void setQueries(List queries) {
		this.queries.clear();
		this.queries.addAll(queries);
	}

	/**
	 * Computes the answers to the specified queries given the specified evidence.
	 * Records the answers by calling the appropriate methods (e.g.,
	 * <code>updateStats</code>) on the specified Query objects.
	 */
	public abstract void answerQueries();

	/**
	 * The BLOG model on which this engine performs inference.
	 */
	protected Model model;

	/**
	 * The evidence set by the last call to <code>setEvidence</code>, or
	 * <code>null</code> if <code>setEvidence</code> has not been called.
	 */
	protected Evidence evidence = null;

	/**
	 * List of Query objects specified by the last call to <code>setQueries</code>
	 * . This list is empty if <code>setQueries</code> has not been called.
	 */
	protected List queries = new ArrayList();
}
