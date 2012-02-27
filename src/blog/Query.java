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

package blog;

import java.util.*;
import java.io.PrintStream;
import ve.Factor;

/**
 * Interface for queries that can be answered by an inference engine.  
 * A Query object records both results from a single run of an inference 
 * algorithm, and statistics about the results of several runs (if the 
 * inference algorithm is randomized, each run may yield different 
 * results).  
 *
 * <p>The <code>getVariables</code> method returns a set of random
 * variables such that the result of this query depends only on the
 * posterior joint distribution of these variables.  There are two
 * ways in which this posterior distribution can be provided to the
 * Query object.  One is to call <code>updateStats</code> repeatedly,
 * passing in partial worlds with weights; these worlds must be 
 * complete enough to define values for all the query's variables.  The 
 * other way is to pass in a factor over these variables using 
 * <code>setPosterior</code>.  
 */
public interface Query {

    /**
     * Prints the results of this query to the given stream.
     */
    void printResults(PrintStream s);

    /**
     * If a log file has been specified, prints the results so far 
     * to that file.  
     *
     * @param numSamples the number of samples taken by the inference 
     *                   engine so far (can be set to zero for 
     *                   non-sampling inference engines)
     */
    void logResults(int numSamples);

    /**
     * Returns a collection of (basic or derived) random variables
     * such that the result of this query depends only on the
     * posterior joint distribution for these variables.
     *
     * @throws IllegalStateException if <code>compile</code> has not 
     *                               yet been called
     */
    Collection<? extends BayesNetVar> getVariables();

    /**
     * Returns true if this query satisfies type and scope constraints.  
     * If there is a type or scope error, prints a message to standard 
     * error and returns false.
     */
    public boolean checkTypesAndScope(Model model);

    /**
     * Does compilation steps that can only be done correctly once the 
     * model is complete.  Prints messages to standard error if any errors 
     * are encountered.  Returns the number of errors encountered.
     */
    public int compile();

    /**
     * Updates the within-run statistics for this query to reflect the
     * given world sampled with the given weight.  The world must be 
     * complete enough to define values for all the variables returned 
     * by <code>getVariables</code>.  
     *
     * <p>The effects of calling both <code>updateStats</code> and 
     * <code>setPosterior</code> in the same run are not defined.  
     */
    void updateStats(PartialWorld world, double weight);

    /**
     * Same as {@link #updateStats(PartialWorld, double)} with weight 1.
     */
    void updateStats(PartialWorld world);

    /**
     * Sets the posterior distribution for the variables returned by
     * <code>getVariables</code>.  This overwrites any results
     * specified previously for the current run using this method or
     * <code>updateStats</code>.
     *
     * <p>The effects of calling both <code>updateStats</code> and 
     * <code>setPosterior</code> in the same run are not defined.  
     *
     * @param posterior factor whose set of variables is the same 
     *                  as the collection returned by getVariables, 
     *                  and whose entries sum to 1
     */
    void setPosterior(Factor posterior);

    /**
     * Ends the current run, records across-run statistics for it, and
     * clears the within-run statistics.
     */
    void zeroOut(); 

    /**
     * Prints across-run statistics.
     */
    void printVarianceResults(PrintStream s);

    /**
     * Returns an object whose toString method yields a description of the 
     * location where this query occurred in an input file.  
     */
    public abstract Object getLocation();
}
