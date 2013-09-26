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
 *  Map
 */
public class MappingTerm extends Expression{
	// Especially for
	//    1. CPD
	//    2. Categorical
	String fromType, toType;
	boolean fromNull, toNull;
	public MappingTerm()
	{
		super();super.setRandom(false);super.setType("Map");
		fromType = ""; toType = "";
		fromNull = false; toNull = false;
	}
	
	public boolean getFromNull(){return fromNull;}
	public boolean getToNull(){return toNull;}
	public void setFromNull(boolean s){fromNull=s;}
	public void setToNull(boolean s){toNull=s;}
	
	public int paramSize(){return super.paramSize() / 2;}
	public void addParam(Expression from, Expression to)
		{super.addParam(from); super.addParam(to);}
	public Expression getFrom(int i)
		{return super.getParam(i * 2);}
	public void setFrom(int i, Expression e)
		{super.setParam(i * 2, e);}
	public Expression getTo(int i)
		{return super.getParam(i * 2 + 1);}
	public void setTo(int i, Expression e)
		{super.setParam(i * 2 + 1, e);}
	public String getBelong(){return super.getType();}
	public void setBelong(String b){super.setType(b);}
	
	public String getFromType(){return fromType;}
	public String getToType(){return toType;}
	public void setFromType(String s){fromType = s;}
	public void setToType(String s){toType = s;}

	
	@Override
	public boolean computeType(Figaro fig, FigaroDAG dag, ErrorMsg msg) {	
		boolean hasMapping = false;
		for(int i=0;i<super.paramSize();++i) {
			if(!super.getParam(i).computeType(fig, dag, msg)) return false;
			if(super.getParam(i) instanceof MappingTerm) hasMapping=true;
		}
		// Set From Type and To Type
		
		if(super.paramSize() % 2 == 1 || super.paramSize() < 2)
		{
			msg.error(-1,-1,"Illegal Parameter Number of Mapping!");
			return false;
		}
		
		String fromT = "", toT = "";
		
		for(int i=0;i<paramSize();++i)
		{
			if(!getFrom(i).isNull())
				fromT = getFrom(i).getType();
			
			if(!getTo(i).isNull())
				toT = getTo(i).getType();
		}
		
		setFromType(fromT);
		setToType(toT);
		
		if(fromT.length()==0||toT.length()==0)
		{
			msg.error(-1,-1,"The From elements or To elements of Mapping Cannot All be NULL!");
			return false;
		}
		
		for(int i=0;i<paramSize();++i){
			if(getFrom(i).isNull())
				getFrom(i).setType(fromT);
			if(getTo(i).isNull())
				getTo(i).setType(toT);
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
buf.append("Map{");
for(int i=0;i<paramSize();++i) {
	if(i>0)buf.append(",");
	buf.append(getFrom(i).toString()+" -> " + getTo(i).toString());
}
buf.append("}");
return buf.toString();
}
public void printLog(PrintWriter writer, String tab){
writer.println(tab + "Mapping Term : #sub = " + paramSize()+"  type = < "+getFromType()+" -> " + getToType()+" >");
for(int i=0;i<paramSize();++i){
	writer.println(tab +  "  >sub#"+i+" : from = "+getFrom(i).toString()+"  to = "+getTo(i).toString());
	writer.println(tab +"   --> From:");
	getFrom(i).printLog(writer, tab+"          ");
	writer.println(tab +"   -->   To:");
	getTo(i).printLog(writer, tab+"          ");
}
}
};
