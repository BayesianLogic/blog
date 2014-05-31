/**
 * 
 */
package test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

import blog.distrib.UniformChoice;
import blog.model.Model;
import blog.objgen.DefaultObjectSet;

/**
 * Unit Tests for Uniform Choice
 */
public class TestUniformChoice extends TestDistribution {

  @Test
  public void testGetProb() {
    Collection<Integer> collection = new ArrayList<Integer>();
    collection.add(1);
    collection.add(3);
    UniformChoice unif = new UniformChoice(constructParams);
    args.add(new DefaultObjectSet(collection));
    assertEquals(0.5, unif.getProb(args, 1), ERROR_BOUND);
    assertEquals(0.5, unif.getProb(args, 3), ERROR_BOUND);
    assertEquals(0.0, unif.getProb(args, 5), ERROR_BOUND);
    // NULL Object Case
    collection.clear();
    args.clear();
    args.add(new DefaultObjectSet(collection));
    assertEquals(1, unif.getProb(args, Model.NULL), ERROR_BOUND);
  }

  @Test
  public void testGetLogProb() {
    Collection<Integer> collection = new ArrayList<Integer>();
    collection.add(1);
    collection.add(3);
    UniformChoice unif = new UniformChoice(constructParams);
    args.add(new DefaultObjectSet(collection));
    assertEquals(Math.log(0.5), unif.getLogProb(args, 1), ERROR_BOUND);
    assertEquals(Math.log(0.5), unif.getLogProb(args, 3), ERROR_BOUND);
    assertEquals(Double.NEGATIVE_INFINITY, unif.getLogProb(args, 5),
        ERROR_BOUND);
    // NULL Object Case
    collection.clear();
    args.clear();
    args.add(new DefaultObjectSet(collection));
    assertEquals(0, unif.getLogProb(args, Model.NULL), ERROR_BOUND);
  }

}
