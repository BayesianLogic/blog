package test.example;

/**
 * Regression test for simple-aircraft.blog
 * (correct as of )
 * 
 * @author akanata
 * @date 
 *
 */
public class TestWeather extends ExampleTest {
	protected static final String DEF_ENGINE = "blog.engine.SamplingEngine";
	protected static final int NUM_SAMPLES = 50000;
	protected static final int NUM_BURNIN = 0;
	protected static final String DEF_SAMPLER = "blog.sample.LWSampler";
	protected static final String DEF_PROPOSER = "blog.GenericProposer";
	protected static final int STAT_INTERVAL = 2500;
	
	private static final double errBound = 2e-2;
	
	public static void main(String[] args) throws IncorrectProbException {
		TestWeather test = new TestWeather();
		test.runTest();
	}

	public TestWeather() {
		super(errBound);
	}

	@Override
	public void setInputModel() {
		pathsSource.add("example/weather.dblog");
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
	public void referenceProbs() {
		// TODO Auto-generated method stub

	}
}
