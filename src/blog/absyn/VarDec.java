package blog.absyn;
import Symbol.Symbol;
public class VarDec extends Dec {
   public Symbol name;
   public boolean escape = true;
   public NameTy typ; /* optional */
   public Stmt init;
   public VarDec(int p, Symbol n, NameTy t, Stmt i) {pos=p; name=n; typ=t; init=i;}
}
