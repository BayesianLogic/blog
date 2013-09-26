package figaro;

import java.io.*;
import java.util.*;

import blog.msg.ErrorMsg;

/**
 * @author Yi Wu
 * @date Sept 25, 2013
 *  
 *  Basic class storing everything about our target Figaro Model
 */

public class Figaro {
	ErrorMsg errorMsg;
	/**
	 * tokens: the translated tokens
	 * 
	 * Term explanation:
	 *    > fix function : fixed function
	 *    > assignment   : random function without parameters
	 *    > feature      : random function with objects as input parameter
	 *    > belong       : the class the corresponding feature takes as input parameter
	 *    > symbol, order: used for building DAG
	 */
	ArrayList<String> tokens;
	
	ArrayList<FigaroQuery> query;
	ArrayList<FigaroEvidence> evidence;
	
	Map<String, FigaroClass> figaroClass;
	Map<String, String> objectBelong;
	Map<String, Integer> distinctSymbolSize; 
	Map<String, String> distinctSymbolBelong;
	Map<String, String> featureBelong;
	Map<String, FigaroFunction> assignment;
	Map<String, FigaroFunction> fixFunction;
	Map<String, Integer> functionCategory;

	Map<String, Integer> symbolCategory;
	Map<String, String> symbolBelong;
	
	ArrayList<String> symbols, order;
	
	public boolean basicBlogType(String t){
		return "Integer".equals(t) || "Real".equals(t) ||
				"Boolean".equals(t) || "String".equals(t);
	}
	
	public String convertType(String t)
	{
		if("Integer".equals(t)) return "Int";
		if("Real".equals(t)) return "Double";
		return t;
	}
	
	public Figaro()
	{	
		tokens = new ArrayList<String>();
		
		query = new ArrayList<FigaroQuery>();
		evidence = new ArrayList<FigaroEvidence>();
		
		figaroClass = new TreeMap<String,FigaroClass>();
		objectBelong = new TreeMap<String,String>();
		distinctSymbolBelong = new TreeMap<String,String>();
		distinctSymbolSize = new TreeMap<String,Integer>();
		featureBelong = new TreeMap<String,String>();
		assignment = new TreeMap<String, FigaroFunction>();
		fixFunction = new TreeMap<String, FigaroFunction>();
		functionCategory = new TreeMap<String,Integer>();
		
		functionCategory.put("isEmptyString", FUNC_SPECIAL);
		
		symbolCategory = new TreeMap<String,Integer>();
		symbolBelong = new TreeMap<String,String>();
		symbols = new ArrayList<String>();
	}
	
	public void setTokens(ArrayList<String> t){tokens = t;}
	public ArrayList<String> getTokens(){return tokens;}
	
	public void setErrorMsg(ErrorMsg _errorMsg){errorMsg = _errorMsg;}
	
	public FigaroClass getClass(String c)
	{ return figaroClass.get(c);}
	
	public void setClass(String name, FigaroClass c)
	{ figaroClass.put(name, c);}
	
	public void setClassNumberStatement(String s, Expression expr)
	{
		FigaroClass c = figaroClass.get(s);
		c.setNumber(expr);
		figaroClass.put(s, c);
	}
	
	public void addClass(String c)
	{ 
		figaroClass.put(c, new FigaroClass(c)); 
		symbols.add(c);
		symbolCategory.put(c, SYMBOL_CLASS_NAME);
		
		symbols.add("#"+c);
		symbolCategory.put("#"+c, SYMBOL_NUMBER_STMT);
		symbolBelong.put("#"+c,c);
	}
	
	public boolean hasClass(String name)
	{ return figaroClass.containsKey(name); }
	
	public boolean hasDistinctSymbol(String name)
	{ return distinctSymbolSize.containsKey(name); }
	
	public boolean hasObject(String name)
	{ return objectBelong.containsKey(name); }
	
