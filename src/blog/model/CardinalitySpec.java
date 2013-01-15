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

package blog.model;

import java.util.*;

import blog.common.Multiset;
import blog.common.UnaryProcedure;
import blog.objgen.ObjectSet;
import blog.sample.EvalContext;


/**
 * Argument specifier that refers to the size of an implicitly defined set.
 */
public class CardinalitySpec extends ArgSpec {
	/**
	 * Creates a new CardinalitySpec that counts the elements in the given
	 * implicitly defined set.
	 */
	public CardinalitySpec(ImplicitSetSpec setSpec) {
		this.setSpec = setSpec;
	}

	public ImplicitSetSpec getSetSpec() {
		return setSpec;
	}

	public Object evaluate(EvalContext context) {
		context.pushEvaluee(this);

		// Note that we don't evaluate the underlying ImplicitSetSpec,
		// because that requires the elements of the set to be determined.
		// We only care about the size of the set.
		ObjectSet set = (ObjectSet) setSpec.getSatisfierSpec().elementSet(context);
		Integer result = null;
		if (set.canDetermineSize()) {
			result = new Integer(set.size());
		}

		context.popEvaluee();
		return result;
	}

	public boolean containsRandomSymbol() {
		return true; // the type symbol
	}

	public boolean checkTypesAndScope(Model model, Map scope) {
		return setSpec.checkTypesAndScope(model, scope);
	}

	/**
	 * Compiles the set specification in this CardinalitySpec.
	 * 
	 * @param callStack
	 *          Set of objects whose compile methods are parents of this method
	 *          invocation. Ordered by invocation order. Used to detect cycles.
	 */
	public int compile(LinkedHashSet callStack) {
		callStack.add(this);
		int errors = setSpec.compile(callStack);
		callStack.remove(this);
		return errors;
	}

	public boolean isNumeric() {
		return true;
	}

	public Collection getSubExprs() {
		return Collections.singletonList(setSpec);
	}

	public ArgSpec getSubstResult(Substitution subst, Set<LogicalVar> boundVars) {
		return new CardinalitySpec((ImplicitSetSpec) setSpec.getSubstResult(subst,
				boundVars));
	}

	public boolean equals(Object o) {
		if (o instanceof CardinalitySpec) {
			CardinalitySpec other = (CardinalitySpec) o;
			return setSpec.equals(other.getSetSpec());
		}
		return false;
	}

	public int hashCode() {
		return setSpec.hashCode();
	}

	public String toString() {
		return ("#" + setSpec);
	}

	public ArgSpec find(Term t) {
		return setSpec.find(t);
	}

	public void applyToTerms(UnaryProcedure procedure) {
		setSpec.applyToTerms(procedure);
	}

	public ArgSpec replace(Term t, ArgSpec another) {
		ArgSpec newSetSpec = setSpec.replace(t, another);
		if (newSetSpec != setSpec)
			return new CardinalitySpec((ImplicitSetSpec) newSetSpec);
		return this;
	}

	private ImplicitSetSpec setSpec;
}
