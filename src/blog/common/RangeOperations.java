package blog.common;

import java.util.*;

/**
 * <code>RangeOperations</code> is a facility for easily writing and efficiently
 * computing expressions with nested cumulative operators (for example,
 * <code>\sum_i \prod_j f(i,j)</code>).
 * <p>
 * It allows easy writing of these expressions through its method
 * {@link #run(Object...)}, best explained by example:
 * <p>
 * <code>
 * Object result = RangeOperations.run(Averaging("x", 0, 10), X());
 * </code>
 * <p>
 * computes the average of integers from 0 to 10.
 * <p>
 * {@link #run(Object...)} receives a variable number of arguments, which can
 * be, among other types (see below), {@link RangeOperation} and
 * {@link DAEFunction}. {@link #Averaging()} is a method returning an instance
 * of {@link #Averaging}, an extension of {@link RangeOperation}. {@link #X()}
 * is a method returning an instance of {@link #X}, an extension of
 * {@link DAEFunction} returning the value of variable <code>"x"</code> in an
 * implicit environment (of type {@link DependencyAwareEnvironment}) which is
 * passed to the {@link DAEFunction}.
 * <p>
 * Therefore, <code>run(Averaging("x", 0, 10), X())</code> denotes an operation
 * setting variable <code>"x"</code> to values from 0 to 10 and averaging over
 * them, and returns that value.
 * <p>
 * Another example is:
 * <p>
 * <code>
 * Object result = RangeOperations.run(Axis("x", 0, 20), Averaging("y", 0, 10), new F());
 * </code>
 * <p>
 * computes a list with 21 elements, where the x-th element contains the
 * average, over values of <code>"y"</code> from 0 to 10, of <code>F()</code>
 * evaluated on an environment with current values for <code>"x"</code> and
 * <code>"y"</code>.
 * <p>
 * An arbitrary number of range operations can be used, and will be performed in
 * the order they are given. User-defined extensions of {@link RangeOperation}
 * and {@link DAEFunction} can be used as well. See the documentation on
 * {@link RangeOperation} for details on how to use it.
 * <p>
 * An important feature of this framework is automatic caching. Suppose
 * <code>F</code> above only depends on x. It would then be wasteful to
 * recalculate it for every new value of y, which is being used as a counter
 * only. This does not happen, however, because
 * {@link DependencyAwareEnvironment} keeps track of such dependencies
 * automatically. IMPORTANT: for this automatic dependency management to occur
 * even for sub-functions inside F, they must be calculated with the
 * {@link DependencyAwareEnvironment#getResultOrRecompute(DAEFunction)} method.
 * The function will always be recomputed if {@link DAEFunction#isRandom()}
 * returns <code>true</code>, or an ancestor function is random.
 * <p>
 * If {@link #run(Object...)} receives Strings as arguments, they are assumed to
 * be variables to be put in the environment with the object right after them as
 * value. If an {@link DependencyAwareEnvironment} is found, it replaces the
 * default (initially empty) environment, removing previous variable values.
 * Subsequent variables are added to this environment.
 * <p>
 * As a convenience, this class already provides a few {@link RangeOperation}
 * extensions: {@link #Averaging(String, int, int, int)},
 * {@link #Axis(String, int, int, int)} and {@link #Summation(String, int, int)}.
 * 
 * @author Rodrigo
 */
public class RangeOperations {

	// /////////////////// CLASS CORE ///////////////////

