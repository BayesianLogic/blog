package test.blog.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import blog.model.Model;


/**
 * End-to-end test for tuple sets, using the tupleset.blog model.
 */
@RunWith(JUnit4.class)
public class TestTupleSet {

  @Test
  public void testRun() {
    Model model = Model.readFromFile("src/test/models/tupleset.blog");
  }
}
