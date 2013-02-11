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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import blog.common.HashMapDiff;
import blog.common.UnaryProcedure;
import blog.objgen.CompiledSetSpec;
import blog.objgen.ObjectSet;
import blog.sample.DefaultEvalContext;
import blog.sample.EvalContext;
import blog.world.PartialWorld;

/**
 * Represents an argument - set with implicit specification of its elements. An
 * ImplicitSetSpec consists of a type <code>type</code>, a variable
 * <code>set_elt</code>, and a formula <code>cond</code>. It evaluates to the
 * set of objects <i>o</i> of type <code>type</code> such that <code>cond</code>
 * is satisfied when <code>set_elt</code> is bound to <i>o</i>.
 */
public class ImplicitSetSpec extends ArgSpec {

	/**
	 * @param set_elt
	 *          name of variable representing an element of the set
	 * 
	 * @param type
	 *          type of objects in the set. May be null, but then the main program
	 *          should exit before the compilation phase.
	 * 
	 * @param cond
	 *          formula that objects in the set must satisfy
	 */
	public ImplicitSetSpec(String set_elt, Type type, Formula cond) {
		var = new LogicalVar(set_elt, type);
		this.cond = cond;
	}

	public ImplicitSetSpec(LogicalVar var, Formula cond) {
		this.var = var;
		this.cond = cond;
	}

	public LogicalVar getGenericSetElt() {

		return var;

	}

	public Type getType() {

		return var.getType();

	}

	public Formula getCond() {

		return cond;

	}

	/**
	 * Returns the set of objects <i>o</i> of type <code>type</code> such that
	 * when <code>var</code> is bound to <i>o</i>, the formula <code>cond</code>
	 * is satisfied in <code>w</code>. This method yields a fatal error if
	 * <code>w</code> is not complete enough to define this set.
	 */
	public Set getSatisfyingSet(PartialWorld w) {
		return (Set) evaluate(new DefaultEvalContext(w, true));
	}

	public Object evaluate(EvalContext context) {
		context.pushEvaluee(this);
		ObjectSet set = getSatisfierSpec().elementSet(context);
		ObjectSet result = set.getExplicitVersion();
		// if (Util.verbose()) {
		// System.out.println("For " + this + " " + context.getAssignmentStr()
		// + ", got explicit set: " + result);
		// }

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
	 * Initializes a compiled version of this set specification.
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

		satisfierSpec = new CompiledSetSpec(var, cond);
		callStack.remove(this);
		return 0;
	}

	public Collection getSubExprs() {
		return Collections.singletonList(cond);
	}

	public Set getFreeVars() {
		Set freeVars = new HashSet(cond.getFreeVars());
		freeVars.remove(var);
		return Collections.unmodifiableSet(freeVars);
	}

	public ArgSpec getSubstResult(Substitution subst, Set<LogicalVar> boundVars) {
		boundVars = new HashSet<LogicalVar>(boundVars);
		boundVars.add(var);
		return new ImplicitSetSpec(var, (Formula) cond.getSubstResult(subst,
				boundVars));
	}

	/**
	 * Two implicit set specifications are equal if they have the same type,
	 * generic element variable, and condition. Two implicit set specifications
	 * that differ only in the choice of generic element variable are equivalent,
	 * but we do not consider them equal, just as we do not consider two
	 * universally quantified formulas equal if they differ in the quantified
	 * variable.
	 */
	public boolean equals(Object o) {
		if (o instanceof ImplicitSetSpec) {
			ImplicitSetSpec other = (ImplicitSetSpec) o;
			return (var.equals(other.getGenericSetElt()) && cond.equals(other
					.getCond()));
		}
		return false;
	}

	public int hashCode() {
		return (var.hashCode() ^ cond.hashCode());
	}

	/**
	 * Returns a string of the form {Type var : cond} where Type is this implicit
	 * set specification's type, var is the generic set element variable, and cond
	 * is the membership condition.
	 */
	public String toString() {
		return ("{" + var.getType() + " " + var.getName() + " : " + cond + "}");
	}

	/**
	 * Returns a compiled version of the set represented by this ImplicitSetSpec.
	 */
	protected CompiledSetSpec getSatisfierSpec() {
		if (satisfierSpec == null) {
			compile(new LinkedHashSet());
		}
		return satisfierSpec;
	}

	public ArgSpec find(Term t) {
		if (var.equals(t))
			return var;
		return cond.find(t);
	}

	public void applyToTerms(UnaryProcedure procedure) {
		var.applyToTerms(procedure);
		cond.applyToTerms(procedure);
	}

	public ArgSpec replace(Term t, ArgSpec another) {
		if (t.equals(var)) // variable is separately quantified.
			return this;

		Formula newCond = (Formula) cond.replace(t, another);
		if (newCond != cond) {
			ImplicitSetSpec result = new ImplicitSetSpec(var, newCond);
			if (satisfierSpec != null)
				result.compile(new LinkedHashSet());
			return result;
		}
		return this;
	}

	private LogicalVar var;
	private Formula cond;

	private CompiledSetSpec satisfierSpec;
}
