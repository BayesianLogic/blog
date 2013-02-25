package blog.model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import blog.world.DefaultPartialWorld;
import blog.bn.RandFuncAppVar;
import blog.distrib.ListInterp;
import blog.sample.EvalContext;
import blog.sample.InstantiatingEvalContext;

public class DecisionFunction extends Function {

	public DecisionFunction(String fname, List arg_types, Type ret_type) {
		super(fname, arg_types, ret_type);
	}

	public void addInterp (FuncAppTerm fat, InstantiatingEvalContext context){
		ArgSpec[] args = fat.getArgs();
		Object[] argValues = new Object[args.length];
		for (int i = 0; i < args.length; ++i) {
			argValues[i] = args[i].evaluate(context);
		}
		Set interpInContext = ((DefaultPartialWorld)context.getPartialWorld()).getDecisionInterp();
		//DefaultPartialWorld thisWorld = ((DefaultPartialWorld)context.getPartialWorld());
		interpInContext.add(new InterpToken(this, argValues));
		return;
	}
	
	public Object getValueInContext(Object[] args, EvalContext context,
			boolean stable) {
		Set interpInContext = ((DefaultPartialWorld)context.getPartialWorld()).getDecisionInterp();
		//DefaultPartialWorld thisWorld = ((DefaultPartialWorld)context.getPartialWorld());
		return Boolean.valueOf(interpInContext.contains(new InterpToken(this, args)));
	}

	
	class InterpToken {
		DecisionFunction f;
		Object[] args;
		
		public InterpToken(DecisionFunction f, Object[] args) {
			this.f = f;
			this.args = args;
		}
		public boolean equals(Object obj) {
			if (obj instanceof InterpToken) {
				InterpToken other = (InterpToken) obj;
				return ((f == other.f) && Arrays.equals(args, other.args));
			}
			return false;
		}
		public int hashCode() {
			int code = f.hashCode();
			for (int i = 0; i < args.length; ++i) {
				code ^= args[i].hashCode();
			}
			return code;
		}
		
	}
}