	public boolean addDistinctSymbol(String name, String tp, int sz)
	{
		if(distinctSymbolBelong.containsKey(name)) return false;
		distinctSymbolBelong.put(name, tp);
		distinctSymbolSize.put(name, sz);
		
		FigaroClass c = figaroClass.get(tp);
		c.addObject(name, sz);
		figaroClass.put(tp, c);

		return true;
	}
	
	public int getDistinctSymbolSize(String name)
	{
		if(!distinctSymbolSize.containsKey(name))return 0;
		return distinctSymbolSize.get(name).intValue();
	}
	
	public String getDistinctSymbolBelong(String name)
	{
		if(!distinctSymbolBelong.containsKey(name)) return "";
		return distinctSymbolBelong.get(name);
	}
	
	public boolean addObject(String name, String tp)
	{
		if(objectBelong.containsKey(name)) return false;
		
		objectBelong.put(name, tp);
		
		return true;
	}
	
	public String getObjectBelong(String name)
	{
		if(!objectBelong.containsKey(name))return "";
		return objectBelong.get(name);
	}
	
	public boolean addFunction(FigaroFunction func)
	{
		if(functionCategory.containsKey(func.getSymbol())) return false;
		
		int cat = FUNC_UNKNOWN;
		if(func.isRandom())
		{ // RandomFunction
			if(func.paramSize()==0) // assignment
			{
				assignment.put(func.getSymbol(), func);
				cat = FUNC_ASSIGNMENT;
				
				symbols.add(func.getSymbol());
				symbolCategory.put(func.getSymbol(), SYMBOL_ASSIGNMENT);
				
			} else // feature
			{
				FigaroClass c = figaroClass.get(func.getBelong());
				c.addFeature(func.getSymbol(), func);
				figaroClass.put(func.getBelong(), c);
				cat = FUNC_FEATURE;
				
				featureBelong.put(func.getSymbol(), func.getBelong());
				
				symbols.add(func.getSymbol());
				symbolCategory.put(func.getSymbol(), SYMBOL_FEATURE);
				symbolBelong.put(func.getSymbol(), func.getBelong());
			}
		} else
		{ // Fixed Function
			fixFunction.put(func.getSymbol(), func);
			cat = FUNC_FIX_FUNCTION;
		}
		functionCategory.put(func.getSymbol(), cat);
		return true;
	}
	
	public FigaroFunction getFunction(String func)
	{
		if(!functionCategory.containsKey(func)) return null;
		switch( functionCategory.get(func) ){
		case FUNC_FIX_FUNCTION: return fixFunction.get(func);
		case FUNC_FEATURE: return figaroClass.get(
							featureBelong.get(func)).getFeature(func);
		case FUNC_ASSIGNMENT: return assignment.get(func);
		default: return null;
		}
	}
	
	public int getFuncCategory(String f)
	{
		if(!functionCategory.containsKey(f)) return FUNC_UNKNOWN;
		return functionCategory.get(f).intValue();
	}
	
	public void addEvidence(FigaroEvidence evi)
	{ evidence.add(evi); }
	
	public void addQuery(FigaroQuery q)
	{ query.add(q); }
	
	public String getFeatureBelong(String func)
	{
		if(featureBelong.containsKey(func))
			return featureBelong.get(func);
		else return "";
	}
	
	/////////////////////////////////////////////////////////////////
	// Part of Checking DAG and Type Checking
	/////////////////////////////////////////////////////////////////
	FigaroDAG dag;
	
