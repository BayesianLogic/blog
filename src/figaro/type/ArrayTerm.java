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
 *  For Array: BLOG semantics [a1,a2,...]
 */
public class ArrayTerm extends Expression{
	/**
	 * dim      : Array Dimension
	 * cons		: whether it has constants as parameter
	 * vars     : whether has Variables
	 * hasNULL  : whether has NULL entry
	 */
	int dim;
	boolean cons, var, valid, hasNull;
	public ArrayTerm(){
		super();
		super.setType("Array");
		dim = 0;
		cons = false; var = false; valid = true;
	}

	public int getDim(){return dim;}
	public boolean hasConstant(){return cons;}
	public boolean hasVariable(){return var;}
	public boolean isValid(){return (cons ^ var) && valid;}
	public boolean isForCPD(){return isValid() && var;}
	
	public boolean isRandom(){return false;}
	
	public boolean addParam(Expression e)
	{
		if(e instanceof ArrayTerm)
		{
			ArrayTerm t = (ArrayTerm) e;
			if(t.hasVariable()){
				valid=false;
				return false;
			}
			if(dim == 0) dim = t.getDim() + 1;
			else 
			{
				if(dim != t.getDim() + 1 || cons) {
					valid = false;
					return false;
				}
			}
		}else
		{
			if(e instanceof Constant) cons=true;
			else
			if(e instanceof Variable) var = true;
			
			if(dim > 1) {valid=false; return false;}
			dim = 1;
		}
		super.addParam(e);
		return isValid();
	}

	@Override
	public boolean computeType(Figaro fig, FigaroDAG dag, ErrorMsg msg) {
		String mid="";
		for(int i=0;i<super.paramSize();++i) {
			if(!super.getParam(i).computeType(fig, dag, msg)) return false;
			if(!super.getParam(i).isNull() && mid.length()==0)
				mid = super.getParam(i).getType();
		}
		
		if(mid.length() == 0) mid = "Nothing";
		else
			for(int i=0;i<super.paramSize();++i)
				if(super.getParam(i).isNull())
					super.getParam(i).setType(mid);
			
		super.setType("Array["+ mid +"]");
		
		return true;
	}
	
////////////////////////////////////
//Debug API
public String toString(){
	StringBuffer buf = new StringBuffer();
	buf.append("Array(");
	for(int i=0;i<super.paramSize();++i) {
		if(i>0)buf.append(",");
		buf.append(super.getParam(i).toString());
	}
	buf.append(")");
	return buf.toString();
}
public void printLog(PrintWriter writer, String tab){
	writer.println(tab + this.toString());
}
};
