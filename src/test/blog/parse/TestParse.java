package test.blog.parse;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;

import blog.absyn.*;
import blog.parse.Parse;
import blog.symbol.Symbol;


/**
 * @author leili, amatsukawa
 * @date Apr 15, 2012
 * @author William Cushing
 * @date 2013/10/6
 */
public class TestParse {

	protected TestParse() {}
	
	public static ByteArrayOutputStream out = new ByteArrayOutputStream();
	public static IndentingPrinter pr = new IndentingPrinter(new PrintStream(out));

	@After
	public static void reset() {
		out.reset();
		pr.reset();
	}
	
	public static String parseToRepr(String s) {
		Parse tester = Parse.parseString(s);
		Absyn parsedTree = tester.getResult();
		parsedTree.printSyntax(pr);
		return out.toString();
	}
	
	public static String toRepr(Stmt... stmts){
		return toRepr(Stmts(stmts));
	}
	public static String toRepr(Absyn x){
		x.printSyntax(pr);
		return out.toString();
	}

	public static SymbolArrayList Symbols(Symbol... xs) { 
		SymbolArray[] ys = new SymbolArray[xs.length];
		for (int i=0; i<xs.length; ++i)
			ys[i] = new SymbolArray(xs[i]);
		return SymbolArrayList.SymbolArrayList(ys);
	}
	public static StmtList Stmts(Stmt... xs) 
	{ return StmtList.StmtList(xs);}
	public static FieldList Fields(Field... xs) 
	{ return FieldList.FieldList(xs);}
	public static ExprList Exprs(Expr... xs) 
	{ return ExprList.ExprList(xs);}

	public static Symbol Symbol(String n) 
	{ return Symbol.Symbol(n);}
	public static Field Field(Symbol n, Ty t) 
	{ return new Field(n,t);}
	public static NameTy Type(Symbol n) 
	{ return new NameTy(n);}
	
	public static OpExpr Expr(int code, Expr l, Expr r)
	{ return new OpExpr(l,code,r); }
	public static DoubleExpr Expr(double x)
	{ return new DoubleExpr(x); }
	public static IntExpr Expr(int x)
	{ return new IntExpr(x); }
	public static BooleanExpr Expr(boolean x)
	{ return new BooleanExpr(x); }
	public static StringExpr Expr(String x)
	{ return new StringExpr(x); }
	public static SymbolExpr Expr(Symbol x)
	{ return new SymbolExpr(x); }
}