	/**
	 * See class ({@link RangeOperations}) documentation.
	 */
	public static Object run(Object... args) {
		List rangeOperations = new LinkedList();
		DependencyAwareEnvironment environment = new DependencyAwareEnvironment();
		DAEFunction function = null;
		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];
			if (arg instanceof RangeOperation)
				rangeOperations.add((RangeOperation) arg);
			else if (arg instanceof DependencyAwareEnvironment)
				environment = (DependencyAwareEnvironment) arg;
			else if (arg instanceof String) {
				String variable = (String) arg;
				Object value = args[++i];
				environment.put(variable, value);
			} else if (arg instanceof DAEFunction)
				function = (DAEFunction) arg;
		}

		return (new RangeOperations(environment, rangeOperations, function)).run();
	}

	public static Object run(List<? extends RangeOperation> rangeOperations,
			DAEFunction function) {
		return (new RangeOperations(new DependencyAwareEnvironment(),
				rangeOperations, function)).run();
	}

	public RangeOperations(DependencyAwareEnvironment environment,
			List<? extends RangeOperation> rangeOperations, DAEFunction function) {
		this.environment = environment;
		this.rangeOperations = new ArrayList(rangeOperations);
		this.function = function;
		for (RangeOperation range : rangeOperations)
			range.getRange().setEnvironment(environment);
	}

	protected Object run() {
		return run(0);
	}

	protected Object run(int i) {
		if (i == rangeOperations.size())
			return environment.getResultOrRecompute(function);

		RangeOperation rangeOp = rangeOperations.get(i);
		rangeOp.getOperator().initialize();
		for (rangeOp.initialize(); rangeOp.hasNext();) {
			rangeOp.next();
			Object subresult = run(i + 1);
			rangeOp.getOperator().increment(subresult);
		}
		return rangeOp.getOperator().getResult();
	}

	/**
	 * Represents a cumulative operator by defining initializing and incrementing
	 * functions.
	 */
	public static interface Operator {
		/** Prepares operator for another round of cumulative operations. */
		public void initialize();

		/** Returns current result. */
		public Object getResult();

		/** Increments result by another value. */
		public void increment(Object value);
	}

	/**
	 * A basic implementation of {@link Operator} keeping a field for the current
	 * result.
	 */
	public static abstract class AbstractOperator implements Operator {
		public Object getResult() {
			return result;
		}

		protected Object result;
	}

	/**
	 * A range must be a {@link NullaryFunction} producing a new iterator, keep a
	 * {@link DependencyAwareEnvironment} set by
	 * {@link #setEnvironment(DependencyAwareEnvironment)} that is updated by its
	 * method {@link #next()}, and receive {@link BinaryProcedure} listeners to be
	 * called with variable and value pairs whenever {@link #next()} is invoked.
	 */
	public static interface Range extends NullaryFunction {
		/** The variable set by this range. */
		public String getName();

		/** Informs the range which environment to use. */
		public void setEnvironment(DependencyAwareEnvironment environment);

		/** Makes range ready for iteration over variable values. */
		public void initialize();

		/** Indicates that there are still values to be iterated over. */
		public boolean hasNext();

		/** Go to the next value. */
		public void next();

		/**
		 * Add a binary procedure to be invoke upon iteration, with variable and
		 * value as parameters.
		 */
		public void addIterationListener(BinaryProcedure listener);
	}

	/**
	 * Provides basic Range functionality, only leaving to the user the task of
	 * defining {@link #evaluate()}, which should provide a new iterator over a
	 * range of values.
	 */
	public static abstract class AbstractRange implements Range {
		/** Builds a range with a given variable name. */
		public AbstractRange(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setEnvironment(DependencyAwareEnvironment environment) {
			this.environment = environment;
		}

		public void initialize() {
			iterator = (Iterator<?>) evaluate();
		}

		public boolean hasNext() {
			return iterator.hasNext();
		}

		public void next() {
			Object value = iterator.next();
			environment.put(name, value);
			for (BinaryProcedure listener : listeners)
				listener.evaluate(name, value);
		}

		public void addIterationListener(BinaryProcedure listener) {
			listeners.add(listener);
		}

		protected String name;
		protected Iterator<?> iterator;
		protected DependencyAwareEnvironment environment;
		protected Collection<BinaryProcedure> listeners = new LinkedList<BinaryProcedure>();
	}

	/**
	 * A combination of {@link Operator} and {@link Range}.
	 */
	public static class RangeOperation {
		public RangeOperation(Operator operator, Range range) {
			this.operator = operator;
			this.range = range;
		}

		public RangeOperation(Range range) {
			this(new Concatenate(), range);
		}

		public Range getRange() {
			return range;
		}

		public Operator getOperator() {
			return operator;
		}

		public void setEnvironment(DependencyAwareEnvironment environment) {
			getRange().setEnvironment(environment);
		}

		public void initialize() {
			getRange().initialize();
		}

		public boolean hasNext() {
			return getRange().hasNext();
		}

		public void next() {
			getRange().next();
		}

		protected Operator operator;
		protected Range range;

	}

	protected ArrayList<? extends RangeOperation> rangeOperations;
	protected DAEFunction function;
	protected DependencyAwareEnvironment environment;

	// ////////////////// END OF CLASS CORE ///////////////////

	// ////////////////// CONVENIENCE CLASSES AND METHODS ///////////////////

	/** A range over integer values. */
	public static class IntegerRange extends AbstractRange {
		public IntegerRange(String name, int first, int last, int step) {
			super(name);
			this.first = first;
			this.last = last;
			this.step = step;
		}

		public IntegerRange(String name, int first, int last) {
			this(name, first, last, 1);
		}

		public Object evaluate() {
			return new IntegerArithmeticSeriesIterator(first, last, step);
		}

		private int first, last, step;
	}

	/** A range over a discrete set of values. */
	public static class DiscreteRange extends AbstractRange {
		public DiscreteRange(String name, Collection collection) {
			super(name);
			this.collection = collection;
		}

		public Object evaluate() {
			return collection.iterator();
		}

		private Collection collection;
	}

	/** A geometric range over integer values. */
	public static class GeometricIntegerRange extends AbstractRange {
		public GeometricIntegerRange(String name, int first, int last, float rate) {
			super(name);
			this.first = first;
			this.last = last;
			this.rate = rate;
		}

		public Object evaluate() {
			return new IntegerGeometricSeriesIterator(first, last, rate);
		}

		private int first, last;
		private float rate;
	}

	public static class IntegerRangeOperation extends RangeOperation {
		public IntegerRangeOperation(Operator operator, String name,
				final int first, final int last, final int step) {
			super(operator, new IntegerRange(name, first, last));
		}

		public IntegerRangeOperation(Operator operator, String name,
				final int first, final int last) {
			this(operator, name, first, last, 1);
		}
	}

	public static class Summation extends RangeOperation {
		public Summation(String name, final int first, final int last,
				final int step) {
			super(new Sum(), new IntegerRange(name, first, last));
		}

		public Summation(String name, final int first, final int last) {
			this(name, first, last, 1);
		}
	}

	/**
	 * An averaging operation over a range, valid over numbers or arbitrarily
	 * nested lists of numbers.
	 */
	public static class Averaging extends RangeOperation {
		public Averaging(Range range) {
			super(new Average(), range);
		}

		public Averaging(String name, final int first, final int last,
				final int step) {
			super(new Average(), new IntegerRange(name, first, last));
		}

		public Averaging(String name, final int first, final int last) {
			this(name, first, last, 1);
		}
	}

	/**
	 * An averaging operation over a range, valid over numbers or arbitrarily
	 * nested lists of numbers, ignoring items not satisfying a given predicate.
	 */
	public static class PredicatedAveraging extends RangeOperation {
		public PredicatedAveraging(Range range, UnaryPredicate predicate) {
			super(new PredicatedAverage(predicate), range);
		}

		public PredicatedAveraging(String name, UnaryPredicate predicate,
				final int first, final int last, final int step) {
			super(new PredicatedAverage(predicate), new IntegerRange(name, first,
					last));
		}

		public PredicatedAveraging(String name, UnaryPredicate predicate,
				final int first, final int last) {
			this(name, predicate, first, last, 1);
		}
	}

	/** An operation concatenating values into a list, with {@link Concatenate}. */
	public static class Axis extends RangeOperation {
		public Axis(Range range) {
			super(range);
		}

		public Axis(String name, final int first, final int last, final int step) {
			super(new IntegerRange(name, first, last, step));
		}

		public Axis(String name, final int first, final int last) {
			this(name, first, last, 1);
		}

		/** Creates an axis on a geometric integer series. */
		public Axis(String name, final int first, final int last, final float rate) {
			super(new GeometricIntegerRange(name, first, last, rate));
		}

		/** Creates an axis on a discrete range. */
		public Axis(String name, Collection collection) {
			super(new DiscreteRange(name, collection));
		}
	}

	/** Cumulative operator concatenating values in a list. */
	protected static class Concatenate extends AbstractOperator {
		public void initialize() {
			result = Util.list();
		}

		public void increment(Object value) {
			((List) result).add(value);
		}
	}

	protected static class Sum extends AbstractOperator {
		public void initialize() {
			result = 0;
		}

		public void increment(Object another) {
			result = ((Number) result).doubleValue()
					+ ((Number) another).doubleValue();
		}
	}

	protected static class Average extends AbstractOperator {
		public void initialize() {
			result = 0;
			weight = 0;
		}

		public void increment(Object another) {
			if (weight == 0) {
				result = another;
				weight = 1;
			} else {
				result = Util.incrementalComponentWiseAverageArbitraryDepth(result,
						weight, another);
				weight++;
			}
		}

		protected int weight = 0;
	}

	/**
	 * Similar to {@link Average} but taking a predicate selecting items to be
	 * considered in average.
	 */
	protected static class PredicatedAverage extends AbstractOperator {
		public PredicatedAverage(UnaryPredicate predicate) {
			this.predicate = predicate;
		}

		public void initialize() {
			result = 0;
			weight = 0;
		}

		public void increment(Object another) {
			if (!predicate.evaluate(another))
				return;

			if (weight == 0) {
				result = another;
				weight = 1;
			} else {
				result = Util.incrementalComponentWiseAverageArbitraryDepth(result,
						weight, another);
				weight++;
			}
		}

		protected int weight = 0;
		protected UnaryPredicate predicate;
	}

	/** Convenience method for constructing an {@link Axis}. */
	public static Axis Axis(Range range) {
		return new Axis(range);
	}

	/** Convenience method for constructing an {@link Axis}. */
	public static Axis Axis(String name, int first, int last, int step) {
		return new Axis(name, first, last, step);
	}

	/** Convenience method for constructing an {@link Axis}. */
	public static Axis Axis(String name, int first, int last) {
		return new Axis(name, first, last);
	}

	/** Convenience method for constructing an {@link Axis}. */
	public static Axis Axis(String name, int first, int last, float rate) {
		return new Axis(name, first, last, rate);
	}

	/** Convenience method for constructing an {@link Axis}. */
	public static Axis Axis(String name, Collection collection) {
		return new Axis(name, collection);
	}

	/** Convenience method for constructing a {@link Summation}. */
	public static Summation Summation(String name, int first, int last, int step) {
		return new Summation(name, first, last, step);
	}

	/** Convenience method for constructing a {@link Summation}. */
	public static Summation Summation(String name, int first, int last) {
		return new Summation(name, first, last);
	}

	/** Convenience method for constructing an {@link Averaging}. */
	public static Averaging Averaging(Range range) {
		return new Averaging(range);
	}

	/** Convenience method for constructing an {@link Averaging}. */
	public static Averaging Averaging(String name, int first, int last, int step) {
		return new Averaging(name, first, last, step);
	}

	/** Convenience method for constructing an {@link Averaging}. */
	public static Averaging Averaging(String name, int first, int last) {
		return new Averaging(name, first, last);
	}

	/** Convenience method for constructing an {@link PredicatedAveraging}. */
	public static PredicatedAveraging PredicatedAveraging(String name,
			UnaryPredicate predicate, int first, int last) {
		return new PredicatedAveraging(name, predicate, first, last);
	}

	// ////////////////// END OF CONVENIENCE CLASSES AND METHODS
	// ///////////////////
}
