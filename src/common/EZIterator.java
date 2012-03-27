package common;

import java.util.*;

/**
 * An abstract class meant to make the implementation of iterators easier. One
 * needs only to extend it and define {@link #calculateNext()}, which must
 * calculate the next object in the sequence, or <code>null</code> if it is
 * over. EZIterator defines the Iterator interface based on this method (without
 * supporting the {@link #removeElement()} method.
 * 
 * @author Rodrigo
 */
public abstract class EZIterator implements Iterator {

	/**
	 * Method responsible for calculating next element in sequence, returning
	 * <code>null</code> if no such element exists.
	 */
	protected abstract Object calculateNext();

	private void ensureBeingOnNext() {
		if (!onNext) {
			next = calculateNext();
			onNext = true;
		}
	}

	public boolean hasNext() {
		ensureBeingOnNext();
		return next != null;
	}

	public Object next() {
		ensureBeingOnNext();
		if (next == null)
			throw new NoSuchElementException();
		onNext = false;
		return next;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	/** Field indicating if next element has already being computed. */
	protected boolean onNext = false;

	/** The next element if already computed. */
	protected Object next;
}