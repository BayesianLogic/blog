package blog.absyn;
import Symbol.Symbol;
public class VarExp extends Stmt {
   public Var var;
   public VarExp(int p, Var v) {pos=p; var=v;}
}   
