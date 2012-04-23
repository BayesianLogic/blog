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

package blog;

import blog.model.Model;
import blog.model.OriginFunction;
import blog.model.Type;
import blog.sample.EvalContext;

/**
 * Represents a generic object of some type, possibly with constraints on the
 * values of its origin functions. Origin functions can be evaluated on these
 * objects, but other functions can't. These objects cannot be used as an
 * argument for any instantiated random variable.
 */
public class GenericObject {
	/**
	 * Creates a new generic object of the given type, with no constraints on its
	 * generating functions.
	 */
	public GenericObject(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	/**
	 * Returns null if the value of <code>g</code> on this object is not
	 * determined.
	 */
	public Object getOriginFuncValue(OriginFunction g) {
		return null;
	}

	/**
	 * Returns true if the given object satisfies the origin function constraints
	 * imposed by this GenericObject, in the given context.
	 */
	public boolean isConsistentInContext(EvalContext context, Object obj) {
		// assume it's of the correct type
		return (obj != Model.NULL);
	}

	public String toString() {
		return ("Generic(" + type + ")");
	}

	private Type type;
}
