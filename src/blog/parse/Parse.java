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

	public Parse() {
	}
	
	public void parseFile(String filename) {
		errorMsg = new blog.msg.ErrorMsg(filename);
		java.io.InputStream inp;
		BLOGParser parser;
		try {
			inp = new java.io.FileInputStream(filename);
		} catch (java.io.FileNotFoundException e) {
			throw new Error("File not found: " + filename);
		}
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
	
	public void parseString(String content) {
		
	}
	
	
	public Absyn getResult() {
		return absyn;
	}
	
}
