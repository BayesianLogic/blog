package test;

//import org.apache.commons.math.fraction.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.commons.math3.fraction.BigFraction;

/**
 * compute urn ball example, using exact method
 * will produce the exact fractional number
 * 
 * to run
 * 
 * @author leili
 * @date May 6, 2012
 */
public class ComputeUrnBall {

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
		ComputeUrnBall cp = new ComputeUrnBall();
		BigFraction[] probN = cp.compute(obs);
		System.out.println("Uniform prior:");
		print(probN);

		ComputeUrnBall cp_poisson = new ComputeUrnBall(6.0);
		BigFraction[] probN_poisson = cp_poisson.compute(obs);
		System.out.println("Poisson prior:");
		print(probN_poisson);
	}

	public static void print(BigFraction[] probN) {
		for (int i = 1; i < probN.length; i++) {
			System.out.print(i);
			System.out.print("\t");
			System.out.println(new BigDecimal(probN[i].getNumerator()).divide(
					new BigDecimal(probN[i].getDenominator()), 100, RoundingMode.HALF_UP)
					.doubleValue());
		}
	}

	private final BigFraction ProbCorrectObs = new BigFraction(4, 5);
	private final BigFraction ProbWrongObs = new BigFraction(1, 5);

	private double lambda = 1;
	private boolean usePoisson = false;
	private int maxN = 8;

	public ComputeUrnBall() {
		// use uniform
	}

	public ComputeUrnBall(boolean usePoisson) {
		this.usePoisson = usePoisson;
		if (usePoisson) {
			maxN = 100;
		}
	}

	public ComputeUrnBall(double lambda) {
		this.usePoisson = true;
		this.lambda = lambda;
		maxN = 100;
	}

	public BigFraction[] compute(int[] obs) {
		BigFraction[] probN = new BigFraction[maxN + 1];
		for (int i = 0; i < probN.length; i++) {
			probN[i] = new BigFraction(0);
		}

		BigFraction p;
		BigFraction totalp = new BigFraction(0);
		BigFraction totalnum = new BigFraction(0);

		for (int N = 1; N <= maxN; N++) {
			// * 1/8
			for (int k = 0; k <= N; k++) {
				// * 1/ (2^N) * (N! / k! / (N-1)!)
				// choice(N, k) * N
				BigFraction tmpnk = choice(N, k);
				totalnum = totalnum.add(tmpnk.multiply(new BigFraction(N)
						.pow(obs.length)));
				p = computePrior(N).multiply(tmpnk).divide(1 << N);
				BigFraction tmpres;
				BigFraction chose0 = new BigFraction(k, N);
				BigFraction chose1 = new BigFraction(N - k, N);
				for (int i = 0; i < obs.length; i++) {
					if (obs[i] == 0) {
						tmpres = chose0.multiply(ProbCorrectObs).add(
								chose1.multiply(ProbWrongObs));
					} else {
						tmpres = chose1.multiply(ProbCorrectObs).add(
								chose0.multiply(ProbWrongObs));
					}
					p = p.multiply(tmpres);
				}
				probN[N] = probN[N].add(p);
			}

			totalp = totalp.add(probN[N]);
		}
		for (int N = 1; N <= maxN; N++) {
			probN[N] = probN[N].divide(totalp);
		}

		System.out.print("the total number of possible worlds:");
		System.out.println(totalnum.toString());
		return probN;
	}

	private static BigFraction bigE = new BigFraction(Math.E);

	private BigFraction computePrior(int n) {
		BigFraction p;
		if (usePoisson) {
			p = new BigFraction(bigE.pow(-lambda));
			for (int k = 1; k <= n; k++) {
				p = p.multiply(new BigFraction(lambda)).divide(k);
			}
		} else {
			p = new BigFraction(1, 8);
		}
		return p;
	}

	private BigFraction choice(int n, int p) {
		if ((p < 0) || (n < p))
			return new BigFraction(0);
		BigFraction res = new BigFraction(1);
		if (p * 2 > n)
			p = n - p;
		for (int i = 1; i <= p; i++) {
			res = res.multiply(new BigFraction(n - i + 1, i));
		}
		return res;
	}

}
