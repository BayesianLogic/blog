package test.blog;

import static blog.BLOGUtil.parseQuery_NE;
import junit.framework.TestCase;
import blog.BLOGUtil;
import blog.common.Util;
import blog.engine.InferenceEngine;
import blog.engine.SamplingEngine;
import blog.model.ArgSpec;
import blog.model.ArgSpecQuery;
import blog.model.Model;

public class MiscTest extends TestCase {

  public static void main(String[] args) throws Exception {
    junit.textui.TestRunner.run(MiscTest.class);
  }

  public void testDBLOGUtilGetTimestepTermsIn() {
    Model model = Model
        .readFromString("random Boolean Weather(Timestep t) = true;");
    ArgSpec a = BLOGUtil.parseArgSpec_NE("{Weather(@10), @13}", model);
    ArgSpec at13 = BLOGUtil.parseTerm_NE("@13", model);
    assertEquals(at13, a.maxTimestep());
  }

  public void testParsingTupleSetSpec() { // to be removed
    Util.initRandom(true);
    Model model = Model.readFromString("random Boolean Weather(Timestep);"
        + "Weather(t) ~ Bernoulli(0.8);");
    ArgSpecQuery query = parseQuery_NE(
        "query {Weather(t) for Timestep t : t = @0 | t = @1 | t = @2};", model);
    InferenceEngine engine = new SamplingEngine(model);
    engine.solve(query);
    query.printResults(System.out);
    assertEquals(
        0.512,
        query.getProb(Util.multiset(Util.list(true), Util.list(true),
            Util.list(true))), 0.1);
  }

  public void testLogSum() {
    double got = Util.logSum(-2000, -2000);
    double expected = -2000 + java.lang.Math.log(2);
    assertTrue(java.lang.Math.abs(got - expected) < 1e-10);

    got = Util.logSum(-2000, -2010);
    expected = -1999.99995460110085332417;
    assertTrue(java.lang.Math.abs(got - expected) < 1e-10);

    got = Util.logSum(-2000, -1000);
    expected = -1000;
    assertTrue(java.lang.Math.abs(got - expected) < 1e-10);

    got = Util.logSum(-1000, -2000);
    expected = -1000;
    assertTrue(java.lang.Math.abs(got - expected) < 1e-10);

    got = Util.logSum(-1000, Double.NEGATIVE_INFINITY);
    expected = -1000;
    assertTrue(java.lang.Math.abs(got - expected) < 1e-10);

    got = Util.logSum(Double.NEGATIVE_INFINITY, -1000);
    expected = -1000;
    assertTrue(java.lang.Math.abs(got - expected) < 1e-10);

    got = Util.logSum(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
    expected = Double.NEGATIVE_INFINITY;
    assertEquals(got, expected);
  }
}
