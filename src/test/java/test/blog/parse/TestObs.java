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
import blog.symbol.Symbol;

/**
 * @author amatsukawa
 * @date Apr 19, 2012
 */

//TODO: These still need testing, but this helps me see the tree structure

public class TestObs extends TestParse {

	@Test
	public void testSimpleDecl() {
		String toParse = "obs ObsColor(Draw1) = Blue;";
		String parsed = TestParse.parseToRepr(toParse);
		System.out.println(parsed);
	}

}
