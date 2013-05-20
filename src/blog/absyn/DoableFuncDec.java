package blog.absyn;

import blog.symbol.Symbol;

/**
 * @author Xiang Cheng
 * @date May 2013
 */
public class DoableFuncDec extends FunctionDec {

	/**
	 * for constant
	 * 
	 * @param p
	 * @param n
	 * @param r
	 * @param b
	 */
	public String referenceFuncName;
	public DoableFuncDec(int line, int p, Symbol n, FieldList a, Ty r, Expr b) {
		super(line, p, Symbol.symbol("doable_"+n.toString()), a, r, b);
		referenceFuncName = n.toString();
	}
	
}
