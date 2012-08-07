package test.example;

import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.math3.fraction.BigFraction;

import test.ComputeUrnBall;

/**
 * Regression test for poisson-ball.blog
 * (correct as of at least July 12, 2012)
 * 
 * @author awong
 * @date July 13, 2012
 *
 */
public class TestPoissonBall extends ExampleTest {
	protected static final String DEF_ENGINE = "blog.engine.SamplingEngine";
	protected static final int NUM_SAMPLES = 50000;
	protected static final int NUM_BURNIN = 0;
	protected static final String DEF_SAMPLER = "blog.sample.LWSampler";
	protected static final String DEF_PROPOSER = "blog.GenericProposer";
	protected static final int STAT_INTERVAL = 2500;
	
	private static final double errBound = 3e-2;
	
	public static void main(String[] args) throws IncorrectProbException {
		TestPoissonBall test = new TestPoissonBall();
		test.runTest();
	}
	
	public TestPoissonBall() {
		super(errBound);
	}
	
	@Override
	public void setInferenceProps() {
		inferenceProps.setProperty("engineClass", DEF_ENGINE);
		inferenceProps.setProperty("numSamples", "" + NUM_SAMPLES);
		inferenceProps.setProperty("burnIn", "" + NUM_BURNIN);
		inferenceProps.setProperty("samplerClass", DEF_SAMPLER);
		inferenceProps.setProperty("proposerClass", DEF_PROPOSER);
		inferenceProps.setProperty("reportInterval", "" + STAT_INTERVAL);
	}
	
	@Override
	public void setInputModel() {
		pathsSource.add("example/poisson-ball.blog");
	}
	
	@Override
	public void referenceProbs() {
		int[] obs = new int[10];
		for (int i = 0; i < obs.length;) {
			obs[i] = 0;
			i = i + 2;
		}
		for (int i = 1; i < obs.length;) {
			obs[i] = 1;
			i = i + 2;
		}
		ComputeUrnBall cp_poisson = new ComputeUrnBall(6.0);
		BigFraction[] probN_poisson = cp_poisson.compute(obs);
		ComputeUrnBall.print(probN_poisson);
		
		for (int i = 1; i < probN_poisson.length; i++) {
			HashMap<Object, Double> probTable = refProbs.get(0);
			probTable.put(i, probN_poisson[i].doubleValue());
		}
	}
}
