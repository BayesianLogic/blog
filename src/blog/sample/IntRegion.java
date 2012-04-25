/**
 * 
 */
package blog.sample;

/**
 * check whether a value is within the region
 * 
 * @author leili
 * @since Apr 24, 2012
 * @see Region
 */
public class IntRegion extends Region {

	private long low;
	private long high;

	/**
	 * create a region over integers; [low, high]
	 * 
	 * @param low
	 * @param high
	 */
	public IntRegion(long low, long high) {
		// valueset = new IntervalsSet(low, high);
		this.low = low;
		this.high = high;
	}

	public IntRegion(int high) {
		this(0, high);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see blog.sample.Region#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(Object value) {
		if (value instanceof Integer) {
			long num = ((Number) value).longValue();
			return (num <= high) && (num >= low);
		}
		return false;
	}
}
