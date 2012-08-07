package test.example;

import java.util.HashMap;

/**
 * Regression test for weather.dblog
 * (correct as of August 6, 2012)
 * 
 * @author akanata
 * @date August 6, 2012
 *
 */
public class TestWeather extends ExampleTest {
	protected static final String DEF_ENGINE = "blog.engine.SamplingEngine";
	protected static final int NUM_SAMPLES = 50000;
	protected static final int NUM_BURNIN = 0;
	protected static final String DEF_SAMPLER = "blog.sample.LWSampler";
	protected static final String DEF_PROPOSER = "blog.GenericProposer";
	protected static final int STAT_INTERVAL = 2500;
	
	private static final double ERR_BOUND = 5e-3;
	
	public static void main(String[] args) throws IncorrectProbException {
		TestWeather test = new TestWeather();
		test.runTest();
	}

	public TestWeather() {
		super(ERR_BOUND);
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
		HashMap<Object, Double> nextWeather = refProbs.get(0);
		nextWeather.put(model.getConstantValue("Dry"), 0.723014607);
		nextWeather.put(model.getConstantValue("Rainy"), 0.276985393);
		
		HashMap<Object, Double> climate = refProbs.get(1);
		climate.put(true, 0.769853934);
		climate.put(false, 0.230146066);
	}
}
