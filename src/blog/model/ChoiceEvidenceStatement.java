/*added by cheng*/

package blog.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;

import blog.bn.BayesNetVar;
import blog.bn.DerivedVar;
import blog.common.Util;
import blog.world.PartialWorld;

/**
 * Represents a statement that a certain term has a certain value. Such a
 * statement is of the form <i>term</i> = <i>value</i>. Currently the values are
 * restricted to be zero-ary functions, so we don't have to worry about
 * evaluating arguments on the righthand side.
 */
public class ChoiceEvidenceStatement {

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
	public ChoiceEvidenceStatement(FuncAppTerm leftSide, ArgSpec output) {
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

		if (leftSide instanceof Term) {
			Type left = ((Term) leftSide).getType();
			if (output instanceof Term) {
				Type right = ((Term) output).getType();
				if ((left != null) && (right != null) && !right.isSubtypeOf(left)) {
					System.err.println("Term " + leftSide + ", of type " + left
							+ ", cannot take value " + output + ", which has type " + right);
					return false;
				}
			} else if (left.isSubtypeOf(BuiltInTypes.ARRAY_REAL)) {
				if (output instanceof ListSpec) {
					output = ((ListSpec) output).transferToMatrix();
				} else if (output instanceof MatrixSpec) {
					// do nothing already good
				} else {
					System.err.println("Term " + leftSide + ", of type " + left
							+ ", cannot take value " + output + ", which has type unkown");
					return false;
				}
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
		
		/*added by cheng*/
		//if (leftValue == null)
		//	Util.fatalError("ChoiceEvidenceStatement.compile: " +
		//			"left side" + leftSide +" is not a fixed function, and will not work with obschoice");
		
		if (!(leftSide instanceof FuncAppTerm && ((FuncAppTerm) leftSide).getFunction() instanceof ChoiceFunction)){
			System.err.println("ChoiceEvidenceStatement.compile: only applicable to choice functions");
			System.exit(0);
		}
		if (rightValue != Boolean.TRUE){
			System.err.println("ChoiceEvidenceStatement.compile: misues of choicefunction for non-boolean evidence");
			System.exit(0);
		}
		observedVar = leftSide.getVariable();
		observedValue = rightValue;
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
		return (observedValue.equals(observedVar.getValue(w)));
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
	public ChoiceEvidenceStatement replace(Term t, ArgSpec another) {
		FuncAppTerm newLeftSide = (FuncAppTerm) leftSide.replace(t, another);
		Term newOutput = (Term) output.replace(t, another);
		if (newOutput != leftSide || newOutput != output) {
			ChoiceEvidenceStatement newVES = new ChoiceEvidenceStatement(newLeftSide,
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
