package test.blog;

import static blog.BLOGUtil.parseEvidence_NE;

import java.util.*;

import blog.BLOGUtil;
import blog.common.Util;
import blog.engine.InferenceEngine;
import blog.engine.SamplingEngine;
import blog.model.ArgSpecQuery;
import blog.model.ModelEvidenceQueries;


import junit.framework.TestCase;

public class LWImportanceSamplerTest extends TestCase {

	private static final double errorMargin = 0.05;

	public void setUp() {
		Util.initRandom(true);
	}

	// These are integration tests. For more complete tests on particular
	// EvidenceLikelihoodWeighter classes, go to their corresponding test classes.

	public void testPresence() throws Exception {
		// This is linked to testPresenceAtLWImportanceSamplerTest.xls.
		String description = "type Aircraft;"
				+ "guaranteed Aircraft a1, a2, a3;"
				+

				"random Real Position(Aircraft);"
				+ "Position(a) if a = a1 then = 1.0 elseif a = a2 then = 2.0 else = 3.0;"
				+

				"type Blip;" + "origin Aircraft Source(Blip);"
				+ "#Blip(Source = a) = 1;" +

				"random Real AppPos(Blip);"
				+ "AppPos(b) ~ Gaussian(Position(Source(b)), 1.0);" +

				"obs {Blip b} = {B1,B2,B3};" + "obs AppPos(B1) = 3;"
				+ "obs AppPos(B2) = 4;" + "obs AppPos(B3) = 5;" +

				"type Dummy;" + "guaranteed Dummy d;"
				+ "query {Source(B1), Source(B2), Source(B3) for Dummy d};" + // dummy
																																			// makes
																																			// it a
																																			// tuple
																																			// instead
																																			// of a
																																			// set

				"query Source(B1);" + "query Source(B2);" + "query Source(B3);";

		ModelEvidenceQueries meq = BLOGUtil
				.parseModelEvidenceQueries_NE(description);
		InferenceEngine sampling = new SamplingEngine(meq.model, Util.properties(
				"samplerClass", "blog.LWImportanceSampler", "numSamples", "10000"));
		sampling.solve(meq);

		for (Iterator it = meq.queries.iterator(); it.hasNext();) {
			ArgSpecQuery query = (ArgSpecQuery) it.next();
			query.printResults(System.out);
		}

		ArgSpecQuery query1 = (ArgSpecQuery) meq.queries.get(1);

		// Computed in testPresenceAtLWImportanceSamplerTest.xls
		double prob1 = BLOGUtil.getProbabilityByString(query1, meq.model, "a1");
		assertEquals(0.738, prob1, errorMargin);
		double prob2 = BLOGUtil.getProbabilityByString(query1, meq.model, "a2");
		assertEquals(0.225, prob2, errorMargin);
		double prob3 = BLOGUtil.getProbabilityByString(query1, meq.model, "a3");
		assertEquals(0.036, prob3, errorMargin);
	}

	public void testAbsence() throws Exception {
		String description = "type RainEvent;" + "guaranteed RainEvent Rainy, Dry;"
				+

				"random RainEvent Weather(Timestep);" + "random Boolean RainyRegion();"
				+

				"RainyRegion ~ Bernoulli(0.5);" +

				"Weather(d)"
				+ "if (d = @0) then ~ TabularCPD[[0.7, 0.3],[0.3, 0.7]](RainyRegion)"
				+ "else ~ TabularCPD[[0.8, 0.2]," + "[0.3, 0.7]," + "[0.5, 0.5],"
				+ "[0.2, 0.8]]" + "(RainyRegion, Weather(Prev(d)));" +

				"query Weather(@6);" +

				"obs Weather(@0) = Rainy;" + "obs Weather(@1) = Rainy;"
				+ "obs Weather(@2) = Rainy;" + "obs Weather(@3) = Rainy;"
				+ "obs Weather(@4) = Dry;" + "obs Weather(@5) = Dry;";

		ModelEvidenceQueries meq = BLOGUtil
				.parseModelEvidenceQueries_NE(description);
		InferenceEngine sampling = new SamplingEngine(meq.model, Util.properties(
				"samplerClass", "blog.LWImportanceSampler", "numSamples", "10000"));
		sampling.solve(meq);

		for (Iterator it = meq.queries.iterator(); it.hasNext();) {
			ArgSpecQuery query = (ArgSpecQuery) it.next();
			query.printResults(System.out);
		}

		ArgSpecQuery query1 = (ArgSpecQuery) meq.queries.get(0);

		double probDry = BLOGUtil.getProbabilityByString(query1, meq.model, "Dry");
		assertEquals(0.72, probDry, errorMargin);
		double probRainy = BLOGUtil.getProbabilityByString(query1, meq.model,
				"Rainy");
		assertEquals(0.28, probRainy, errorMargin);
	}

	public void testSetSampling() throws Exception {
		double variance = 1;
		String description = "type Blip;" + "guaranteed Blip b1, b2, b3;"
				+ "random Real ApparentPos(Blip b)"
				+ "if     b = b1 then ~ Gaussian(1, " + variance + ")"
				+ "elseif b = b2 then ~ Gaussian(2, " + variance + ")"
				+ "elseif b = b3 then ~ Gaussian(3, " + variance + ")"
				+ "              else ~ Gaussian(4, " + variance + ");" + ""
				+ "obs {ApparentPos(b) for Blip b} = {1.0, 2.0, 3.0};" + ""
				+ "query ApparentPos(b1);";

		ModelEvidenceQueries meq = BLOGUtil
				.parseModelEvidenceQueries_NE(description);
		InferenceEngine sampling = new SamplingEngine(meq.model, Util.properties(
				"samplerClass", "blog.LWImportanceSampler", "numSamples", "5000"));
		sampling.solve(meq);

		for (Iterator it = meq.queries.iterator(); it.hasNext();) {
			ArgSpecQuery query = (ArgSpecQuery) it.next();
			query.printResults(System.out);
		}

		ArgSpecQuery query = (ArgSpecQuery) meq.queries.get(0);

		double probOf1 = 0.738;
		double probOf2 = 0.224;
		double probOf3 = 0.037;
		assertEquals(probOf1,
				BLOGUtil.getProbabilityByString(query, meq.model, "1.0"), errorMargin);
		assertEquals(probOf2,
				BLOGUtil.getProbabilityByString(query, meq.model, "2.0"), errorMargin);
		assertEquals(probOf3,
				BLOGUtil.getProbabilityByString(query, meq.model, "3.0"), errorMargin);
	}

}
