/**
 * 
 */
package blog.parse;

import blog.absyn.Absyn;

/**
 * @author leili
 * @since Apr 18, 2012
 * 
 */
public class Parse {

	protected blog.msg.ErrorMsg errorMsg;
	protected Absyn absyn;

	public Parse(java.io.Reader inp, blog.msg.ErrorMsg msg) {
		BLOGParser parser;
		try {
			parser = new BLOGParser(new BLOGLexer(inp, errorMsg), errorMsg);
			/* open input files, etc. here */

			parser. /* debug_ */parse();
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
		blog.msg.ErrorMsg errorMsg = new blog.msg.ErrorMsg("STRING" + content.hashCode());
		java.io.Reader inp;
		try {
			inp = new java.io.StringReader(content);
		} catch (Throwable e) {
			throw new Error("String is null!");
		}
		return new Parse(inp, errorMsg);		
	}
	
	
	public Absyn getResult() {
		return absyn;
	}
	
}
