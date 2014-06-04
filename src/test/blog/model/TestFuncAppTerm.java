package test.blog.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import blog.BLOGUtil;
import blog.model.ArgSpec;
import blog.model.Model;
import blog.type.Timestep;

/**
 * Unit tests for FuncAppTerm.
 * 
 * @author cberzan
 * @since Jun 3, 2014
 */
@RunWith(JUnit4.class)
public class TestFuncAppTerm {
  @Test
  public void testGetTimestep() {
    Model model = Model
        .readFromString("random Boolean Weather(Timestep t) = true;");
    ArgSpec at13 = BLOGUtil.parseTerm_NE("@13", model);
    assertEquals(Timestep.at(13), at13.getTimestep());
  }
}
