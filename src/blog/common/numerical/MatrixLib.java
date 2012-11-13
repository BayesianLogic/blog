package blog.common.numerical;

/**
 * Exposes different matrix libraries to BLOG using a consistent set of
 * methods.  Different libraries may be used with BLOG without significant
 * code modifications.
 * 
 * @author awong
 * @date November 5, 2012
 */
public interface MatrixLib {	
	/**
	 * Gives the value of an element of this matrix
	 * 
	 * @param x the x-index
	 * @param y the y-index
	 * @return 
	 */
	public double elementAt(int x, int y);
	
	/**
	 * Returns the contents of this matrix
	 */
	public String toString();
	
	/**
	 * Returns number of rows in this matrix
	 */
	public int rowLen();
	
	/**
	 * Returns number of columns in this matrix
	 */
	public int colLen();
}
