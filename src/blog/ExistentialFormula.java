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

import blog.common.HashMapDiff;

/**
 * Represents an existential instantiation for one variable satisfying an
 * expression of type Formula.
 */
public class ExistentialFormula extends Formula {

	/**
	 * @param varName
	 *          name of existentially quantified variable
	 * 
	 * @param varType
	 *          type being quantified over. May be null, but then the main program
	 *          should exit before the compilation phase.
	 * 
	 * @param cond
	 *          formula in the scope of the quantifier
	 */
	public ExistentialFormula(String varName, Type varType, Formula cond) {
		var = new LogicalVar(varName, varType);
		this.cond = cond;
	}

	public ExistentialFormula(LogicalVar var, Formula cond) {
		this.var = var;
		this.cond = cond;
	}

	/**
	 * Returns the logical variable governed by the existential quantifier in this
	 * formula.
	 */
	public LogicalVar getLogicalVariable() {
		return var;
	}

	/**
	 * Returns the type that the existential quantifier in this formula ranges
	 * over.
	 */
	public Type getType() {
		return var.getType();
	}

	/**
	 * Returns the formula inside the existential quantifier.
	 */
	public Formula getCond() {
		return cond;
	}

	public Object evaluate(EvalContext context) {
		context.pushEvaluee(this);

		ObjectSet witnesses = getWitnessSpec().elementSet(context);
		Boolean result = null;
		if (witnesses.canDetermineIsEmpty()) {
			result = Boolean.valueOf(!witnesses.isEmpty());
			if (witnessSpec.dependsOnIdOrder(context)) {
				System.out
						.println("Warning: Short-circuiting an iteration that depends on "
								+ "order of object identifiers, while evaluating " + this
								+ ".  MCMC states may define overlapping events.");
			}
		}

		context.popEvaluee();
		return result;
	}

	public boolean containsRandomSymbol() {
		return true; // the type symbol
	}

	public boolean checkTypesAndScope(Model model, Map scope) {
		Map extendedScope = new HashMapDiff(scope);
		extendedScope.put(var.getName(), var);
		return cond.checkTypesAndScope(model, extendedScope);
	}

	/**
	 * Compiles a specification for the set of witnesses for this existential
	 * formula.
	 * 
	 * @param callStack
	 *          Set of objects whose compile methods are parents of this method
	 *          invocation. Ordered by invocation order. Used to detect cycles.
	 */
	public int compile(LinkedHashSet callStack) {
		callStack.add(this);
		int errors = cond.compile(callStack);
		if (errors > 0) {
			return errors;
		}

		witnessSpec = new CompiledSetSpec(var, cond);
		callStack.remove(this);
		return 0;
	}

	/**
	 * The standard form of an existential formula (exists x psi) is (exists x
	 * psi'), where psi' is the standard form of psi.
	 */
	public Formula getStandardForm() {
		return new ExistentialFormula(var, cond.getStandardForm());
	}

	/**
	 * A formula equivalent to the negation of an exisential formula (exists x
	 * psi) is (forall x !psi).
	 */
	protected Formula getEquivToNegationInternal() {
		return new UniversalFormula(var, new NegFormula(cond));
	}

	public List getSubformulas() {
		return Collections.singletonList(cond);
	}

	public Set getFreeVars() {
		Set freeVars = new HashSet(cond.getFreeVars());
		freeVars.remove(var);
		return Collections.unmodifiableSet(freeVars);
	}

	public boolean isQuantified() {
		return true;
	}

	public Set getSatisfiersIfExplicit(EvalContext context, LogicalVar subject,
			GenericObject genericObj) {
		Set satisfiers = null;
		boolean createdNewSet = false;

		// We use an unfiltered iterator over possible values of the
		// quantified variable because the set of quantified-variable
		// values that satisfy cond often depends on the value of subject.
		// We hope the ObjGenGraphs used for the unfiltered iterator don't
		// use subject (if they do, canDetermineNext will return false).
		ObjectIterator iter = getWitnessSpec().unfilteredIterator(context);
		while (iter.hasNext()) {
			Object varValue = iter.next();
			context.assign(var, varValue);
			Set condSat = cond.getSatisfiersIfExplicit(context, subject, genericObj);
			if (condSat == Formula.ALL_OBJECTS) {
				// Short-circuit: all objects make cond true with this
				// value of the quantified variable, so no point in looking
				// at other values.
				return Formula.ALL_OBJECTS;
			}
			if (condSat != null) {
				// Subject values that make cond true with this value of
				// the quantified variable thereby make the whole
				// existential formula true.
				if (satisfiers == null) {
					satisfiers = condSat;
				} else {
					if (!createdNewSet) {
						// in case satisfiers was unmodifiable...
						satisfiers = new LinkedHashSet(satisfiers);
						createdNewSet = true;
					}
					satisfiers.addAll(condSat);
				}
			}
		}
		context.unassign(var);
		if (!iter.canDetermineNext()) {
			return null;
		}

		return satisfiers;
	}

	public ArgSpec getSubstResult(Substitution subst, Set<LogicalVar> boundVars) {
		boundVars = new HashSet<LogicalVar>(boundVars);
		boundVars.add(var);
		return new ExistentialFormula(var, (Formula) cond.getSubstResult(subst,
				boundVars));
	}

	public String toString() {
		return ("exists " + var.getType() + " " + var.getName() + " (" + cond + ")");
	}

	public boolean equals(Object o) {
		if (o instanceof ExistentialFormula) {
			ExistentialFormula other = (ExistentialFormula) o;
			return (var.equals(other.getLogicalVariable()) && cond.equals(other
					.getCond()));
		}
		return false;
	}

	public int hashCode() {
		return (getClass().hashCode() ^ var.hashCode() ^ cond.hashCode());
	}

	/**
	 * Returns a CompiledSetSpec for the set of objects that are witnesses to this
	 * existential formula: that is, the objects that make the condition true.
	 */
	protected CompiledSetSpec getWitnessSpec() {
		if (witnessSpec == null) {
			compile(new LinkedHashSet());
		}
		return witnessSpec;
	}

	public ArgSpec replace(Term t, ArgSpec another) {
		if (t.equals(var))
			return this; // var is separated quantified.
		Formula newCond = (Formula) cond.replace(t, another);
		if (newCond != cond)
			return compileAnotherIfCompiled(new ExistentialFormula(var, newCond));
		return this;
	}

	private LogicalVar var;
	private Formula cond;

	private CompiledSetSpec witnessSpec;
}
