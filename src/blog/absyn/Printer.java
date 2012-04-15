package blog.absyn;

public class Printer {

	java.io.PrintStream out;

	public Printer(java.io.PrintStream o) {
		out = o;
	}

	void indent(int d) {
		for (int i = 0; i < d; i++)
			out.print(' ');
	}

	void say(String s) {
		out.print(s);
	}

	void say(Double s) {
		out.print(s.toString());
	}

	void say(int i) {
		Integer local = new Integer(i);
		out.print(local.toString());
	}

	void say(boolean b) {
		Boolean local = new Boolean(b);
		out.print(local.toString());
	}

	void sayln(String s) {
		say(s);
		say("\n");
	}
}
