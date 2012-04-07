package blog.absyn;
import Symbol.Symbol;
public class WhileExp extends Stmt {
   public Stmt test, body;
   public WhileExp(int p, Stmt t, Stmt b) {pos=p; test=t; body=b;}
}
