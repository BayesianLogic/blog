package blog.absyn;

import blog.symbol.Symbol;

/**
 * Distribution declaration
 * 
 * @author leili
 * @date Apr 22, 2012
 * 
 */
public class DistributionDec extends FunctionDec {

  public DistributionDec(int p, Symbol n, FieldList a, Ty r) {
    this(0, p, n, a, r);
  }

  public DistributionDec(int line, int pos, Symbol n, FieldList a, Ty r) {
    super(line, pos, n, a, r, null);
  }

  @Override
  public void printTree(Printer pr, int d) {
    // to be removed
  }
}
