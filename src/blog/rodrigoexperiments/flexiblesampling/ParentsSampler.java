package blog.rodrigoexperiments.flexiblesampling;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import blog.DependencyModel;
import blog.PartialWorld;
import blog.DependencyModel.Distrib;
import blog.bn.VarWithDistrib;

public class ParentsSampler extends AbstractSampler {

	public ParentsSampler(VarWithDistrib seedVar, PartialWorld world,
			Set beingExternallyInstantiatedVars, SamplerFactory fallbackFactory) {
		super(seedVar, world, beingExternallyInstantiatedVars, fallbackFactory);
	}

	public static Sampler applies(VarWithDistrib seedVar, PartialWorld world,
			Set beingExternallyInstantiatedVars, SamplerFactory fallbackFactory) {
		return new ParentsSampler(seedVar, world, beingExternallyInstantiatedVars,
				fallbackFactory);
	}

	public void sampleStory() {
		DependencyModel.Distrib proposal = seedVar.getDistrib(context);
		setValueStoryPoint(seedVar, proposal);
	}
}
