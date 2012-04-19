package test.blog.parse;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import blog.absyn.*;
import blog.parse.Parse;
import blog.symbol.Symbol;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author leili, amatsukawa
 * @date Apr 15, 2012
 * 
 */
public class TestParse {

	public static ByteArrayOutputStream out = new ByteArrayOutputStream();
	public static Printer pr = new Printer(new PrintStream(out));
	

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		TestParse.out.reset();
	}

	@Test
	public void testTypeDeclaration() {
		
		String parsed = TestParse.parsedStringRepr("type Test;");
		
		Stmt[] stmts= {new TypeDec(0, Symbol.symbol("Test"))};
		Absyn shouldBeTree = makeStmtList(stmts);
		shouldBeTree.printTree(pr, 0);
		String shouldBe = out.toString();
		
		assertEquals(shouldBe, parsed);
	}
	
	public static String parsedStringRepr(String s) {
		Parse tester = Parse.parseString(s);
		Absyn parsedTree = tester.getResult();
		parsedTree.printTree(pr, 0);
		String parsed = out.toString();
		out.reset();
		return parsed;
	}
	
	public static StmtList makeStmtList(Stmt[] stmts) {
		StmtList head = null;
		for(int i = stmts.length-1; i > -1; i--) {
			head = new StmtList(stmts[i], head);
		}
		return head;
	}

}
