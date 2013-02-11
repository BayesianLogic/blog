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
 * * Neither the name of the Massachusetts Institute of Technology 
 *   nor the names of its contributors may be used to endorse or 
 *   promote products derived from this software without specific  
 *   prior written permission.
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

import java.util.List;
import java.util.Set;

import blog.model.DependencyModel;
import blog.sample.DefaultEvalContext;
import blog.sample.EvalContext;
import blog.sample.InstantiatingEvalContext;
import blog.sample.ParentRecEvalContext;
import blog.world.PartialWorld;

/**
 * Abstract class for variables that have a distribution specified by the BLOG
 * model. This includes number variables and random function application
 * variables.
 */
public abstract class VarWithDistrib extends BasicVar {
	/**
	 * Creates a new VarWithDistrib with the given tuple of arguments or
	 * generating objects.
	 */
	protected VarWithDistrib(Object[] args) {
		super(args);
	}

	/**
	 * Creates a new VarWithDistrib with the given tuple of arguments or
	 * generating objects. If <code>stable</code> is true, then the caller
	 * guarantees that the given <code>args</code> array will not be modified
	 * externally.
	 */
	protected VarWithDistrib(Object[] args, boolean stable) {
		super(args, stable);
	}

	/**
	 * Creates a new VarWithDistrib with the given tuple of arguments or
	 * generating objects.
	 */
	protected VarWithDistrib(List argList) {
		super(argList);
	}

	/**
	 * Returns the dependency model for this variable.
	 */
	public abstract DependencyModel getDepModel();

	/**
	 * Returns the CPD and argument values in the first satisfied clause of this
	 * variable's dependency statement in the given context. Returns null if the
	 * partial world in this context is not complete enough to determine the first
	 * satisfied clause and its CPD arguments.
	 */
	public abstract DependencyModel.Distrib getDistrib(EvalContext context);

	/**
	 * same as {@link #getDistrib(EvalContext)}, with a {@link DefaultEvalContext}
	 * constructed with a given world.
	 */
	public DependencyModel.Distrib getDistrib(PartialWorld world) {
		return getDistrib(new DefaultEvalContext(world));
	}

	/**
	 * Returns the set of parents of this variable in the given partial world. The
	 * parents are those random variables which, if they changed, could change the
	 * first satisfied clause in this variable's dependency statement or the
	 * values of CPD arguments in that clause. This method yields a fatal error if
	 * the partial world is not complete enough to determine this variable's
	 * parents.
	 * 
	 * @return Set of BayesNetVar
	 */
	public Set getParents(PartialWorld w) {
		ParentRecEvalContext context = new ParentRecEvalContext(w, true);
		getDistrib(context);
		return context.getParents();
	}

	public BasicVar getFirstUninstParent(PartialWorld w) {
		ParentRecEvalContext context = new ParentRecEvalContext(w, false);
		getDistrib(context);
		return context.getLatestUninstParent();
	}

	public void ensureDetAndSupported(InstantiatingEvalContext instantiator) {
		if (instantiator.isInstantiated(this)) {
			getDistrib(instantiator); // ensure supported
		} else {
			// Calling getValue will cause the instantiator to find the
			// distribution for this variable and instantiate it.
			instantiator.getValue(this);
		}
	}

	/**
	 * determine whether ready to sample this random variable
	 * i.e. all parents of this random variable is already sampled
	 * 
	 * @param w
	 * @return
	 */
	public boolean canSample(PartialWorld world) {
		// TODO this implementation is NOT efficient!!!!
		// leili 2012-09-19
		return this.getDistrib(world) != null;
	}
}
