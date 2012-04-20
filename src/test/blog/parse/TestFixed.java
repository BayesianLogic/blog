/**
 * 
 */
package test.blog.parse;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import blog.symbol.Symbol;
import blog.absyn.*;

/**
 * @author amatsukawa
 * @date Apr 19, 2012
 */
public class TestFixed extends TestParse {
	
	@Test
	public void testFixedConstantDeclaration() {
		{
			String parsed = TestParse.parsedStringRepr("fixed Real a = 1.0;");
			Stmt[] stmts= {
				new FixedFuncDec(0, Symbol.symbol("a"), new NameTy(0, Symbol.symbol("Real")), new DoubleExpr(0, 1.0))
			};
			String shouldBe = TestParse.shouldBeRepr(stmts);
			assertEquals(shouldBe, parsed);
		}
		TestParse.out.reset();
		{
			String parsed = TestParse.parsedStringRepr("fixed Boolean a = true;");
			Stmt[] stmts= {
				new FixedFuncDec(0, Symbol.symbol("a"), new NameTy(0, Symbol.symbol("Boolean")), new BooleanExpr(0, true))
			};
			String shouldBe = TestParse.shouldBeRepr(stmts);
			assertEquals(shouldBe, parsed);
		}
	}

	@Test
	public void testFixedConstantWithExpr() {
		{
			String parsed = TestParse.parsedStringRepr("fixed Real a = 1.0 + b;");
			Stmt[] stmts= {
				new FixedFuncDec(0, Symbol.symbol("a"), new NameTy(0, Symbol.symbol("Real")), 
						new OpExpr(0, new DoubleExpr(0, 1.0), OpExpr.PLUS, new FuncCallExpr(0, Symbol.symbol("b"), null)))
			};
			String shouldBe = TestParse.shouldBeRepr(stmts);
			assertEquals(shouldBe, parsed);
		}
	}
}
