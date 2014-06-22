package blog.absyn;

import blog.symbol.Symbol;

/**
 * @author leili
 * @date Apr 22, 2012
 * 
 */
public class DistributionDec extends Dec {
  public Symbol name;
  public Symbol classname;
  public ExprList params;

  public DistributionDec(int p, Symbol n, Symbol cn, ExprList a) {
    this(0, p, n, cn, a);
  }

  public DistributionDec(int line, int col, Symbol n, Symbol cn, ExprList a) {
    super(line, col);
    name = n;
    classname = cn;
    params = a;
  }

  @Override
  public void printTree(Printer pr, int d) {
    pr.indent(d);
    pr.sayln("DistributionDec(");
    pr.indent(d + 1);
    pr.say(name.toString());
    pr.sayln(",");
    pr.say(classname.toString());
    pr.sayln(",");
    params.printTree(pr, d + 1);
  }
}
