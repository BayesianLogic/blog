/**
 * 
 */
package blog.engine.onlinePF.parser;

import blog.engine.onlinePF.absyn.Absyn;
import java.io.InputStream;

public class Parse {

	protected Absyn absyn;
	protected InputStream inp;

	public Parse(java.io.Reader inp) {
		PolicyParser parser;
		try {
			parser = new PolicyParser(new PolicyLexer(inp));
			parser.parse();
			absyn = parser.parseResult;
		} catch (Throwable e) {
			e.printStackTrace();
			throw new Error(e.toString());
		} finally {
			try {
				inp.close();
			} catch (java.io.IOException e) {
			}
		}
	}

	public static Parse parseFile(String filename) {
		java.io.Reader inp;
		try {
			inp = new java.io.FileReader(filename);
		} catch (java.io.FileNotFoundException e) {
			throw new Error("File not found: " + filename);
		}
		return new Parse(inp);
	}

	public static Parse parseString(String content) {
		java.io.Reader inp;
		try {
			inp = new java.io.StringReader(content);
		} catch (Throwable e) {
			throw new Error("String is null!");
		}
		return new Parse(inp);
	}


	public Absyn getParseResult() {
		return absyn;
	}

	public static void main(String[] args) {
		Parse parse = parseFile("/home/saasbook/git/dblog/src/blog/engine/onlinePF/parser/test_policy");
		int x = 1;
	}
}
