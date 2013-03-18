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
    String code = "fixed Integer x = 1; fixed Integer y = 2; "
        + "fixed Integer z = x + y; query z;";
    Main.runFromString(code, args);
  }

  @Test
  public void testMultiplication() {
    String code = "fixed Real x = 1; fixed Real y = 2; "
        + "fixed Real square = x * x; query square;";
    Main.runFromString(code, args);
  }
  
  @Test
  public void testMultiInFunction() {
    String code = "random Integer qstate(Integer t) {\n" + "\n"
        + "  if t != 0 then\n" + "\n" + "    = t*2\n" + "\n" + "  else\n"
        + "\n" + "    = 0\n" + "\n" + "};"
        + "query qstate(2); query qstate(0);";
    Main.runFromString(code, args);
  }

  @Test
  public void testBoolean() {
    String code = "fixed Boolean x = true; \n fixed Boolean y =! x; \n query y;";
    Main.runFromString(code, args);
  }
  
  @Test
  public void testMin() {
    String code = "fixed Integer small = min({Integer k: k > 3 & k < 7});\n"
        + "query small;";
    Main.runFromString(code, args);
  }

  @Test
  public void testMax() {
    String code = "fixed Integer large = max({Integer k: k > 3 & k < 7});\n"
        + "query large;";
    Main.runFromString(code, args);
  }

  @Test
  public void testRoundLower() {
	String code = "fixed Integer rounded = round(1.4);\n"
        + "query rounded;";
	Main.runFromString(code, args);
  }
  
  @Test
  public void testRoundHigher() {
	String code = "fixed Integer rounded = round(1.5);\n"
        + "query rounded;";
	Main.runFromString(code, args);
  }
}
