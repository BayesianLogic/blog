/*
 * Copyright (c) 2007, Massachusetts Institute of Technology
 * Copyright (c) 2005-2006, Regents of the University of California
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

package common;

import java.util.*;
import java.io.*;

/**
 * An engine for finding the maximum-weight matching in a complete
 * bipartite graph.  Suppose we have two sets <i>S</i> and <i>T</i>,
 * both of size <i>n</i>.  For each <i>i</i> in <i>S</i> and <i>j</i>
 * in <i>T</i>, we have a weight <i>w<sub>ij</sub></i>.  A perfect
 * matching <i>X</i> is a subset of <i>S</i> x <i>T</i> such that each
 * <i>i</i> in <i>S</i> occurs in exactly one element of <i>X</i>, and
 * each <i>j</i> in <i>T</i> occurs in exactly one element of
 * <i>X</i>.  Thus, <i>X</i> can be thought of as a one-to-one
 * function from <i>S</i> to <i>T</i>.  The weight of <i>X</i> is the
 * sum, over (<i>i</i>, <i>j</i>) in <i>X</i>, of
 * <i>w<sub>ij</sub></i>.  A BipartiteMatcher takes the number
 * <i>n</i> and the weights <i>w<sub>ij</sub></i>, and finds a perfect
 * matching of maximum weight.
 *
 * It uses the Hungarian algorithm of Kuhn (1955), as improved and
 * presented by E. L. Lawler in his book <cite>Combinatorial
 * Optimization: Networks and Matroids</cite> (Holt, Rinehart and
 * Winston, 1976, p. 205-206).  The running time is
 * O(<i>n</i><sup>3</sup>).  The weights can be any finite real
 * numbers; Lawler's algorithm assumes positive weights, so if
 * necessary we add a constant <i>c</i> to all the weights before
 * running the algorithm.  This increases the weight of every perfect
 * matching by <i>nc</i>, which doesn't change which perfect matchings
 * have maximum weight.
 *
 * If a weight is set to Double.NEGATIVE_INFINITY, then the algorithm will 
 * behave as if that edge were not in the graph.  If all the edges incident on 
 * a given node have weight Double.NEGATIVE_INFINITY, then the final result 
 * will not be a perfect matching, and an exception will be thrown.  
 */
public class BipartiteMatcher {
    /**
     * Creates a BipartiteMatcher without specifying the graph size.  Calling 
     * any other method before calling reset will yield an 
     * IllegalStateException.
     */
    public BipartiteMatcher() {
	n = -1;
    }

    /**
     * Creates a BipartiteMatcher and prepares it to run on an n x n graph.  
     * All the weights are initially set to 1.  
     */
    public BipartiteMatcher(int n) {
	reset(n);
    }

    /**
     * Resets the BipartiteMatcher to run on an n x n graph.  The weights are 
     * all reset to 1.
     */
    public void reset(int n) {
	if (n < 0) {
	    throw new IllegalArgumentException("Negative num nodes: " + n);
	}
	this.n = n;

	weights = new double[n][n];
	for (int i = 0; i < n; i++) {
	    for (int j = 0; j < n; j++) {
		weights[i][j] = 1;
	    }
	}
	minWeight = 1;
	maxWeight = Double.NEGATIVE_INFINITY;

	sMatches = new int[n];
	tMatches = new int[n];
	sLabels = new int[n];
	tLabels = new int[n];
	u = new double[n];
	v = new double[n];
	pi = new double[n];
    }

    /**
     * Sets the weight w<sub>ij</sub> to the given value w. 
     *
     * @throws IllegalArgumentException if i or j is outside the range [0, n).
     */
    public void setWeight(int i, int j, double w) {
	if (n == -1) {
	    throw new IllegalStateException("Graph size not specified.");
	}
	if ((i < 0) || (i >= n)) {
	    throw new IllegalArgumentException("i-value out of range: " + i);
	}
	if ((j < 0) || (j >= n)) {
	    throw new IllegalArgumentException("j-value out of range: " + j);
	}
	if (Double.isNaN(w)) {
	    throw new IllegalArgumentException("Illegal weight: " + w);
	}

	weights[i][j] = w;
	if ((w > Double.NEGATIVE_INFINITY) && (w < minWeight)) {
	    minWeight = w;
	}
	if (w > maxWeight) {
	    maxWeight = w;
	}
    }

