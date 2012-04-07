package blog.absyn;
import Symbol.Symbol;
public class IntExp extends Stmt {
   public int value;
   public IntExp(int p, int v) {pos=p; value=v;}
}
