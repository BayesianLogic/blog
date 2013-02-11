package blog.sample;

import blog.bn.BasicVar;

/**
 * An {@link EvalContext} that instantiates variables as needed, and therefore
 * allows the user to ask whether a variable is yet instantiated or not.
 * {@link ClassicInstantiatingEvalContext} used to have this name but other
 * instantiating contexts were added later so the name was lifted to an
 * interface instead.
 * 
 * @author Rodrigo
 */
public interface InstantiatingEvalContext extends EvalContext {
	public boolean isInstantiated(BasicVar var);
}
