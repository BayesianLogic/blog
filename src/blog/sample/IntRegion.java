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

	/*
	 * (non-Javadoc)
	 * 
	 * @see blog.sample.Region#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return low > high;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see blog.sample.Region#isSingleton()
	 */
	@Override
	public boolean isSingleton() {
		return low == high;
	}

	private int low;
	private int high;

	/**
	 * create a region over integers; [low, high]
	 * 
	 * @param low
	 * @param high
	 */
	public IntRegion(int low, int high) {
		// valueset = new IntervalsSet(low, high);
		this.low = low;
		this.high = high;
	}

	public IntRegion(int high) {
		this(high, high);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see blog.sample.Region#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(Object value) {
		if ((value instanceof Integer) || (value instanceof Long)) {
			int num = ((Number) value).intValue();
			return (num <= high) && (num >= low);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see blog.sample.Region#getOneValue()
	 */
	@Override
	public Object getOneValue() {
		return low;
	}

	public long getMin() {
		return low;
	}

	public long getMax() {
		return high;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ("[" + low + "," + high + "]");
	}
}
