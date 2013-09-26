package figaro;

import java.io.PrintWriter;

import blog.msg.ErrorMsg;

/**
 * @author Yi Wu
 * @date Sept 25, 2013
 *  
 *  Storing Evidence (corresponding to Obs ...)
 */

public class FigaroEvidence {
	Expression e1, e2; // Obs e1 = e2;
	String refer;
	public FigaroEvidence (Expression _e1, Expression _e2)
	{
		e1 = _e1; e2 = _e2; refer = "";
	}
	public Expression getLeft(){return e1;}
	public Expression getRight(){return e2;}
	public String getRefer(){return refer;}
	public void setRefer(String r){refer = r;}
	
	public boolean computeType(Figaro fig, FigaroDAG dag, ErrorMsg msg)
	{
		dag.setCurr("", ""); dag.setSubFinal(true);
		if(!e1.computeType(fig, dag, msg) || !e2.computeType(fig, dag, msg))
			return false;
		return true;
	}
	
////////////////////////////////////////////////////////
// Debug API
public void printLog(PrintWriter writer, String tab){
	writer.println(tab + "Evidence < left = right >");
	writer.println(tab + " >Left Expr: ");
	e1.printLog(writer, tab + "    ");
	writer.println(tab + " >Right Expr: ");
	e2.printLog(writer, tab + "    ");
}

}
