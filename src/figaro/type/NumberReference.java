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
 *  Number Statement
 */
public class NumberReference extends Expression{
	String belong;
	public NumberReference(String type)
	{
		super();
		super.setRandom(true);
		super.setType("Int");
		belong = type;
	}
	
	public String getBelong(){return belong;}

	@Override
	public boolean computeType(Figaro fig, FigaroDAG dag, ErrorMsg msg) {
		// Here No Parameters
		
		// Build Dependency
		
		if(dag.getCurrClass().length() > 0) {
			if(dag.getCurrClass().equals(belong))
			{
				msg.error(-1,-1,"Illegal Number Statement Dependency for Class < " + belong + " >");
				return false;
			}
			dag.addEdge("#"+belong, dag.getCurrClass());
		}
		else
		if(dag.getCurrFunc().length() > 0)
			dag.addEdge("#"+belong, dag.getCurrFunc());
		else
		if(!dag.isSubFinal()){
			msg.error(-1,-1,"Illegal Number Statement Detected while building dependency!");
			return false;
		}
		
		return true;
	}
////////////////////////////////////
//Debug API
public String toString(){
return "(#"+getBelong()+")";
}
public void printLog(PrintWriter writer, String tab){
writer.println(tab + "Number STMT : #<class = "+getBelong()+" >");
}

}
