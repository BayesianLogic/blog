/**
 * 
 */
package expmt.modular;

import java.io.PrintStream;

import blog.common.Util;
import blog.distrib.Poisson;

/**
 * @author leili
 * @since May 2, 2012
 * 
 */
public class SampleSumOfVariables {

	SampleSumOfVariables(AircraftBlipSampler s) {
		sampler = s;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 3) {
			System.out
					.println("expecting two arguments: lambda_of_aircraft lambda_of_blip_per_aircraft num_blip_at_each_timestep");
		}

		// SumSamplerForPoisson sumsampler = new RangeSumSamplerForPoisson();

		// SumSamplerForPoisson sumsampler = new RejectionSumSamplerForPoisson();

		SumSamplerForPoisson sumsampler = new RescalingSumSamplerForPoisson();

		AircraftBlipSampler s = new AircraftBlipSampler(
				Double.parseDouble(args[0]), Double.parseDouble(args[1]), sumsampler);

		int[] evid = new int[args.length - 2];

		for (int i = 2; i < args.length; i++) {
			evid[i - 2] = Integer.parseInt(args[i]);
		}

		// run(Double.parseDouble(args[0]), Double.parseDouble(args[1]), evid,
		// sumsampler);
		double lam1 = Double.parseDouble(args[0]);
		double lam2 = Double.parseDouble(args[1]);
		numSample = 10;

		// run(lam1, lam2, evid, sumsampler);

		for (int num = 1; num < 11; num++) {
			evid = new int[num];
			for (int i = 0; i < evid.length; i++)
				evid[i] = 3;

			System.out.println("sampling for " + num + " evidences");
			run(lam1, lam2, evid, sumsampler);

		}
	}

	public static SampleSumOfVariables run(double airlambda, double bliplambda,
			int[] evid, SumSamplerForPoisson sumsampler) {

		AircraftBlipSampler s = new AircraftBlipSampler(airlambda, bliplambda,
				sumsampler);

		s.setEvidence(evid);
		SampleSumOfVariables engine = new SampleSumOfVariables(s);

		Util.initRandom(false);
		engine.inference();
		engine.normalize();
		engine.printResult(System.out);
		return engine;
	}

	void inference() {
		probs = new double[MAX];
		int i;
		for (i = 0; i < probs.length; i++) {
			probs[i] = 0;
		}
		totalWeight = 0;
		for (i = 0; i < numSample; i++) {
			double w = sampler.nextSample();
			int na = sampler.numAircraft;
			probs[na] += w;
			totalWeight += w;
			checkConsistency();
		}
	}

	void checkConsistency() {
		if (sampler.getLastWeight() > 0) {
			numConsistentSample++;
		}
	}

	void normalize() {
		for (int i = 0; i < probs.length; i++) {
			probs[i] /= totalWeight;
		}
	}

	void printResult(PrintStream out) {
		out.println("fraction of consistent worlds: "
				+ ((double) numConsistentSample / numSample));
		for (int i = 0; i < probs.length; i++) {
			if (probs[i] > 0) {
				out.print(i);
				out.print('\t');
				out.println(probs[i]);
			}
		}
	}

	AircraftBlipSampler sampler;

	static int numSample = 1000000;
	static int MAX = 1000;
	double[] probs;
	double totalWeight;
	int numConsistentSample = 0;

}

class AircraftBlipSampler {

	AircraftBlipSampler(double airLambda, double blipLambda,
			SumSamplerForPoisson sumsampler) {
		this.airLambda = airLambda;
		this.blipLambda = blipLambda;
		this.airpoisson = new Poisson(airLambda);
		this.blipPoisson = new Poisson(blipLambda);
		this.sumsampler = sumsampler;
	}

	/**
	 * @param evid
	 *          evid[i] is the evidence denoting how many blips there are for time
	 *          tick i
	 */
	void setEvidence(int[] evid) {
		blipEvid = evid;
		numBlips = new int[blipEvid.length][];
	}

	double getLastWeight() {
		return weight;
	}

	double nextSample() {
		weight = sampleNumAircraft();
		if (numAircraft < 1) {
			weight = 0;
		} else {
			for (int t = 0; t < blipEvid.length; t++) {
				weight *= sampleBlipAtT(t);
			}
		}
		return weight;
	}