    /**
     * Returns a maximum-weight perfect matching relative to the weights 
     * specified with setWeight.  The matching is represented as an array arr 
     * of length n, where arr[i] = j if (i,j) is in the matching.
     */
    public int[] getMatching() {
	if (n == -1) {
	    throw new IllegalStateException("Graph size not specified.");
	}
	if (n == 0) {
	    return new int[0];
	}
	ensurePositiveWeights();

	// Step 0: Initialization
	eligibleS.clear();
	eligibleT.clear();
	for (int i = 0; i < n; i++) {
	    sMatches[i] = -1;
	    tMatches[i] = -1;

	    u[i] = maxWeight; // ambiguous on p. 205 of Lawler, but see p. 202
	    v[i] = 0;
	    pi[i] = Double.POSITIVE_INFINITY;

	    // this is really first run of Step 1.0
	    sLabels[i] = EMPTY_LABEL; 
	    eligibleS.add(new Integer(i));

	    tLabels[i] = NO_LABEL;
	}

	while (true) {
	    // Augment the matching until we can't augment any more given the 
	    // current settings of the dual variables.  
	    while (true) {
		// Steps 1.1-1.4: Find an augmenting path
		int lastNode = findAugmentingPath();
		if (lastNode == -1) {
		    break; // no augmenting path
		}
				
		// Step 2: Augmentation
		flipPath(lastNode);
		for (int i = 0; i < n; i++) {
		    pi[i] = Double.POSITIVE_INFINITY;
		    sLabels[i] = NO_LABEL;
		    tLabels[i] = NO_LABEL;
		}

		// This is Step 1.0
		eligibleS.clear();
		for (int i = 0; i < n; i++) {
		    if (sMatches[i] == -1) {
			sLabels[i] = EMPTY_LABEL;
			eligibleS.add(new Integer(i));
		    }
		}

		eligibleT.clear();
	    }

	    // Step 3: Change the dual variables

	    // delta1 = min_i u[i]
	    double delta1 = Double.POSITIVE_INFINITY;
	    for (int i = 0; i < n; i++) {
		if (u[i] < delta1) {
		    delta1 = u[i];
		}
	    }

	    // delta2 = min_{j : pi[j] > 0} pi[j]
	    double delta2 = Double.POSITIVE_INFINITY;
	    for (int j = 0; j < n; j++) {
		if ((pi[j] >= TOL) && (pi[j] < delta2)) {
		    delta2 = pi[j];
		}
	    }

	    if (delta1 < delta2) {
		// In order to make another pi[j] equal 0, we'd need to 
		// make some u[i] negative.  
		break; // we have a maximum-weight matching
	    }
			
	    changeDualVars(delta2);
	}

	int[] matching = new int[n];
	for (int i = 0; i < n; i++) {
	    matching[i] = sMatches[i];
	}
	return matching;
    }

    /**
     * Tries to find an augmenting path containing only edges (i,j) for which 
     * u[i] + v[j] = weights[i][j].  If it succeeds, returns the index of the 
     * last node in the path.  Otherwise, returns -1.  In any case, updates 
     * the labels and pi values.
     */
    int findAugmentingPath() {
	while ((!eligibleS.isEmpty()) || (!eligibleT.isEmpty())) {
	    if (!eligibleS.isEmpty()) {
		int i = ((Integer) eligibleS.get(eligibleS.size() - 1)).
		    intValue();
		eligibleS.remove(eligibleS.size() - 1);
		for (int j = 0; j < n; j++) {
		    // If pi[j] has already been decreased essentially
		    // to zero, then j is already labeled, and we
		    // can't decrease pi[j] any more.  Omitting the 
		    // pi[j] >= TOL check could lead us to relabel j
		    // unnecessarily, since the diff we compute on the
		    // next line may end up being less than pi[j] due
		    // to floating point imprecision.
		    if ((tMatches[j] != i) && (pi[j] >= TOL)) {
			double diff = u[i] + v[j] - weights[i][j];
			if (diff < pi[j]) {
			    tLabels[j] = i;
			    pi[j] = diff;
			    if (pi[j] < TOL) {
				eligibleT.add(new Integer(j));
			    }
			}
		    }
		}
	    } else {
		int j = ((Integer) eligibleT.get(eligibleT.size() - 1)).
		    intValue();
		eligibleT.remove(eligibleT.size() - 1);
		if (tMatches[j] == -1) {
		    return j; // we've found an augmenting path
		} 

		int i = tMatches[j];
		sLabels[i] = j;
		eligibleS.add(new Integer(i)); // ok to add twice
	    }
	}

	return -1;
    }

