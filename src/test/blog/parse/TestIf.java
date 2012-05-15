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
		String parsed = TestParse.parsedStringRepr("Random real test ~ Poisson(5);");
	}

	@Test
	public void testIfDecl() {
		String parsed = TestParse.parsedStringRepr("Random real test if 1 == 2 then ~ Poisson(5);");
		parsed = TestParse.parsedStringRepr("Random real test if 1 == 2 then ~ Poisson(5) else ~ Poisson(4);");
		System.out.println(parsed);
	}
	
}
