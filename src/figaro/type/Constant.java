package figaro.type;

import java.io.PrintWriter;

import blog.msg.ErrorMsg;
import figaro.*;
/**
 * @author Yi Wu
 * @date Sept 25, 2013
 * 
 *  Inherited from Expression
 *  For Constant: Constant Value, Constant Variable, or NULL
 */
public class Constant extends Expression {
	String value;
	
	public boolean isNull()
	{
		return "null".equals(value);
	}
	
	public Constant(String _value)
	{
		super();
		value = _value;
		if(value.equals("null")) {super.setType(""); super.setLikelyNull(true);}
		else
		if(isInteger())super.setType("Int");
		else
		if(isDouble())super.setType("Double");
		else
		if(isBoolean())super.setType("Boolean");
		else super.setType("String");
	}
	public String getValue(){return value;}
	public void setValue(String v){value = v;}
	
	public boolean isInteger(){
		try { 
	        Integer.parseInt(getValue()); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	    return true;
	}
	
	public int intValue(){
		int ret = 0;
		try { 
	        ret = Integer.parseInt(getValue()); 
	    } catch(NumberFormatException e) {  
	    	return 0;
	    }
	    return ret;
	}
	
	public boolean isDouble(){
		try { 
	        Double.parseDouble(getValue()); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	    return true;
	}

	public double doubleValue(){
		double ret =0;
		try { 
	        ret = Double.parseDouble(getValue()); 
	    } catch(NumberFormatException e) { 
	        return 0; 
	    }
	    return ret;
	}
	
	public boolean isBoolean()
	{
		try { 
	        Boolean.parseBoolean(getValue()); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	    return true;
	}
	
	public boolean basicType()
	{
		return "Int".equals(super.getType()) ||
				"Double".equals(super.getType()) ||
				"Boolean".equals(super.getType());
	}
	
	@Override
	public boolean computeType(Figaro fig, FigaroDAG dag, ErrorMsg msg) {
		return true;
	}
	
	////////////////////////////////////
	//Debug API
	public String toString(){
		return value;
	}
	public void printLog(PrintWriter writer, String tab){
		writer.println(tab + this.toString());
	}
}
