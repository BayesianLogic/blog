/**
 * 
 */
package blog.ui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;

import blog.common.Util;

/**
 * @author leili
 * @date Apr 29, 2012
 */
public class Shell {

	private Shell(Reader is) {
		this.in = new BufferedReader(is);
	}

	private static final String PROMPT = ">> ";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Util.setSuppressError(true);
		blog.Main.init(args);
		Shell sh = new Shell(new InputStreamReader(System.in));
		Util.setSuppressError(false);
		sh.run();
	}

	private BufferedReader in;
	private PrintStream out = System.out;

	private void run() {
		String input = null;
		StringBuilder stb = new StringBuilder();
		boolean newline = true;
		do {
			if (newline) {
				out.print(PROMPT);
				out.flush();
			}
			try {
				input = in.readLine().trim();
			} catch (Throwable e) {
				e.printStackTrace();
				input = "";
			}
			stb.append(input);
			stb.append("\n");
			if (input.endsWith(";"))
				newline = true;
			else
				newline = false;
			if (input.equalsIgnoreCase("exit"))
				return;
			if (input.startsWith("query") || input.startsWith("Query")) {
				blog.Main.stringSetup(stb.toString());
				blog.Main.run();
				stb = new StringBuilder();
			}
		} while (true);
	}

}
