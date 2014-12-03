package test.blog.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

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
public class TestArgSpec {
  @Test
  public void testMaxTimestep() {
    Model model = Model.fromString("fixed Boolean Weather(Timestep t) = true;");
    ArgSpec a = BLOGUtil.parseArgSpec("Weather(@13)", model);
    assertEquals(Timestep.at(13), a.maxTimestep());
  }
}