	public boolean checkType(ErrorMsg msg)
	{
		/* Check Type of All Expressions
		 * Fix Function
		 * Assignment
		 * Class
		 *   1. Instances, Number Statement
		 *   2. Feature
		 *   
		 * Final : setSubFinal(true)  --> this means we do not need to generate dependency
		 *   Query
		 *   Evidence
		 */
		for(FigaroFunction func : fixFunction.values())
		{
			// Note here: Special Mark  ---> Cannot Build Dependency during recursion
			dag.setCurr("","");
			for(int i=0;i<func.paramSize();++i)
				dag.addCurrParam(func.getParam(i).getSymbol(), func.getParam(i).getType());
			if(!func.computeType(this, dag, msg)) return false;
			if(func.isRandom())
			{
				msg.error(-1,-1,"Type detected random for < Fix Function > " + func.getSymbol() );
				return false;
			}
		}
		for(FigaroFunction func : assignment.values()) {
			dag.setCurr("",func.getSymbol());
			if(!func.computeType(this, dag, msg)) return false;
		}
		for(FigaroClass c : figaroClass.values())
			if(!c.computeType(this, dag, msg)) return false;
		
		// Special : No Need to Build Dependency
		for(int i=0;i<evidence.size();++i)
			if(!evidence.get(i).computeType(this,dag,msg)) return false;
					
		for(int i=0;i<query.size();++i)
			if(!query.get(i).computeType(this,dag,msg)) return false;
					
		return true;
	}
	
	public boolean checkDAG(ErrorMsg errorMsg)
	{
		dag = new FigaroDAG(symbols);
		if(!checkType(errorMsg)) return false;
		if(!dag.compute(this)) return false;
		order = dag.getOrder();
		return true;
	}
	
	///////////// API For Translator //////////////////
	
	public ArrayList<String> getOrder(){return order;}
	
	public ArrayList<FigaroQuery> getQueryList(){return query;}
	public ArrayList<FigaroEvidence> getEvidenceList(){return evidence;}
	
	public Collection<FigaroFunction> getFixFunction(){return fixFunction.values();}
	
	/////////////////////////////////////////////
	// All Constants
	// ... For Function Category
	public final static int FUNC_UNKNOWN = 0;
	public final static int FUNC_FIX_FUNCTION = 1;
	public final static int FUNC_FEATURE = 2;
	public final static int FUNC_ASSIGNMENT = 3;
	public final static int FUNC_SPECIAL = 4;
	// ... For Symbols Used in DAG
	public final static int SYMBOL_UNKNOWN = -1;
	public final static int SYMBOL_GENERAL = 0;// Not Used for DAG
	public final static int SYMBOL_CLASS_NAME = 1;
	public final static int SYMBOL_NUMBER_STMT = 2;
	public final static int SYMBOL_FEATURE = 3;
	public final static int SYMBOL_ASSIGNMENT = 4;
	// Note: FIX_FUNCION is not defined here ==> not considered in symbols
	
	////////////////////////////////////////////
	
	final static String[] illegalPat = {"main","val","var","class","extends","Element","ElementCollection",
				"Object","def","new"};
	public boolean isIllegalSymbol(String s)
	{
		for(int i=0;i<illegalPat.length;++i)
			if(s.equals(illegalPat[i])) return true;
		return false;
	}
	
	////////////////////////////////////////////
	// Debug API
	PrintWriter writer;
	public void printLog(File file)
	{
		try {
			writer = new PrintWriter(new FileWriter(file));
		} catch (IOException e) {
			System.out.println("Error When Opening Log file < "+file+" > : " + e);
			return ;
		}
		
		writer.println("Fix Function :");
		for(FigaroFunction func : fixFunction.values())
			func.printLog(writer," ");
		writer.println("");
		
		writer.println("Assignment :");
		for(FigaroFunction func : assignment.values())
			func.printLog(writer," ");
		writer.println("");
		
		writer.println("FigaroClass :");
		for(FigaroClass clss : figaroClass.values())
			clss.printLog(writer," ");
		writer.println("");
		
		writer.println("Observation :");
		for(int i=0;i<evidence.size();++i)
			evidence.get(i).printLog(writer,"");
		writer.println("");
		
		writer.println("Query :");
		for(int i=0;i<query.size();++i)
			query.get(i).printLog(writer," ");
		writer.println("");
		
		writer.println("++++++++++++++++++++++++++");
		writer.println("Orders: " + order);
		
		writer.close();
	}
}
