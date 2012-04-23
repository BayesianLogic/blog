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

package blog.bn;

import java.util.*;

import blog.Timestep;
import blog.sample.ClassicInstantiatingEvalContext;
import blog.sample.InstantiatingEvalContext;
import blog.world.PartialWorld;

/**
 * A random variable that serves as a node in a Bayes net (directed graphical
 * model).
 */
public interface BayesNetVar {
	/**
	 * Returns true if the given world is complete enough to determine the value
	 * of this random variable.
	 */
	boolean isDetermined(PartialWorld w);

	/**
	 * Returns the value of this random variable in the given world.
	 * 
	 * @throws IllegalArgumentException
	 *           if the given partial world is not complete enough to determine
	 *           the value of this variable
	 */
	Object getValue(PartialWorld w);

	/**
	 * Returns the set of parents of this variable in the given partial world. The
	 * parents are those random variables which, if they changed, could change the
	 * probability of this variable having a given value. This method yields a
	 * fatal error if the partial world is not complete enough to determine this
	 * variable's parents.
	 * 
	 * @return Set of BayesNetVar
	 */
	Set getParents(PartialWorld w);

	/**
	 * Returns the first parent of this variable that is uninstantiated in the
	 * given partial world. This method uses a decision tree view of the
	 * variable's CPD; it walks down the tree until it reaches a node
	 * corresponding to an uninstantiated variable. If all of this variable's
	 * active parents in the given partial world are instantiated, then this
	 * method returns null.
	 */
	BasicVar getFirstUninstParent(PartialWorld w);

	/**
	 * Ensures that the partial world underlying the given
	 * InstantiatingEvalContext is complete enough to determine the value of this
	 * variable and to determine its probability distribution conditional on its
	 * parents.
	 */
	void ensureDetAndSupported(InstantiatingEvalContext instantiator);

	/**
	 * Convenience version of
	 * {@link #ensureDetAndSupported(ClassicInstantiatingEvalContext)} taking a
	 * world and creating an InstantiatingEvalContext on it.
	 */
	void ensureDetAndSupported(PartialWorld world);

	/**
	 * Returns the timestep that this variable is associated with, or null if it
	 * is atemporal.
	 */
	Timestep timestep();
}
