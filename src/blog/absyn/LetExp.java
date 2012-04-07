package blog.absyn;
import Symbol.Symbol;
public class LetExp extends Stmt {
   public DecList decs;
   public Stmt body;
   public LetExp(int p, DecList d, Stmt b) {pos=p; decs=d; body=b;}
}
