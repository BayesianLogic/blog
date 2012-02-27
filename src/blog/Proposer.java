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

/**
 * Interface for Metropolis-Hastings proposal distributions.  A Proposer 
 * object must be able to create an initial state for the Markov chain, and 
 * propose a new state x' given any state x.  It must also be able to 
 * compute the proposal ratio q(x | x') / q(x' | x), where q is the proposal 
 * distribution.
 *
 * <p>Implementations of the Proposer interface should have a constructor 
 * with two arguments: a blog.Model object defining the prior distribution, 
 * and a java.util.Properties object specifying configuration parameters.  
 */
public interface Proposer {
    /**
     * Returns a PartialWorldDiff whose current version serves as the
     * initial state for the Markov chain (its saved version is
     * initially an empty instantiation).  This world satisfies the
     * given evidence and is complete enough to answer the given
     * queries.  Furthermore, the proposer stores the given evidence
     * and queries so that <code>proposeNextState</code> can also
     * maintain these properties.
     *
     * @param queries List of Query objects
     */
    PartialWorldDiff initialize(Evidence evidence, List queries);

    /**
     * Proposes a next state for the Markov chain given the current state.  
     * The world argument is a <i>saved</i> PartialWorldDiff that the proposer 
     * can modify to create the proposal; the saved version that underlies  
     * this PartialWorldDiff is the state before the proposal.  Returns 
     * the log proposal ratio:
     *    log (q(x | x') / q(x' | x))
     *
     * <p>The proposed world satisfies the evidence and is complete enough 
     * to answer the queries specified in the last call to 
     * <code>initialize</code>.  
     *
     * Note that if this proposal distribution is a mixture or cycle of 
     * more elementary proposal distributions, the proposal probabilities 
     * q(x | x') and q(x' | x) may be specific to the elementary distribution 
     * used for this proposal.  
     *
     * @throws IllegalStateException if <code>initialize</code> 
     *                               has not been called
     */
    double proposeNextState(PartialWorldDiff proposedWorld);

    /**
     * Prints any relevant statistics about the internal behavior of
     * this proposer.
     */
    void printStats();

    /**
     * Updates any statistics maintained by this proposer to reflect the 
     * fact that the most recent proposal was accepted 
     * (if <code>accepted</code> is true) or rejected 
     * (if <code>accepted</code> is false).
     */
    void updateStats(boolean accepted);
}
