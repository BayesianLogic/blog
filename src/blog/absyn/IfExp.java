package blog.absyn;
import blog.symbol.Symbol;
public class IfExp extends Expr {
   public Stmt test;
   public Stmt thenclause;
   public Stmt elseclause; /* optional */
   public IfExp(int p, Stmt x, Stmt y) {pos=p; test=x; thenclause=y; elseclause=null;}
   public IfExp(int p, Stmt x, Stmt y, Stmt z) {pos=p; test=x; thenclause=y; elseclause=z;}
}
