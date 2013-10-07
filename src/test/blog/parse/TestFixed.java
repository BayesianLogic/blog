/**
 * 
 */
package test.blog.parse;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import blog.absyn.DistinctSymbolDec;
import blog.absyn.FixedFuncDec;
import blog.absyn.OpExpr;


/**
 * @author amatsukawa
 * @date Apr 19, 2012
 * @author William Cushing
 * @date 2013/10/6
 */
public class TestFixed extends TestParse {

	@Test
	public void testFixedConstantDeclaration() {
		{
			String parsed = parseToRepr("fixed Real a = 1.0;");
			String generated = toRepr(							
					new FixedFuncDec(
							Symbol("a"), 
							Type(Symbol("Real")), 
							Expr(1.0))
					);
			assertEquals(generated, parsed);
		}
		reset();
		{
			String parsed = parseToRepr("fixed Boolean a = true;");
			String generated = toRepr(
					new FixedFuncDec(
						Symbol("a"), 
						Type(Symbol("Boolean")), 
						Expr(true))
					);
			assertEquals(generated, parsed);
		}
	}

	@Test
	public void testFixedConstantWithExpr() {
		String parsed = parseToRepr("fixed Real a = 1.0 + b;");
		String generated = toRepr( 
				new FixedFuncDec(Symbol("a"), 
						Type(Symbol("Real")), 
						Expr(	OpExpr.PLUS, 
								Expr(1.0),
								Expr(Symbol("b"))
								)));
		assertEquals(generated, parsed);
	}

	@Test
	public void testBuiltinDistinct_String() {
		String parsed = parseToRepr("fixed String a = \"hello\";");
		String generated = toRepr( 
				new FixedFuncDec(Symbol("a"), 
						Type(Symbol("String")), 
						Expr("hello")));
		assertEquals(generated, parsed);
	}

	@Test
	public void testDistinct() {
		String parsed = parseToRepr("distinct Real a, b;");
		String generated = toRepr( 
				new DistinctSymbolDec(
						Type(Symbol("Real")), 
						Symbols(Symbol("a"), 
								Symbol("b")) 
								));
		assertEquals(generated, parsed);
	}

	@Test
	public void testFixedFunction() {
		String parsed = parseToRepr("fixed Real add(Real a, Real b) = a + b;");
		String generated = toRepr( 
				new FixedFuncDec(Symbol("add"), 
						Fields(	Field(Symbol("a"),Type(Symbol("Real"))),
								Field(Symbol("b"),Type(Symbol("Real"))) ),
						Type(Symbol("Real")), 
						Expr(	OpExpr.PLUS,
								Expr(Symbol("a")),
								Expr(Symbol("b")))));
		assertEquals(generated, parsed);
	}

}
