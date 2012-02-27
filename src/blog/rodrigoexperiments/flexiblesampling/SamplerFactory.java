package blog.rodrigoexperiments.flexiblesampling;

import java.util.*;

import blog.PartialWorld;
import blog.VarWithDistrib;

public interface SamplerFactory {
    public Sampler make(VarWithDistrib seedVar, PartialWorld world, Set beingExternallyInstantiatedVars);
}
