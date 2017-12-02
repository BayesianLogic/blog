/**
 * 
 */
package test.blog.distrib;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;

import blog.common.numerical.MatrixFactory;
import blog.common.numerical.MatrixLib;
import blog.distrib.Multinomial;

/**
 * @author cgioia
 * @since Jun 16, 2014
 * 
 */
public class TestMultinomial implements TestDistributions {
  private final double ERROR = 10e-5;

  /** Multinomial. n = 3, p = [0.5, 0.25, 0.25]. */
  public void testMultinomial1(Multinomial mult) {
    assertEquals(0.1875, mult.getProb(new ArrayList<Integer>() {
      {
        add(1);
        add(1);
        add(1);
      }
    }), ERROR);
    assertEquals(Math.log(0.1875), mult.getLogProb(new ArrayList<Integer>() {
      {
        add(1);
        add(1);
        add(1);
      }
    }), ERROR);
    assertEquals(0.1875, mult.getProb(new ArrayList<Integer>() {
      {
        add(2);
        add(1);
        add(0);
      }
    }), ERROR);
    assertEquals(Math.log(0.1875), mult.getLogProb(new ArrayList<Integer>() {
      {
        add(2);
        add(1);
        add(0);
      }
    }), ERROR);
  }

  /** Multinomial. n = 4, p = [0.25, 0.25, 0.25, 0.25]. */
  public void testMultinomial2(Multinomial mult) {
    assertEquals(0.09375, mult.getProb(new ArrayList<Integer>() {
      {
        add(1);
        add(1);
        add(1);
        add(1);
      }
    }), ERROR);
    assertEquals(Math.log(0.09375), mult.getLogProb(new ArrayList<Integer>() {
      {
        add(1);
        add(1);
        add(1);
        add(1);
      }
    }), ERROR);
    assertEquals(3.90625e-3, mult.getProb(new ArrayList<Integer>() {
      {
        add(4);
        add(0);
        add(0);
        add(0);
      }
    }), ERROR);
    assertEquals(Math.log(3.90625e-3),
        mult.getLogProb(new ArrayList<Integer>() {
          {
            add(4);
            add(0);
            add(0);
            add(0);
          }
        }), ERROR);
    assertEquals(0, mult.getProb(new ArrayList<Integer>() {
      {
        add(2);
        add(1);
        add(1);
        add(1);
      }
    }), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY,
        mult.getLogProb(new ArrayList<Integer>() {
          {
            add(2);
            add(1);
            add(1);
            add(1);
          }
        }), ERROR);
    assertEquals(0, mult.getProb(new ArrayList<Integer>() {
      {
        add(2);
        add(-1);
        add(1);
        add(1);
      }
    }), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY,
        mult.getLogProb(new ArrayList<Integer>() {
          {
            add(2);
            add(-1);
            add(1);
            add(1);
          }
        }), ERROR);
  }

  @Test
  public void testProbabilityViaConstructor() {
    // no longer needed. will be removed
  }

  @Test
  public void testProbabilityViaSetParams() {
    Multinomial mult = new Multinomial();
    mult.setParams(new Object[] { 3,
        MatrixFactory.createColumnVector(0.5, 0.25, 0.25) });
    testMultinomial1(mult);
    mult.setParams(new Object[] { 4,
        MatrixFactory.createColumnVector(0.25, 0.25, 0.25, 0.25) });
    testMultinomial2(mult);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments() {
    Multinomial mult = new Multinomial();
    mult.setParams(new Object[] { null,
        MatrixFactory.createColumnVector(0.5, 0.25, 0.25) });
    mult.sampleVal();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments2() {
    Multinomial mult = new Multinomial();
    mult.setParams(new Object[] { 2, null });
    mult.sampleVal();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments() {
    Multinomial mult = new Multinomial();
    MatrixLib lib = MatrixFactory.ones(2, 3);
    mult.setParams(new Object[] { 2, lib });
    mult.sampleVal();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments2() {
    Multinomial mult = new Multinomial();
    mult.setParams(new Object[] { -1, MatrixFactory.createColumnVector(1, 1) });
    mult.sampleVal();
  }

  @Test
  public void testNormalization() {
    Multinomial mult = new Multinomial();
    mult.setParams(new Object[] { 4,
        MatrixFactory.createColumnVector(1, 1, 1, 1) });
    testMultinomial2(mult);
  }

  @Test
  public void testDoubleSet() {
    Multinomial mult = new Multinomial();
    mult.setParams(new Object[] { null, null });
    mult.setParams(new Object[] { null,
        MatrixFactory.createColumnVector(2, 1, 1) });
    mult.setParams(new Object[] { 3, null });
    testMultinomial1(mult);
    mult.setParams(new Object[] { 4, null });
    mult.setParams(new Object[] { null,
        MatrixFactory.createColumnVector(1, 1, 1, 1) });
    testMultinomial2(mult);
  }

  @Test
  public void testSetParamsIntegerArguments() {
    // not needed
  }

  @Test
  public void testGetProbIntegerArguments() {
    // not needed
  }

  @Test
  public void testGetFiniteSupport() {
    Multinomial mult = new Multinomial();
    mult.setParams(2, MatrixFactory.createColumnVector(0.3, 0, 0.7));
    Object[] list = mult.getFiniteSupport();
    assertEquals(3, list.length);
    ArrayList<Integer> temp = new ArrayList<Integer>();
    temp.add(0);
    temp.add(0);
    temp.add(2);
    assertEquals(list[0], temp);
    temp.clear();
    temp.add(1);
    temp.add(0);
    temp.add(1);
    assertEquals(list[1], temp);
    temp.clear();
    temp.add(2);
    temp.add(0);
    temp.add(0);
    assertEquals(list[2], temp);

    mult = new Multinomial();
    mult.setParams(3, MatrixFactory.createColumnVector(0.3, 0.3, 0.6));
    list = mult.getFiniteSupport();
    assertEquals(10, list.length);

    temp.clear();
    temp.add(0);
    temp.add(0);
    temp.add(3);
    assertEquals(list[0], temp);
    temp.clear();
    temp.add(0);
    temp.add(1);
    temp.add(2);
    assertEquals(list[1], temp);
    temp.clear();
    temp.add(0);
    temp.add(2);
    temp.add(1);
    assertEquals(list[2], temp);
    temp.clear();
    temp.add(0);
    temp.add(3);
    temp.add(0);
    assertEquals(list[3], temp);
    temp.clear();
    temp.add(1);
    temp.add(0);
    temp.add(2);
    assertEquals(list[4], temp);
    temp.clear();
    temp.add(1);
    temp.add(1);
    temp.add(1);
    assertEquals(list[5], temp);
    temp.clear();
    temp.add(1);
    temp.add(2);
    temp.add(0);
    assertEquals(list[6], temp);
    temp.clear();
    temp.add(2);
    temp.add(0);
    temp.add(1);
    assertEquals(list[7], temp);
    temp.clear();
    temp.add(2);
    temp.add(1);
    temp.add(0);
    assertEquals(list[8], temp);
    temp.clear();
    temp.add(3);
    temp.add(0);
    temp.add(0);
    assertEquals(list[9], temp);
  }
}
