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

public class TestIf extends TestParse {
	
	@Test
	public void testSimpleDecl() {
		String parsed = TestParse.parsedStringRepr("Random real test(Ball b) ~ Poisson(5);");
	}

	@Test
	public void testIfDecl() {
		String parsed = TestParse.parsedStringRepr("Random Color test if 1 == 2 & 1 == 3 then ~ Poisson(5);");
		String toParse = "random Real test " +
				"if 1 == 2 then ~ Poisson(5) " + 
				"else if 1 == 3 then ~ Poisson(4);";
		parsed = TestParse.parsedStringRepr(toParse);
		System.out.println(parsed);
	}
	
}
