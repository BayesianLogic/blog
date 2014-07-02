package blog.absyn;

/**
 * @author leili
 * @date Apr 22, 2012
 * 
 */
public class ExprList extends Absyn implements Iterable<Expr> {
  public Expr head;
  public ExprList next;

  public ExprList(Expr h, ExprList t) {
    super();
    if (h != null) {
      this.head = h;
      this.line = h.line;
      this.col = h.col;
    }
    next = t;
  }

  @Override
  public void printTree(Printer pr, int d) {
    pr.indent(d);
    pr.say("ExprList(");
    if (head != null) {
      pr.sayln("");
      head.printTree(pr, d + 1);
      if (next != null) {
        pr.sayln(",");
        next.printTree(pr, d + 1);
      }
    }
    pr.say(")");
  }

  /**
   * @see StmtList.Iterator
   */
  public class Iterator implements java.util.Iterator<Expr> {
    ExprList curr = null;

    public Iterator(ExprList ExprList) {
      curr = ExprList;
    }

    public boolean hasNext() {
      return curr != null;
    }

    public Expr next() {
      Expr o = curr.head;
      curr = curr.next;
      return o;
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  public Iterator iterator() {
    return new Iterator(this);
  }

  /**
   * @see StmtList#StmtList(Stmt...)
   */
  public static ExprList ExprList(Expr... xs) {
    ExprList head = null;
    for (int i = xs.length - 1; i > -1; --i)
      head = new ExprList(xs[i], head);
    return head;
  }

}
