/*
 * Copyright (c) 2012, Regents of the University of California
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

package blog.bn;

import blog.common.DGraph;
import blog.world.PartialWorld;

/**
 * A contingent bayes net (CBN) contains a set of random variables V. For each
 * random variable X in V, there is an associated domain dom(X) and decision
 * tree T_X. The decision tree is a binary tree where each node is a predicate
 * on some subset of V. Each leaf of T_X is a probability distribution
 * parametrized by a subset of V. (Summarized from Arora et. al, UAI-10)
 * 
 * @author Da Tang
 * @since Sep 07, 2014
 */

public interface CBN extends DGraph {
  /**
   * An empty CBN
   */
  static final CBN EMPTY_CBN = new DefaultCBN();

  /**
   * Calculating whether an edge Y -> Z is contingent on variable X or not in
   * the
   * PartialWorld or not.
   * 
   */
  boolean isContingentOn(PartialWorld world, BayesNetVar X, BayesNetVar Y,
      BayesNetVar Z);
}
