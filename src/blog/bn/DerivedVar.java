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

import blog.ParentRecEvalContext;
import blog.Timestep;
import blog.model.ArgSpec;
import blog.sample.DefaultEvalContext;
import blog.sample.EvalContext;
import blog.sample.InstantiatingEvalContext;
import blog.world.PartialWorld;

/**
 * A random variable whose value is a deterministic function of some basic
 * variables. In this default implementation, the DerivedVar's value is given by
 * some ArgSpec.
 */
public class DerivedVar extends AbstractBayesNetVar {
	/**
	 * Creates a new DerivedVar whose value is given by <code>argSpec</code>.
	 */
	public DerivedVar(ArgSpec argSpec) {
		this.argSpec = argSpec;
	}

	/**
	 * Returns the ArgSpec that determines this variable's value.
	 */
	public ArgSpec getArgSpec() {
		return argSpec;
	}

	public boolean isDetermined(PartialWorld w) {
		return argSpec.isDetermined(w);
	}

	/**
	 * Returns the value of this random variable in the given world.
	 */
	public Object getValue(PartialWorld w) {
		return argSpec.evaluate(new DefaultEvalContext(w, true));
	}

	/**
	 * Returns the value of this random variable in the given context.
	 */
	public Object getValue(EvalContext context) {
		return argSpec.evaluate(context);
	}

	public Set getParents(PartialWorld w) {
		ParentRecEvalContext context = new ParentRecEvalContext(w, true);
		context.pushEvaluee(this);
		argSpec.evaluate(context);
		context.popEvaluee();
		return context.getParents();
	}

	public BasicVar getFirstUninstParent(PartialWorld w) {
		ParentRecEvalContext context = new ParentRecEvalContext(w, false);
		context.pushEvaluee(this);
		argSpec.evaluate(context);
		context.popEvaluee();
		return context.getLatestUninstParent();
	}

	public void ensureDetAndSupported(InstantiatingEvalContext instantiator) {
		instantiator.pushEvaluee(this);
		Object value = argSpec.evaluate(instantiator);
		// System.out.println("DerivedVar.ensure...: " + this + " determined as " +
		// value);
		// System.out.println("World is " +
		// System.identityHashCode(instantiator.world));
		instantiator.popEvaluee();
		// Note that 'determined' does not mean 'instantiated'.
	}

	public Timestep timestep() {
		return null;
	}

	/**
	 * Returns the string representation of this derived variable's ArgSpec.
	 */
	public String toString() {
		return ("/*DerivedVar*/ " + argSpec.toString());
		// The comments around "DerivedVar" allows us to parse the string.
	}

	protected ArgSpec argSpec;
}
