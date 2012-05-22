/**
 * 
 */
package test.blog.parse;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import blog.absyn.BooleanExpr;
import blog.absyn.DistinctSymbolDec;
import blog.absyn.DoubleExpr;
import blog.absyn.FieldList;
import blog.absyn.FixedFuncDec;
import blog.absyn.FuncCallExpr;
import blog.absyn.NameTy;
import blog.absyn.OpExpr;
import blog.absyn.Stmt;
import blog.absyn.StringExpr;
import blog.absyn.SymbolArray;
import blog.absyn.SymbolArrayList;
import blog.absyn.SymbolExpr;
import blog.symbol.Symbol;

/**
 * @author amatsukawa
 * @date Apr 19, 2012
 */
public class TestFixed extends TestParse {

	@Test
	public void testFixedConstantDeclaration() {
		{
			String parsed = TestParse.parsedStringRepr("fixed Real a = 1.0;");
			Stmt[] stmts = { new FixedFuncDec(0, Symbol.symbol("a"), new NameTy(0,
					Symbol.symbol("Real")), new DoubleExpr(0, 1.0)) };
			String shouldBe = TestParse.shouldBeRepr(stmts);
			assertEquals(shouldBe, parsed);
		}
		TestParse.out.reset();
		{
			String parsed = TestParse.parsedStringRepr("fixed Boolean a = true;");
			Stmt[] stmts = { new FixedFuncDec(0, Symbol.symbol("a"), new NameTy(0,
					Symbol.symbol("Boolean")), new BooleanExpr(0, true)) };
			String shouldBe = TestParse.shouldBeRepr(stmts);
			assertEquals(shouldBe, parsed);
		}
	}

	@Test
	public void testFixedConstantWithExpr() {
		String parsed = TestParse.parsedStringRepr("fixed Real a = 1.0 + b;");
		Stmt[] stmts = { new FixedFuncDec(0, Symbol.symbol("a"), new NameTy(0,
				Symbol.symbol("Real")), new OpExpr(0, new DoubleExpr(0, 1.0),
				OpExpr.PLUS, new SymbolExpr(0, Symbol.symbol("b")))) };
		String shouldBe = TestParse.shouldBeRepr(stmts);
		assertEquals(shouldBe, parsed);
	}

	@Test
	public void testBuiltinDistinct_String() {
		String parsed = TestParse.parsedStringRepr("fixed String a = \"hello\";");
		Stmt[] stmts = { new FixedFuncDec(0, Symbol.symbol("a"), new NameTy(0,
				Symbol.symbol("String")), new StringExpr(0, "hello")) };
		String shouldBe = TestParse.shouldBeRepr(stmts);
		assertEquals(shouldBe, parsed);
	}

	@Test
	public void testDistinct() {
		String parsed = TestParse.parsedStringRepr("distinct Real a, b;");
		Stmt[] stmts = { new DistinctSymbolDec(0, new NameTy(0,
				Symbol.symbol("Real")), new SymbolArrayList(new SymbolArray(0,
				Symbol.symbol("a")), new SymbolArrayList(new SymbolArray(0,
				Symbol.symbol("b")), null))) };
		String shouldBe = TestParse.shouldBeRepr(stmts);
		assertEquals(shouldBe, parsed);
	}

	@Test
	public void testFixedFunction() {
		String parsed = TestParse
				.parsedStringRepr("fixed Real add(Real a, Real b) = a + b;");
		Stmt[] stmts = { new FixedFuncDec(0, Symbol.symbol("add"), new FieldList(
				Symbol.symbol("a"), new NameTy(0, Symbol.symbol("Real")),
				new FieldList(Symbol.symbol("b"), new NameTy(0, Symbol.symbol("Real")),
						null)), new NameTy(0, Symbol.symbol("Real")), new OpExpr(0,
				new SymbolExpr(0, Symbol.symbol("a")), OpExpr.PLUS,
				new SymbolExpr(0, Symbol.symbol("b")))) };
		String shouldBe = TestParse.shouldBeRepr(stmts);
		assertEquals(shouldBe, parsed);
	}

}
