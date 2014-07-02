package blog.type;

public class INT extends Type {
	public INT () {}
	public boolean coerceTo(Type t) {return (t.actual() instanceof INT);}
}

