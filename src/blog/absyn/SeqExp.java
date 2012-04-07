package blog.absyn;
import Symbol.Symbol;
public class SeqExp extends Stmt {
   public StmtList list;
   public SeqExp(int p, StmtList l) {pos=p; list=l;}
}
