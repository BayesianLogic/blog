package blog;

import test.blog.FormulaLikelihoodTest;
import test.blog.LWImportanceSamplerTest;
import test.blog.MiscTest;
import test.blog.ParticleFilterTest;
import test.blog.SymbolEvidenceLikelihoodWeighterTest;
import test.blog.TupleSetSpecEvidenceLikelihoodWeighterTest;
import junit.framework.*;

/**
 * A test suit gathering all junit TestCase's available.
 */
public class TestAll {
	public static void main(String[] args) {
		TestSuite test = new TestSuite();

		test.addTestSuite(test.blog.BLOGTest.class);
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
