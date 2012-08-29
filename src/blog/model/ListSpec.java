package blog.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import blog.Substitution;
import blog.common.UnaryProcedure;
import blog.sample.EvalContext;

/**
 * ArgSpec that represents a list.  For now supports only constants
 * (distinct objects and literals of built-in types) and function names.
 * 
 * @author awong
 */

public class ListSpec extends ArgSpec {
	
	List<ArgSpec> elements;
	boolean compiled;
	
	public ListSpec() {
		elements = new ArrayList<ArgSpec>();
		compiled = false;
	}
	
	public ListSpec(List<ArgSpec> args) {
		elements = args;
		compiled = false;
	}
	
	/**
	 * To compile a list, it is only necessary to compile
	 * each of its possible values.
	 */
	public int compile(LinkedHashSet callStack) {
		compiled = true;
		callStack.add(this);
		int errors = 0;
		
		if (elements.isEmpty()) {
			System.err.println("List or array expression is empty");
			errors = 1;
		}
		else {
			for (ArgSpec listTerm: elements) {
				errors += listTerm.compile(callStack);
			}
		}
		
		callStack.remove(this);
		return errors;
	}

	@Override
	public Object evaluate(EvalContext context) {
		List<Object> evalContents = new ArrayList<Object>();
		for (ArgSpec element: elements) {
			evalContents.add(element.evaluate(context));
		}
		return evalContents;
	}

	/**
	 * Lists only handle distinct objects and built-in types for now.
	 */
	@Override
	public boolean containsRandomSymbol() {
		for (ArgSpec item: elements) {
			if (item.containsRandomSymbol()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean checkTypesAndScope(Model model, Map scope) {
		// Check typing of all symbols in the multiset
		for (ArgSpec obj: elements) {
			if (!obj.checkTypesAndScope(model, scope)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns term in ArgSpec equal to a given term t, or null if there isn't
	 * any.
	 */
	@Override
	public ArgSpec find(Term t) {
		for (ArgSpec listTerm: elements) {
			if (listTerm.equals(t)) {
				return listTerm;
			}
		}
		return null;
	}

	/**
	 * Applies a procedure to all terms in this ArgSpec which satisfy a given
	 * predicate to a given collection.
	 */
	@Override
	public void applyToTerms(UnaryProcedure procedure) {
	}

	/**
	 * Returns an ArgSpec resulting from the replacement of all occurrences of a
	 * term by another, if there is any, or self. A new ArgSpec is compiled if
	 * this is compiled.
	 */
	@Override
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
	@Override
	public ArgSpec getSubstResult(Substitution subst, Set<LogicalVar> boundVars) {
		return null;
	}
	
	public String toString() {
		return elements.toString();
	}
	
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	// TODO: write a better hash function because this one sucks
	public boolean equals(Object o) {
		return o.hashCode() == this.hashCode();
	}
}
