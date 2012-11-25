/**
 * 
 */
package test.blog;

import org.junit.Test;

import blog.Main;

/**
 * @author leili
 * @since Nov 23, 2012
 * 
 */
public class TestOperator {

	private static String[] args = { "-n", "1" };

	@Test
	public void testConstantPLus() {
		String code = "fixed Integer x = 1 + 2; \n " + "query x;";
		Main.runFromString(code, args);
	}

	@Test
	public void testConstantMult() {
		String code = "fixed Integer x = 1 * 2; \n " + "query x;";
		Main.runFromString(code, args);
	}

	@Test
	public void testConstantDiv() {
		String code = "fixed Real x = 1.0 / 2.0; \n " + "query x;";
		Main.runFromString(code, args);
	}

	@Test
	public void testConstantMinus() {
		String code = "fixed Real x = 1.0 - 2.0; \n " + "query x;";
		Main.runFromString(code, args);
	}

	@Test
	public void testVarPlus() {
		String code = "fixed Integer x = 1; fixed Integer y = 2; fixed Integer z = x + y; "
				+ "query z;";
		Main.runFromString(code, args);
	}

	@Test
	public void testArrayPlus() {
		String code = "fixed Real[] x = [1.0, 2.0, 3.0]; fixed Real[] y = [4.0, 5.0, 6.0]; fixed Real[] z = x + y; "
				+ "query z;";
		Main.runFromString(code, args);
	}

}
