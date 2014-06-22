package test.blog.parse;

import java.io.ByteArrayOutputStream;

import blog.absyn.Absyn;
import blog.absyn.BooleanExpr;
import blog.absyn.DistributionExpr;
import blog.absyn.DoubleExpr;
import blog.absyn.Expr;
import blog.absyn.ExprList;
import blog.absyn.Field;
import blog.absyn.FieldList;
import blog.absyn.FuncCallExpr;
import blog.absyn.ImplicitSetExpr;
import blog.absyn.IntExpr;
import blog.absyn.NameTy;
import blog.absyn.OpExpr;
import blog.absyn.PrettyPrinter;
import blog.absyn.Stmt;
import blog.absyn.StmtList;
import blog.absyn.StringExpr;
import blog.absyn.SymbolArray;
import blog.absyn.SymbolArrayList;
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
   * Rather than allocating new printing objects all the time, this class
   * holds on to a PrettyPrinter and its wrapped ByteArrayOutputStream.
   * <p>
   * This makes running tests more efficient, but more fragile; the internal
   * state of these fields must be carefully reset between tests.
   */
  protected TestParse() {
  }

  public static ByteArrayOutputStream out = new ByteArrayOutputStream();
  public static PrettyPrinter pr = new PrettyPrinter(out);
  {
    pr.printSourceLocations = false;
  }

  /**
   * The first call to this method will be a no-op, of course.
   * That is not something that needs to be worried about;
   * much of the point of this class is to save some allocation/deallocation
   * overhead
   * over a great many test cases. Supposing that motivation is valid,
   * then the single wasted first call to reset() is not important.
   */
  public static void reset() {
    out.reset();
    pr.reset();
  }

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
    // the fullproof way to guarantee that mutable state is reset:
    // reset it now!
    reset();

    Parse tester = Parse.parseString(s);
    Absyn parsedTree = tester.getResult();
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
    // the fullproof way to guarantee that mutable state is reset:
    // reset it now!
    reset();
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

  public static DistributionExpr Distribution(Symbol n, ExprList args) {
    return new DistributionExpr(n, args);
  }

  public static ImplicitSetExpr ImplicitSet(Symbol n, Ty t, Expr cond) {
    return new ImplicitSetExpr(t, n, cond);
  }

  public static ImplicitSetExpr ImplicitSet(Symbol n, Ty t) {
    return new ImplicitSetExpr(t, n, null);
  }
}
