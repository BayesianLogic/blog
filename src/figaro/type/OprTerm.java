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
 *  ~ Operator ~
 */
public class OprTerm extends Expression{
	final public static int PLUS = 1,MINUS = 2, MUL = 3, DIV = 4, MOD = 5, POWER = 6; // scala.math.pow(a,b);
	final public static int EQ = 11, NEQ = 12, LT = 13, LEQ = 14, GT = 15, GEQ = 16;
	final public static int AND = 21, OR = 22, NOT = 23;
	final public static int SUB = 31; // Only Distinct Reference, Constant Value Reference
	
	int opr;
	public OprTerm(){
		super();
		opr = 0;
	}

	@Override
	public boolean computeType(Figaro fig, FigaroDAG dag, ErrorMsg msg) {
		for(int i=0;i<super.paramSize();++i) {
			if(!super.getParam(i).computeType(fig, dag, msg)) return false;
			if(super.getParam(i).isNull())
			{
				if(opr != EQ && opr != NEQ)
				{
					msg.error(-1,-1,"Parameter of < " + getSymbol() + " > cannot be NULL");
					return false;
				}
			}
		}
		
		// Check NULL 
		if(opr == EQ || opr == NEQ)
		{
			Expression left = super.getParam(0), right = super.getParam(1);
			if(left.isNull() && right.isNull())
			{
				msg.error(-1,-1,"Both Sides of an Operator Cannot Both become NULL!");
				return false;
			}
			if(left.isNull()) left.setType(right.getType());
			if(right.isNull()) right.setType(left.getType());
		}
		
		// Assume Type Matching Are Correct
		if(isCond()){
			super.setType("Boolean");
		} else
		{
			if(opr == POWER) super.setType("Double");
			else
			{
				String left=super.getParam(0).getType(),right=super.getParam(1).getType();
				if("String".equals(left) || "String".equals(right)) {
					if(opr != PLUS)
					{
						msg.error(-1,-1,"Illegal Operator for String < " + getSymbol() + " >");
						return false;
					}
					super.setType("String");
				}
				else
				if("Double".equals(left) || "Double".equals(right)) super.setType("Double");
				else super.setType("Int");
			}
		}
		return true;
	}
	
	public int getOpr(){return opr;}
	public void setOpr(int o){opr = o;}
	
	public String getSymbol()
	{
		switch(opr)
		{
		case PLUS: return "+"; 
		case MINUS: return "-"; 
		case MUL: return "*"; 
		case DIV: return "/"; 
		case MOD: return "%"; 
		case POWER: return ""; // Special for POWER
		case EQ: return "=="; 
		case NEQ: return "!=";
		case LT: return "<"; 
		case LEQ: return "<="; 
		case GT: return ">"; 
		case GEQ: return ">=";
		case AND: return "&&";
		case OR: return "||"; 
		case NOT: return "!";
		}
		return "";
	}
	
	public boolean isUnary(){return opr == NOT;}
	public boolean isBinary(){return opr != NOT;}
	
	public boolean isNumeric(){return opr == PLUS || opr == MINUS || opr == MUL || opr == DIV || opr == MOD || opr == POWER;}
	public boolean isCompare(){
		return opr == EQ|| opr == NEQ|| opr == LT|| opr == GT|| opr == LEQ|| opr == GEQ; }
	public boolean isBoolean(){
		return opr == AND || opr == OR || opr == NOT; }
	public boolean isCond(){return isBoolean() || isCompare();}
	
////////////////////////////////////
//Debug API
public String toString(){
if(opr==POWER)return "pow("+super.getParam(0).toString()+","+super.getParam(1).toString()+")";
if(opr==NOT)return "NOT("+super.getParam(0).toString()+")";
else
	return "("+super.getParam(0).toString()+")"+getSymbol()+"("+super.getParam(1).toString()+")";
}
public void printLog(PrintWriter writer, String tab){
writer.println(tab + "Operation : Op \""+(opr==POWER?"Math.pow":getSymbol())+"\"");
for(int i=0;i<super.paramSize();++i)
{
	writer.println(tab+"  >Param#"+i +":");
	super.getParam(i).printLog(writer, tab+"     ");
}
}
}
