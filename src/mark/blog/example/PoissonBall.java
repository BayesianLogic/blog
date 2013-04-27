package mark.blog.example;

/**
 * compute urn ball example, using exact method
 * will produce the exact using double (will be faster than fraction number)
 * 
 * @see ComputeUrnBall.java
 * @author leili
 * @date May 6, 2012
 */
public class PoissonBall {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int numObs = 32;
		if (args.length > 0) {
			numObs = Integer.parseInt(args[0]);
		}
		int[] obs = new int[numObs];
		for (int i = 0; i < obs.length;) {
			obs[i] = 0;
			i = i + 2;
		}
		for (int i = 1; i < obs.length;) {
			obs[i] = 1;
			i = i + 2;
		}
		PoissonBall cp = new PoissonBall();
		double[] probN = cp.compute(obs);
		System.out.println("Uniform prior:");
		print(probN);

		PoissonBall cp_poisson = new PoissonBall(6.0);
		double[] probN_poisson = cp_poisson.compute(obs);
		System.out.println("Poisson prior:");
		print(probN_poisson);
	}

	public static void print(double[] probN) {
		for (int i = 1; i < probN.length; i++) {
			System.out.print(i);
			System.out.print("\t");
			System.out.println(probN[i]);
		}
	}

	private final double ProbCorrectObs = 4.0 / 5;
	private final double ProbWrongObs = 1.0 / 5;

	private double lambda = 1;
	private boolean usePoisson = false;
	private int maxN = 8;

	public PoissonBall() {
		// use uniform
	}

	public PoissonBall(boolean usePoisson) {
		this.usePoisson = usePoisson;
		if (usePoisson) {
			maxN = 100;
		}
	}

	public PoissonBall(double lambda) {
		this.usePoisson = true;
		this.lambda = lambda;
		maxN = 100;
	}

	public double[] compute(int[] obs) {
		double[] probN = new double[maxN + 1];
		for (int i = 0; i < probN.length; i++) {
			probN[i] = 0;
		}

		double p = 0;
		double totalp = 0;
		double totalnum = 0;

		for (int N = 1; N <= maxN; N++) {
			// * 1/8
			for (int k = 0; k <= N; k++) {
				// * 1/ (2^N) * (N! / k! / (N-1)!)
				// choice(N, k) * N
				totalnum = totalnum + choice(N, k) * (N ^ obs.length);
				p = computePrior(N) * (choice(N, k) / Math.pow(2, N));
				double tmpres;
				double chose0 = (1.0 * k) / N;
				double chose1 = (1.0 * (N - k)) / N;
				for (int i = 0; i < obs.length; i++) {
					if (obs[i] == 0) {
						tmpres = chose0 * ProbCorrectObs + chose1 * ProbWrongObs;
					} else {
						tmpres = chose1 * ProbCorrectObs + chose0 * ProbWrongObs;
					}
					p = p * tmpres;
				}
				probN[N] = probN[N] + p;
			}

			totalp = totalp + probN[N];
		}
		for (int N = 1; N <= maxN; N++) {
			probN[N] = probN[N] / totalp;
		}

		System.out.print("the total number of possible worlds:");
		System.out.println(totalnum);
		return probN;
	}

	private static double bigE = Math.E;

	private double computePrior(int n) {
		double p;
		if (usePoisson) {
			p = Math.exp(-lambda);
			for (int k = 1; k <= n; k++) {
				p = (p * lambda) / k;
			}
		} else {
			p = 1.0 / 8;
		}
		return p;
	}

	private double choice(int n, int p) {
		if ((p < 0) || (n < p))
			return 0;
		double res = 1;
		if (p * 2 > n)
			p = n - p;
		for (int i = 1; i <= p; i++) {
			res = (res * (n - i + 1)) / i;
		}
		return res;
	}

}
