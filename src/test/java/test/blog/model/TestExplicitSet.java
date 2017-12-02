package test.blog.model;

import org.junit.Test;

import blog.model.Model;

/**
 * End-to-end test for explicit sets, using the explicitset.blog model.
 */
public class TestExplicitSet {

  @Test
  public void testRun() {
    Model.fromFile("src/test/models/explicitset.blog");
  }
}
