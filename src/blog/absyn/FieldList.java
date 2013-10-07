package blog.absyn;

import java.util.ArrayList;
import java.util.ListIterator;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

import blog.absyn.StmtList.Iterator;
import blog.symbol.Symbol;

/**
 * @author leili
 * @date Apr 22, 2012
 * 
 */
public class FieldList extends Absyn implements Iterable<Field> {
	public Field head;
	public FieldList next;

	public FieldList(Symbol name, Ty type, FieldList tail) {
		this(new Field(name,type),tail);
	}
	public FieldList(Field field, FieldList tail) {
		super(field.line,field.col);
		head=field;
		next=tail;
	}
	
	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.sayln("FieldList(");
		head.typ.printTree(pr, d + 1);
		if (head.var != null) {
			pr.sayln(",");
			pr.indent(d + 1);
			pr.say(head.var.toString());
		}
		if (next != null) {
			pr.sayln(",");
			next.printTree(pr, d + 1);
		}
		pr.say(")");
	}
	
	/**
	 * @see StmtList.Iterator
	 */
	public class Iterator implements java.util.Iterator<Field> {
		FieldList curr=null;
		public Iterator(FieldList FieldList) { curr = FieldList; }
		public boolean hasNext() { return curr != null; }
		public Field next() {
			Field o = curr.head;
			curr = curr.next;
			return o;
		}
		public void remove() { throw new UnsupportedOperationException(); }
	}
	public Iterator iterator() { return new Iterator(this);}

	/**
	 * @see StmtList#StmtList(Stmt...)
	 */
	public static FieldList FieldList(Field... xs) {
		FieldList head = null;
		for(int i = xs.length-1; i > -1; --i)
			head = new FieldList(xs[i], head);
		return head;
	}


}
