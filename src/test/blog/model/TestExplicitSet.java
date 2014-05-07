package test.blog.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import blog.model.Model;


/**
 * End-to-end test for explicit sets, using the explicitset.blog model.
 */
@RunWith(JUnit4.class)
public class TestExplicitSet {

  @Test
  public void testRun() {
    Model model = Model.readFromFile("src/test/models/explicitset.blog");
  }
}

