package blog.absyn;
import Symbol.Symbol;
public class ArrayExp extends Stmt {
   public Symbol typ;
   public Stmt size, init;
   public ArrayExp(int p, Symbol t, Stmt s, Stmt i) {pos=p; typ=t; size=s; init=i;}
}
