/**
 * 
 */
package test.blog.parse;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import blog.absyn.Stmt;
import blog.absyn.TypeDec;
import blog.symbol.Symbol;

/**
 * @author amatsukawa
 * @date Apr 19, 2012
 */

// TODO: These still need testing, but this helps me see the tree structure

public class TestIf extends TestParse {
	
	@Test
	public void testSimpleDecl() {
		String toParse = "random Color ObsColor(Draw d) " + 
				"~ Categorical({Blue -> 0.8, Green -> 0.2});";
		String parsed = TestParse.parsedStringRepr(toParse);
		System.out.println(parsed);
	}

	@Test
	public void testIfDecl() {
		String toParse = "random Real test " +
				"if 1 == 2 then ~ Poisson(5) " + 
				"else if 1 == 3 then ~ Poisson(4);";
		String parsed = TestParse.parsedStringRepr(toParse);
		parsed = TestParse.parsedStringRepr("Random Color test(some d) if Ball(d) == 2 & 1 == 3 then ~ Poisson(5.0);");
		System.out.println(parsed);
	}
	
}
