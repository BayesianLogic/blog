package blog.absyn;
import Symbol.Symbol;
public class RecordExp extends Stmt {
   public Symbol typ;
   public FieldExpList fields;
   public RecordExp(int p, Symbol t, FieldExpList f) {pos=p; typ=t;fields=f;}
}
