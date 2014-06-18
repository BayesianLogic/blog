package test.blog;

import static blog.BLOGUtil.parseEvidence_NE;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import blog.BLOGUtil;
import blog.DBLOGUtil;
import blog.common.Util;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.Query;
import blog.type.Timestep;

/**
 * Unit tests for DBLOGUtil.
 */
@RunWith(JUnit4.class)
public class TestDBLOGUtil {
  @Test
  public void testSplitEvidenceInTime() {
    Model model = Model
        .fromString("random Boolean Weather(Timestep t) = true;"
            + "random Boolean Dummy ~ Bernoulli(0.5);");
    Evidence evidence;
    String evidenceDescription = "obs Weather(@15) = true;"
        + "obs Weather(@2) = true;" + "obs Dummy = true;";
    // TODO: test SymbolEvidenceStatements too.
    evidence = parseEvidence_NE(evidenceDescription, model);

    Map<Timestep, Evidence> splitEvidence = DBLOGUtil
        .splitEvidenceInTime(evidence);
    // System.out.println(splitEvidence);
    assertEquals(splitEvidence.size(), 3);
    // TODO: Make it easy to construct ValueEvidenceStatements, so that we
    // aren't forced to compare string representations here...
    assertEquals("[Dummy = true]", splitEvidence.get(null).toString());
    assertEquals("[Weather(@2) = true]", splitEvidence.get(Timestep.at(2))
        .toString());
    assertEquals("[Weather(@15) = true]", splitEvidence.get(Timestep.at(15))
        .toString());
  }

  @Test
  public void testSplitQueriesInTime() {
    Model model = Model
        .fromString("random Boolean Weather(Timestep t) = true;"
            + "random Boolean Dummy ~ Bernoulli(0.5);");
    ArrayList<Query> queries = new ArrayList<Query>();
    queries.add(BLOGUtil.parseQuery_NE("query Dummy;", model));
    queries.add(BLOGUtil.parseQuery_NE("query Weather(@3);", model));
    queries.add(BLOGUtil.parseQuery_NE("query Weather(@13);", model));

    Map<Timestep, List<Query>> splitQueries = DBLOGUtil
        .splitQueriesInTime(queries);
    // System.out.println(splitQueries);
    assertEquals(3, splitQueries.size());
    assertEquals(Util.list(queries.get(0)), splitQueries.get(null));
    assertEquals(Util.list(queries.get(1)), splitQueries.get(Timestep.at(3)));
    assertEquals(Util.list(queries.get(2)), splitQueries.get(Timestep.at(13)));
  }
}
