package blog.absyn;

import blog.symbol.Symbol;

/**
 * @author leili
 * @date Apr 22, 2012
 */
public class RandomFuncDec extends FunctionDec {

	/**
	 * for constant
	 * 
	 * @param p
	 * @param n
	 * @param r
	 * @param b
	 */
	public RandomFuncDec(int line, int p, Symbol n, FieldList a, Ty r, Expr b) {
		super(line, p, n, a, r, b);
	}

	public RandomFuncDec(int p, Symbol n, FieldList a, Ty r, Expr b) {
		this(0, p, n, a, r, b);
	}

}
