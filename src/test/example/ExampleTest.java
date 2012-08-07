package test.example;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import blog.Main;
import blog.common.Histogram;
import blog.common.Util;
import blog.engine.InferenceEngine;
import blog.model.ArgSpecQuery;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.Query;

/**
 * Base class for example regression tests
 * 
 * To write a regression test for a new model, do the following:
 * 1) Define a constructor with body "super(errBound)".
 * 2) Define setInputModel, adding paths of model files to pathsSource.
 * 3) Define setInferenceProps, adding arguments for the inference engine to inferenceProps.
 * 4) Define referenceProbs, generating "correct" probability tables to compare against the model.
 * 5) Define main, constructing an instance of the test class and calling runTest() on it.
 * 
 * Refer to test.example.TestPoissonBall as an example.
 * 
 * @author awong
 * @date July 14, 2012
 */
public abstract class ExampleTest {
	protected ArrayList<String> pathsSource;
	protected Properties inferenceProps;
	
	protected Model model;
	protected Evidence facts;
	protected ArrayList<Query> queries;
	protected ArrayList<HashMap<Object, Double> > refProbs;
	
	private double errBound;
	
	public ExampleTest(double maxBound) {
		pathsSource = new ArrayList<String>();
		model = new Model();
		facts = new Evidence();
		queries = new ArrayList<Query>();
		inferenceProps = new Properties();
		errBound = maxBound;
		Util.initRandom(false);
	}
	
	/**
	 * Executes a test
	 * @throws IncorrectProbException 
	 */
	public void runTest() throws IncorrectProbException {
		setInputModel();
		loadModel();
		setInferenceProps();
		referenceProbs();
		modelProbs();
		
		if (!isCorrectProbs()) {
			throw new IncorrectProbException();
		}
	}
	
	/**
	 * Defines paths of files containing model/evidence/queries
	 * (extensions .blog, .dblog, .eblog, .mblog, .qblog)
	 */
	public abstract void setInputModel();
	
	/**
	 * Reads models contained in predefined paths and constructs Model,
	 * Evidence, and a list of Query objects.
	 */
	public void loadModel() {
		Main.simpleSetupFromFiles(model, facts, queries, pathsSource);
		
		refProbs = new ArrayList<HashMap<Object, Double> >();
		for (int i = 0; i < queries.size(); i++) {
			refProbs.add(new HashMap<Object, Double>());
		}
	}
	
	/**
	 * Allows setting of inference engine options via inferenceProps
	 * TODO: command-line options (requires reimplementing Main.parseOptions)
	 */
	public abstract void setInferenceProps();
	
	/**
	 * Computes probability tables for queries in this model using a
	 * model-specific algorithm.
	 */
	public abstract void referenceProbs();
	
	/**
	 * Uses BLOG's inference engine to construct probability tables for
	 * queries in this model.
	 */
	public void modelProbs() {
		InferenceEngine engine = InferenceEngine.constructEngine(model,
				inferenceProps);
		Main.printTimes(System.out, "-", 80);
		engine.setEvidence(facts);
		engine.setQueries(queries);
		engine.answerQueries();

		// Print query results
		System.out.println("======== Query Results =========");
		for (Iterator iter = queries.iterator(); iter.hasNext();) {
			Query q = (Query) iter.next();
			q.printResults(System.out);
			
//			if (!(q instanceof ArgSpecQuery)) {
//				throw new IllegalStateException("Query has no histogram (not ArgSpecQuery)");
//			}
//			
//			Histogram hist = ((ArgSpecQuery)q).getHistogram();			
//			Collection varVals = hist.elementSet();
//			for (Iterator keys = varVals.iterator(); keys.hasNext();) {
//				Object key = keys.next();
//				System.out.println(key + ":\t" + hist.getWeight(key) / hist.getTotalWeight());
//			}
		}

		System.out.println();
	}
	
	/**
	 * Given the probability tables constructed by BLOG and by the
	 * reference algorithm, compares them for equality.
	 */
	public boolean isCorrectProbs() {
		if (queries.size() != refProbs.size()) {
			throw new IllegalStateException("Model contains " + queries.size() +
					" probability tables, not " + refProbs.size());
		}
		
		Iterator<HashMap<Object, Double>> refIter = refProbs.iterator();
		for (Iterator<Query> modelIter = queries.iterator(); modelIter.hasNext();) {
			Query q = modelIter.next();
			
			if (!(q instanceof ArgSpecQuery)) {
				throw new IllegalStateException("Query has no histogram (not ArgSpecQuery)");
			}
			
			Histogram hist = ((ArgSpecQuery)q).getHistogram();
			HashMap<Object, Double> baseline = refIter.next();
			
			Collection histKeys = hist.elementSet();
			Collection baseKeys = baseline.keySet();
			
			Set<Object> allKeys = new HashSet<Object>();
			allKeys.addAll(histKeys);
			allKeys.addAll(baseKeys);
			
			for (Iterator keys = allKeys.iterator(); keys.hasNext();) {
				Object key = keys.next();
				if (!baseline.containsKey(key)) {
					if ((hist.getWeight(key) / hist.getTotalWeight()) > errBound) {
						System.out.println("Model: p(" + key + ") = " + (hist.getWeight(key) / hist.getTotalWeight()));
						System.out.println("Base: p(" + key + ") = 0");
						return false;
					}
				}
				else if (hist.getWeight(key) == 0) {
					if (baseline.get(key) > errBound) {
						System.out.println("Model: p(" + key + ") = 0");
						System.out.println("Base: p(" + key + ") = " + baseline.get(key));
						return false;
					}
				}
				else {
					if (Math.abs((hist.getWeight(key) / hist.getTotalWeight()) - baseline.get(key)) > errBound) {
						System.out.println("Model: p(" + key + ") = " + (hist.getWeight(key) / hist.getTotalWeight()));
						System.out.println("Base: p(" + key + ") = " + baseline.get(key));
						return false;
					}
				}
			}
		}
		return true;
	}
}