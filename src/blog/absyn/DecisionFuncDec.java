package blog.absyn;

import blog.symbol.Symbol;

/*added by cheng*/
/**
 * @author cheng
 * @date Jan 10, 2012
 */
public class DecisionFuncDec extends FunctionDec {

	/**
	 * for constant
	 * 
	 * @param p
	 * @param n
	 * @param r
	 * @param b
	 */
	public DecisionFuncDec(int line, int p, Symbol n, FieldList a, Ty r) {
		super(line, p, n, a, r, null);
	}

	public DecisionFuncDec(int p, Symbol n, FieldList a, Ty r) {
		this(0, p, n, a, r);
	}

}
