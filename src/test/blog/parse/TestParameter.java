package test.blog.parse;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import blog.absyn.NameTy;
import blog.absyn.ParameterDec;
import blog.absyn.Stmt;
import blog.symbol.Symbol;

/**
 * @author amatsukawa
 * @date Apr 15, 2012
 * 
 */

public class TestParameter extends TestParse {

	@Test
	public void testParamDeclaration() {
		
		String parsed = TestParse.parsedStringRepr("param Real a;");
		
		Stmt[] stmts= {
			new ParameterDec(0, new NameTy(0, Symbol.symbol("Real")), Symbol.symbol("a"), null)
		};
		String shouldBe = TestParse.shouldBeRepr(stmts);
		
		assertEquals(shouldBe, parsed);
	}
	
	@Test
	public void testMultipleParamDeclarations() {
		
		String parsed = TestParse.parsedStringRepr("param Real a; param NaturalNum x;");
		
		Stmt[] stmts= {
			new ParameterDec(0, new NameTy(0, Symbol.symbol("Real")), Symbol.symbol("a"), null),
			new ParameterDec(0, new NameTy(0, Symbol.symbol("NaturalNum")), Symbol.symbol("x"), null)
		};
		String shouldBe = TestParse.shouldBeRepr(stmts);
		
		assertEquals(shouldBe, parsed);
	}
	
	// TODO: Parameter declaration with an expression
	
}
