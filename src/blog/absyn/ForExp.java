package blog.absyn;
import Symbol.Symbol;
public class ForExp extends Stmt {
   public VarDec var;
   public Stmt hi, body;
   public ForExp(int p, VarDec v, Stmt h, Stmt b) {pos=p; var=v; hi=h; body=b;}
}
