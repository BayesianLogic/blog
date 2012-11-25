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
	
	/**
	 * Returns a row of the matrix as specified
	 * 
	 * @param i the index of the row
	 */
	public MatrixLib sliceRow(int i);
	
	/**
	 * Returns the sum of this matrix with the one provided
	 */
	public MatrixLib plus(MatrixLib otherMat);
	
	/**
	 * Returns the difference of this matrix with the one provided
	 */
	public MatrixLib minus(MatrixLib otherMat);
	
	/**
	 * Returns the scalar product of this matrix with the given value
	 */
	public MatrixLib timesScale(double scale);
	
	/**
	 * Returns the matrix product of this matrix with the one provided
	 */
	public MatrixLib timesMat(MatrixLib otherMat);
	
	/**
	 * Returns the determinant of this matrix
	 */
	public double det();
	
	/**
	 * Returns the transpose of this matrix
	 */
	public MatrixLib transpose();
	
	/**
	 * Returns the inverse of this matrix
	 */
	public MatrixLib inverse();
	
	/**
	 * Returns a lower triangular matrix representing the Cholesky
	 * decomposition of this matrix 
	 */
	public MatrixLib choleskyFactor();
}
