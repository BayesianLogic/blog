package figaro.type;

import java.io.PrintWriter;

import blog.msg.ErrorMsg;
import figaro.Expression;
import figaro.Figaro;
import figaro.FigaroDAG;
import figaro.FigaroFunction;
/**
 * @author Yi Wu
 * @date Sept 25, 2013
 * 
 *  Inherited from Expression
 *  FunctionCall
 */
public class FunctionCallTerm extends Expression{
	String func;
	boolean withinClass;
	public FunctionCallTerm(String f)
	{
		super(); super.setRandom(true);
		func = f;
		withinClass = false;
	}
	public boolean isWithinClass(){return withinClass;}
	public String getFunc(){return func;}
	public void setFunc(String f){func = f;}
	
	//TODO : setRandom();
	
	@Override
	public boolean computeType(Figaro fig, FigaroDAG dag, ErrorMsg msg) {
		/*
		 * Type of functions
		 *   1. Fixed Function
		 *   2. Special Function : isEmptyString
		 *   3. Feature  --> build dependency
		 */
		boolean hasMapping = false;
		for(int i=0;i<super.paramSize();++i) {
			if(!super.getParam(i).computeType(fig, dag, msg)) return false;
			if(super.getParam(i) instanceof MappingTerm) hasMapping=true;
			
			if(super.getParam(i).isNull())
			{
				msg.error(-1,-1,"For FunctionCall < " + func + " > Parameter Cannot Be Null!");
				return false;
			}
		}

		switch(fig.getFuncCategory(func))
		{
		case Figaro.FUNC_SPECIAL : // isEmptyString
			if("isEmptyString".equals(func))
				super.setType("Boolean");
			break;
		case Figaro.FUNC_FIX_FUNCTION:
		{
			FigaroFunction f = fig.getFunction(func);
			super.setType(f.getType());
		}
			break;
		case Figaro.FUNC_FEATURE:
		{
			FigaroFunction f = fig.getFunction(func);
			
			super.setType(f.getType());
			
			// Build Dependency
			String from = f.getBelong();
			if(from.equals(dag.getCurrClass())) {// Inside a Class
				if(dag.getCurrFunc().length() == 0)
				{
					msg.error(-1,-1,"Error When Building Dependency while processing calling of function < " + func + " >");
					return false;
				}
				withinClass = true;
				dag.addEdge(func, dag.getCurrFunc());
			} else // Dependency between two classes
			if(dag.getCurrClass().length() > 0) // itself a feature 
				dag.addEdge("#"+from, dag.getCurrClass());
			else
			if(dag.getCurrFunc().length() > 0) // itself an assignment
				dag.addEdge("#"+from, dag.getCurrFunc());
			else
			if(!dag.isSubFinal()){
				msg.error(-1,-1,"Error When Building Dependency while processing calling of function < " + func + " >");
				return false;
			}
		}
			break;
		default:
			msg.error(-1,-1,"Error on function call of < " + func + " >");
			return false;
		}
		
		if(hasMapping)
		{
			msg.error(-1,-1,"Illegal Appearance of Mapping");
			return false;
		}
		return true;
	}
	
////////////////////////////////////
//Debug API
public String toString(){
StringBuffer buf=new StringBuffer();
buf.append(func+"(");
for(int i=0;i<super.paramSize();++i){
	if(i>0)buf.append(",");
	buf.append(super.getParam(i).toString());
}
buf.append(")");
return buf.toString();
}
public void printLog(PrintWriter writer, String tab){
writer.println(tab + "FunctionCall < " + func+" >");
writer.println(tab + " >Parameter: ");
for(int i=0;i<super.paramSize();++i)
super.getParam(i).printLog(writer, tab+"   ");
}
}