    /**
     * Given an augmenting path ending at lastNode, "flips" the path.  This 
     * means that an edge on the path is in the matching after the flip if 
     * and only if it was not in the matching before the flip.  An augmenting 
     * path connects two unmatched nodes, so the result is still a matching. 
     */ 
    void flipPath(int lastNode) {
	while (lastNode != EMPTY_LABEL) {
	    int parent = tLabels[lastNode];

	    // Add (parent, lastNode) to matching.  We don't need to 
	    // explicitly remove any edges from the matching because: 
	    //  * We know at this point that there is no i such that 
	    //    sMatches[i] = lastNode.  
	    //  * Although there might be some j such that tMatches[j] =
	    //    parent, that j must be sLabels[parent], and will change 
	    //    tMatches[j] in the next time through this loop.  
	    sMatches[parent] = lastNode;
	    tMatches[lastNode] = parent;
					
	    lastNode = sLabels[parent];
	}
    }

    void changeDualVars(double delta) {
	for (int i = 0; i < n; i++) {
	    if (sLabels[i] != NO_LABEL) {
		u[i] -= delta;
	    }
	}
		
	for (int j = 0; j < n; j++) {
	    if (pi[j] < TOL) {
		v[j] += delta;
	    } else if (tLabels[j] != NO_LABEL) {
		pi[j] -= delta;
		if (pi[j] < TOL) {
		    eligibleT.add(new Integer(j));
		}
	    }
	}
    }

    /**
     * Ensures that all weights are either Double.NEGATIVE_INFINITY, 
     * or strictly greater than zero.
     */
    private void ensurePositiveWeights() {
	// minWeight is the minimum non-infinite weight
	if (minWeight < TOL) {
	    for (int i = 0; i < n; i++) {
		for (int j = 0; j < n; j++) {
		    weights[i][j] = weights[i][j] - minWeight + 1;
		}
	    }

	    maxWeight = maxWeight - minWeight + 1;
	    minWeight = 1;
	}
    }

    private void printWeights() {
	for (int i = 0; i < n; i++) {
	    for (int j = 0; j < n; j++) {
		System.out.print(weights[i][j] + " ");
	    }
	    System.out.println("");
	}
    }

    /**
     * Tolerance for comparisons to zero, to account for
     * floating-point imprecision.  We consider a positive number to
     * be essentially zero if it is strictly less than TOL.
     */
    private static final double TOL = 1e-10;

    int n;

    double[][] weights;
    double minWeight;
    double maxWeight;

    // If (i, j) is in the mapping, then sMatches[i] = j and tMatches[j] = i.  
    // If i is unmatched, then sMatches[i] = -1 (and likewise for tMatches). 
    int[] sMatches;
    int[] tMatches;

    static final int NO_LABEL = -1;
    static final int EMPTY_LABEL = -2;

    int[] sLabels;
    int[] tLabels;

    double[] u;
    double[] v;
	
    double[] pi;

    List eligibleS = new ArrayList();
    List eligibleT = new ArrayList();	

    public static void main(String[] args) {
	BufferedReader reader = 
	    new BufferedReader(new InputStreamReader(System.in));
	BipartiteMatcher matcher = new BipartiteMatcher();
	int n = 0;

	try {
	    System.out.print("Num nodes on each side> ");
	    String num = reader.readLine();
	    n = Integer.parseInt(num);
	    matcher.reset(n);

	    for (int i = 0; i < n; i++) {
		System.out.print("Weights out of node " + i + "> ");
		String weightStr = reader.readLine();

		StringTokenizer tokenizer = new StringTokenizer(weightStr);
		for (int j = 0; j < n; j++) {
		    double w = Double.parseDouble(tokenizer.nextToken());
		    matcher.setWeight(i, j, w);
		}
	    }
	} catch (IOException e) {
	    Util.fatalError(e);
	}

	int[] matching = matcher.getMatching();
	System.out.println("Maximum-weight matching:");
	for (int i = 0; i < n; i++) {
	    System.out.println(i + ": " + matching[i]);
	}
    }	
}
