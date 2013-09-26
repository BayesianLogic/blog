package figaro;

import blog.msg.ErrorMsg;

import java.io.PrintWriter;
import java.util.*;


/**
 * @author Yi Wu
 * @date Sept 25, 2013
 * 
 * Basic class for every object storing expressions
 */

public abstract class Expression {
	
	/**
	 * Parameter List (each expression can be considered as a function)
	 *   > param  : parameter list
	 *   > refer  : what should be displayed when this expression being called
	 *   > type   : return type
	 *   
	 *   > random : whether refer is a distribution (note val a = 1 is not, but val a = Constant(1) is)
	 *   > canNull: whether refer is possible to be NULL
	 */
	ArrayList<Expression> param; 
	String refer, type; 
	boolean random, canNull;
	
	public Expression()
	{
		param = new ArrayList<Expression>();
		refer = ""; type = ""; random = false; canNull = false;
	}
	
	public boolean isNull(){return false;};
	public boolean likelyNull(){return canNull;}
	public void setLikelyNull(boolean r){canNull=r;}
	
	public boolean addParam(Expression e){param.add(e); return true;}
	public int paramSize(){return param.size();}
	public Expression getParam(int id){return param.get(id);}
	public void setParam(ArrayList<Expression> list){param = list;}
	public void setParam(int id, Expression expr){param.set(id, expr);}
	
	// Used to Generate Type Recursively
	public abstract boolean computeType(Figaro fig, FigaroDAG dag, ErrorMsg msg);
	
	public String getRefer(){return refer;}
	public void setRefer(String r){refer = r;}
	
	public String getType(){return type;}
	public void setType(String t){type = t;}
	
	public boolean isRandom(){return random;}
	public void setRandom(boolean r){random = r;}
	
	
////////////////////////////////////
// Debug API
	abstract public String toString();
	abstract public void printLog(PrintWriter writer, String tab);
}
