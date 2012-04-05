package blog.common;

import java.util.*;

/**
 * An extension of {@link TreeMap<String, Object>} that is able to keep track
 * and manage dependencies between its entries. This is useful if we want to use
 * the map as a cache for multiple variables and want to make sure that all
 * stored values are valid and consistent. If a variable v1 depends on v2 and v2
 * is changed, we want v1 to be removed so it is recalculated next time it is
 * used.
 * <p>
 * This is guaranteed by, every time a function on variables is calculated, this
 * being done so via the method {@link #getResultOrRecompute(DAEFunction)},
 * where the {@link DAEFunction} provided recalculates the quantity from the
 * environment, which is passed as its only argument. If a function's
 * {@link DAEFunction#isRandom()} returns <code>true</code>, or an ancestor
 * function is random, it is always recomputed.
 * <p>
 * The environment keeps track of dependencies and only recalculates functions
 * when this is really needed.
 * 
 * @author Rodrigo
 */
public class DependencyAwareEnvironment extends TreeMap<String, Object> {

	public DependencyAwareEnvironment() {
	}

	public DependencyAwareEnvironment(String variable, Object value) {
		super.put(variable, value);
	}

	/**
	 * Returns the result of the evaluation of a {@link DAEFunction} on the
	 * current environment, only actually evaluating it if the variables it
	 * depends on have been changed since the last evaluation (during which
	 * invocations of {@link #get(String)} are monitored in order to determine
	 * such dependencies).
	 */
	public Object getResultOrRecompute(DAEFunction function) {
		String variable = function.toString();

		if (function.isRandom())
			randomVariables.add(variable);

		if (!randomVariables.contains(variable) && containsKey(variable))
			return super.get(variable);

		startingCalculation(variable);
		Object value = function.evaluate(this);
		finishedCalculation(variable, value); // this sets variable to value.
		return get(variable); // calling get ensures that variable is recorded as
													// parent of whatever is being watched now.
	}

	private void startingCalculation(String variable) {
		watchedVariables.push(variable);
	}

	public Object get(String variable) {
		if (!watchedVariables.isEmpty()) {
			children.add(variable, watchedVariables.peek());
			// System.out.println(watchedVariables.peek() + " recorded as child of " +
			// variable);
			if (randomVariables.contains(variable))
				randomVariables.add(watchedVariables.peek());
		}
		return super.get(variable);
	}

	private void finishedCalculation(String variable, Object value) {
		if (!watchedVariables.isEmpty()
				&& !watchedVariables.peek().equals(variable))
			Util.fatalError("DependencyAwareEnvironment.finishedCalculation called on "
					+ variable
					+ " when "
					+ watchedVariables.peek()
					+ " is the currently calculated variable.");
		put(variable, value);
		watchedVariables.pop();
	}

	public Object put(String variable, Object value) {
		// System.out.println("Setting " + variable + " to " + value);
		Object previousValue = super.get(variable);
		if ((value == null && previousValue == null) || value.equals(previousValue))
			return value;
		removeChildrenOf(variable);
		return super.put(variable, value);
	}

	public void remove(String variable) {
		removeChildrenOf(variable);
		super.remove(variable);
	}

	protected void removeChildrenOf(String variable) {
		for (String child : ((Collection<String>) children.get(variable))) {
			// System.out.println("Removing child " + child + " of " + variable);
			remove(child);
		}
		children.remove(variable);
	}

	/** Map from variables to set of variables depending on it. */
	private MultiMap children = new HashMultiMap();
	private Set randomVariables = new HashSet();
	private Stack<String> watchedVariables = new Stack<String>();

	// /////////// CONVENIENCE METHODS ///////////////

	public Object getWithDefault(String variable, Object defaultValue) {
		Object result = get(variable);
		if (result == null)
			return defaultValue;
		return result;
	}

	public int getInt(String variable) {
		return ((Integer) get(variable)).intValue();
	}
}
