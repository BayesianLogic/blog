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
 * @date Apr 15, 2012
 *
 */
public class TestType extends TestParse {
	
	@Test
	public void testTypeDeclaration() {
		
		String parsed = TestParse.parsedStringRepr("type Test;");
		
		Stmt[] stmts= {new TypeDec(0, Symbol.symbol("Test"))};
		String shouldBe = TestParse.shouldBeRepr(stmts);
		
		assertEquals(shouldBe, parsed);
	}
	
	@Test
	public void testMultipleTypeDeclarations() {
		
		String parsed = TestParse.parsedStringRepr("type Test; type Test2;");
		
		Stmt[] stmts= {
				new TypeDec(0, Symbol.symbol("Test")),
				new TypeDec(0, Symbol.symbol("Test2")),
			};
		String shouldBe = TestParse.shouldBeRepr(stmts);
		
		assertEquals(shouldBe, parsed);
	}

}
