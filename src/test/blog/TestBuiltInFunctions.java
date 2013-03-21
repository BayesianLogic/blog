package test.blog;

import org.junit.Test;

import blog.Main;

/**
 * @author awong
 * @since Mar 18, 2013
 * 
 */
public class TestBuiltInFunctions {

  private static String[] args = { "-n", "1" };
  
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
