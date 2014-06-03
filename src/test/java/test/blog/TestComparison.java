package test.blog;

import org.junit.Test;

import blog.Main;

/**
 * @author awong
 * @since Mar 18, 2013
 * 
 */
public class TestComparison {

  private static String[] args = { "-n", "1" };
	  
  @Test
  public void testLessThan() {
    String code = "fixed Boolean x = 3 < 5;" +
    		"query x;";
    Main.runFromString(code, args);
  }
  
  @Test
  public void testLessThanOrEqual() {
    String code = "fixed Boolean x = 5 <= 5;" +
    		"query x;";
    Main.runFromString(code, args);
  }
  
  @Test
  public void testBounded() {
	String code = "fixed Integer c = 7;" +
			"fixed Boolean x = c > 5 & c >= 7 & c <= 7 & c < 11;" +
			"query x;";
	Main.runFromString(code, args);
  }
}
