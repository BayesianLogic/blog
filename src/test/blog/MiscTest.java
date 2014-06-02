package test.blog;

import static blog.BLOGUtil.parseEvidence_NE;
import static blog.BLOGUtil.parseQuery_NE;

import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import blog.BLOGUtil;
import blog.DBLOGUtil;
import blog.common.Util;
import blog.engine.InferenceEngine;
import blog.engine.SamplingEngine;
import blog.model.ArgSpec;
import blog.model.ArgSpecQuery;
import blog.model.Evidence;
import blog.model.Model;
import blog.type.Timestep;

public class MiscTest extends TestCase {

  public static void main(String[] args) throws Exception {
    junit.textui.TestRunner.run(MiscTest.class);
  }

  public void testDBLOGUtilGetTimestepTermsIn() {
    Model model = Model
        .readFromString("random Boolean Weather(Timestep t) = true;");

    ArgSpec a;
    ArgSpec at10 = BLOGUtil.parseTerm_NE("@10", model);
    ArgSpec at13 = BLOGUtil.parseTerm_NE("@13", model);
    Set timesteps;

    a = BLOGUtil.parseArgSpec_NE("{Weather(@10), @13}", model);
    timesteps = Util.set(at10, at13);
    assertEquals(timesteps, DBLOGUtil.getTimestepTermsIn(a, Util.set()));
  }

  public void testSplitEvidenceByMaxTimestep() {
    Model model = Model.readFromString("random Boolean Weather(Timestep);"
        + "Weather(t) = true;" + "random Boolean Dummy;" + "Dummy = true;");

    Evidence evidence;

    String evidenceDescription = "obs Weather(@15) = true;"
        + "obs Weather(@2) = true;" + "obs Dummy = true;"
        + "obs (Weather(@15)=true & Weather(@1)=false)=true;"
        + "obs (Weather(@1)=true & Weather(@2)=false)=true;";

    evidence = parseEvidence_NE(evidenceDescription, model);

    Map<Timestep, Evidence> sortedEvidence = DBLOGUtil
        .splitEvidenceInTime(evidence);
    System.out.println(sortedEvidence);
    // TODO: test the above, don't just print it
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
