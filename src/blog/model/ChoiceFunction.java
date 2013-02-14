package blog.model;

import java.util.Arrays;
import java.util.List;

import blog.distrib.ListInterp;
import blog.sample.InstantiatingEvalContext;

public class ChoiceFunction extends NonRandomFunction {

	public ChoiceFunction(String fname, List arg_types, Type ret_type) {
		super(fname, arg_types, ret_type);
		this.setInterpretation(new ListInterp(arg_types.size()));
	}

	public void addInterp (FuncAppTerm fat, InstantiatingEvalContext context){
		ArgSpec[] args = fat.getArgs();
		Object[] argValues = new Object[args.length];
		for (int i = 0; i < args.length; ++i) {
			argValues[i] = args[i].evaluate(context);
		}
		((ListInterp) getInterpretation()).add(Arrays.asList(argValues));
		return;
	}
}
