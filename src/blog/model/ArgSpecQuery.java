/*
 * Copyright (c) 2005, 2006, Regents of the University of California
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

package blog.model;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import ve.Factor;
import ve.Potential;
import blog.Main;
import blog.PartialWorld;
import blog.bn.BasicVar;
import blog.bn.BayesNetVar;
import blog.common.Histogram;
import blog.common.UnaryFunction;

/**
 * A query on the value of a given {@link ArgSpec}.
 * 
 * The class accepts a normalizer, that is, a UnaryFunction mapping each
 * possible value to a representative in a user-defined equivalency class. The
 * representative is the one stored in the query's histogram. By default, the
 * normalizer is the identity function.
 */
public class ArgSpecQuery extends AbstractQuery {

	public ArgSpecQuery(ArgSpec argSpec) {
		this.argSpec = argSpec;

		if (Main.histOut() != null) {
			outputFile = Main.filePrintStream(Main.histOut() + "-trial" + +trialNum
					+ ".data");
		}
	}

	public ArgSpecQuery(ArgSpecQuery another) {
		this(another.argSpec());
		if (another.getNormalizer() != null)
			this.setNormalizer(another.getNormalizer());
		if (another.variable != null)
			compile(); // if another is compiled, compile this one too.
	}

	public ArgSpec argSpec() {
		return getArgSpec();
	}

	public void printResults(PrintStream s) {
		s.println("Distribution of values for " + getArgSpec());
		List entries = new ArrayList(histogram.entrySet());

		if (getArgSpec().isNumeric())
			Collections.sort(entries, NUMERIC_COMPARATOR);
		else
			Collections.sort(entries, WEIGHT_COMPARATOR);

		for (Iterator iter = entries.iterator(); iter.hasNext();) {
			Histogram.Entry entry = (Histogram.Entry) iter.next();
			double prob = entry.getWeight() / histogram.getTotalWeight();
			s.println("\t" + prob + "\t" + entry.getElement());
		}
	}

	public void logResults(int numSamples) {
		final List entries = new ArrayList(histogram.entrySet());
		for (Iterator iter = entries.iterator(); iter.hasNext();) {
			Histogram.Entry entry = (Histogram.Entry) iter.next();
			double prob = entry.getWeight() / histogram.getTotalWeight();
			PrintStream s = getOutputFile(entry.getElement());
			s.println("\t" + numSamples + "\t" + prob);
		}

		if ((numSamples == Main.numSamples()) && (Main.histOut() != null)) {
			Comparator c = new Comparator() {
				public int compare(Object o1, Object o2) {
					Integer i1 = new Integer(((Histogram.Entry) o1).getElement()
							.toString());
					Integer i2 = new Integer(((Histogram.Entry) o2).getElement()
							.toString());
					return i1.compareTo(i2);
				}
			};
			Collections.sort(entries, c);
			for (Iterator iter = entries.iterator(); iter.hasNext();) {
				Histogram.Entry entry = (Histogram.Entry) iter.next();
				double prob = entry.getWeight() / histogram.getTotalWeight();
				outputFile.println("\t" + entry.getElement() + "\t" + prob);
			}
		}
	}

	public Collection<? extends BayesNetVar> getVariables() {
		if (variable == null) {
			throw new IllegalStateException("Query has not yet been compiled.");
		}
		return Collections.singleton(variable);
	}

	public BayesNetVar getVariable() {
		if (variable == null) {
			throw new IllegalStateException("Query has not yet been compiled.");
		}
		return variable;
	}

	public boolean checkTypesAndScope(Model model) {
		if (getArgSpec() instanceof Term) {
			Term termInScope = ((Term) getArgSpec()).getTermInScope(model,
					Collections.EMPTY_MAP);
			if (termInScope == null) {
				return false;
			}
			argSpec = termInScope;
			return true;
		}
		return getArgSpec().checkTypesAndScope(model, Collections.EMPTY_MAP);
	}

