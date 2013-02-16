package blog.model;

import java.util.Arrays;
import java.util.List;
import blog.world.DefaultPartialWorld;
import blog.bn.RandFuncAppVar;
import blog.distrib.ListInterp;
import blog.sample.EvalContext;
import blog.sample.InstantiatingEvalContext;

public class ChoiceFunction extends Function {

	public ChoiceFunction(String fname, List arg_types, Type ret_type) {
		super(fname, arg_types, ret_type);
	}

	public void addInterp (FuncAppTerm fat, InstantiatingEvalContext context){
		ArgSpec[] args = fat.getArgs();
		Object[] argValues = new Object[args.length];
		for (int i = 0; i < args.length; ++i) {
			argValues[i] = args[i].evaluate(context);
		}
		((DefaultPartialWorld)context.getPartialWorld()).getDecisionInterp(this.getSig()).add(Arrays.asList(argValues));
		return;
	}
	
	public Object getValueInContext(Object[] args, EvalContext context,
			boolean stable) {
		ListInterp interpInContext = ((DefaultPartialWorld)context.getPartialWorld()).getDecisionInterp(this.getSig());

		return interpInContext.getValue(Arrays.asList(args));
	}
	
}
