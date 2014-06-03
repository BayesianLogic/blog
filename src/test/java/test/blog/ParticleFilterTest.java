package test.blog;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;

import junit.framework.TestCase;
import blog.BLOGUtil;
import blog.Main;
import blog.common.Histogram;
import blog.common.Util;
import blog.engine.ParticleFilter;
import blog.model.ArgSpecQuery;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.ModelEvidenceQueries;

/**
 * Unit testing for the {@link ParticleFilter}. Because sampling can potentially
 * fail no matter the error margin, tests sometimes fail. This should be rare,
 * however. If so, the user can check the indicated error to see if things look
 * ok, or run the test again.
 */
public class ParticleFilterTest extends TestCase {

	// Configuration:
	private double delta = 0.05; // the allowed difference between
																// expected and computed values

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(ParticleFilterTest.class);
	}

	/** Sets particle filter properties to default values before every test. */
	public void setUp() {
		Util.initRandom(true);
		setDefaultParticleFilterProperties();
	}

	private void setDefaultParticleFilterProperties() {
		properties = new Properties();
		properties.setProperty("numParticles", "5000");
		properties.setProperty("samplerClass", "blog.LWSampler");
		properties.setProperty("useDecayedMCMC", "false");
		properties.setProperty("numMoves", "1");
	}

	private static final String weatherModelString = "type RainEvent;"
			+ "guaranteed RainEvent Rainy, Dry;" +

			"random RainEvent Weather(Timestep);" + "random RainEvent RainyRegion();"
			+

			"RainyRegion ~ TabularCPD[[0.5, 0.5]]();" +

			"Weather(d) "
			+ " 	if (d = @0) then ~ TabularCPD[[0.7, 0.3],[0.3, 0.7]](RainyRegion)"
			+ "	else ~ TabularCPD[[0.8 , 0.2]," + "	                  [0.3, 0.7],"
			+ "	                  [0.6 , 0.4]," + "	                  [0.2, 0.8]]"
			+ "	             (RainyRegion, Weather(Prev(d)));";

	private static final String symbolEvidenceModelString = "type Aircraft;"
			+ "guaranteed Aircraft a1, a2, a3;"
			+ ""
			+ "random Real Position;"
			+ "Position(a) if a = a1 then = 2.0 else if a = a2 then = 3.0 else = 4.0;"
			+ "" + "type Blip;" + "origin Aircraft Source(Blip);" + "#Blip(a) = 1;"
			+ "" + "random Real AppPos(Blip);"
			+ "AppPos(b) ~ Gaussian(Position(Source(a)), 1);";

	// public void testSymbolEvidence() throws Exception {
	// setModel(symbolEvidenceModelString);
	//
	// assertProb("obs {Blip b} = {B1,B2,B3};" +
	// "obs Position(B1) = 3.0;" +
	// "obs Position(B2) = 4.0;'" +
	// "obs Position(B3) = 5.0;" +
	// "query Source(B1);",
	// "a1", 1);
	// }

	public void test1() throws Exception {
		setModel(weatherModelString);

		assertProb("obs Weather(@0)=Rainy; query Weather(@0);", "Rainy", 1);
		assertProb("obs Weather(@1)=Rainy; query Weather(@1);", "Rainy", 1);
		assertProb("obs Weather(@2)=Rainy; query Weather(@2);", "Rainy", 1);
		assertProb("query Weather(@3);", "Rainy", 0.7611510791366907); // calculated
																																		// by exact
																																		// inference
		assertProb("query RainyRegion;", "Rainy", 0.8057553956834532);
	}

	public void test1a() throws Exception {
		// Same as test1, but tests evidence being thrown in with the model.
		setModel(weatherModelString + "obs Weather(@0)=Rainy; "
				+ "obs Weather(@1)=Rainy; " + "obs Weather(@2)=Rainy;");
		assertProb("query Weather(@3);", "Rainy", 0.7611510791366907); // calculated
																																		// by exact
																																		// inference
		assertProb("query RainyRegion;", "Rainy", 0.8057553956834532);
	}

	public void test2() throws Exception {
		setModel(weatherModelString);

		assertProb("query Weather(@3);", "Rainy", 0.47185);
		assertProb("query RainyRegion;", "Rainy", 0.5);
	}

	public void testLongerInterval() throws Exception {
		setModel(weatherModelString);

		properties.setProperty("numParticles", "1000");

		assertProb("query Weather(@15);", "Rainy", 0.45);
		assertProb("query RainyRegion;", "Rainy", 0.5);

		setDefaultParticleFilterProperties();
	}

	private void setModel(String newModelString) throws Exception {
		model = new Model();
		Evidence evidence = new Evidence();
		LinkedList queries = new LinkedList();
		Main.stringSetup(model, evidence, queries, newModelString);
		particleFilter = new ParticleFilter(model, properties);
		particleFilter.setEvidence(evidence);
		particleFilter.setQueries(queries);
	}

	private void assertProb(String evidenceAndQuery, String valueString,
			double expected) throws Exception {
		ModelEvidenceQueries meq = BLOGUtil.parseAndTranslateFromString(model,
				evidenceAndQuery);
		particleFilter.take(meq.evidence);
		particleFilter.answer(meq.queries);
		assertEquals(expected, BLOGUtil.getProbabilityByString(
				getQuery(meq.queries), model, valueString), delta);
		outputQueries(meq.queries);
	}

	private void outputQueries(Collection queries) {
		for (Iterator it = queries.iterator(); it.hasNext();) {
			ArgSpecQuery query = (ArgSpecQuery) it.next();
			for (Iterator it2 = query.getHistogram().entrySet().iterator(); it2
					.hasNext();) {
				Histogram.Entry entry = (Histogram.Entry) it2.next();
                double prob = query.getHistogram().getProb(entry.getElement());
				System.out.println("Prob. of " + query + " = " + entry.getElement()
						+ " is " + prob);
			}
		}
	}

	/**
	 * Helper function that gets a collection assumed to contain a single query
	 * and returns that query.
	 */
	private ArgSpecQuery getQuery(Collection singleton) {
		return (ArgSpecQuery) Util.getFirst(singleton);
	}

	private static Properties properties;
	private static ParticleFilter particleFilter;
	private static Model model;
}
