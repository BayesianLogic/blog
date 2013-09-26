package figaro.type;

import java.io.PrintWriter;

import blog.msg.ErrorMsg;
import figaro.Expression;
import figaro.Figaro;
import figaro.FigaroDAG;
/**
 * @author Yi Wu
 * @date Sept 25, 2013
 * 
 *  Inherited from Expression
 *  If ~ then ~ else ~
 *  Note condition and thenclause cannot be NULL 
 */
public class IfTerm extends Expression{
	public IfTerm(){super(); super.setRandom(true);}

	@Override
	public boolean computeType(Figaro fig, FigaroDAG dag, ErrorMsg msg) {
		boolean hasMapping = false;
		for(int i=0;i<super.paramSize();++i) {
			if(!super.getParam(i).computeType(fig, dag, msg)) return false;
			if(super.getParam(i) instanceof MappingTerm) hasMapping=true;
		}

		if(super.getParam(0).isNull() || super.getParam(1).getType().length() == 0
				|| super.getParam(1).isNull())
		{
			msg.error(-1,-1,"Condition or Then Clause of If Cannot be NULL");
			return false;
		}
		
		super.setType(super.getParam(1).getType());
		
		if(super.getParam(2).isNull())
			super.getParam(2).setType(super.getParam(1).getType());
		
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
return "IF("+super.getParam(0).toString()+","+super.getParam(1).toString()+","+super.getParam(2).toString()+")";
}
public void printLog(PrintWriter writer, String tab){
writer.println(tab + "If Statement");
writer.println(tab + " >Cond: ");
super.getParam(0).printLog(writer,tab+"    ");
writer.println(tab + " >Then: ");
super.getParam(1).printLog(writer,tab+"    ");
writer.println(tab + " >Else: ");
super.getParam(2).printLog(writer,tab+"    ");
}
}
