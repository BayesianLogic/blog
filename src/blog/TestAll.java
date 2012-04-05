package blog;

import junit.framework.*;

/**
 * A test suit gathering all junit TestCase's available.
 */
public class TestAll {
	public static void main(String[] args) {
		TestSuite test = new TestSuite();

		test.addTestSuite(blog.BLOGTest.class);
		test.addTestSuite(FormulaLikelihoodTest.class);
		test.addTestSuite(LWImportanceSamplerTest.class);
		test.addTestSuite(MiscTest.class);
		test.addTestSuite(ParticleFilterTest.class);
		test.addTestSuite(SymbolEvidenceLikelihoodWeighterTest.class);
		test.addTestSuite(TupleSetSpecEvidenceLikelihoodWeighterTest.class);
		test.addTestSuite(blog.common.DistanceTest.class);

		junit.textui.TestRunner.run(test);
	}
}
