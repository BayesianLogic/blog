package blog.absyn;
import Symbol.Symbol;
public class AssignExp extends Stmt {
   public Var var;
   public Stmt exp;
   public AssignExp(int p, Var v, Stmt e) {pos=p; var=v; exp=e;}
}
