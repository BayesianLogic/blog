package blog.common;

/** The identity function. */
public class IdentityFunction implements UnaryFunction {
	public Object evaluate(Object o) {
		return o;
	}

	/** Returns a shared instance of this class. */
	public static IdentityFunction getInstance() {
		if (instance == null)
			instance = new IdentityFunction();
		return instance;
	}

	private static IdentityFunction instance;
}
