/*
 * Copyright (c) 2007 Massachusetts Institute of Technology
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
 * * Neither the name of the Massachusetts Institute of Technology nor
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

package blog.sample;

import java.util.*;

import blog.bn.BasicVar;
import blog.bn.NumberVar;
import blog.bn.RandFuncAppVar;
import blog.common.Util;
import blog.model.Model;
import blog.model.Type;
import blog.world.PartialWorld;

/**
 * Evaluation context that instantiates random variables as needed, using some
 * very simple rules. Number variables are set to zero. Random function
 * application variables are set to the first guaranteed object of their return
 * type, or Model.NULL if the return type has no guaranteed objects.
 */
public class SimpleInstEvalContext extends ParentRecEvalContext {
	/**
	 * Creates a new SimpleInstEvalContext using the given world.
	 */
	public SimpleInstEvalContext(PartialWorld world) {
		super(world);
	}

	public boolean isInstantiated(BasicVar var) {
		return (world.getValue(var) != null);
	}

	protected Object getOrComputeValue(BasicVar var) {
		Object value = world.getValue(var);
		if (value == null) {
			if (var instanceof NumberVar) {
				value = new Integer(0);
			} else if (var instanceof RandFuncAppVar) {
				Type retType = ((RandFuncAppVar) var).getType();
				value = retType.getGuaranteedObject(0);
				if (value == null) {
					value = Model.NULL;
				}
			} else {
				throw new IllegalArgumentException("Don't know how to instantiate: "
						+ var);
			}
		}

		if (parents.add(var)) {
			var.ensureStable();
		}
		return value;
	}

	// Note that we don't have to override getSatisfiers, because the
	// DefaultEvalContext implementation of getSatisfiers calls getValue
	// on the number variable.
}
