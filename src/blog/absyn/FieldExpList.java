package blog.absyn;
import Symbol.Symbol;
public class FieldExpList extends Absyn {
   public Symbol name;
   public Stmt init;
   public FieldExpList tail;
   public FieldExpList(int p, Symbol n, Stmt i, FieldExpList t) {pos=p; 
	name=n; init=i; tail=t;
   }
}
