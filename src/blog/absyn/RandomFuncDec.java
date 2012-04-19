package blog.absyn;

import blog.symbol.Symbol;

public class RandomFuncDec extends FunctionDec {

	/**
	 * for constant
	 * 
	 * @param p
	 * @param n
	 * @param r
	 * @param b
	 */
	public RandomFuncDec(int p, Symbol n, Ty r, Expr b) {
		this(p, n, null, r, b);
	}

	public RandomFuncDec(int p, Symbol n, FieldList a, Ty r) {
		this(p, n, a, r, null);
	}

	public RandomFuncDec(int p, Symbol n, FieldList a, Ty r, Expr b) {
		super(p, n, a, r, b);
	}

}
