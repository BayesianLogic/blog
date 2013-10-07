package blog.absyn;

import blog.symbol.Symbol;

/**
 * @author leili
 * @date Apr 22, 2012
 */
public class SymbolExpr extends Expr {
    public Symbol name;

    public SymbolExpr(Symbol n) {this(0,n);}
    public SymbolExpr(int p, Symbol n) {
        this(0, p, n);
    }

    public SymbolExpr(int line, int col, Symbol n) {
        super(line, col);
        name = n;
    }


	@Override
    public void printTree(Printer pr, int d) {
        pr.indent(d);
        pr.sayln("SymbolExpr(");
        pr.indent(d);
        pr.say(name.toString());
        pr.say(")");
    }
}
