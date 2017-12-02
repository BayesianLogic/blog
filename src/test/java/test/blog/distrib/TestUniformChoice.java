/**
 * 
 */
package test.blog.distrib;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.HashSet;

import org.junit.Test;

import blog.distrib.UniformChoice;

/**
 * Unit Tests for Uniform Choice
 */
public class TestUniformChoice implements TestDistributions {
  private final double ERROR = 10e-3;

  @Test
  public void testProbabilityViaConstructor() {
    // no longer needed. will be removed.
  }

  @Test
  public void testProbabilityViaSetParams() {
    Collection<Integer> coll = new HashSet<Integer>();
    coll.add(5);
    coll.add(7);
    UniformChoice unif = new UniformChoice();
    unif.setParams(coll);
    assertEquals(0.5, unif.getProb(5), ERROR);
    assertEquals(0.0, unif.getProb(6), ERROR);
    assertEquals(0.5, unif.getProb(7), ERROR);
    assertEquals(0.0, unif.getProb("oak"), ERROR);
    assertEquals(Math.log(0.5), unif.getLogProb(5), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY, unif.getLogProb(6), ERROR);
    assertEquals(Math.log(0.5), unif.getLogProb(7), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY, unif.getLogProb("oak"), ERROR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments() {
    UniformChoice unif = new UniformChoice();
    unif.setParams(new Object[] { null });
    unif.getProb(1);
  }

  @Test
  public void testIncorrectArguments() {
    // no need
  }

  @Test
  public void testDoubleSet() {
    Collection<Integer> coll = new HashSet<Integer>();
    coll.add(5);
    coll.add(7);
    UniformChoice unif = new UniformChoice();
    unif.setParams(coll);

    Collection<Integer> coll2 = new HashSet<Integer>();
    coll2.add(5);
    unif.setParams(coll2);
    assertEquals(1.0, unif.getProb(5), ERROR);
    assertEquals(0.0, unif.getProb(7), ERROR);
  }

  @Test
  public void testSetParamsIntegerArguments() {
    // no need
  }

  @Test
  public void testGetProbIntegerArguments() {
    // no need
  }

  @Test
  public void testGetFiniteSupport() {
    Collection<Integer> coll = new HashSet<Integer>();
    coll.add(5);
    coll.add(7);
    UniformChoice unif = new UniformChoice();
    unif.setParams(coll);
    Object[] val = unif.getFiniteSupport();
    assertEquals(2, val.length);
    assertEquals(5, val[0]);
    assertEquals(7, val[1]);
  }

}
