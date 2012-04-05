package blog.common;

/**
 * An object encapsulating another object. This is useful when closures are
 * using an object, thus requiring it to be final, but we know we can change it
 * anyway.
 */
public class Mutable {
	public Object object;
}
