package blog;

import java.util.Set;

public abstract class AbstractBayesNetVar implements BayesNetVar {

    public void ensureDetAndSupported(PartialWorld world) {
	ensureDetAndSupported(new ClassicInstantiatingEvalContext(world));
    }
}
