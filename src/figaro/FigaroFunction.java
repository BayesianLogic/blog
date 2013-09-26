package figaro;

import java.io.PrintWriter;
import java.util.*;

import blog.msg.ErrorMsg;
import figaro.type.*;

/**
 * @author Yi Wu
 * @date Sept 25, 2013
 *  
 *  Storing Information about a BLOG function
 */

public class FigaroFunction {
	
	/**
	 * symbol    : function name
	 * type      : return type
	 * belong    : if this function is a feature (namely taking objects as input parameter), 
	 *                 what type is the corresponding input
	 * param     : parameters
	 * random    : whether it is a random function
	 * expr      : expression
	 */
	
	String symbol, type, belong;
	boolean random;
	ArrayList<Variable> param;
	Expression expr;
	
	FigaroFunction(String _symbol, String _type, boolean _random)
	{
		symbol = _symbol; type = _type; random = _random; belong = "";
		param = new ArrayList<Variable>();
		expr = null;
	}
	
	public int paramSize(){return param.size();}
	public Variable getParam(int id){return param.get(id);}
	public void setParam(int id, Variable v){param.set(id, v);}
	public void addParam(Variable v){param.add(v);}
	
	public Expression getExpr(){return expr;}
	public void setExpr(Expression e){expr = e;}
	
	public String getSymbol(){return symbol;}
	public String getType(){return type;}
	public boolean isRandom(){return random;}
	
	public String getBelong(){return belong;}
	public void setBelong(String r){belong = r;}
	
	boolean computeType(Figaro fig, FigaroDAG dag, ErrorMsg msg)
	{
		/*
		 * Assume  this function is Random Assignment (no input parameter)
		 *   :  type symbol ~ expr
		 */
		
		if(symbol.equals("main"))
		{
			msg.error(-1,-1,"Illegal Function Name for function/assignment : main");
			return false;
		}
		
		return expr.computeType(fig, dag, msg);
	}
	
	
	////////////////////////////////////////////////////////
	// Debug API
	public void printLog(PrintWriter writer, String tab)
	{
		writer.println(tab + "Function < name = " + symbol + " , type = "+type+" >");
		writer.print(tab + " >Parameters: ");
		for(int i=0;i<param.size();++i)
			writer.print("("+param.get(i).getSymbol()+":"+param.get(i).getType()+"), "); writer.println();
		writer.println(tab + " >Expression: ");
		
		expr.printLog(writer, tab + "    ");
	}
}
