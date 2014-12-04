package test.blog.model;

import org.junit.Test;

import blog.model.Model;

/**
 * End-to-end test for tuple sets, using the tupleset.blog model.
 */
public class TestTupleSet {

  @Test
  public void testRun() {
    Model.fromFile("src/test/models/tupleset.blog");
  }
}