	double sampleNumAircraft() {
		int[] x = { 0 };
		double w = sumsampler.sampleParent(x, airLambda, blipLambda, blipEvid);
		numAircraft = x[0];
		return w;
	}

	double sampleBlipAtT(int t) {
		int n = blipEvid[t];
		numBlips[t] = new int[numAircraft];
		return sumsampler.sample(n, numBlips[t], blipPoisson);
	}

	int[] blipEvid;
	int numAircraft;

	// numBlips[i][j] denote time i, source of Aircraft j;
	int[][] numBlips;

	double airLambda;
	Poisson airpoisson;
	double blipLambda;
	Poisson blipPoisson;
	double weight = 0;
	private SumSamplerForPoisson sumsampler;
}

abstract class SumSamplerForPoisson {
	abstract public double sample(int sum, int[] x, Poisson poss);

	public double sampleParent(int[] x, double lambdaParent, double lambdaChild,
			int[] evid) {
		x[0] = Poisson.sampleInt(lambdaParent);
		return 1.0;
	}
}

class RangeSumSamplerForPoisson extends SumSamplerForPoisson {

	public double sample(int sum, int[] x, Poisson poss) {
		if (sum < 0)
			return 0;
		int k = x.length - 1;
		double w = 1;
		double w0 = poss.cdf(0, 0);
		int i;
		for (i = 0; i < k; i++) {
			if (sum == 0) {
				w *= w0;
				x[i] = 0;
			} else {
				int s = -1;
				do {
					s = poss.sampleInt();
				} while (s > sum);
				x[i] = s;
				w *= poss.cdf(0, sum);
				sum = sum - s;
			}
		}
		x[k] = sum;
		w *= Math.exp(poss.computeLogProb(sum));
		return w;
	}

}

class RejectionSumSamplerForPoisson extends SumSamplerForPoisson {
	public double sample(int sum, int[] x, Poisson poss) {
		if (sum < 0)
			return 0;
		int k = x.length - 1;
		int i;
		for (i = 0; i <= k; i++) {
			int s = poss.sampleInt();
			x[i] = s;
			sum = sum - s;
		}
		if (sum == 0)
			return 1.0;
		else
			return 0;
	}
}

class RescalingSumSamplerForPoisson extends SumSamplerForPoisson {

	/*
	 * (non-Javadoc)
	 * 
	 * @see expmt.modular.SumSamplerForPoisson#sampleParent(int[], double, double,
	 * int[])
	 */
	@Override
	public double sampleParent(int[] x, double lambdaParent, double lambdaChild,
			int[] evid) {
		int i;
		int s = 0;
		for (i = 0; i < evid.length; i++) {
			s += evid[i];
		}
		double lambda1 = ((double) s) / evid.length / lambdaChild;
		x[0] = Poisson.sampleInt(lambda1);
		double w = Poisson.computeLogProb(lambdaParent, x[0]);
		w = Math.exp(w - Poisson.computeLogProb(lambda1, x[0]));
		return w;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see expmt.modular.SumSamplerForPoisson#sample(int, int[],
	 * blog.distrib.Poisson)
	 */
	@Override
	public double sample(int sum, int[] x, Poisson poss) {
		if (sum < 0)
			return 0;
		int k = x.length - 1;
		double w = 1;
		double w0 = poss.cdf(0, 0);
		int i;
		for (i = 0; i < k; i++) {
			if (sum == 0) {
				w *= w0;
				x[i] = 0;
			} else {
				int s = -1;
				double lambda = ((double) sum) / (k - i);
				do {
					s = Poisson.sampleInt(lambda);
				} while (s > sum);
				x[i] = s;
				w *= Math.exp(poss.computeLogProb(s)
						- Poisson.computeLogProb(lambda, s))
						* Poisson.cdf(lambda, 0, sum);
				sum = sum - s;
			}
		}
		x[k] = sum;
		w *= Math.exp(poss.computeLogProb(sum));
		return w;
	}
}

class Aircraft {
	int id;

	Aircraft(int id) {
		this.id = id;
	}

	public String toString() {
		return "(Aircraft, " + id + ")";
	}

}

class Blip {
	Aircraft source;
	int time;

	Blip(Aircraft s, int t) {
		this.source = s;
		time = t;
	}

	Blip(Aircraft s) {
		this(s, -1);
	}

	public String toString() {
		return "(Blip, Source=" + source.toString() + ", Time=" + time + ")";
	}
}
