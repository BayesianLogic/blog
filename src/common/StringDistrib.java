/*
 * Copyright (c) 2006, Regents of the University of California
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

/**
 * Interface for objects that define probability distributions over strings. In
 * addition to passing strings to the <code>getProb</code> or
 * <code>getLogProb</code> methods, a client can pass one string to
 * <code>setString</code> and then call versions of <code>getProb</code> and
 * <code>getLogProb</code> that operate on substrings of that string. Some
 * implementations may be able to preprocess the string passed to
 * <code>setString</code> and return the probabilities of substrings very
 * efficiently.
 */
public interface StringDistrib {
	/**
	 * Sets the string to be used by subsequent calls to the substring versions of
	 * <code>getProb</code> and <code>getLogProb</code>.
	 */
	void setString(String str);

	/**
	 * Returns the probability of the given string.
	 */
	double getProb(String str);

	/**
	 * Returns the probability of the substring from the given start index up to
	 * the given end index in the string passed to the last call to
	 * <code>setString</code>
	 * 
	 * @throws IllegalStateException
	 *           if <code>setString</code> has not been called
	 * 
	 * @throws IllegalArgumentException
	 *           if the given start and end indices are not valid in the specified
	 *           string
	 */
	double getProb(int start, int end);

	/**
	 * Returns the natural log probability of the given string.
	 */
	double getLogProb(String str);

	/**
	 * Returns the natural log probability of the substring from the given start
	 * index up to the given end index in the string passed to the last call to
	 * <code>setString</code>
	 * 
	 * @throws IllegalStateException
	 *           if <code>setString</code> has not been called
	 * 
	 * @throws IllegalArgumentException
	 *           if the given start and end indices are not valid in the specified
	 *           string
	 */
	double getLogProb(int start, int end);
}
