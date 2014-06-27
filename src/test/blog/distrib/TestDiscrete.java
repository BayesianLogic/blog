package test.blog.distrib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import blog.common.numerical.MatrixFactory;
import blog.distrib.Discrete;

/**
 * @author cgioia
 * @since Jun 16, 2014
 */
public class TestDiscrete implements TestDistributions {
  private final double ERROR = 10e-5;

  /* Discrete (0.2, 0.5, 0.3). */
  public void testDiscrete1(Discrete disc) {
    assertEquals(0.0, disc.getProb(-1), ERROR);
    assertEquals(0.2, disc.getProb(0), ERROR);
    assertEquals(0.5, disc.getProb(1), ERROR);
    assertEquals(0.3, disc.getProb(2), ERROR);
    assertEquals(0.0, disc.getProb(3), ERROR);
    assertEquals(0.0, disc.getProb(20), ERROR);

    assertEquals(Double.NEGATIVE_INFINITY, disc.getLogProb(-1), ERROR);
    assertEquals(Math.log(0.2), disc.getLogProb(0), ERROR);
    assertEquals(Math.log(0.5), disc.getLogProb(1), ERROR);
    assertEquals(Math.log(0.3), disc.getLogProb(2), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY, disc.getLogProb(3), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY, disc.getLogProb(20), ERROR);
  }

  @Test
  public void testProbabilityViaConstructor() {
    // no longer needed. will be removed.
  }

  @Test
  public void testProbabilityViaSetParams() {
    Discrete disc = new Discrete();
    disc.setParams(new Object[] { MatrixFactory.createRowVector(0.2, 0.5, 0.3) });
    testDiscrete1(disc);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments() {
    Discrete disc = new Discrete();
    disc.setParams(new Object[] { null });
    disc.sampleVal();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments() {
    Discrete disc = new Discrete();
    disc.setParams(new Object[] { MatrixFactory.createRowVector(1, -1, 2) });
    disc.sampleVal();
  }

  @Test
  public void testNormalization() {
    Discrete disc = new Discrete();
    disc.setParams(new Object[] { MatrixFactory.createRowVector(4, 10, 6) });
    testDiscrete1(disc);
  }

  @Test
  public void testDoubleSet() {
    Discrete disc = new Discrete();
    disc.setParams(new Object[] { null });
    disc.setParams(new Object[] { MatrixFactory.createRowVector(0.2, 0.5, 0.3) });
    testDiscrete1(disc);
    disc.setParams(new Object[] { MatrixFactory.createRowVector(2, 5, 3) });
    testDiscrete1(disc);
  }

}
