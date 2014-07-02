package blog.absyn;

import blog.symbol.Symbol;

/**
 * @author leili
 * @date Apr 22, 2012
 */
public class FixedFuncDec extends FunctionDec {

  public FixedFuncDec(Symbol n, FieldList a, Ty r, Expr b) {
    this(0, n, a, r, b);
  }

  public FixedFuncDec(int p, Symbol n, FieldList a, Ty r, Expr b) {
    this(0, p, n, a, r, b);
  }

  public FixedFuncDec(int line, int col, Symbol n, FieldList a, Ty r, Expr b) {
    super(line, col, n, a, r, b);
  }

  /**
   * for constants
   * 
   * @param line
   * @param col
   * @param n
   * @param r
   * @param b
   */
  public FixedFuncDec(int line, int col, Symbol n, Ty r, Expr b) {
    this(line, col, n, null, r, b);
  }

  public FixedFuncDec(int p, Symbol n, Ty r, Expr b) {
    this(0, p, n, r, b);
  }

  public FixedFuncDec(Symbol n, NameTy r, Expr b) {
    this(0, n, r, b);
  }
}
