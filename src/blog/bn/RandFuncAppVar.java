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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blog.ObjectIdentifier;
import blog.common.Util;
import blog.model.DependencyModel;
import blog.model.FuncAppTerm;
import blog.model.LogicalVar;
import blog.model.Model;
import blog.model.RandomFunction;
import blog.model.Term;
import blog.model.Type;
import blog.sample.EvalContext;

/**
 * A random variable whose value is the value of a certain random function on a
 * certain tuple of arguments.
 */
public class RandFuncAppVar extends VarWithDistrib {
	/**
	 * Creates a RandFuncAppVar for the given function applied to the given tuple
	 * of arguments.
	 */
	public RandFuncAppVar(RandomFunction f, List args) {
		super(args);
		this.f = f;
	}

	/**
	 * Creates a RandFuncAppVar for the given function applied to the given tuple
	 * of arguments.
	 */
	public RandFuncAppVar(RandomFunction f, Object[] args) {
		super(args);
		this.f = f;
	}

	/**
	 * Creates a RandFuncAppVar for the given function applied to the given tuple
	 * of arguments. If <code>stable</code> is true, then the caller guarantees
	 * that the given <code>args</code> array will not be modified externally.
	 */
	public RandFuncAppVar(RandomFunction f, Object[] args, boolean stable) {
		super(args, stable);
		this.f = f;
	}

	/**
	 * Returns the function being applied in this function application variable.
	 */
	public final RandomFunction func() {
		return f;
	}

	/**
	 * Returns the return type of this variable's function.
	 */
	public Type getType() {
		return f.getRetType();
	}

	public int getOrderingIndex() {
		return f.getDepModel().getCreationIndex();
	}

	public DependencyModel getDepModel() {
		return f.getDepModel();
	}

	public DependencyModel.Distrib getDistrib(EvalContext context) {
		context.pushEvaluee(this);
		DependencyModel depModel = f.getDepModel();
		if (depModel == null) {
			Util.fatalErrorWithoutStack("Can't get distribution for random variable because function "
					+ f.getSig() + " has no dependency statement.");
		}

		DependencyModel.Distrib distrib = depModel.getDistribWithBinding(context,
				f.getArgVars(), args(), Model.NULL);
		context.popEvaluee();
		return distrib;
	}

	public FuncAppTerm getCanonicalTerm(Map logicalVarForObj) {
		List argTerms = new ArrayList();
		for (int i = 0; i < args.length; ++i) {
			Object arg = args[i];
			Term term = (LogicalVar) logicalVarForObj.get(arg);
			if (term == null) {
				if (arg instanceof ObjectIdentifier) {
					throw new IllegalArgumentException(
							"No logical variable specified for object identifier " + arg);
				}
				term = f.getArgTypes()[i].getCanonicalTerm(arg);
				if (term == null) {
					throw new UnsupportedOperationException(
							"Can't get canonical term for object " + arg + " of type "
									+ f.getArgTypes()[i]);
				}
			}
			argTerms.add(term);
		}
		return new FuncAppTerm(f, argTerms);
	}

	public Object clone() {
		return new RandFuncAppVar(f, args);
	}

	/**
	 * Two RandFuncAppVar objects are equal if they have the same function and
	 * their argument arrays are equal (recall that Arrays.equals calls the
	 * <code>equals</code> method on each corresponding pair of objects in the two
	 * arrays).
	 */
	public boolean equals(Object obj) {
		if (obj instanceof RandFuncAppVar) {
			RandFuncAppVar other = (RandFuncAppVar) obj;
			return ((f == other.func()) && Arrays.equals(args, other.args()));
		}
		return false;
	}

	public int hashCode() {
		int code = f.hashCode();
		for (int i = 0; i < args.length; ++i) {
			code ^= args[i].hashCode();
		}
		return code;
	}

	public String toString() {
		if (args.length == 0) {
			return f.toString();
		}

		StringBuffer buf = new StringBuffer();
		buf.append(f);
		buf.append("(");
		buf.append(args[0]);
		for (int i = 1; i < args.length; ++i) {
			buf.append(", ");
			buf.append(args[i]);
		}
		buf.append(")");
		return buf.toString();
	}
	
	/**
	 * for observable type
	 */
	public String toObsString(HashMap m) {
		if (args.length == 0) {
			return f.toString();
		}

		StringBuffer buf = new StringBuffer();
		buf.append(f);
		buf.append("(");
		buf.append(blog.engine.onlinePF.ObservabilitySignature.toObsString(args[0],m));
		for (int i = 1; i < args.length; ++i) {
			buf.append(", ");
			buf.append(blog.engine.onlinePF.ObservabilitySignature.toObsString(args[i],m));
		}
		buf.append(")");
		return buf.toString();
	}

	private RandomFunction f;

}
