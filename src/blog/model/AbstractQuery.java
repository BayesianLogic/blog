/*
 * Copyright (c) 2007, 2008, Regents of the University of California
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

import blog.PartialWorld;
import ve.Factor;

/**
 * An implementation of Query that defines default implementations to some
 * methods.
 * 
 * @author Rodrigo, Brian
 */
public abstract class AbstractQuery implements Query {
	/**
	 * Throws an UnsupportedOperationException.
	 */
	public void setPosterior(Factor posterior) {
		throw new UnsupportedOperationException(this.getClass().getName()
				+ " does not support setPosterior.");
	}

	/**
	 * If a log file has been specified, prints the results so far to that file.
	 * 
	 * <p>
	 * This default implementation does nothing.
	 * 
	 * @param numSamples
	 *          the number of samples taken by the inference engine so far (can be
	 *          set to zero for non-sampling inference engines)
	 */
	public void logResults(int numSamples) {
	}

	/**
	 * Returns an object whose toString method yields a description of the
	 * location where this query occurred in an input file.
	 * 
	 * <p>
	 * This default implementation returns a string indicating that this query was
	 * generated internally, rather than from an input file.
	 */
	public Object getLocation() {
		return "(auto-generated)";
	}

	public void updateStats(PartialWorld world) {
		updateStats(world, 1.0);
	}
}
