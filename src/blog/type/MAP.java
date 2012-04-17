package blog.type;

public class MAP extends Type {
	public Type from, to;

	public MAP(Type e1, Type e2) {
		from = e1;
		to = e2;
	}

	public boolean coerceTo(Type t) {
		return (t.actual() instanceof MAP) && (from.coerceTo(((MAP) t).from))
				&& (to.coerceTo(((MAP) t).to));
	}
}
