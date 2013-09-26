package figaro;

import java.io.PrintWriter;

import blog.msg.ErrorMsg;

/**
 * @author Yi Wu
 * @date Sept 25, 2013
 *  
 *  Storing Query Information
 *  
 *     > query expr
 *     statement = expr.toString();
 */

public class FigaroQuery {
	String statement;
	Expression expr;
	
	public FigaroQuery(Expression e, String state)
	{
		expr = e; statement = state;
	}
	
	public String getStatement(){return statement;} 
	
	public Expression getExpr(){return expr;}
	
	public boolean computeType(Figaro fig, FigaroDAG dag, ErrorMsg msg)
	{
		dag.setCurr("", ""); dag.setSubFinal(true);
		if(!expr.computeType(fig, dag, msg))
			return false;
		return true;
	}
	
////////////////////////////////////////////////////////
//Debug API
public void printLog(PrintWriter writer, String tab){
	writer.println(tab + "Query < Expr >");
	writer.println(tab + " >Left Expr: ");
	expr.printLog(writer, tab + "    ");
}
}
