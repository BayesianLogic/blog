/**
 * 
 */
package test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ TestBernoulli.class, TestBeta.class, TestBinomial.class,
    TestBooleanDistrib.class, TestCategorical.class, TestDirichlet.class,
    TestExponential.class, TestGamma.class, TestGeometric.class,
    TestMultivarGaussian.class, TestNegativeBinomial.class, TestPoisson.class,
    TestUniformInt.class, TestUniformReal.class, TestUnivariateGaussian.class })
/**
 * Test Suite for all Distributions
 */
public class TestSuiteDistributions {

}
