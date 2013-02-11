package blog.type;

public class DOUBLE extends Type {
	public DOUBLE () {}
	public boolean coerceTo(Type t) {return (t.actual() instanceof DOUBLE);}
}

