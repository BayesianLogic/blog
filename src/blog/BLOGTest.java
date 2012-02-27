package blog;

import java.util.Properties;
import common.Util;

import junit.framework.TestCase;

public class BLOGTest extends TestCase {

    protected void setUp() throws Exception {
	super.setUp();
	Util.initRandom(true);
    }

    public void testInference() {
	String modelDescription;
	Properties properties;
	String evidenceDescription;
	String queryDescription;
	String engineClassName;
	String queryValueDescription;
	double expectedProbability;
	double calculatedProbability;
	double margin;
	String samplerClassDefault = "blog.rodrigoexperiments.flexiblesampling.LWSampler";
//	String samplerClassDefault = "blog.LWSampler";
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	modelDescription =
	    "random Boolean Y ~ Bernoulli[0.3]();";
	evidenceDescription = "";
	queryDescription = "query Y;";
	queryValueDescription = "true";
	expectedProbability = 0.3;
	margin = 0.1;
	engineClassName = "blog.SamplingEngine";
	properties = Util.properties("samplerClass", samplerClassDefault);
	
	testProblem(modelDescription, properties, evidenceDescription,
		queryDescription, engineClassName, queryValueDescription, expectedProbability, margin);
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	modelDescription =
	    "random Boolean X ~ Bernoulli[0.7]();" +
	    "random Boolean Y if X then ~ Bernoulli[0.3]() else ~ Bernoulli[0.9]();";
	evidenceDescription = "";
	queryDescription = "query Y;";
	queryValueDescription = "true";
	expectedProbability = 0.48;
	margin = 0.1;
	engineClassName = "blog.SamplingEngine";
	properties = Util.properties("samplerClass", samplerClassDefault);
	
	testProblem(modelDescription, properties, evidenceDescription,
		queryDescription, engineClassName, queryValueDescription, expectedProbability, margin);
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	modelDescription =
	    "random Boolean X ~ Bernoulli[0.7]();" +
	    "random Boolean Y if X then ~ Bernoulli[0.3]() else ~ Bernoulli[0.9]();";
	evidenceDescription = "obs Y = true;";
	queryDescription = "query X;";
	queryValueDescription = "true";
	expectedProbability = 0.4375;
	margin = 0.1;
	engineClassName = "blog.SamplingEngine";
	properties = Util.properties("samplerClass", samplerClassDefault);
	
	testProblem(modelDescription, properties, evidenceDescription,
		queryDescription, engineClassName, queryValueDescription, expectedProbability, margin);
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	modelDescription =
	    "type RainEvent; " +
	    "guaranteed RainEvent Rainy, Dry; " +

	    "random RainEvent Weather(Timestep); " +

	    "Weather(d)  " +
	    " 	if (d = @0) then ~ TabularCPD[[0.7, 0.3]]() " +
	    "	else ~ TabularCPD[[0.8, 0.2], " +
	    "	                  [0.3, 0.7]] " +
	    "	             (Weather(Prev(d)));";

	evidenceDescription = "obs Weather(@2) = Rainy;";
	queryDescription = "query Weather(@6);";
	queryValueDescription = "Rainy";
	expectedProbability = 0.625; // not necessarily the exact solution
	margin = 0.1;
	engineClassName = "blog.SamplingEngine";
	properties = Util.properties("samplerClass", samplerClassDefault, "numSamples", "20000");
	
	testProblem(modelDescription, properties, evidenceDescription,
		queryDescription, engineClassName, queryValueDescription, expectedProbability, margin);
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	modelDescription =
	    "type RainEvent; " +
	    "guaranteed RainEvent Rainy, Dry; " +

	    "random RainEvent Weather(Timestep); " +
	    "random Boolean RainyRegion(); " +

	    "RainyRegion ~ Bernoulli[0.5](); " +

	    "Weather(d)  " +
	    " 	if (d = @0) then ~ TabularCPD[[0.7, 0.3],[0.3, 0.7]](RainyRegion) " +
	    "	else ~ TabularCPD[[0.8, 0.2], " +
	    "	                  [0.3, 0.7], " +
	    "	                  [0.5, 0.5], " +
	    "	                  [0.2, 0.8]] " +
	    "	             (RainyRegion, Weather(Prev(d)));";

	evidenceDescription = "obs Weather(@2) = Rainy;";
	queryDescription = "query Weather(@6);";
	queryValueDescription = "Rainy";
	expectedProbability = 0.52; // not necessarily the exact solution
	margin = 0.1;
	engineClassName = "blog.SamplingEngine";
	properties = Util.properties("samplerClass", samplerClassDefault, "numSamples", "20000");
	
	testProblem(modelDescription, properties, evidenceDescription,
		queryDescription, engineClassName, queryValueDescription, expectedProbability, margin);
    }

    private void testProblem(String modelDescription,
	    Properties properties, String evidenceDescription,
	    String queryDescription, String engineClassName,
	    String queryValueDescription, double expectedProbability, double margin) {

	double calculatedProbability;
	Model model;
	Evidence evidence;
	ArgSpecQuery query;
	InferenceEngine inferenceEngine;
	
	model = BLOGUtil.parseModel_NE(modelDescription);
	evidence = BLOGUtil.parseEvidence_NE(evidenceDescription, model);
	query = BLOGUtil.parseQuery_NE(queryDescription, model);
	inferenceEngine = InferenceEngine.constructEngine(engineClassName, model, properties);
	inferenceEngine.solve(query, evidence);
	query.printResults(System.out);
	calculatedProbability = BLOGUtil.getProbabilityByString(query, model, queryValueDescription);
	
	assertEquals(expectedProbability, calculatedProbability, margin);
    }
}
