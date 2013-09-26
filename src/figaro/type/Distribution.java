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
 *  For Distribution
 */
public class Distribution extends Expression {
	String symbol, origin;
	
	public Distribution(){super();super.setRandom(true);}
	
	//TODO: to support MultivarGaussian and Dirichlet
	final static String[] VALID_NAMES = 
		{"Categorical","Exponential","Gamma","Beta",
		 "Bernoulli","UniformReal","UniformInt",
		 "UniformChoice","Gaussian","Geometric",
		 "Binomial","Poisson","TabularCPD"};// TODO : Move TabularCPD to Distribution
	final static String[] CONVERT_NAMES =
		{"Select","Exponential","Gamma","Beta",
		 "Flip","Uniform","IntSelector",
		 "Uniform","Normal","Geometric",
		 "Binomial","Poisson","CPD"};
	final static String[] RETURN_TYPE =
		{"", "Double","Double","Double",
		 "Boolean", "Double", "Int", 
		 "", "Double", "Int",
		 "Int","Int",""};
	
	public boolean initSymbol(String s)
	{
		origin = s;
		for(int i=0;i<VALID_NAMES.length;++i)
			if(VALID_NAMES[i].equals(s)) {
				super.setType(RETURN_TYPE[i]);
				symbol = CONVERT_NAMES[i];
				return true;
			}
		return false;
	}
	
	public String getSymbol(){return symbol;}
	public String getOrigin(){return origin;}
	
	public boolean isRandom(){return true;}
	
	@Override
	public boolean computeType(Figaro fig, FigaroDAG dag, ErrorMsg msg)
	{
		boolean hasMapping = false;
		for(int i=0;i<super.paramSize();++i) { 
			if(!super.getParam(i).computeType(fig, dag, msg)) return false;
			if(super.getParam(i) instanceof MappingTerm) hasMapping = true;
			
			if(super.getParam(i).isNull())
			{
				msg.error(-1,-1,"Not that for < Distribution : " + symbol + " > Parameter cannot be NULL");
				return false;
			}
		}
		if(super.getType().length() > 0) return !hasMapping;
		
		// TabularCPD, Categorical and UniformChoice
		if(super.paramSize() == 0) return false;
		
		if(origin.equals("TabularCPD"))
		{
			if(super.paramSize() != 2 || !(super.getParam(0) instanceof MappingTerm))
			{
				msg.error(-1,-1,"Error on Parameters of < TabularCPD >");
				return false;
			}
			MappingTerm term = (MappingTerm) super.getParam(0);
			
			super.setType(term.getToType());
			
		} else
		if(origin.equals("UniformChoice")){
			super.setType(super.getParam(0).getType()); // Param(0) instanceof SetTerm
		} else
		{ // Categorical (variable/constant -> weight)
		  // Note: check whether weights sum up to exactly 1
			if(!origin.equals("Categorical"))
			{
				msg.error(-1, -1, "Error When Processing Distribution < " + origin+" >");
				return false;
			}
			
			MappingTerm term = (MappingTerm) super.getParam(0);
			
			double weight = 0;
			for(int i=0;i<term.paramSize();++i) {
				double subW = ((Constant)term.getTo(i)).doubleValue();
				if(subW < 0)
				{
					msg.error(-1,-1,"Weight in Categorical Distribution Cannot be negative!");
					return false;
				}
				weight += subW;
			}
			
			if(weight > 1)
			{
				msg.error(-1,-1,"Sum of Weights in Categorical Distribution Cannot be Greater than 1!");
				return false;
			}
			
			super.setType(term.getFromType());
			if(weight < 1 - 1e-8){
				Constant null_term = new Constant("null");
				null_term.setType(term.getFromType());
				term.addParam(null_term, new Constant(Double.toString(1 - weight)));
			}
			return true;
		}
		return true;
	}
	
	
////////////////////////////////////
//Debug API
public String toString(){
	StringBuffer buf=new StringBuffer();
	buf.append(symbol+"(");
	for(int i=0;i<super.paramSize();++i){
		if(i>0)buf.append(",");
		buf.append(super.getParam(i).toString());
	}
	buf.append(")");
	return buf.toString();
}
public void printLog(PrintWriter writer, String tab){
	writer.println(tab + "Distribution < " + symbol+" > origin = "+origin);
	writer.println(tab + " >Parameter: ");
	for(int i=0;i<super.paramSize();++i)
		super.getParam(i).printLog(writer, tab+"   ");
}
}
