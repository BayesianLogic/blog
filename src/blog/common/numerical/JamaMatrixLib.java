package blog.common.numerical;

import Jama.Matrix;

public class JamaMatrixLib implements MatrixLib {
	
	private Matrix values;
	
	public JamaMatrixLib(double[][] contents) {
		values = new Matrix(contents);
	}

	@Override
	public double elementAt(int i, int j) {
		return values.get(i, j);
	}
	
	@Override
	public String toString() {
		return values.toString();
	}
	
	@Override
	public int rowLen() {
		return values.getRowDimension();
	}
	
	@Override
	public int colLen() {
		return values.getColumnDimension();
	}
}
