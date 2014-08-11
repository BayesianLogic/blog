/**
 * 
 */
package test.blog.distrib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import blog.distrib.Categorical;

/**
 * @author cgioia
 * @since Jun 17, 2014
 */
public class TestCategorical implements TestDistributions {
  private final double ERROR = 10e-5;

  /** Categorical. "Albert" => 0.5, "Bob" => 0.2, "Craig" => 0.3. */
  public void testCategorical1(Categorical cat) {
    assertEquals(0.0, cat.getProb("Andy"), ERROR);
    assertEquals(0.5, cat.getProb("Albert"), ERROR);
    assertEquals(0.2, cat.getProb("Bob"), ERROR);
    assertEquals(0.3, cat.getProb("Craig"), ERROR);
    assertEquals(0.0, cat.getProb(null), ERROR);

    assertEquals(Double.NEGATIVE_INFINITY, cat.getLogProb("Andy"), ERROR);
    assertEquals(Math.log(0.5), cat.getLogProb("Albert"), ERROR);
    assertEquals(Math.log(0.2), cat.getLogProb("Bob"), ERROR);
    assertEquals(Math.log(0.3), cat.getLogProb("Craig"), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY, cat.getLogProb(null), ERROR);
  }

  @Test
  public void testProbabilityViaConstructor() {
    // not needed. will be removed
  }

  @Test
  public void testProbabilityViaSetParams() {
    Categorical cat = new Categorical();
    HashMap<String, Object> map = new HashMap<String, Object>();
    map.put("Bob", 0.2);
    map.put("Craig", 0.3);
    map.put("Albert", 0.5);
    cat.setParams(new Object[] { map });
    testCategorical1(cat);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments() {
    Categorical cat = new Categorical();
    cat.setParams(new Object[] { null });
    cat.sampleVal();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments() {
    Categorical cat = new Categorical();
    HashMap<String, Object> map = new HashMap<String, Object>();
    map.put("Bob", 0.2);
    map.put("Craig", -0.01);
    cat.setParams(new Object[] { map });
    cat.sampleVal();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testProbabilitySumTooSmall() {
    Categorical cat = new Categorical();
    HashMap<String, Object> map = new HashMap<String, Object>();
    map.put("Albert", 0.0);
    map.put("Bob", 0.0);
    cat.setParams(new Object[] { map });
    cat.sampleVal();
  }

  @Test
  public void testNormalization() {
    Categorical cat = new Categorical();
    HashMap<String, Object> map = new HashMap<String, Object>();
    map.put("Bob", 6.0);
    map.put("Craig", 9.0);
    map.put("Albert", 15.0);
    cat.setParams(new Object[] { map });
    testCategorical1(cat);
  }

  @Test
  public void testDoubleSet() {
    Categorical cat = new Categorical();
    HashMap<String, Object> map = new HashMap<String, Object>();
    map.put("Albert", 0.5);
    map.put("Bob", 0.2);
    map.put("Craig", 0.3);
    cat.setParams(new Object[] { null });
    cat.setParams(new Object[] { map });
    testCategorical1(cat);
  }

  @Test
  public void testSetParamsIntegerArguments() {
    Categorical cat = new Categorical();
    HashMap<String, Object> map = new HashMap<String, Object>();
    map.put("Albert", 15);
    map.put("Bob", 6);
    map.put("Craig", 9);
    cat.setParams(new Object[] { map });
    testCategorical1(cat);
  }

  @Test
  public void testGetProbIntegerArguments() {
    // not needed
  }

  @Test
  public void testGetFiniteSupport() {
    Categorical cat = new Categorical();
    HashMap<String, Object> map = new HashMap<String, Object>();
    map.put("Bob", 0.2);
    map.put("Craig", 0.3);
    map.put("Albert", 0.5);
    map.put("Andy", 0.0);
    cat.setParams(new Object[] { map });

    List<Object> list = cat.getFiniteSupport();
    assertEquals(3, list.size());
    assertTrue(list.contains("Bob"));
    assertTrue(list.contains("Craig"));
    assertTrue(list.contains("Albert"));
    // Andy should not exist because its probability is zero
    assertTrue(!list.contains("Andy"));
  }

}
