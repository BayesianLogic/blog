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
 *  For Set: Only Explicit Set and Implicit Set without condition now
 */
public class SetTerm extends Expression{
	// Here Belong = Type
	// Only Valid for UniformChoice
	public SetTerm(){
		super(); super.setRandom(false);
	}
	
	public SetTerm(String t){
		super(); super.setType(t); super.setRandom(false);
	}
	
	public String getBelong(){return super.getType();}
	public void setBelong(String c){super.setType(c);}
	
	@Override
	public boolean computeType(Figaro fig, FigaroDAG dag, ErrorMsg msg) {
		for(int i=0;i<super.paramSize();++i)
			if(!super.getParam(i).computeType(fig, dag, msg))return false;
		if(super.paramSize() > 0)
			super.setType(super.getParam(0).getType());
		
		// Build Dependency
		if(dag.getCurrClass().length()>0)
		{
			if(dag.getCurrClass().equals(getBelong()))
			{
				msg.error(-1,-1,"Dependency Cyclic on < Set Expression > of Class < " + getBelong() + " >");
				return false;
			}
			dag.addEdge("#"+getBelong(), dag.getCurrClass());
		} else
		if(dag.getCurrFunc().length() > 0)
			dag.addEdge("#"+getBelong(), dag.getCurrFunc());
		return true;
	}
	
	public Variable getParam(int id){return (Variable)super.getParam(id);}
	
	public boolean isExplicit(){return super.paramSize() > 0;}
	public boolean isAllType(){return super.paramSize() == 0;}
	
	
////////////////////////////////////
//Debug API
public String toString(){
if(super.paramSize()==0)return "Set{class = "+super.getType()+"}";
else {
	StringBuffer buf = new StringBuffer();
	buf.append("Set{ class = " + super.getType()+": ");
	for(int i=0;i<super.paramSize();++i)
		buf.append(super.getParam(i).toString()+",");
	buf.append("}");
	return buf.toString();
}
}
public void printLog(PrintWriter writer, String tab){
writer.println(tab + "Set : < class = " + super.getType()+" >");
writer.print(tab+" >Instances: ");
if(super.paramSize()==0)writer.println("__ALL__");else 
	{
		writer.println(" # = "+super.paramSize());
		for(int i=0;i<super.paramSize();++i){
			writer.println(tab+"  ->INS#"+i+":");
			super.getParam(i).printLog(writer, tab+"    ");
		}
	}
}

}
