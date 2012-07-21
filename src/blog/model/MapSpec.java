package blog.model;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import blog.Substitution;
import blog.common.UnaryProcedure;
import blog.sample.EvalContext;

/**
 * ArgSpec that represents a map statement, such as
 * {Blue -> 0.5, Green -> 0.5}.
 * For now, only supports fixed(nonrandom) objects.
 * 
 * @author amatsukawa
 * @author leili (2012/07/20 modified)
 */

public class MapSpec extends ArgSpec {

	Map<ArgSpec, ArgSpec> map;
	boolean compiled;

	/**
	 * Create empty ArgSpec,
	 * Add Elements to it later on.
	 */
	public MapSpec() {
		this.map = new HashMap<ArgSpec, ArgSpec>();
		compiled = false;
	}

	public MapSpec(Map<ArgSpec, ArgSpec> m) {
		// TODO need to check the type consistency
		this.map = m;
		compiled = false;
	}

	/**
	 * TODO: Need to check correctness
	 * 
	 * To compile a map, just compile each of its possible values
	 */
	public int compile(LinkedHashSet callStack) {
		compiled = true;
		callStack.add(this);
		int errors = 0;
		if (map.isEmpty()) {
			System.err.println("Map expression is empty");
			errors = 1;
		} else {
			for (ArgSpec arg : map.values()) {
				errors += arg.compile(callStack);
			}
		}
		callStack.remove(this);
		return errors;
	}

	/**
	 * 
	 */
	public Object evaluate(EvalContext context) {
		return map;
	}

	public Map<ArgSpec, ArgSpec> getMap() {
		return map;
	}

	/**
	 * For now, we only consider constant maps
	 */
	public boolean containsRandomSymbol() {
		return false;
	}

	public boolean checkTypesAndScope(Model model, Map scope) {
		// TODO: implement
		return false;
	}

	/**
	 * Returns term in ArgSpec equal to a given term t, or null if there isn't
	 * any.
	 */
	public ArgSpec find(Term t) {
		return null;
	}

	/**
	 * Applies a procedure to all terms in this ArgSpec which satisfy a given
	 * predicate to a given collection.
	 */
	public void applyToTerms(UnaryProcedure procedure) {
	}

	/**
	 * Returns an ArgSpec resulting from the replacement of all occurrences of a
	 * term by another, if there is any, or self. A new ArgSpec is compiled if
	 * this is compiled.
	 */
	public ArgSpec replace(Term t, ArgSpec another) {
		return null;
	}

	/**
	 * Returns the result of applying the substitution <code>subst</code> to this
	 * expression, excluding the logical variables in <code>boundVars</code>. This
	 * method is used for recursive calls. The set <code>boundVars</code> should
	 * contain those variables that are bound in the syntax tree between this
	 * sub-expression and the top-level expression to which the substitution is
	 * being applied.
	 */
	public ArgSpec getSubstResult(Substitution subst, Set<LogicalVar> boundVars) {
		return null;
	}
}