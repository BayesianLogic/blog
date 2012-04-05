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

package blog;

import java.util.*;

import blog.common.AbstractTupleIterator;
import blog.common.HashMapDiff;
import blog.common.HashMultiset;
import blog.common.Multiset;
import blog.common.UnaryFunction;
import blog.common.UnaryProcedure;
import blog.common.Util;
import blog.model.Model;
import blog.model.Term;
import blog.model.Type;
import blog.objgen.ObjGenGraph;


/**
 * Represents an argument - set consisting of implicitly specified tuples. The
 * components of each tuple are assumed to be Terms. The general form of the
 * i-th component of a tuple is specified by the object in the i-th position of
 * <code>terms</code> tuple. The tuple is evaluated for each assignment of
 * values to <code>vars</code> that makes the formula <code>cond</code> true.
 */
public class TupleSetSpec extends ArgSpec {

	/**
	 * @param termList
	 *          List of Term objects denoting elements of tuples
	 * 
	 * @param varTypeList
	 *          List of Type objects representing types of the variables. Some of
	 *          these types may be null, but then the main program should exit
	 *          before the compilation phase.
	 * 
	 * @param varList
	 *          List of String objects serving as names of variables
	 * 
	 * @param cond
	 *          Formula that each tuple of objects bound to the variables must
	 *          satisfy
	 */
	public TupleSetSpec(List termList, List varTypeList, List varList,
			Formula cond) {
		terms = new Term[termList.size()];
		termList.toArray(terms);

		vars = new LogicalVar[varList.size()];
		for (int i = 0; i < vars.length; ++i) {
			vars[i] = new LogicalVar((String) varList.get(i),
					(Type) varTypeList.get(i));
		}

		this.cond = cond;

		varValues = new Object[vars.length];
	}

	public TupleSetSpec(Term[] terms, LogicalVar[] vars, Formula cond) {
		this.terms = (Term[]) terms.clone();
		this.vars = (LogicalVar[]) vars.clone();
		this.cond = cond;
	}

	public Term[] getGenericTuple() {

		return terms;

	}

	public LogicalVar[] getParams() {

		return vars;

	}

	public Formula getCond() {

		return cond;

	}

	/**
	 * Given a context, iterates over each assignment to the TupleSetSpec
	 * parameters satisfying condition, temporarily placing it in the context and
	 * evaluating a given function on the context. It returns the set of this
	 * function's results, or null if any of them is null.
	 * 
	 * <p>
	 * This method first enumerates the assignments that satisfy the first
	 * disjunct, then those that satisfy the second but not the first, etc. If the
	 * VarValuesIterator for the first disjunct returns infinitely many values,
	 * this process will never invoke the VarValuesIterators for the remaining
	 * disjuncts. But there's no such thing as short-circuit evaluation for
	 * TupleSetSpecs anyway: we always go over the whole set of assignments.
	 */
	public Multiset evaluate(EvalContext context, UnaryFunction function) {
		if (disjuncts == null) {
			compile(new LinkedHashSet());
		}

		Multiset s = new HashMultiset();
		boolean undetermined = false;

		for (int i = 0; i < disjuncts.size(); ++i) {
			// Iterate over assignments that might satisfy disjunct i
			ObjectIterator iter = new VarValuesIterator(context, i);
			while (iter.hasNext()) {
				((List) iter.next()).toArray(varValues);
				context.assignTuple(vars, varValues);
				Boolean isFirst = isFirstSatisfiedDisjunct(context, i);
				if (isFirst == null) {
					undetermined = true;
					break;
				}
				if (isFirst.booleanValue()) {
					// These values satisfy disjunct i and no earlier disjunct
					Object element = function.evaluate(context);
					if (element == null) {
						undetermined = true;
						break;
					}
					s.add(element);
				}
			}
			if (undetermined || !iter.canDetermineNext()) {
				undetermined = true;
				break;
			}
		}

		context.unassignTuple(vars);
		return (undetermined ? null : s);
	}

