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
		
		String parsed = TestParse.parseToRepr("param Real a;");
		
		Stmt[] stmts= {
			new ParameterDec(new NameTy(Symbol("Real")), Symbol("a"), null)
		};
		String shouldBe = TestParse.toRepr(stmts);
		
		assertEquals(shouldBe, parsed);
	}
	
	@Test
	public void testMultipleParamDeclarations() {
		
		String parsed = TestParse.parseToRepr("param Real a; param NaturalNum x;");
		
		Stmt[] stmts= {
			new ParameterDec(0, new NameTy(0, Symbol.Symbol("Real")), Symbol.Symbol("a"), null),
			new ParameterDec(0, new NameTy(0, Symbol.Symbol("NaturalNum")), Symbol.Symbol("x"), null)
		};
		String shouldBe = TestParse.toRepr(stmts);
		
		assertEquals(shouldBe, parsed);
	}
	
	// TODO: Parameter declaration with an expression
	
}
