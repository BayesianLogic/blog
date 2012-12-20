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

package blog.sample;

import java.io.PrintStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import blog.bn.BasicVar;
import blog.bn.BayesNetVar;
import blog.bn.NumberVar;
import blog.common.HashMapWithPreimages;
import blog.common.MapWithPreimages;
import blog.common.Util;
import blog.model.LogicalVar;
import blog.model.NonGuaranteedObject;
import blog.model.Type;
import blog.objgen.ObjectSet;
import blog.world.PartialWorld;

/**
 * Basic implementation of the EvalContext interface. When the underlying
 * partial world is not complete enough to support a call to
 * <code>getValue</code> or <code>getSatisfiers</code>, this implementation
 * either prints an error message and exits the program, or just returns null.
 * Returning null is the default behavior; the fatal error behavior can be
 * obtained by constructing a DefaultEvalContext with the
 * <code>errorIfUndet</code> flag set to true.
 */
public class DefaultEvalContext implements EvalContext {
	/**
	 * Creates a new evaluation context using the given partial world. This
	 * DefaultEvalContext instance will return null from access methods if the
	 * world is not complete enough to determine the correct value.
	 */
	public DefaultEvalContext(PartialWorld world) {
		this.world = world;
	}

	/**
	 * Creates a new evaluation context using the given partial world. If the
	 * <code>errorIfUndet</code> flag is true, the access methods on this instance
	 * will print error messages and exit the program if the world is not complete
	 * enough to determine the correct return value.
	 */
	public DefaultEvalContext(PartialWorld world, boolean errorIfUndet) {
		this.world = world;
		this.errorIfUndet = errorIfUndet;
	}

	public Object getValue(BasicVar var) {
		Object value = world.getValue(var);
		if (value == null) {
			handleMissingVar(var);
		}
		return value;
	}

	public ObjectSet getSatisfiers(NumberVar popApp) {
		if (getValue(popApp) != null) { // note that we use our getValue method
			return world.getSatisfiers(popApp);
		}

		handleMissingVar(popApp);
		return null;
	}

	public NumberVar getPOPAppSatisfied(Object obj) {
		// might want to report invalid objects
		return world.getPOPAppSatisfied(obj);
	}

	public boolean usesIdentifiers(Type type) {
		return world.getIdTypes().contains(type);
	}

	public Boolean objectExists(Object obj) {
		if (obj instanceof NonGuaranteedObject) {
			NonGuaranteedObject ngObj = (NonGuaranteedObject) obj;
			NumberVar nv = ngObj.getNumberVar();
			Integer varValue = (Integer) getValue(nv);
			if (varValue == null) {
				return null;
			}
			return Boolean.valueOf(ngObj.getNumber() <= varValue.intValue());
		}

		return Boolean.TRUE;
	}

	public Object getLogicalVarValue(LogicalVar var) {
		if (assignment == null) {
			return null;
		}
		return assignment.get(var);
	}

	public Set getLogicalVarValues() {
		if (assignment == null) {
			return Collections.EMPTY_SET;
		}
		return assignment.valueSet();
	}

	public String getAssignmentStr() {
		if (assignment == null) {
			return Collections.EMPTY_MAP.toString();
		}
		return assignment.toString();
	}

	public void assign(LogicalVar var, Object value) {
		if (assignment == null) {
			assignment = new HashMapWithPreimages();
		}
		assignment.put(var, value);
	}

	public void assignTuple(LogicalVar[] vars, Object[] values) {
		if (assignment == null) {
			assignment = new HashMapWithPreimages();
		}
		for (int i = 0; i < vars.length; ++i) {
			assignment.put(vars[i], values[i]);
		}
	}

	public void unassign(LogicalVar var) {
		if (assignment != null) {
			assignment.remove(var);
		}
	}

	public void unassignTuple(LogicalVar[] vars) {
		if (assignment != null) {
			for (int i = 0; i < vars.length; ++i) {
				assignment.remove(vars[i]);
			}
		}
	}

	/**
	 * Pushes the given object onto the stack of objects being evaluated. This
	 * stack is maintained purely so that the EvalContext can print more
	 * informative error messages.
	 * 
	 */
	public void pushEvaluee(Object evaluee) {
		if (evaluees == null) {
			evaluees = new LinkedList();
		}
		evaluees.addLast(evaluee);
	}

	/**
	 * Pops the top object from the stack of objects being evaluated.
	 * 
	 * @throws IllegalStateException
	 *           if the stack of objects being evaluated is empty
	 */
	public void popEvaluee() {
		if ((evaluees == null) || evaluees.isEmpty()) {
			throw new IllegalStateException("Evaluee stack is empty.");
		}
		evaluees.removeLast();
	}

	/**
	 * Prints the sequence of objects being evaluated, in the order they were
	 * added to the stack, to the given stream.
	 */
	public void printEvalTrace(PrintStream s) {
		if (evaluees != null) {
			for (Iterator iter = evaluees.iterator(); iter.hasNext();) {
				s.println(iter.next());
			}
		}
	}

	/**
	 * Handle the situation where an access method needs the value of the given
	 * variable, but it is not instantiated. This method either prints an error
	 * message and exits the program, or does nothing, depending on the value of
	 * the <code>errorIfUndet</code> field.
	 */
	protected void handleMissingVar(BayesNetVar var) {
		if (errorIfUndet & Util.verbose()) {
			System.err.println("Error with evaluation trace:");
			printEvalTrace(System.err);
			Util.fatalError("Variable " + var + " is not instantiated.");
		}
	}

	public/* protected -- for debugging */PartialWorld world;

	protected boolean errorIfUndet = false;

	private MapWithPreimages assignment;
	private LinkedList evaluees;

	/*
	 * (non-Javadoc)
	 * 
	 * @see blog.sample.EvalContext#getPartialWorld()
	 */
	@Override
	public PartialWorld getPartialWorld() {
		return world;
	}
}