	/**
	 * Compiles the underlying ArgSpec, and initializes the variable corresponding
	 * to this query.
	 */
	public int compile() {
		int errors = getArgSpec().compile(new LinkedHashSet());
		if (errors == 0) {
			variable = getArgSpec().getVariable();
		}
		return errors;
	}

	public void updateStats(PartialWorld world, double weight) {
		// System.out.println("ArqSpecQuery.updateStats: World is " +
		// System.identityHashCode(world));
		Object value = getArgSpec().evaluate(world);
		// System.out.println("ArqSpecQuery.updateStats: " + argSpec +
		// " determined as " + value);
		// System.out.println("ArgSpecQuery: increasing weight of " + value + " by "
		// + weight);
		histogram.increaseWeight(value, weight);
	}

	public void setPosterior(Factor posterior) {
		if (!posterior.getRandomVars().contains((BasicVar) variable)) {
			throw new IllegalArgumentException("Query variable " + variable
					+ " not covered by factor on " + posterior.getRandomVars());
		}
		if (posterior.getRandomVars().size() > 1) {
			throw new IllegalArgumentException("Answer to query on " + variable
					+ " should be factor on " + "that variable alone, not "
					+ posterior.getRandomVars());
		}

		Potential pot = posterior.getPotential();
		Type type = pot.getDims().get(0);
		histogram.clear();
		for (Object o : type.getGuaranteedObjects()) {
			histogram.increaseWeight(o, pot.getValue(Collections.singletonList(o)));
		}
	}

	public void zeroOut() {
		// histList.add(histogram);
		// histogram = new Histogram();
		trialNum++;
		if ((outputFile != null) && (trialNum != Main.numTrials())) {
			outputFile = Main.filePrintStream(Main.histOut() + "-trial" + trialNum
					+ ".data");
		}
		outputFiles = new HashMap();

		histogram.clear();

		// We don't record across-run statistics
	}

	public void printVarianceResults(PrintStream s) {
		s.println("\tVariance of " + getArgSpec() + " results is not computed.");
		// printVarStats(s);
	}

	/**
	 * Print the query summary: mean histogram (because we have to calculate it
	 * anyway, variation distance (sum of absolute values of each element from its
	 * mean), and element-wise variance.
	 * 
	 * Yes, I know this makes many, many passes over histList, but variation
	 * distance needs to know the mean first, so I might as well change as little
	 * code as possible.
	 * 
	 * private void printVarStats(PrintStream s) { Set objs = new TreeSet(); for
	 * (int i = 0; i < histList.size() ; i++) { objs.addAll(((Histogram)
	 * histList.get(i)).elementSet()); }
	 * 
	 * double vardist = 0; Histogram meanHist = findMeanHist(objs); Histogram
	 * sumSquareHist = new Histogram(); for (int i = 0 ; i < histList.size() ;
	 * i++) { Histogram hist = (Histogram) histList.get(i); Iterator objIter =
	 * hist.elementSet().iterator(); while (objIter.hasNext()) { Object obj =
	 * objIter.next(); double normWeight =
	 * hist.getWeight(obj)/hist.getTotalWeight(); vardist +=
	 * Math.abs(meanHist.getWeight(obj) - normWeight);
	 * sumSquareHist.increaseWeight(obj, normWeight*normWeight); } }
	 * 
	 * s.println("Query summary for: " + argSpec);
	 * 
	 * s.println("  Mean histogram: (value, probability)"); Iterator objIter =
	 * objs.iterator(); while (objIter.hasNext()) { Object obj = objIter.next();
	 * s.println("\t" + obj + "\t" + meanHist.getWeight(obj)); }
	 * 
	 * s.println("  Variation distance: " + vardist);
	 * 
	 * s.println("  Element-wise variance: (value, variance, standard dev)");
	 * objIter = objs.iterator(); while (objIter.hasNext()) { Object obj =
	 * objIter.next(); double mu2 = Math.pow(meanHist.getWeight(obj), 2); double
	 * var = Math.abs(sumSquareHist.getWeight(obj) - mu2); s.println("\t" + obj +
	 * "\t" + var + "\t" + Math.sqrt(var)); } }
	 * 
	 * /** findMeanHist returns a Histogram representing the mean values for each
	 * of the elements in histList. It takes as argument a Set of Objects, objs,
	 * which is a set of all objects in all Histograms in histList.
	 * 
	 * private Histogram findMeanHist(Set objs) { Histogram meanHist = new
	 * Histogram(); for (int i = 0 ; i < histList.size() ; i++) { Histogram hist =
	 * (Histogram) histList.get(i); double denom = hist.getTotalWeight(); Iterator
	 * objIter = hist.elementSet().iterator(); while (objIter.hasNext()) { Object
	 * obj = objIter.next(); double numer = hist.getWeight(obj);
	 * meanHist.increaseWeight(obj, numer/denom); } }
	 * 
	 * // Normalize mean probabilities Histogram normMean = new Histogram();
	 * Iterator objIter = objs.iterator(); double denom =
	 * meanHist.getTotalWeight(); while (objIter.hasNext()) { Object obj =
	 * objIter.next(); normMean.increaseWeight(obj,
	 * meanHist.getWeight(obj)/denom); }
	 * 
	 * return normMean; }
	 */