	/**
	 * Returns the value of this argument specification in the given context, or
	 * null if the partial world in this context is not complete enough to
	 * evaluate this ArgSpec.
	 */
	public Object evaluate(EvalContext context) {
		context.pushEvaluee(this);
		Object result = evaluate(context, new UnaryFunction() {
			public Object evaluate(Object contextObj) {
				return evaluateTuple((EvalContext) contextObj);
			}
		});
		context.popEvaluee();
		return result;
	}

	/**
	 * Returns a multiset containing all instantiations of the generic tuple into
	 * random variables, given a context, or <code>null</code> if they are
	 * undetermined.
	 */
	public Multiset getRandomVariables(EvalContext context) {
		return evaluate(context, new UnaryFunction() {
			public Object evaluate(Object contextObj) {
				return makeVarsTuple(contextObj);
			}
		});
	}

	private List makeVarsTuple(Object contextObj) {
		List varsTuple = new ArrayList(terms.length);
		for (int termIndex = 0; termIndex < terms.length; termIndex++) {
			BayesNetVar var = makeVar(terms[termIndex], (EvalContext) contextObj);
			if (var == null)
				return null;
			varsTuple.add(var);
		}
		return varsTuple;
	}

	private BayesNetVar makeVar(Term term, EvalContext context) {
		if (!(term instanceof FuncAppTerm))
			Util.fatalError("Terms in TupleSetSpecs must be FuncAppTerm.");
		FuncAppTerm fTerm = (FuncAppTerm) term;
		if (!(fTerm.getFunction() instanceof RandomFunction))
			Util.fatalError("Terms in TupleSetSpecs must be random function terms.");
		List argValues = ArgSpec.evaluate(context, Arrays.asList(fTerm.getArgs()));
		if (argValues.contains(null))
			return null;
		RandFuncAppVar var = new RandFuncAppVar(
				(RandomFunction) fTerm.getFunction(), argValues);
		return var;
	}

	public boolean containsRandomSymbol() {
		return true; // the type symbol
	}

	public boolean checkTypesAndScope(Model model, Map scope) {
		boolean correct = true;
		Map extendedScope = new HashMapDiff(scope);
		for (int i = 0; i < vars.length; ++i) {
			if (extendedScope.containsKey(vars[i].getName())
					&& !scope.containsKey(vars[i].getName())) {
				System.err.println(getLocation() + ": Variable \"" + vars[i]
						+ "\" declared more than once in " + "variable list.");
				correct = false;
			}
			extendedScope.put(vars[i].getName(), vars[i]);
		}

		for (int i = 0; i < terms.length; ++i) {
			Term termInScope = terms[i].getTermInScope(model, extendedScope);
			if (termInScope == null) {
				correct = false;
			} else {
				terms[i] = termInScope;
			}
		}

		correct = (correct && cond.checkTypesAndScope(model, extendedScope));

		return correct;
	}

	/**
	 * Initializes ObjGenGraph objects for the possible bindings to the variables
	 * in this TupleSetSpec.
	 * 
	 * @param callStack
	 *          Set of objects whose compile methods are parents of this method
	 *          invocation. Ordered by invocation order. Used to detect cycles.
	 */
	public int compile(LinkedHashSet callStack) {
		callStack.add(this);
		int errors = cond.compile(callStack);
		for (int i = 0; i < terms.length; ++i) {
			errors += terms[i].compile(callStack);
		}
		if (errors > 0) {
			return errors;
		}

		// Convert cond to propositional DNF and treat each disjunct
		// separately. For each disjunct, keep array of object generation
		// graphs, one for each element of the tuple. In the graph for
		// variable i, later variables are free.

		disjuncts = cond.getPropDNF().getDisjuncts();
		objGenGraphs = new ObjGenGraph[disjuncts.size()][vars.length];
		for (int i = 0; i < disjuncts.size(); ++i) {
			List constraints = ((ConjFormula) disjuncts.get(i)).getConjuncts();
			for (int j = 0; j < vars.length; ++j) {
				List freeVars = Arrays.asList(vars).subList(j, vars.length);
				objGenGraphs[i][j] = new ObjGenGraph(vars[j].getType(), vars[j],
						constraints, new HashSet(freeVars));
			}
		}

		callStack.remove(this);
		return 0;
	}

