package blog.absyn;

import blog.symbol.Symbol;

/**
 * @author leili
 * @date Apr 22, 2012
 */
public class FixedFuncDec extends FunctionDec {

	/**
	 * for constant
	 * 
	 * @param p
	 * @param n
	 * @param r
	 * @param b
	 */
	public FixedFuncDec(int p, Symbol n, FieldList a, Ty r, Expr b) {
		this(0, p, n, a, r, b);
	}

	public FixedFuncDec(int line, int pos, Symbol n, FieldList a, Ty r, Expr b) {
		super(line, pos, n, a, r, b);
	}

	/**
	 * @param i
	 * @param symbol
	 * @param nameTy
	 * @param booleanExpr
	 */
	public FixedFuncDec(int p, Symbol n, NameTy r, Expr b) {
		this(0, p, n, null, r, b);
	}
}
