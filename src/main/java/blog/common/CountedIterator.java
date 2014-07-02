package blog.common;

import java.util.Iterator;

/**
 * An iterator based on another iterator, but ranging over a given number of its
 * elements only.
 */
public class CountedIterator extends EZIterator {
	public CountedIterator(int numOfTimes, Iterator base) {
		this.numOfTimes = numOfTimes;
		this.base = base;
	}

	protected Object calculateNext() {
		if (counter > numOfTimes)
			return null;
		if (base.hasNext()) {
			counter++;
			return base.next();
		}
		return null;
	}

	private int counter = 0;
	private int numOfTimes;
	private Iterator base;
}
