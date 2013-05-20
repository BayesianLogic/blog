package blog.engine.onlinePF;

import java.util.List;

import blog.model.DependencyModel;
import blog.model.RandomFunction;
import blog.model.Type;

public class DoableRandomFunction extends RandomFunction{

	public DoableRandomFunction(String fname, List arg_types,
			Type ret_type, DependencyModel depmodel) {
		super(fname, arg_types, ret_type, depmodel);
		
		referencedName = fname.substring(11);
		
		
		String rtn = "(";
		int argIndex = 0;
		String functionCall = referencedName + "(";
		for (Object o : arg_types){
			if (! o.toString().equals("Timestep")){
				rtn += "forall " + o.toString() + " " + "argumentOf" + argIndex + " ";
				if (argIndex ==0)
					functionCall += "argumentOf" + argIndex;
				else
					functionCall += ", argumentOf" + argIndex;
				
				argIndex++;
			}
			else{
				if (argIndex ==0)
					functionCall += "t";
				else
					functionCall += ", t";
			}
		}
		functionCall+=")";
		rtn += ("(" + functionCall + " == null | true)");
		rtn +=")";
		queryString = rtn; 
	}
	
	private String makeQueryString(){
		String rtn = "";
		
		return rtn;
	}
	
	String referencedName;
	public final String queryString; //used to force blog to query all function application arguments

}
