package benchmark.blog;

import java.util.HashMap;
import java.util.Map;

import blog.common.Util;
import blog.distrib.Bernoulli;

/**
 * Native implementation of CSI inference algorithm.
 * Used for a performance comparison with BLOG.
 * 
 * @author awong
 */
public class CSI {

	// Parameters for model
	private static final double PROB_U = 0.3;
	private static final double PROB_V = 0.9;
	private static final double PROB_W = 0.1;
	private static final double PROB_X_IF_ARG = 0.8;
	private static final double PROB_X_NOT_ARG = 0.2;
	
	// Variables for model
	private boolean u;
	private boolean v;
	private boolean w;
	private boolean x;
	
	// Distributions for sampling in model
	private Bernoulli drawU;
	private Bernoulli drawV;
	private Bernoulli drawW;
	private Bernoulli drawXIfArg;
	private Bernoulli drawXNotArg;
	
	// Parameters for inference algorithm
	private static final int NUM_SAMPLES = 50000;
	
	// Queries
	private Map<Boolean, Integer> distribX;
	private int samplesDrawn;
	
	public CSI() {
		Util.initRandom(false);
		distribX = new HashMap<Boolean, Integer>();
		
		drawU = new Bernoulli(PROB_U);
		drawV = new Bernoulli(PROB_V);
		drawW = new Bernoulli(PROB_W);
		drawXIfArg = new Bernoulli(PROB_X_IF_ARG);
		drawXNotArg = new Bernoulli(PROB_X_NOT_ARG);
	}
	
	public void runInference(int numSamples) {
		samplesDrawn = numSamples;
		
		for (int i = 0; i < numSamples; i++) {
			u = drawU.sampleVal();
			v = drawV.sampleVal();
			w = drawW.sampleVal();
			if (u) {
				x = w ? drawXIfArg.sampleVal() : drawXNotArg.sampleVal();
			} else {
				x = v ? drawXIfArg.sampleVal() : drawXNotArg.sampleVal();
			}
			distribX.put(x, distribX.containsKey(x) ? distribX.get(x) + 1 : 1);
		}
	}
	
	public void printStats() {
		System.out.println("Total samples: " + samplesDrawn);
		System.out.println("\nValue of X:");
		for (Boolean val : distribX.keySet()) {
			System.out.println(val + ":\t" + (double)distribX.get(val) / samplesDrawn);
		}
	}
	
	public static void main(String[] args) {
		CSI model = new CSI();
		model.runInference(NUM_SAMPLES);
		model.printStats();
	}
}
