/**
 * 
 */
package test.blog.parse;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import blog.absyn.Absyn;
import blog.absyn.Stmt;
import blog.absyn.TypeDec;
import static blog.symbol.Symbol.Symbol;

/**
 * @author amatsukawa
 * @date Apr 15, 2012
 * @author William Cushing
 * @date 2013/10/6
 *
 */
public class TestType extends TestParse {
	
	@Test
	public void testTypeDeclaration() {
		
		String parsed = parseToRepr("type Test;");
		String generated = toRepr(Stmts(new TypeDec(Symbol("Test"))));
		
		assertEquals(generated, parsed);
	}
	
	@Test
	public void testMultipleTypeDeclarations() {
		
		String parsed = parseToRepr("type Test; type Test2;");
		String generated = 
			toRepr(Stmts(
					new TypeDec(Symbol("Test")),
					new TypeDec(Symbol("Test2"))
					));
		
		assertEquals(generated, parsed);
	}

}
