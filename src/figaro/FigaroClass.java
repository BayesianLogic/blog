package figaro;

import java.io.PrintWriter;
import java.util.*;

import figaro.type.Constant;

import blog.msg.ErrorMsg;

/**
 * @author Yi Wu
 * @date Sept 25, 2013
 *  
 *  Storing everything about a class to be generated in Figaro
 */

public class FigaroClass {
	
	/**
	 * symbol   : class name
	 * distinct : instances
	 * feature  : random function taking this class as input parameter
	 * number   : number expression
	 * random   : whether number statement is a distribution 
	 */
	
	String symbol = "";
	ArrayList<String> distinct; 
	ArrayList<Integer> distinctSize;
	Map<String, FigaroFunction> feature;
	Expression number;
	
	boolean random = false;
	
	public boolean randomNumber(){return random;}
	
	public FigaroClass(String _name)
	{
		number = null;
		symbol = _name;
		distinct = new ArrayList<String>();
		distinctSize = new ArrayList<Integer>();
		feature = new TreeMap<String,FigaroFunction>();
	}
	
	public void addObject(String name, int sz)
	{ distinct.add(name); distinctSize.add(sz);}
	
	public void setNumber(Expression expr)
	{ number = expr; }
	
	public Expression getNumber()
	{ return number; }
	
	public void addFeature(String s, FigaroFunction f)
	{ feature.put(s, f);}
	
	public FigaroFunction getFeature(String s)
	{ return feature.get(s);}
	
	public String getSymbol(){return symbol;}
	
	public ArrayList<String> getDistinctList(){return distinct;}
	public ArrayList<Integer> getDistinctSizeList(){return distinctSize;}
	
	public boolean computeType(Figaro fig, FigaroDAG dag, ErrorMsg msg)
	{
		// Set Number Statement
		if(number == null)
		{
			random = false;
			int tot = 0;
			for(int i=0;i<distinctSize.size();++i) tot += distinctSize.get(i);
			if(tot == 0)
			{
				msg.error(-1,-1,"< Number Statement Setting > Error for Class < " + symbol + " >: No distinct nor Number Statement Specified");
				return false;
			}
			number = new Constant(""+tot);
		} else
			random = !(number instanceof Constant);
		
		/*
		 * Add Dependency 
		 *   1. Class Declar
		 *   2. Feature
		 *   3. Number Statement
		 * Check Type
		 *   1. Feature
		 *   2. Number Statement 
		 */
		
		// Add Dependency
		dag.addEdge(symbol, "#"+symbol);
		for(FigaroFunction func : feature.values()) {
			dag.addEdge(symbol, func.getSymbol());
			dag.addEdge(func.getSymbol(), "#"+symbol);
		}
		
		// Check Type
		for(FigaroFunction func : feature.values()) {
			dag.setCurr(symbol, func.getSymbol());
			dag.addCurrParam(func.getParam(0).getSymbol(), func.getParam(0).getType());
			if(!func.computeType(fig, dag, msg))
			{
				msg.error(-1,-1,"Error While Checking Type for Function [ " + func.getSymbol() + " ]");
				return false;
			}
		}
		return true;
	}
	
	
////////////////////////////////////////////////////////
//Debug API
public void printLog(PrintWriter writer, String tab){
	writer.println(tab + "Class < name = " + symbol + ">");
	writer.print(tab + " >Instances: ");
	for(int i=0;i<distinct.size();++i)
		writer.print(distinct.get(i)+"["+distinctSize.get(i)+"], "); writer.println();
	writer.println(tab + " >Number Statement: ");
	
	if(number == null) return ;
	number.printLog(writer, tab + "    ");
	writer.println(tab + " >Features: ");
	for(FigaroFunction func : feature.values())
		func.printLog(writer, tab + "   ");
}
}
