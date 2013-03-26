/**
 * 
 */
package blog.engine.onlinePF.parser;

import blog.engine.onlinePF.FileCommunicator;
import blog.engine.onlinePF.PolicyModel;
import blog.engine.onlinePF.QueryResult;
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
		PolicyModel p = PolicyModel.policyFromFile("/home/saasbook/git/dblog/src/blog/engine/onlinePF/parser/test_policy");
		String queries = p.getQueries(0);
		FileCommunicator fc = new FileCommunicator("/home/saasbook/git/dblog/src/blog/engine/onlinePF/parser/fcf");
		fc.printInput("pos(@0)==0	[true:1.000000000]\n-----pos(@0)==1	[true:0.000000000]\n-----");
		String qr = "";
		String t = fc.readInput();
		while (t != null){
			qr = qr+t;
			t = fc.readInput();
		}
		
		QueryResult q = new QueryResult(qr, 0);
		String x = p.getDecisions(q);
		if (!x.equals("decide applied_action(up,@0)=true;"))
			System.err.print("brokeeeeeeeeeeeeeeeeeeeee");
		else
			System.out.print("workessssssssssss");
		int y= 1;
	}
}
