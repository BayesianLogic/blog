package blog.bn;

import java.util.Set;

import blog.ClassicInstantiatingEvalContext;
import blog.PartialWorld;

public abstract class AbstractBayesNetVar implements BayesNetVar {

	public void ensureDetAndSupported(PartialWorld world) {
		ensureDetAndSupported(new ClassicInstantiatingEvalContext(world));
	}
}
