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
	public FixedFuncDec(int p, Symbol n, Ty r, Expr b) {
		this(p, n, null, r, b);
	}

	public FixedFuncDec(int p, Symbol n, FieldList a, Ty r) {
		this(p, n, a, r, null);
	}

	public FixedFuncDec(int p, Symbol n, FieldList a, Ty r, Expr b) {
		super(p, n, a, r, b);
	}

}
