package blog.rodrigoexperiments.flexiblesampling;

import java.util.*;

import blog.bn.VarWithDistrib;
import blog.world.PartialWorld;

public interface SamplerFactory {
	public Sampler make(VarWithDistrib seedVar, PartialWorld world,
			Set beingExternallyInstantiatedVars);
}
