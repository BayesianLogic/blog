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

	/**
	 * check whether the particular value is in the region of this region
	 * 
	 * @param value
	 * @return
	 */
	public boolean contains(Object value) {
		// default region contains everything
		return true;
	}

}
