package blog.absyn;
import Symbol.Symbol;
public class CallExp extends Stmt {
   public Symbol func;
   public StmtList args;
   public CallExp(int p, Symbol f, StmtList a) {pos=p; func=f; args=a;}
}
