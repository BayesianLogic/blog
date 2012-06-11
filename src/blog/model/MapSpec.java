package blog.model;

import java.util.*;

/**
 * ArgSpec that represents a map statement, such as 
 * {Blue -> 0.5, Green -> 0.5}.
 *  For now, only supports fixed parameters. 
 *
 *  @author amatsukawa
 */

public class MapSpec extends ArgSpec {

    Map<String, ArgSpec> map;
    boolean compiled;

    /** 
     * Create empty ArgSpec,
     *  Add Elements to it later on.
     */ 
    public MapSpec() {
        this.map = new HashMap<String, ArgSpec>();
        compiled = false;
    }

    public MapSpec(Map<String, ArgSpec> m) {
        this.map = m;
        compiled = false;
    }

    /**
     * TODO: Need to check correctness
     *
     * To compile a map, just compile each of its possible values
     */
    public int compile(LinkedHashSet callStack) {
        compled = true;
        callStack.add(this);
        int errors = 0;
        if (map.isEmpty()) {
            System.err.println("Map expression is empty");
            errors = 1;
        } else {
            for(ArgSpec arg : map.values()) {
                errors += arg.compile(callStack);
            }
        }
        callStack.remove(this);
        return errors;
    }

    public Object evaluate(EvalContext context) {
        //  TODO: implement
        return null;
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
    public abstract ArgSpec find(Term t);

    /**
     * Applies a procedure to all terms in this ArgSpec which satisfy a given
     * predicate to a given collection.
     */
    public abstract void applyToTerms(UnaryProcedure procedure);

    /**
     * Returns an ArgSpec resulting from the replacement of all occurrences of a
     * term by another, if there is any, or self. A new ArgSpec is compiled if
     * this is compiled.
     */
    public abstract ArgSpec replace(Term t, ArgSpec another);

    /**
     * Returns the result of applying the substitution <code>subst</code> to this
     * expression, excluding the logical variables in <code>boundVars</code>. This
     * method is used for recursive calls. The set <code>boundVars</code> should
     * contain those variables that are bound in the syntax tree between this
     * sub-expression and the top-level expression to which the substitution is
     * being applied.
     */
    public abstract ArgSpec getSubstResult(Substitution subst,
            Set<LogicalVar> boundVars);
}