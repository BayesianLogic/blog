package blog.absyn;

import blog.symbol.Symbol;

/**
 * @author leili
 * @date Apr 22, 2012
 */
public class ObservableFuncDec extends FunctionDec {

	/**
	 * for constant
	 * 
	 * @param p
	 * @param n
	 * @param r
	 * @param b
	 */
	public ObservableFuncDec(int line, int p, Symbol n, FieldList a, Ty r, Expr b) {
		super(line, p, Symbol.symbol("observable_"+n.toString()), a, r, b);
	}
	
}
