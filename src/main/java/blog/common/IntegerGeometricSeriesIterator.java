package blog.common;

/** A geometric series rounded to the next integer. */
public class IntegerGeometricSeriesIterator extends EZIterator {
	public IntegerGeometricSeriesIterator(int start, int end, float rate) {
		currentElement = start;
		this.end = end;
		floatIt = new GeometricSeriesIterator(start, end, rate);
	}

	protected Object calculateNext() {
		if (!floatIt.hasNext())
			return null;
		Float nextFloat = (Float) floatIt.next();
		return Math.round(nextFloat.floatValue());
	}

	private float currentElement;
	private float end;
	private GeometricSeriesIterator floatIt;
}
