package common;

/**
 * A UnaryFunction receiving a {@link DependencyAwareEnvironment} object. This
 * allows the user to write an <code>evaluate</code> method taking an already
 * typed parameter, avoiding a cast.
 * 
 * @author Rodrigo
 */
public abstract class DAEFunction implements UnaryFunction {

	public Object evaluate(Object x) {
		return evaluate((DependencyAwareEnvironment) x);
	}

	public abstract Object evaluate(DependencyAwareEnvironment environment);

	public boolean isRandom() {
		return false;
	}
}
