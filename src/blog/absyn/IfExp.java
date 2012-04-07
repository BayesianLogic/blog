package blog.absyn;
import Symbol.Symbol;
public class IfExp extends Stmt {
   public Stmt test;
   public Stmt thenclause;
   public Stmt elseclause; /* optional */
   public IfExp(int p, Stmt x, Stmt y) {pos=p; test=x; thenclause=y; elseclause=null;}
   public IfExp(int p, Stmt x, Stmt y, Stmt z) {pos=p; test=x; thenclause=y; elseclause=z;}
}
