/**
 * 
 */
package test.blog.distrib;

import java.util.LinkedList;
import static org.junit.Assert.*;

import org.junit.Test;
import blog.distrib.Poisson;
import org.apache.commons.math.distribution.*;

/**
 * @author leili
 *
 */
public class PoissonTest {

	/**
	 * Test method for {@link blog.distrib.Poisson#getLogProb(java.util.List, java.lang.Object)}.
	 */
	@Test
	public void testGetLogProb() {
		LinkedList lambda = new LinkedList();
		lambda.add(new Double(6.0));
		Poisson p = new Poisson(lambda);
		//TO-DO
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link blog.distrib.Poisson#toString()}.
	 */
	@Test
	public void testToString() {
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link blog.distrib.Poisson#getProb(java.util.List, java.lang.Object)}.
	 */
	@Test
	public void testGetProb() {
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link blog.distrib.Poisson#sampleVal(java.util.List, blog.Type)}.
	 */
	@Test
	public void testSampleVal() {
//		fail("Not yet implemented");
	}

}
