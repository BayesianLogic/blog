/*
 * Copyright (c) 2005, 2006, Regents of the University of California
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

import java.util.*;
import java.io.PrintStream;

import blog.GenericObject;
import blog.bn.RandFuncAppVar;
import blog.sample.EvalContext;

/**
 * Represents random functions, whose value for the given tuple of arguments
 * changes from world to world.
 * 
 * @see blog.model.Function
 */
public class RandomFunction extends Function {

	public RandomFunction(String fname, List arg_types, Type ret_type,
			DependencyModel depmodel) {

		super(fname, arg_types, ret_type);
		this.depmodel = depmodel;

	}

	/**
	 * Returns the logical variables that stand for the function arguments in this
	 * function's dependency model.
	 */
	public LogicalVar[] getArgVars() {
		if (argVars == null) {
			throw new IllegalStateException("Argument variables not set for " + this);
		}
		return argVars;
	}

	/**
	 * Sets the variables that will stand for the function arguments in this
	 * function's dependency model.
	 * 
	 * @param vars
	 *          List of String objects representing the variables
	 */
	public void setArgVars(List vars) {
		argVars = new LogicalVar[vars.size()];
		for (int i = 0; i < argVars.length; ++i) {
			argVars[i] = new LogicalVar((String) vars.get(i), getArgTypes()[i]);
		}
	}

	/**
	 * If this function is indexed by time, returns the variable that stands for
	 * its time argument in its dependency model. Otherwise returns null.
	 * 
	 * @throws IllegalStateException
	 *           if this function's argument variables have not been set yet.
	 */
	public LogicalVar getTimeVar() {
		if (argVars == null) {
			throw new IllegalStateException("Argument variables not set for " + this);
		}

		if (isTimeIndexed()) {
			return argVars[argVars.length - 1];
		}
		return null;
	}

	public DependencyModel getDepModel() {
		return depmodel;
	}

	public void setDepModel(DependencyModel depmodel) {
		this.depmodel = depmodel;
	}

	public boolean hasDepModel() {
		return (depmodel != null);
	}

	/**
	 * Returns a basic random variable for this function with no arguments.
	 */
	public RandFuncAppVar rv() {
		Object[] args = {};
		return new RandFuncAppVar(this, args, true);
	}

	/**
	 * Returns a basic random variable for this function with the given single
	 * argument.
	 */
	public RandFuncAppVar rv(Object arg) {
		Object[] args = { arg };
		return new RandFuncAppVar(this, args, true);
	}

	/**
	 * Returns a basic random variable for this function with the given two
	 * arguments.
	 */
	public RandFuncAppVar rv(Object arg1, Object arg2) {
		Object[] args = { arg1, arg2 };
		return new RandFuncAppVar(this, args, true);
	}

	/**
	 * Returns a basic random variable for this function with the given array of
	 * arguments.
	 */
	public RandFuncAppVar rvWithArgs(Object[] args) {
		return new RandFuncAppVar(this, args);
	}

	public boolean checkTypesAndScope(Model model) {
		if (depmodel == null) {
			return true; // no errors
		}

		Map scope = new HashMap();
		for (int i = 0; i < argVars.length; ++i) {
			scope.put(argVars[i].getName(), argVars[i]);
		}

		return depmodel.checkTypesAndScope(model, scope);
	}

	/**
	 * Ensures that this function has a dependency statement, and compiles that
	 * dependency statement. If this function is in the call stack, does nothing
	 * -- recursion among random functions is allowed.
	 * 
	 * @param callStack
	 *          Set of objects whose compile methods are parents of this method
	 *          invocation. Ordered by invocation order. Used to detect cycles.
	 */
	public int compile(LinkedHashSet callStack) {
		if (compiled) {
			return 0;
		}

		if (callStack.contains(this)) {
			return 0; // recursion ok
		}

		callStack.add(this);
		int errors = 0;
		if (depmodel != null) {
			errors = depmodel.compile(callStack);
		}

		callStack.remove(this);
		compiled = true;
		return errors;
	}

	/**
	 * Prints the dependency statement for this function to the given stream.
	 */
	public void printDepStatement(PrintStream s) {
		s.print("random ");
		s.print(getRetType());
		s.print(" ");
		s.print(getName());
		if ((argVars != null) && (argVars.length > 0)) {
			s.print("(");
			for (int i = 0; i < argVars.length; ++i) {
				s.print(argVars[i].getType());
				s.print(" ");
				s.print(argVars[i].getName());
				if (i + 1 < argVars.length) {
					s.print(", ");
				}
			}
			s.print(")");
		}
		s.println();
		if (depmodel != null) {
			depmodel.print(s);
		}
	}

	public Object getValueInContext(Object[] args, EvalContext context,
			boolean stable) {
		for (int i = 0; i < args.length; ++i) {
			if (args[i] == Model.NULL) {
				return Model.NULL;
			}
			if (args[i] instanceof GenericObject) {
				return null; // can't determine value on generic object
			}
		}

		RandFuncAppVar rv = new RandFuncAppVar(this, args, stable);
		return context.getValue(rv);
	}

	private LogicalVar[] argVars = null;
	private DependencyModel depmodel = null;
	private boolean compiled = false;
}
