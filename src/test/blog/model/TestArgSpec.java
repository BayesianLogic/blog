package test.blog.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import blog.model.ArgSpec;
import blog.model.ArgSpecQuery;
import blog.model.Model;
import blog.model.Queries;
import blog.type.Timestep;

/**
 * Unit tests for ArgSpec.
 * 
 * @author cberzan
 * @since Jun 4, 2014
 */
@RunWith(JUnit4.class)
public class TestArgSpec {
  @Test
  public void testMaxTimestep() {
    Model model = Model.fromString("fixed Boolean Weather(Timestep t) = true;");
    Queries queries = new Queries(model);
    queries.addFromString("query Weather(@13);");
    ArgSpec a = ((ArgSpecQuery) queries.get(0)).argSpec();
    assertEquals(Timestep.at(13), a.maxTimestep());
  }
}
