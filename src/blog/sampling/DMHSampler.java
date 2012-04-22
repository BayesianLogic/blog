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

package blog.sampling;

import java.util.*;

import blog.DecayedProposer;
import blog.common.Util;
import blog.model.Evidence;
import blog.model.Model;

/**
 * Implements a Metropolis-Hastings sampler with the {@link DecayedProposer}.
 */
public class DMHSampler extends MHSampler {
	/**
	 * Creates a new sampler for the given BLOG model. The properties table
	 * specifies configuration parameters to be passed to the super class
	 * {@link MHSampler} and the proposer {@link DecayedProposer}.
	 */
	public DMHSampler(Model model, Properties properties) {
		super(model, properties);
	}

	/** Method responsible for initializing the proposer field. */
	protected void constructProposer(Properties properties) {
		proposer = new DecayedProposer(model, properties);
	}

	public int getMaxRecall() {
		return ((DecayedProposer) proposer).getMaxRecall();
	}

	/**
	 * Adds to evidence to be considered during sampling (typically for new time
	 * steps).
	 */
	public void add(Evidence evidence) {
		this.evidence.addAll(evidence);
		((DecayedProposer) proposer).add(evidence);
	}

	/**
	 * Adds queries to the queries to be considered during sampling. (typically
	 * for new time steps).
	 */
	public void addQueries(Collection queries) {
		this.queries.addAll(queries);
		((DecayedProposer) proposer).addQueries(queries);
	}
}
