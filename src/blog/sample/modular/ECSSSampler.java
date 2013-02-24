package blog.sample.modular;

public class ECSSSampler {

	public static void main(String[] args) {
		double[][] prob = new double[5][7];
		double p = 1.0/6;
		prob[0] = new double[]{0, p, p, p, p, p, p};
		prob[1] = new double[]{0, p, p, p, p, p, p};
		prob[2] = new double[]{0, p, p, p, p, p, p};
		prob[3] = new double[]{0, p, p, p, p, p, p};
		prob[4] = new double[]{0, 1, 0, 0, 0, 0, 0};
		
		                     
		
		/*
		int[] result = ecss(6, 6, prob);
		
		for (int i = 0; i < result.length; i++) {
			System.out.print(result[i] + " ");
		}*/
		
		int[] bench = new int[5];
		for (int i = 0; i < 100000; i++) {
			int[] result = ecss(6, 6, prob);
			for (int j = 0; j < result.length; j++) {
				if (result[j] == 2) {
					bench[j]++;
				}
			}
		}
		for (int i = 0; i < bench.length; i++) {
			System.out.print(bench[i] + " ");
		}
		
	}
	
	
	/**
	 * 
	 * @param sum the observed sum
	 * @param max the maximum value that the values can take
	 * @param prob 2d double array. first dimension is for each of the variables, and second is probably of getting i'th value
	 */
	public static int[] ecss(int sum, int max, double[][] prob){

		int numVars = prob.length;
		
		// A[i][j] represents the probability of the last i samples summing up to the value j
		// ie, A[2][4] = .3 is saying that there is .3 probability that the last 2 variables sum to 4
		double[][] A = new double[numVars + 1][sum + 1];
		
		for (int i = 0; i <= sum; i++) {
			A[0][i] = 0;
		}
		
		for (int i = 0; i <= Math.min(sum, max); i++) {
			A[1][i] = prob[numVars - 1][i];
		}
		
		for (int i = 1; i < numVars; i++) {
			// the current 
			int m = numVars - i - 1;
			for (int t = 0; t <= sum; t++) {
				double ss = 0;
				for (int j = 0; j < t; j++) {
					ss += prob[m][j] * A[i][t-j]; 
				}
				A[i+1][t] = ss;
				
			}
		}
		
		/*for (int i = 0; i < A.length; i++) {
			for (int j = 0; j <= sum; j++) {
				System.out.format("%.4f ", A[i][j]);
			}
			System.out.println();
		}*/
		
		if (A[numVars][sum] == 0) {
			System.out.println("Impossible setting");
			return null;
		}
		
		int curSum = sum;
		double[] q = new double[Math.min(curSum, max) + 1];
		int[] vals = new int[numVars];
		
		for (int i = 1; i < numVars; i++) {
			for (int j = 0; j <= Math.min(curSum, max); j++) {
				q[j] = prob[i-1][j] * A[numVars-i][curSum-j]/A[numVars-i+1][curSum];
			}
			
			vals[i-1] = randomCategorical(q);
			curSum -= vals[i-1];
		}
		vals[numVars-1] = curSum;
		
		return vals;
		
	}
	
	private static int randomCategorical(double[] p) {
		double val = Math.random();
		
		int i = 0;
		while (val > p[i]) {
			val -= p[i];
			i++;
		}
		
		return i;
	}
}
