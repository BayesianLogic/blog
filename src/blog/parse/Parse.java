/**
 * 
 */
package blog.parse;

import java.io.InputStream;

import blog.absyn.Absyn;
import blog.absyn.IndentingPrinter;
import blog.absyn.Printer;
import blog.msg.ErrorMsg;

/**
 * @author leili
 * @since Apr 18, 2012
 * 
 */
public class Parse {

	protected ErrorMsg errorMsg;
	protected Absyn absyn;
	protected InputStream inp;

	public Parse(java.io.Reader inp, ErrorMsg errorMsg) {
		this.errorMsg = errorMsg;
		BLOGParser parser;
		try {
			parser = new BLOGParser(new BLOGLexer(inp, errorMsg), errorMsg);
			/* open input files, etc. here */

			parser. /* debug_ */parse();
			// modified by leili, only for debug purpose
			// parser.debug_parse();
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
		blog.msg.ErrorMsg errorMsg = new blog.msg.ErrorMsg(filename);
		java.io.Reader inp;
		try {
			inp = new java.io.FileReader(filename);
		} catch (java.io.FileNotFoundException e) {
			throw new Error("File not found: " + filename);
		}
		return new Parse(inp, errorMsg);
	}

	public static Parse parseString(String content) {
		blog.msg.ErrorMsg errorMsg = new blog.msg.ErrorMsg("String: "
		    + content.hashCode());
		java.io.Reader inp;
		try {
			inp = new java.io.StringReader(content);
		} catch (Throwable e) {
			throw new Error("String is null!");
		}
		return new Parse(inp, errorMsg);
	}

	public ErrorMsg getErrorMsg() {
		return errorMsg;
	}

	public Absyn getResult() {
		return absyn;
	}

	public static void main(String[] args) {
		Parse parse = parseFile(args[0]);
		parse.getResult().printSyntax(new IndentingPrinter(System.out));
	}
}
