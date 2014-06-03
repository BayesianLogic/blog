/**
 * 
 */
package test.blog.parse;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import blog.absyn.DistributionExpr;
import blog.absyn.ImplicitSetExpr;
import blog.absyn.RandomFuncDec;
import blog.absyn.Stmt;
import blog.absyn.TypeDec;
import blog.symbol.Symbol;

/**
 * @author amatsukawa
 * @date Apr 19, 2012
 * @author William Cushing
 * @date 2013/10/6
 */

// TODO: These still need testing, but this helps me see the tree structure

public class TestIf extends TestParse {
	
	@Test
	public void testSimpleDecl() {
		String toParse = 
				"random Color ObsColor(Draw d) ~ UniformChoice({Ball b});";
		String parsed = parseToRepr(toParse);
		String generated = 
				toRepr(Stmts(new RandomFuncDec(
						Symbol("ObsColor"),
						Fields(Field(
								Symbol("d"),
								Type(Symbol("Draw")))),
						Type(Symbol("Color")),
						Distribution(
								Symbol("UniformChoice"),
								Exprs(ImplicitSet(
												Symbol("b"),
												Type(Symbol("Ball"))
												))
								)
						)));
		assertEquals(generated, parsed);
	}

	@Test
	public void testIfDecl1() {
		String toParse = "random Real test " +
				"if 1 == 2 then ~ Poisson(5) " + 
				"else if 1 == 3 then ~ Poisson(4);";
		String parsed = parseToRepr(toParse);
		System.out.println(parsed);		
	}
	
	@Test
	public void testIfDecl2() {
		String parsed = parseToRepr("random Color test(some d) if Ball(d) == 2 & 1 == 3 then ~ Poisson(5.0);");
		System.out.println(parsed);
	}
	
}
