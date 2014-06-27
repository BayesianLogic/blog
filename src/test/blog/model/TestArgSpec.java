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
 * Unit tests for ArgSpec.
 * 
 * @author cberzan
 * @since Jun 4, 2014
 */
@RunWith(JUnit4.class)
public class TestArgSpec {
  @Test
  public void testMaxTimestep() {
    Model model = Model
        .readFromString("random Boolean Weather(Timestep t) = true;");
    ArgSpec a = BLOGUtil.parseArgSpec_NE("Weather(@13)", model);
    assertEquals(Timestep.at(13), a.maxTimestep());
  }
}
