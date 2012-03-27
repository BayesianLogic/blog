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
import common.Util;

/**
 * Represents a statement that a certain term has a certain value. Such a
 * statement is of the form <i>term</i> = <i>value</i>. Currently the values are
 * restricted to be zero-ary functions, so we don't have to worry about
 * evaluating arguments on the righthand side.
 */
public class ValueEvidenceStatement {

	/**
	 * Creates a ValueEvidenceStatement of the form: <code>leftSide</code> =
	 * <code>output</code>.
	 * 
	 * @param leftSide
	 *          the argspec whose value is being specified.
	 * 
	 * @param output
	 *          a argspec denoting the value
	 */
	public ValueEvidenceStatement(ArgSpec leftSide, ArgSpec output) {
		this.leftSide = leftSide;
		this.output = output;
	}

	public ArgSpec getLeftSide() {
		return leftSide;
	}

	public ArgSpec getOutput() {
		return output;
	}

	/**
	 * Returns the observed variable.
	 * 
	 * @throws IllegalStateException
	 *           if <code>compile</code> has not yet been called.
	 */
	public BayesNetVar getObservedVar() {
		if (observedVar == null) {
			throw new IllegalStateException("Evidence statement has not "
					+ "been compiled yet.");
		}
		return observedVar;
	}

	/**
	 * Returns the observed value.
	 * 
	 * @throws IllegalStateException
	 *           if <code>compile</code> has not yet been called.
	 */
	public Object getObservedValue() {
		if (observedValue == null) {
			throw new IllegalStateException("Evidence statement has not "
					+ "been compiled yet.");
		}
		return observedValue;
	}

	/**
	 * Returns true if this statement satisfies type and scope constraints. If
	 * there is a type or scope error, prints a message to standard error and
	 * returns false.
	 */
	public boolean checkTypesAndScope(Model model) {
		Map scope = Collections.EMPTY_MAP;

		ArgSpec leftSideInScope = leftSide.getArgSpecInScope(model, scope);
		if (leftSideInScope == null) {
			return false;
		}
		leftSide = leftSideInScope;

		ArgSpec outputInScope = output.getArgSpecInScope(model, scope);
		if (outputInScope == null) {
			return false;
		}
		output = outputInScope;

		if (leftSide instanceof Term && output instanceof Term) {
			// TODO: decide whether to have ArgSpec be typed as well.
			Type left = ((Term) leftSide).getType();
			Type right = ((Term) output).getType();
			if ((left != null) && (right != null) && !right.isSubtypeOf(left)) {
				System.err.println("Term " + leftSide + ", of type " + left
						+ ", cannot take value " + output + ", which has type " + right);
				return false;
			}
		}

		return true;
	}

	/**
	 * Compiles both sides of this evidence statement, and initializes the
	 * observed variable and value.
	 */
	public int compile(LinkedHashSet callStack) {
		compiled = true;

		int errors = 0;

		errors += leftSide.compile(callStack);
		errors += output.compile(callStack);

		Object leftValue = leftSide.getValueIfNonRandom();
		Object rightValue = output.getValueIfNonRandom();
		if (rightValue != null) {
			if (leftValue != null) {
				if (leftValue.equals(rightValue)) {
					System.out.println("Note: evidence \"" + this + "\" is "
							+ "vacuous because both sides are "
							+ "non-random and have same value.");
				} else {
					Util.fatalError("Evidence asserts that " + leftSide
							+ ", which has the non-random value " + leftValue + " ("
							+ leftValue.getClass() + "), is equal to " + output
							+ ", which has the distinct non-random " + "value " + rightValue
							+ " (" + rightValue.getClass() + ").", false);
				}
			}

			// This statement is saying that a particular variable is
			// equal to rightValue.
			observedVar = leftSide.getVariable();
			observedValue = rightValue;
		} else {
			// We need to let the observed variable be an equality
			// sentence, and the observed value be TRUE.
			if (!(leftSide instanceof Term)) {
				Util.fatalError("Boolean value evidence must be a term.");
			}
			Formula eq = new EqualityFormula((Term) leftSide, (Term) output);
			errors += eq.compile(callStack);
			observedVar = new DerivedVar(eq);
			observedValue = Boolean.TRUE;
		}

		return errors;
	}

	/**
	 * Returns true if the given partial world is complete enough to determine
	 * whether this evidence statement is true or not.
	 */
	public boolean isDetermined(PartialWorld w) {
		return observedVar.isDetermined(w);
	}

	/**
	 * Returns true if, in this function evidence statement, the function
	 * application term and the output constant symbol have the same denotation in
	 * the given world.
	 */
	public boolean isTrue(PartialWorld w) {
		return (observedVar.getValue(w).equals(observedValue));
	}

	/**
	 * Returns an object whose toString method yields a description of the
	 * location where this statement occurred in an input file.
	 */
	public Object getLocation() {
		return leftSide.getLocation();
	}

	/**
	 * Returns a ValueEvidenceStatement resulting from replacing a term by another
	 * in this ValueEvidenceStatement, or same if there is no replacement.
	 */
	public ValueEvidenceStatement replace(Term t, ArgSpec another) {
		Term newLeftSide = (Term) leftSide.replace(t, another);
		Term newOutput = (Term) output.replace(t, another);
		if (newOutput != leftSide || newOutput != output) {
			ValueEvidenceStatement newVES = new ValueEvidenceStatement(newLeftSide,
					newOutput);
			if (compiled)
				newVES.compile(new LinkedHashSet());
			return newVES;
		}
		return this;
	}

	public String toString() {
		if (observedVar == null) {
			return (leftSide + " = " + output);
		}
		return (observedVar + " = " + observedValue);
	}

	private ArgSpec leftSide;
	private ArgSpec output;
	private boolean compiled = false;

	private BayesNetVar observedVar;
	private Object observedValue;

}
