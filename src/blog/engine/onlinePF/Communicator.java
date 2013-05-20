package blog.engine.onlinePF;

import java.io.PrintStream;

public abstract class Communicator {
	public PrintStream p;
	public abstract void printInput (String message);
	public abstract void printInputNL (String message);
	public abstract String readInput ();
}
