/**
 * 
 */
package test.blog.parse;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import blog.absyn.FuncCallExpr;
import blog.absyn.SymbolExpr;
import blog.absyn.ValueEvidence;

/**
 * @author amatsukawa
 * @date Apr 19, 2012
 * @author leili
 * @date June 21, 2014
 */

public class TestObs extends TestParse {

  @Test
  public void testSimpleDecl() {
    String toParse = "obs ObsColor(Draw1) = Blue;";
    String parsed = parseToRepr(toParse);
    String generated = toRepr(Stmts(new ValueEvidence(0, new FuncCallExpr(0,
        Symbol("ObsColor"), Exprs(new SymbolExpr(Symbol("Draw1")))),
        Expr(Symbol("Blue")))));
    assertEquals(parsed, generated);
  }

}
