package blog.absyn;

import blog.symbol.Symbol;

/**
 * @author leili
 * @date Apr 22, 2012
 */
public class OriginFuncDec extends FunctionDec {

	/**
	 * for constant
	 * 
	 * @param p
	 * @param n
	 * @param r
	 * @param b
	 */
	public OriginFuncDec(int p, Symbol n, FieldList a, Ty r) {
		super(p, n, a, r, null);
	}

}
