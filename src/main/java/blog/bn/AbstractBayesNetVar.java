package blog.bn;

import blog.sample.ClassicInstantiatingEvalContext;
import blog.world.PartialWorld;

public abstract class AbstractBayesNetVar implements BayesNetVar {

  public void ensureDetAndSupported(PartialWorld world) {
    ensureDetAndSupported(new ClassicInstantiatingEvalContext(world));
  }
}
