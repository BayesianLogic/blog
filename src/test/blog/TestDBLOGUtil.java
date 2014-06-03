package test.blog;

import static blog.BLOGUtil.parseEvidence_NE;
import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import blog.DBLOGUtil;
import blog.model.Evidence;
import blog.model.Model;
import blog.type.Timestep;

/**
 * Unit tests for DBLOGUtil.
 */
@RunWith(JUnit4.class)
public class TestDBLOGUtil {
  @Test
  public void testSplitEvidenceInTime() {
    Model model = Model
        .readFromString("random Boolean Weather(Timestep t) = true;"
            + "random Boolean Dummy ~ Bernoulli(0.5);");
    Evidence evidence;
    String evidenceDescription = "obs Weather(@15) = true;"
        + "obs Weather(@2) = true;" + "obs Dummy = true;";
    // TODO: test SymbolEvidenceStatements too.
    evidence = parseEvidence_NE(evidenceDescription, model);

    Map<Timestep, Evidence> splitEvidence = DBLOGUtil
        .splitEvidenceInTime(evidence);
    System.out.println(splitEvidence);
    assertEquals(splitEvidence.size(), 3);
    // TODO: Make it easy to construct ValueEvidenceStatements, so that we
    // aren't forced to compare string representations here...
    assertEquals(splitEvidence.get(null).toString(), "[Dummy = true]");
    assertEquals(splitEvidence.get(Timestep.at(2)).toString(),
        "[Weather(@2) = true]");
    assertEquals(splitEvidence.get(Timestep.at(15)).toString(),
        "[Weather(@15) = true]");
  }

  @Test
  public void testSplitQueriesInTime() {

  }
}
