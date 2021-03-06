package test.blog.parse;

import java.io.ByteArrayOutputStream;

import blog.absyn.Absyn;
import blog.absyn.BooleanExpr;
import blog.absyn.DoubleExpr;
import blog.absyn.Expr;
import blog.absyn.ExprList;
import blog.absyn.Field;
import blog.absyn.FieldList;
import blog.absyn.FuncCallExpr;
import blog.absyn.IntExpr;
import blog.absyn.NameTy;
import blog.absyn.OpExpr;
import blog.absyn.PrettyPrinter;
import blog.absyn.Stmt;
import blog.absyn.StmtList;
import blog.absyn.StringExpr;
import blog.absyn.SymbolArray;
import blog.absyn.SymbolArrayList;
import blog.absyn.TupleSetExpr;
import blog.absyn.Ty;
import blog.parse.Parse;
import blog.symbol.Symbol;

/**
 * @author leili, amatsukawa
 * @date Apr 15, 2012
 * @author William Cushing
 * @date 2013/10/6
 */
public class TestParse {

  /**
   * Takes a string holding a blog model, parses it, and spits it back out
   * in fully-parenthesized form.
   * 
   * @param s
   *          a model in hard-to-parse form
   * @return the same model in easy-to-parse form
   * 
   */
  public static String parseToRepr(String s) {
    Parse tester = Parse.parseString(s);
    Absyn parsedTree = tester.getResult();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrettyPrinter pr = new PrettyPrinter(out);
    pr.printSourceLocations = false;
    pr.printSyntax(parsedTree);
    return out.toString();
  }

  /**
   * This is the same thing as x.toString(),
   * but (maybe) <b>slightly</b> more efficient
   * if many trees are to be dumped to strings, because it holds on
   * to some intermediate objects and reinitializes them,
   * rather than using new() to create entirely new ones.
   * <p>
   * Which may not, in fact, end up being a speed improvement. Java code is
   * idiomatically rife with uses of new(), so high-performance JVM
   * implementations will have invested effort in optimizing frequent
   * allocation+deallocations.
   * <p>
   * It might very well be the case that the use of temporary objects would be
   * faster, and less error-prone...!
   * 
   * @param x
   *          an abstract syntax object
   * @return its string representation
   */
  public static String toRepr(Absyn x) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrettyPrinter pr = new PrettyPrinter(out);
    pr.printSourceLocations = false;
    pr.printSyntax(x);
    return out.toString();
  }

  /**
   * @deprecated Use {@link #toRepr(Absyn)}
   */
  public static String toRepr(Stmt... stmts) {
    return toRepr(Stmts(stmts));
  }

  /* ********************************************************************* */
  /* Helper/factory-like methods for improving readability of test cases */
  /* See testFixed for examples of use. */
  /* ********************************************************************* */

  public static SymbolArrayList Symbols(Symbol... xs) {
    SymbolArray[] ys = new SymbolArray[xs.length];
    for (int i = 0; i < xs.length; ++i)
      ys[i] = new SymbolArray(xs[i]);
    return SymbolArrayList.SymbolArrayList(ys);
  }

  public static StmtList Stmts(Stmt... xs) {
    return StmtList.StmtList(xs);
  }

  public static FieldList Fields(Field... xs) {
    return FieldList.FieldList(xs);
  }

  public static ExprList Exprs(Expr... xs) {
    return ExprList.ExprList(xs);
  }

  public static Symbol Symbol(String n) {
    return Symbol.Symbol(n);
  }

  public static Field Field(Symbol n, Ty t) {
    return new Field(n, t);
  }

  public static NameTy Type(Symbol n) {
    return new NameTy(n);
  }

  public static OpExpr Expr(int code, Expr l, Expr r) {
    return new OpExpr(l, code, r);
  }

  public static DoubleExpr Expr(double x) {
    return new DoubleExpr(x);
  }

  public static IntExpr Expr(int x) {
    return new IntExpr(x);
  }

  public static BooleanExpr Expr(boolean x) {
    return new BooleanExpr(x);
  }

  public static StringExpr Expr(String x) {
    return new StringExpr(x);
  }

  public static FuncCallExpr Expr(Symbol x) {
    return new FuncCallExpr(0, x, null);
  }

  public static FuncCallExpr Distribution(Symbol n, ExprList args) {
    return new FuncCallExpr(0, n, args);
  }

  public static TupleSetExpr TupleSet(Symbol n, Ty t, Expr cond) {
    return new TupleSetExpr(0, Exprs(new FuncCallExpr(0, n, null)),
        new FieldList(n, t, null), cond);
  }

  public static TupleSetExpr TupleSet(Symbol n, Ty t) {
    return TupleSet(n, t, null);
  }
}
