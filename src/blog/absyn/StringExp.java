package blog.absyn;
import Symbol.Symbol;
public class StringExp extends Stmt {
   public String value;
   public StringExp(int p, String v) {pos=p; value=v;}
}
