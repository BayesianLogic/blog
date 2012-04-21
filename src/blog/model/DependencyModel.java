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

import blog.EqualsCPD;
import blog.EvalContext;
import blog.POP;
import blog.bn.BasicVar;
import blog.bn.VarWithDistrib;
import blog.common.Util;
import blog.distrib.CondProbDistrib;

/**
 * Represents dependency statements for functions and number statements for
 * potential object patterns. It consists of a list of clauses the
 * dependency/number statement consists of. Each DependencyModel also has a
 * default value: if none of the clauses are satisfied, then the child variable
 * has the default value with probability 1. The default value is Boolean.FALSE
 * for Boolean functions, null for all other functions, and Integer(0) for POPs.
 * 
 * @see blog.model.Function
 * @see blog.POP
 */
public class DependencyModel {

	/**
	 * Nested class representing a distribution over child values, in the form of
	 * a CPD and a list of values for the CPD's arguments.
	 */
	public static class Distrib {
		public Distrib(CondProbDistrib cpd, List argValues) {
			this.cpd = cpd;
			this.argValues = argValues;
		}

		public CondProbDistrib getCPD() {
			return cpd;
		}

		public List getArgValues() {
			return Collections.unmodifiableList(argValues);
		}

		public Object sampleVal(Type type) {
			return cpd.sampleVal(argValues, type);
		}

		public Object sampleVal(VarWithDistrib var) {
			return cpd.sampleVal(argValues, var.getType());
		}

		public double getProb(Object value) {
			return cpd.getProb(argValues, value);
		}

		public String toString() {
			return (cpd + "(" + argValues + ")");
		}

		private CondProbDistrib cpd;
		private List argValues;
	}

	public DependencyModel(List cl, Type childType, Object defaultVal) {

		clause_lst = cl;
		this.childType = childType;
		this.defaultVal = defaultVal;

		Term defaultTerm = childType.getCanonicalTerm(defaultVal);
		if (defaultTerm == null) {
			Util.fatalError("No canonical term for default value " + defaultVal
					+ " of type " + childType);
		}
		defaultClause = new Clause(TrueFormula.TRUE, EqualsCPD.CPD,
				Collections.singletonList(defaultTerm));
	}

	public List getClauseList() {

		return clause_lst;

	}

	public Object getDefaultValue() {
		return defaultVal;
	}

	/**
	 * Returns the CPD and argument values for the first satisfied clause in the
	 * context obtained by binding the given variables to the given objects. If
	 * any of the given objects does not exist, returns a distribution that is
	 * deterministically equal to the given <code>valueWhenArgsDontExist</code>.
	 * If the context is not complete enough to determine the first satisfied
	 * clause and its argument values, this method returns null.
	 */
	public Distrib getDistribWithBinding(EvalContext context, LogicalVar[] vars,
			Object[] objs, Object valueWhenArgsDontExist) {
		for (int i = 0; i < objs.length; ++i) {
			Boolean exists = context.objectExists(objs[i]);
			if (exists == null) {
				return null;
			}
			if (!exists.booleanValue()) {
				return new DependencyModel.Distrib(EqualsCPD.CPD,
						Collections.singletonList(valueWhenArgsDontExist));
			}
		}

		context.assignTuple(vars, objs);
		DependencyModel.Distrib distrib = getDistrib(context);
		context.unassignTuple(vars);
		return distrib;
	}

	/**
	 * Returns the CPD and argument values for the first satisfied clause in the
	 * given context. If the context is not complete enough to determine the first
	 * satisfied clause and its argument values, then this method returns null.
	 */
	public Distrib getDistrib(EvalContext context) {
		Clause activeClause = getActiveClause(context);
		if (activeClause == null) {
			return null;
		}
		return activeClause.getDistrib(context);
	}

	/**
	 * Returns the first clause in this dependency model whose condition is
	 * satisfied in the given context. If no clause's condition is satisfied, this
	 * method returns an automatically-constructed default clause whose condition
	 * is "true" and whose CPD is an EqualsCPD with an argument denoting this
	 * dependency model's default value. If the given context is not complete
	 * enough to determine the first satisfied clause, this method returns null.
	 */
	public Clause getActiveClause(EvalContext context) {
		for (Iterator iter = clause_lst.iterator(); iter.hasNext();) {
			Clause clause = (Clause) iter.next();
			Boolean condValue = (Boolean) clause.getCond().evaluate(context);
			if (condValue == null) {
				return null; // condition's truth value not determined
			}

			if (condValue.booleanValue()) {
				// this is the first satisfied clause
				return clause;
			}
		}

		// None of the clauses are satisfied.
		return defaultClause;
	}

	/**
	 * If, in the given context, this dependency model specifies that the child is
	 * equal to one of its parents, then this method returns that "equal parent".
	 * Otherwise it returns null. This method also returns null if the given
	 * context is not complete enough to determine the equal parent.
	 */
	public BasicVar getEqualParent(EvalContext context) {
		for (Iterator iter = clause_lst.iterator(); iter.hasNext();) {
			Clause clause = (Clause) iter.next();
			Boolean condValue = (Boolean) clause.getCond().evaluate(context);
			if (condValue == null) {
				return null; // condition's truth value not determined
			}

			if (condValue.booleanValue()) {
				// This is the first satisfied clause
				return clause.getEqualParent(context);
			}
		}

		// None of the clauses are satisfied.
		return null;
	}

	/**
	 * Prints this dependency model to the given stream. Each clause is printed on
	 * a separate line, and each line is indented 1 tab. The first clause begins
	 * with "if"; all subsequent clauses begin with "elseif".
	 */
	public void print(PrintStream s) {
		for (int i = 0; i < clause_lst.size(); ++i) {
			Clause c = (Clause) clause_lst.get(i);
			s.print("\t");
			if (i > 0) {
				s.print("else");
			}
			s.println(c);
		}
	}

	public boolean checkTypesAndScope(Model model, Map scope) {
		boolean correct = true;

		for (Iterator iter = clause_lst.iterator(); iter.hasNext();) {
			Clause c = (Clause) iter.next();
			if (!c.checkTypesAndScope(model, scope, childType)) {
				correct = false;
			}
		}

		return correct;
	}

	/**
	 * Creates CPD objects for this dependency model, and does any necessary
	 * compilation on the conditions and CPD arguments. Prints messages to
	 * standard error if any errors occur. Returns the number of errors
	 * encountered.
	 * 
	 * @param callStack
	 *          Set of objects whose compile methods are parents of this method
	 *          invocation. Ordered by invocation order. Used to detect cycles.
	 */
	public int compile(LinkedHashSet callStack) {
		callStack.add(this);
		int errors = 0;

		for (Iterator iter = clause_lst.iterator(); iter.hasNext();) {
			errors += ((Clause) iter.next()).compile(callStack);
		}

		callStack.remove(this);
		return errors;
	}

	/**
	 * Returns an index indicating when this dependency model was defined.
	 */
	public int getCreationIndex() {
		return creationIndex;
	}

	private List clause_lst; // of Clause, not including the default clause
	private Clause defaultClause;
	private Type childType;
	private Object defaultVal;
	private int creationIndex = Model.nextCreationIndex();

	private static int numCreated = 0;
}
