package blog.type;

public class ARRAY extends Type {
	public Type element;

	public ARRAY(Type e) {
		element = e;
	}

	public boolean coerceTo(Type t) {
		return (t.actual() instanceof ARRAY)
				&& (element.coerceTo(((ARRAY) t).element));
	}
}
