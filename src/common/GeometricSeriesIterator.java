package common;

/**
 * Implements an iterator going over a geometric series.
 * 
 * @author Rodrigo
 */
public class GeometricSeriesIterator extends EZIterator {
	public GeometricSeriesIterator(float initialElement, float limit, float rate) {
		currentElement = initialElement;
		this.limit = limit;
		this.rate = rate;
		next = currentElement;
		onNext = true;
	}

	protected Object calculateNext() {
		currentElement *= rate;
		if (currentElement > limit)
			return null;
		return currentElement;
	}

	public float currentElement;
	public float limit;
	public float rate;
}
