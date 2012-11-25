/**
 * 
 */
package test.blog;

import org.junit.Before;
import org.junit.Test;

import blog.Main;

/**
 * @author leili
 * @since Nov 25, 2012
 * 
 */
public class TestArray {

	private static String[] args = { "-n", "1" };

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testMultiVariateGaussian() {
		String code = "fixed Real[] mu = [10; 10; 10]; \n"
				+ "fixed Real[] sigma = [10, 0, 0; 0, 10, 0; 0, 0, 10];\n"
				+ "random Real[] x ~ MultivarGaussian(mu, sigma); \n" + "query x;";
		Main.runFromString(code, args);
	}

	@Test
	public void testArrayPlus() {
		String code = "fixed Real[] x = [1.0, 2.0, 3.0]; "
				+ "fixed Real[] y = [4.0, 5.0, 6.0]; query x + y; ";
		Main.runFromString(code, args);
	}

}