	public Collection getSubExprs() {
		return Util.concat(Arrays.asList(terms), Collections.singletonList(cond));
	}

	public Set getFreeVars() {
		Set freeVars = new HashSet(cond.getFreeVars());
		for (int i = 0; i < terms.length; ++i) {
			freeVars.add(terms[i].getFreeVars());
		}

		for (int i = 0; i < vars.length; ++i) {
			freeVars.remove(vars[i]);
		}
		return Collections.unmodifiableSet(freeVars);
	}

	public ArgSpec getSubstResult(Substitution subst, Set<LogicalVar> boundVars) {
		boundVars = new HashSet<LogicalVar>(boundVars);
		for (int i = 0; i < vars.length; ++i) {
			boundVars.add(vars[i]);
		}

		Term[] newTerms = new Term[terms.length];
		for (int i = 0; i < terms.length; ++i) {
			newTerms[i] = (Term) terms[i].getSubstResult(subst, boundVars);
		}

		return new TupleSetSpec(newTerms, vars, (Formula) cond.getSubstResult(
				subst, boundVars));
	}

	/**
	 * Two tuple set specifications are equivalent if they have the same tuple of
	 * terms, variable type list, variable list, and condition. There are various
	 * ways to construct a tuple set specification that is equivalent to a given
	 * specification (rename the variables, reorder the variables and types,
	 * etc.), but we do not consider those variations equal to the given
	 * specification.
	 */
	public boolean equals(Object o) {
		if (o instanceof TupleSetSpec) {
			TupleSetSpec other = (TupleSetSpec) o;
			return (Arrays.equals(terms, other.getGenericTuple())
					&& Arrays.equals(vars, other.getParams()) && cond.equals(other
					.getCond()));
		}
		return false;
	}

	public int hashCode() {
		int code = cond.hashCode();
		for (int i = 0; i < terms.length; ++i) {
			code ^= terms[i].hashCode();
		}
		for (int i = 0; i < vars.length; ++i) {
			code ^= vars[i].hashCode();
		}
		return code;
	}

	/**
	 * Returns a string of the form <blockquote> {t1, ..., tK for T1 v1, ..., TN
	 * vN : cond} </blockquote> where t1, ..., tK are the terms, T1, ..., TN are
	 * the variable types, v1, ..., vN are the variables, and cond is the
	 * condition.
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("{");

		if (terms.length > 0) {
			buf.append(terms[0]);
			for (int i = 1; i < terms.length; ++i) {
				buf.append(", ");
				buf.append(terms[i]);
			}
		}

		buf.append(" for ");

		if (vars.length > 0) {
			buf.append(vars[0].getType() + " " + vars[0]);
			for (int i = 1; i < vars.length; ++i) {
				buf.append(", ");
				buf.append(vars[i].getType() + " " + vars[i]);
			}
		}

		buf.append(" : ");
		buf.append(cond);
		buf.append("}");
		return buf.toString();
	}

	/**
	 * Evaluates the terms <code>terms</code> in the given context and returns a
	 * list of their values. Returns null if any of these values are null.
	 */
	private List evaluateTuple(EvalContext context) {
		List termValues = new ArrayList();
		for (int i = 0; i < terms.length; ++i) {
			Object val = terms[i].evaluate(context);
			if (val == null) {
				return null;
			}
			termValues.add(val);
		}
		return termValues;
	}

