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

/**
 * Represents decision functions, which have the following properties:
 * 	- Contains no probability distribution/dependency statement, and does not affect the weight of possible worlds
 * 	- By default, it is false for all arguments
 *  - The function interpretation is updated online by decisionEvidenceStatements of the form f(args) = true;
 * 
 * @see blog.model.Function
 */
public class DecisionFunction extends Function {

	/**
	 * Constructor for DecisionFunction
	 * @param fname name of the decision function
	 * @param arg_types types of arguments
	 * @param ret_type type of return value
	 */
	public DecisionFunction(String fname, List arg_types, Type ret_type) {
		super(fname, arg_types, ret_type);
	}

	/**
	 * Function for updating the interpretation of the decision function for a single eval context
	 * 
	 * @param fat the funcAppTerm (must be a decisionfunction) whose interpretation is to be updated
	 * @param context the context to which the interpretation will be added
	 */
	public static void addInterp (FuncAppTerm fat, InstantiatingEvalContext context){
		ArgSpec[] args = fat.getArgs();
		Object[] argValues = new Object[args.length];
		for (int i = 0; i < args.length; ++i) {
			argValues[i] = args[i].evaluate(context);
		}
		Set interpInContext = ((DefaultPartialWorld)context.getPartialWorld()).getDecisionInterp();
		//DefaultPartialWorld thisWorld = ((DefaultPartialWorld)context.getPartialWorld());
		interpInContext.add(new DecisionFuncAppVar((DecisionFunction) fat.getFunction(), argValues));
		return;
	}
	
	/**
	 * @param args an array of argument objects
	 * @param context the context in which the DecisionFunction will be evaluated
	 * @return The value of the decision function in the specified context
	 */
	public Object getValueInContext(Object[] args, EvalContext context,
			boolean stable) {
		Set interpInContext = ((DefaultPartialWorld)context.getPartialWorld()).getDecisionInterp();
		//DefaultPartialWorld thisWorld = ((DefaultPartialWorld)context.getPartialWorld());
		return Boolean.valueOf(interpInContext.contains(new DecisionFuncAppVar(this, args)));
	}

	/**
	 * An token used to keep track of additions to the decisionfunction's interpretation.
	 * This will be added to a HasSet in the context.
	 * Note that despite its name, it does not actually extend AbstactBayesNetVar because it serves no purpose other than as stated above.  
	 * @author cheng
	 *
	 */
	static class DecisionFuncAppVar {
		DecisionFunction f;
		Object[] args;
		
		public DecisionFuncAppVar(DecisionFunction f, Object[] args) {
			this.f = f;
			this.args = args;
		}
		public String toString() {
			if (args.length == 0) {
				return f.toString();
			}

			StringBuffer buf = new StringBuffer();
			buf.append(f);
			buf.append("(");
			buf.append(args[0]);
			for (int i = 1; i < args.length; ++i) {
				buf.append(", ");
				buf.append(args[i]);
			}
			buf.append(")");
			return buf.toString();
		}

		public boolean equals(Object obj) {
			if (obj instanceof DecisionFuncAppVar) {
				DecisionFuncAppVar other = (DecisionFuncAppVar) obj;
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
