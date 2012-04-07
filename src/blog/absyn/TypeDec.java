package blog.absyn;
import Symbol.Symbol;
public class TypeDec extends Dec {
   public Symbol name;
   public Ty ty;
   public TypeDec next;
   public TypeDec(int p, Symbol n, Ty t, TypeDec x) {pos=p; name=n; ty=t; next=x;}
}