	private Boolean isFirstSatisfiedDisjunct(EvalContext context, int disjIndex) {
		Formula disj = (Formula) disjuncts.get(disjIndex);
		Boolean disjValue = (Boolean) disj.evaluate(context);
		if (disjValue == null) {
			return null;
		}
		if (!disjValue.booleanValue()) {
			return Boolean.FALSE;
		}

		for (int i = 0; i < disjIndex; ++i) {
			Formula otherDisj = (Formula) disjuncts.get(i);
			Boolean otherDisjValue = (Boolean) otherDisj.evaluate(context);
			if (otherDisjValue == null) {
				return null;
			}
			if (otherDisjValue.booleanValue()) {
				return Boolean.FALSE;
			}
		}

		return Boolean.TRUE;
	}

	/**
	 * Inner class for iterating over tuples of objects that satisfy the tuple of
	 * object generation graphs for a particular disjunct. An
	 * AbstractTupleIterator is not guaranteed to return every tuple after
	 * finitely many steps: if its last object generation graph has infinitely
	 * many satisfiers, it will loop forever returning tuples that only differ in
	 * their last element. But this is ok because short-circuit evaluation is
	 * impossible for TupleSetSpecs anyway.
	 */
	private class VarValuesIterator extends AbstractTupleIterator implements
			ObjectIterator {
		VarValuesIterator(EvalContext context, int disjIndex) {
			super(vars.length);
			this.context = context;
			this.disjIndex = disjIndex;
		}

		protected Iterator getIterator(int varIndex, List varValues) {
			if (!canDetermineNext()) {
				return Collections.EMPTY_SET.iterator();
			}

			for (int i = 0; i < vars.length; ++i) {
				if (i < varIndex) {
					context.assign(vars[i], varValues.get(i));
				} else {
					context.unassign(vars[i]);
				}
			}

			return objGenGraphs[disjIndex][varIndex].iterator(context);
		}

		protected void doneWithIterator(Iterator iter) {
			if (!((ObjectIterator) iter).canDetermineNext()) {
				// The reason why iter's getNext method returned false was
				// that the partial world was not complete enough
				canDetermineNext = false;
			}
		}

		public int skipIndistinguishable() {
			return 0;
		}

		public boolean canDetermineNext() {
			return canDetermineNext;
		}

		EvalContext context;
		int disjIndex;

		boolean canDetermineNext = true;
	}

	public ArgSpec find(Term t) {
		ArgSpec result;

		result = (ArgSpec) Util.findFirstEquals(Arrays.asList(vars), t);
		if (result != null)
			return result;

		result = (ArgSpec) Util.findFirstEquals(Arrays.asList(terms), t);
		if (result != null)
			return result;

		return cond.find(t);
	}

	public void applyToTerms(UnaryProcedure procedure) {
		int i;
		for (i = 0; i != terms.length; i++) {
			vars[i].applyToTerms(procedure);
		}
		for (i = 0; i != terms.length; i++) {
			terms[i].applyToTerms(procedure);
		}
		cond.applyToTerms(procedure);
	}

	public ArgSpec replace(Term t, ArgSpec another) {
		int i;
		for (i = 0; i != vars.length; i++) {
			if (t.equals(vars[i])) // vars are separately quantified.
				return this;
		}

		Term[] newTerms = new Term[terms.length];
		boolean replacement = false;
		for (i = 0; i != terms.length; i++) {
			Term newTerm = (Term) terms[i].replace(t, another);
			replacement = replacement || newTerm != terms[i];
			newTerms[i] = newTerm;
		}

		Formula newCond = (Formula) cond.replace(t, another);
		replacement = replacement || newCond != cond;

		if (replacement) {
			TupleSetSpec result = new TupleSetSpec(newTerms, vars, newCond);
			if (disjuncts != null)
				result.compile(new LinkedHashSet());
			return result;
		}

		return this;
	}

	private Term[] terms;
	private LogicalVar[] vars;
	private Formula cond;

	List disjuncts; // of ConjFormula;
	ObjGenGraph[][] objGenGraphs;

	private Object[] varValues; // scratch space for variable values
}
