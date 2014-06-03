package test.blog;

import org.junit.runner.JUnitCore;

/**
 * A test suit gathering all junit TestCase's available.
 */
public class TestAll {
	public static void main(String[] args) {

		JUnitCore.runClasses(test.blog.BLOGTest.class, FormulaLikelihoodTest.class,
				LWImportanceSamplerTest.class, MiscTest.class,
				ParticleFilterTest.class, SymbolEvidenceLikelihoodWeighterTest.class,
				TupleSetSpecEvidenceLikelihoodWeighterTest.class,
				test.blog.DistanceTest.class, test.blog.TestOperator.class,
				test.blog.TestBuiltInFunctions.class, test.blog.TestComparison.class);
	}
}
