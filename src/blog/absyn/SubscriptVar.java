package blog.absyn;
import Symbol.Symbol;
public class SubscriptVar extends Var {
   public Var var;
   public Stmt index;
   public SubscriptVar(int p, Var v, Stmt i) {pos=p; var=v; index=i;}
}
