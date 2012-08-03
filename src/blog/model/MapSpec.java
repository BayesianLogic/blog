package blog.model;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
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
	
	Map<ArgSpec, Object> map;
	boolean compiled;
	
	// Until compilation, need these lists to store values
	List<ArgSpec> keys;
	List<Object> values;

	/**
	 * Create empty ArgSpec,
	 * Add Elements to it later on.
	 */
	public MapSpec() {
		this.map = new HashMap<ArgSpec, Object>();
		compiled = false;
	}

	public MapSpec(Map<ArgSpec, Object> m) {
		// TODO need to check the type consistency
		this.map = m;
		compiled = false;
	}
	
	public MapSpec(List<ArgSpec> objs, List<Object> probs) {
		this.map = new HashMap<ArgSpec, Object>();
		compiled = false;
		keys = objs;
		values = probs;
	}

	/**
	 * TODO: Need to check correctness
	 * 
	 * To compile a map, compile each of its possible values,
	 * then hash K/V pairs of objects and probs into map
	 */
	public int compile(LinkedHashSet callStack) {
		compiled = true;
		callStack.add(this);
		int errors = 0;
		if (keys.isEmpty()) {
			System.err.println("Map expression is empty");
			errors = 1;
		}
		else {
			for (ArgSpec arg : keys) {
				errors += arg.compile(callStack);
			}
			for (Object o: values) {
                if (o instanceof Term) {
                    Term t = (Term) o;
                    errors += t.compile(callStack);
                } else if (o instanceof Clause) {
                    Clause c = (Clause) o;
                    errors += c.compile(callStack);
                } else {
                    throw new IllegalArgumentException("Map values must " + 
                        "either be terms or clauses!");
                } 
			}
			
			for (int i = 0; i < keys.size(); i++) {
				map.put(keys.get(i), values.get(i));
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

	public Map<ArgSpec, Object> getMap() {
		return map;
	}

	/**
	 * For now, we only consider constant maps
	 */
	public boolean containsRandomSymbol() {
		return false;
	}

    public boolean checkTypesAndScope(Model model, Map scope) {
        return checkTypesAndScope(model, scope, null);
    }

	public boolean checkTypesAndScope(Model model, Map scope, Type childType) {
		// Check typing of all symbols in the map
		for (ArgSpec obj: keys) {
			if (!obj.checkTypesAndScope(model, scope)) {
				return false;
			}
		}

        for (Object val : values) {
            if (val instanceof Clause) {
                Clause c = (Clause) val;
                if (!c.checkTypesAndScope(model, scope, childType)) {
                    return false;
                }
            }
        }
		
		return true;
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
