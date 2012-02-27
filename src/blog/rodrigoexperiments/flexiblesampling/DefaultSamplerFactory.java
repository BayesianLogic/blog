package blog.rodrigoexperiments.flexiblesampling;

import java.lang.reflect.Method;
import java.util.*;

import blog.PartialWorld;
import blog.VarWithDistrib;

import common.Util;

/**
 * A class responsible for providing the best sampler for a seed variable,
 * given a partial world and a set of variables in the process of
 * being instantiated.
 * It must be constructed with the set of sampler classes to be considered.
 * @author rodrigo
 *
 */
public class DefaultSamplerFactory implements
	SamplerFactory {

    public DefaultSamplerFactory() {
	importanceSamplerClasses = Util.list(ParentsSampler.class);
    }

    public DefaultSamplerFactory(Collection<Class> importanceSamplerClasses) {
	this.importanceSamplerClasses = importanceSamplerClasses;
    }

    public Sampler make(VarWithDistrib seedVar, PartialWorld world, Set beingExternallyInstantiatedVars) {
	for (Class importanceSamplerClass : importanceSamplerClasses) {
	    try {
		Method applies = importanceSamplerClass.getMethod("applies", new Class[] {VarWithDistrib.class, PartialWorld.class, Set.class, SamplerFactory.class});
		Sampler sampler = (Sampler) applies.invoke(null, seedVar, world, beingExternallyInstantiatedVars, this);
		if (sampler != null)
		    return sampler;
	    } catch (Exception e) {
		throw new Error("Could not invoke 'applies' for " + importanceSamplerClass, e);
	    }
	}
	return null;
    }

    private Collection<Class> importanceSamplerClasses;
}
