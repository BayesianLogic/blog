package test.example;

import java.util.Properties;

/**
 * Regression test for simple-aircraft.blog
 * (correct as of at least June 8, 2012)
 * 
 * @author akanata
 * @date July 13, 2012
 *
 */
public class TestSimpleAircraft extends ExampleTest{
	protected static final String DEF_ENGINE = "blog.engine.SamplingEngine";
	protected static final int NUM_SAMPLES = 10000;
	protected static final int NUM_BURNIN = 0;
	protected static final String DEF_SAMPLER = "blog.sample.LWSampler";
	protected static final String DEF_PROPOSER = "blog.GenericProposer";
	protected static final int STAT_INTERVAL = 500;
	
	private static final double errBound = 1e-3;
	
	public static void main(String[] args) throws IncorrectProbException {
		TestSimpleAircraft test = new TestSimpleAircraft();
		test.runTest();
	}
	
	public TestSimpleAircraft() {
		super(errBound);
	}
	
	@Override
	public void setInferenceProps() {
		inferenceProps = new Properties();

		inferenceProps.setProperty("engineClass", DEF_ENGINE);
		inferenceProps.setProperty("numSamples", "" + NUM_SAMPLES);
		inferenceProps.setProperty("burnIn", "" + NUM_BURNIN);
		inferenceProps.setProperty("samplerClass", DEF_SAMPLER);
		inferenceProps.setProperty("proposerClass", DEF_PROPOSER);
		inferenceProps.setProperty("reportInterval", "" + STAT_INTERVAL);
	}
	
	@Override
	public void setInputModel() {
		pathsSource.add("example/simple-aircraft.blog");
	}
	
	@Override
	public void referenceProbs() {
		
	}
}
