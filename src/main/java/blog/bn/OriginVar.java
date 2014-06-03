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

package blog.bn;

import java.util.*;

import blog.ObjectIdentifier;
import blog.sample.InstantiatingEvalContext;
import blog.type.Timestep;
import blog.world.PartialWorld;

/**
 * A variable whose value is the POP application satisfied by a given object
 * identifier. The POP application is represented as a NumberVar.
 */
public class OriginVar extends AbstractBayesNetVar {
	/**
	 * Creates a new variable representing the origin of the given object
	 * identifier.
	 */
	public OriginVar(ObjectIdentifier id) {
		this.id = id;
	}

	/**
	 * Returns the identifier that this OriginVar applies to.
	 */
	public ObjectIdentifier getIdentifier() {
		return id;
	}

	/**
	 * Returns true if the given world is complete enough to determine the value
	 * of this random variable.
	 */
	public boolean isDetermined(PartialWorld w) {
		return true;
	}

	/**
	 * Returns the value of this random variable in the given world.
	 * 
	 * @throws IllegalArgumentException
	 *           if the given partial world is not complete enough to determine
	 *           the value of this variable
	 */
	public Object getValue(PartialWorld w) {
		return w.getPOPAppSatisfied(id);
	}

	/**
	 * Returns the set of parents of this variable in the given partial world. The
	 * parents are those random variables which, if they changed, could change the
	 * probability of this variable having a given value. This method yields a
	 * fatal error if the partial world is not complete enough to determine this
	 * variable's parents.
	 * 
	 * @return Set of BayesNetVar
	 */
	public Set getParents(PartialWorld w) {
		return Collections.EMPTY_SET;
	}

	public BasicVar getFirstUninstParent(PartialWorld w) {
		return null;
	}

	/**
	 * Ensures that the partial world underlying the given
	 * InstantiatingEvalContext is complete enough to determine the value of this
	 * variable and to determine its probability distribution conditional on its
	 * parents.
	 */
	public void ensureDetAndSupported(InstantiatingEvalContext instantiator) {
		// do nothing
	}

	public Timestep timestep() {
		return null;
	}

	public String toString() {
		return ("Origin(" + id + ")");
	}

	/**
	 * Two OriginVars are equal if they are for the same object identifier.
	 */
	public boolean equals(Object o) {
		if (o instanceof OriginVar) {
			OriginVar other = (OriginVar) o;
			return (id == other.getIdentifier());
		}
		return false;
	}

	public int hashCode() {
		return id.hashCode();
	}

	private ObjectIdentifier id;
}
