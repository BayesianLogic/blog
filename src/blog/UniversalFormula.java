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
import common.HashMapDiff;

/**
 * Represents a universal instantiation for one variable using one expression of
 * type Formula.
 */
public class UniversalFormula extends Formula {

	/**
	 * @param varName
	 *          name of universally quantified variable
	 * 
	 * @param varType
	 *          type being quantified over. May be null, but then the main program
	 *          should exit before the compilation phase.
	 * 
	 * @param cond
	 *          formula in the scope of the quantifier
	 */
	public UniversalFormula(String varName, Type varType, Formula cond) {
		var = new LogicalVar(varName, varType);
		this.cond = cond;
	}

	public UniversalFormula(LogicalVar var, Formula cond) {
		this.var = var;
		this.cond = cond;
	}

	/**
	 * Returns the logical variable governed by the universal quantifier in this
	 * formula.
	 */
	public LogicalVar getLogicalVariable() {
		return var;
	}

	/**
	 * Returns the type that the universal quantifier in this formula ranges over.
	 */
	public Type getType() {
		return var.getType();
	}

	/**
	 * Returns the formula inside the universal quantifier.
	 */
	public Formula getCond() {
		return cond;
	}

	public Object evaluate(EvalContext context) {
		context.pushEvaluee(this);

		ObjectSet counterexamples = getCounterexampleSpec().elementSet(context);
		Boolean result = null;
		if (counterexamples.canDetermineIsEmpty()) {
			result = Boolean.valueOf(counterexamples.isEmpty());
			if (counterexampleSpec.dependsOnIdOrder(context)) {
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

	/**
	 * The standard form of a universal formula (forall x psi) is (forall x psi'),
	 * where psi' is the standard form of psi.
	 */
	public Formula getStandardForm() {
		return new UniversalFormula(var, cond.getStandardForm());
	}

	/**
	 * A formula equivalent to the negation of a universal formula (forall x psi)
	 * is (exists x !psi).
	 */
	protected Formula getEquivToNegationInternal() {
		return new ExistentialFormula(var, new NegFormula(cond));
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

	public boolean checkTypesAndScope(Model model, Map scope) {
		Map extendedScope = new HashMapDiff(scope);
		extendedScope.put(var.getName(), var);
		return cond.checkTypesAndScope(model, extendedScope);
	}

	/**
	 * Compiles a specification for the set of counterexamples to this universal
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

		counterexampleSpec = new CompiledSetSpec(var, new NegFormula(cond));
		callStack.remove(this);
		return 0;
	}

	public Set getSatisfiersIfExplicit(EvalContext context, LogicalVar subject,
			GenericObject genericObj) {
		List varValuesToCheck = new ArrayList();
		Set explicitCondSat = null;

		// We look at values of the quantified variable that might
		// make cond false. Values that make cond true regardless of
		// the value of subject are not interesting because they don't
		// give us an explicit satisfier set. We use an unfiltered
		// iterator because the set of quantified-variable values that
		// make cond false often depends on the value of subject. We
		// hope the ObjGenGraphs used for the unfiltered iterator
		// don't use subject (if they do, canDetermineNext will return false).
		ObjectIterator varValueIter = getCounterexampleSpec().unfilteredIterator(
				context);
		while (varValueIter.hasNext()) {
			Object varValue = varValueIter.next();
			context.assign(var, varValue);
			Set condSat = cond.getSatisfiersIfExplicit(context, subject, genericObj);
			if ((condSat != null) && (condSat != Formula.ALL_OBJECTS)) {
				explicitCondSat = condSat;
				break; // we have an explicit satisifer set
			}

			if (condSat != Formula.ALL_OBJECTS) {
				varValuesToCheck.add(varValue);
			}
		}
		context.unassign(var);
		if (!varValueIter.canDetermineNext()) {
			return null;
		}

		if (explicitCondSat != null) {
			// For some value of the quantified variable, we have
			// an explicit set of subject values that make cond true.
			// The formula satisfiers are those subject values that
			// make cond true for all the other quantified variable
			// values too.

			if (explicitCondSat.isEmpty()) {
				return explicitCondSat; // no need to filter
			}

			// Get the rest of the variable values
			while (varValueIter.hasNext()) {
				varValuesToCheck.add(varValueIter.next());
			}
			if (!varValueIter.canDetermineNext()) {
				return null;
			}

			Set satisfiers = new LinkedHashSet();
			for (Iterator subjectValIter = explicitCondSat.iterator(); subjectValIter
					.hasNext();) {
				Object subjectVal = subjectValIter.next();
				context.assign(subject, subjectVal);
				if (trueWithAllVarValues(context, varValuesToCheck)) {
					satisfiers.add(subjectVal);
				}
			}
			context.unassign(subject);
			return satisfiers;
		}

		// We did not get an explicit satisfier set for any value of
		// the quantified variable. Still, if all the quantified
		// variable values we looked at made cond true regardless of
		// the subject value, we can return Formula.ALL_OBJECTS.
		if (varValuesToCheck.isEmpty()) {
			return Formula.ALL_OBJECTS;
		}
		return null;
	}

	public ArgSpec getSubstResult(Substitution subst, Set<LogicalVar> boundVars) {
		boundVars = new HashSet<LogicalVar>(boundVars);
		boundVars.add(var);
		return new UniversalFormula(var, (Formula) cond.getSubstResult(subst,
				boundVars));
	}

	public String toString() {
		return ("forall " + var.getType() + " " + var.getName() + " (" + cond + ")");
	}

	public boolean equals(Object o) {
		if (o instanceof UniversalFormula) {
			UniversalFormula other = (UniversalFormula) o;
			return (var.equals(other.getLogicalVariable()) && cond.equals(other
					.getCond()));
		}
		return false;
	}

	public int hashCode() {
		return (getClass().hashCode() ^ var.hashCode() ^ cond.hashCode());
	}

	/**
	 * Returns a CompiledSetSpec for the set of objects that are counterexamples
	 * to this universal formula: that is, the objects that make the condition
	 * false.
	 */
	protected CompiledSetSpec getCounterexampleSpec() {
		if (counterexampleSpec == null) {
			compile(new LinkedHashSet());
		}
		return counterexampleSpec;
	}

	private boolean trueWithAllVarValues(EvalContext context, Collection varValues) {
		boolean allTrue = true;
		for (Iterator iter = varValues.iterator(); iter.hasNext();) {
			Object varValue = iter.next();
			context.assign(var, varValue);
			Boolean condValue = (Boolean) cond.evaluate(context);
			if ((condValue == null) || !condValue.booleanValue()) {
				allTrue = false;
				break;
			}
		}
		context.unassign(var);
		return allTrue;
	}

	public ArgSpec replace(Term t, ArgSpec another) {
		if (t.equals(var))
			return this; // var is separated quantified.
		Formula newCond = (Formula) cond.replace(t, another);
		if (newCond != cond)
			return compileAnotherIfCompiled(new UniversalFormula(var, newCond));
		return this;
	}

	LogicalVar var;
	Formula cond;

	CompiledSetSpec counterexampleSpec;
}
