/**
 * 
 */
package blog.sample;

/**
 * A class denote the possible range of sample values
 * 
 * @author leili
 * @since Apr 24, 2012
 * 
 */
public class Region {

	public static Region FULL_REGION = new Region();

	/**
	 * check whether the particular value is in the region of this region
	 * 
	 * @param value
	 * @return
	 */
	public boolean contains(Object value) {
		// default region contains everything
		return (value != null);
	}

	/**
	 * 
	 * @return whether the region is empty
	 * 
	 */
	public boolean isEmpty() {
		return false;
	}

	/**
	 * 
	 * @return true if the region contains only one point
	 */
	public boolean isSingleton() {
		return false;
	}

	/**
	 * 
	 * @return sample value from this possible region, it might be randomly
	 *         chosen
	 */
	public Object getOneValue() {
		return null;
	}

}