	/**
	 * Every object should have an output file. If it does not yet exist, create
	 * one; otherwise return it.
	 */
	private PrintStream getOutputFile(Object o) {
		PrintStream s = (PrintStream) outputFiles.get(o);
		if (s == null) {
			s = Main.filePrintStream(Main.outputPath() + "-trial" + trialNum + "."
					+ o.toString() + ".data");
			outputFiles.put(o, s);
		}
		return s;
	}

	public Histogram getHistogram() {
		return histogram;
	}

	public Collection elementSet() {
		return getHistogram().elementSet();
	}

	public double getProb(Object entry) {
		return getHistogram().getProb(entry);
	}

	/**
	 * Returns a collection with up to <code>n</code> entries, and with the
	 * minimum number of elements comprising at least <code>percentile</code> of
	 * total mass.
	 */
	public Collection getNBestButInUpper(int n, double percentile) {
		return getHistogram().getNBestButInUpper(n, percentile);
	}

	public Object getLocation() {
		return getArgSpec().getLocation();
	}

	/**
	 * Remove entries from histogram such that at least
	 * <code>(1 - percentile)</code> of the total weight remains.
	 */
	public void prune(double percentile) {
		histogram.prune(percentile);
	}

	public UnaryFunction getNormalizer() {
		return histogram.getNormalizer();
	}

	public void setNormalizer(UnaryFunction normalizer) {
		histogram.setNormalizer(normalizer);
	}

	public String toString() {
		if (variable == null) {
			return getArgSpec().toString();
		}
		return variable.toString();
	}

	/**
	 * @return the argSpec
	 */
	public ArgSpec getArgSpec() {
		return argSpec;
	}

	private static Comparator WEIGHT_COMPARATOR = new Comparator() {
		public int compare(Object o1, Object o2) {
			double diff = (((Histogram.Entry) o1).getWeight() - ((Histogram.Entry) o2)
					.getWeight());
			if (diff < 0) {
				return 1;
			} else if (diff > 0) {
				return -1;
			}
			return 0;
		}
	};
	private static final Comparator NUMERIC_COMPARATOR = new Comparator() {
		public int compare(Object o1, Object o2) {
			Object e1 = ((Histogram.Entry) o1).getElement();
			Object e2 = ((Histogram.Entry) o2).getElement();
			double n1 = ((Number) e1).doubleValue();
			double n2 = ((Number) e2).doubleValue();
			if (n1 < n2) {
				return -1;
			} else if (n1 > n2) {
				return 1;
			}
			return 0;
		}
	};
	protected ArgSpec argSpec;
	protected BayesNetVar variable;
	protected Histogram histogram = new Histogram();
	// private LinkedList histList = new LinkedList(); // of Histogram
	protected int trialNum = 0;

	protected Map outputFiles = new HashMap(); // of PrintStream
	protected PrintStream outputFile = null;
}
