package figaro.type;
import java.io.PrintWriter;

import blog.msg.ErrorMsg;
import figaro.*;
/**
 * @author Yi Wu
 * @date Sept 25, 2013
 * 
 *  Inherited from Expression
 *  Variable
 */
public class Variable extends Expression {
	// Variable can be an object or a reference or an assignment
	//          eg. A(1)(2);
	// Note: here < type == belong >
	String symbol, prefix, belong;
	private void setPrefix()
	{
		prefix = symbol;
		for(int i=0;i<symbol.length();++i)
			if(symbol.charAt(i) == '(')
			{
				prefix = symbol.substring(0,i-1);
				break;
			}
	}
	public Variable(String _symbol, String _type) {
		super(); super.setType(_type);
		symbol = _symbol;
		setPrefix();
		belong = "";
	}
	public Variable(String _symbol) {
		super();
		symbol = _symbol;
		setPrefix();
		belong = "";
	}
	public String getSymbol(){return symbol;}
	public void setSymbol(String s){symbol = s;}
	
	public String getPrefix(){return prefix;}
	
	public String getBelong(){return belong;}
	public void setBelong(String b){belong = b;}

	@Override
	public boolean computeType(Figaro fig, FigaroDAG dag, ErrorMsg msg) {
		/*
		 * Variable Type:
		 *   1. Parameter Reference ---> Not Support  TODO: 
		 *   2. Fix Value Reference : FIX_FUNC, FIX_FUNC + subscript
		 *   3. Instance : An Object 
		 *          (1)  Normal Object
		 *          (2)  Object with subscript
		 *   4. Assignment Reference : ASSIGNMENT
		 *   
		 * Note: No Parameter
		 */
		
		if(dag.hasCurrParam(symbol))
		{
			super.setType(dag.getCurrParamType(symbol));
			return true;
		}
		
		if(fig.getFuncCategory(prefix) == Figaro.FUNC_FIX_FUNCTION)
		{ // FIX_FUNC
			if(!prefix.equals(symbol))
			{ // Reference of a fixed Array
				int dim = 0;
				for(int i=0;i<symbol.length();++i)
					if(symbol.charAt(i)=='[')dim++;
				String full = fig.getFunction(prefix).getType();
				// Array[  Array[ + .... + ] + ] ...
				super.setType(full.substring(dim * 6, full.length() - dim));
			} else
				super.setType(fig.getFunction(prefix).getType());
		} else
		if(fig.getFuncCategory(prefix) == Figaro.FUNC_ASSIGNMENT)
		{ // ASSIGNMENT
			FigaroFunction f = fig.getFunction(prefix);
			super.setType(f.getType());
			if(dag.getCurrClass().length() > 0)
				dag.addEdge(prefix, dag.getCurrClass());
			else
			if(dag.getCurrFunc().length() > 0)
				dag.addEdge(prefix, dag.getCurrFunc());
			else
			if(!dag.isSubFinal()){
				msg.error(-1,-1,"Dependency Building Failure! Processing < Variable > symbol = "+symbol);
				return false;
			}
		} else
		if(fig.hasDistinctSymbol(prefix)){
			belong = fig.getDistinctSymbolBelong(prefix);
			super.setType(belong);
			if(dag.getCurrClass().length() > 0) { // Currently A feature
				if(belong.equals(dag.getCurrClass())){ // A Feature Refers an Instance of itself
					// TODO:
					msg.error(-1,-1,"Dependency Failure: We Do Not Support < Feature Function > Refering an Instance of its own Class!");
					return false;
				}
				dag.addEdge("#" + belong, dag.getCurrClass());
			} else
			if(dag.getCurrFunc().length() > 0) // Currently an Assignment
				dag.addEdge("#" + belong, dag.getCurrFunc());
			else
			if(!dag.isSubFinal()){
				msg.error(-1,-1,"Dependency Building Failure! Processing < Variable > symbol = "+symbol);
				return false;
			}
		}
		
		return true;
	}

	
////////////////////////////////////
//Debug API
public String toString(){
return symbol;
}
public void printLog(PrintWriter writer, String tab){
writer.println(tab + "Symbol < "+symbol+" >    prefix = "+prefix);
writer.println(tab+"   > type = "+super.getType()+"  belong = "+getBelong());
}
}
