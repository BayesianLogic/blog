package expmt.rodrigoexperiments.flexiblesampling;

import java.util.*;

import blog.bn.VarWithDistrib;
import blog.model.DependencyModel;
import blog.model.DependencyModel.Distrib;
import blog.world.PartialWorld;

public abstract class AbstractSampler implements Sampler {

	public AbstractSampler(VarWithDistrib seedVar, PartialWorld world,
			Set beingExternallyInstantiatedVars, SamplerFactory fallbackFactory) {
		this.seedVar = seedVar;
		this.world = world;
		this.beingExternallyInstantiatedVars = beingExternallyInstantiatedVars;
		this.fallbackFactory = fallbackFactory;
	}

	public Sample sample() {
		instantiating = true;
		return executeSampleStory();
	}

	public Sample measure() {
		instantiating = false;
		return executeSampleStory();
	}

	public Sample unsample() {
		Sample sample = measure();
		uninstantiateBlockVarsAfterMeasuringWhenUnsampling(); // uninstantiated at
																													// end only because
																													// subsequent
																													// "samplings" may
																													// depend on value of
																													// previous ones.
		return sample;
	}

	private Sample executeSampleStory() {
		weight = 1;
		blockVars = new LinkedList();
		beingExternallyInstantiatedVars.add(seedVar);
		context = new EvalContext(world, beingExternallyInstantiatedVars,
				fallbackFactory);
		sampleStory();
		weight *= context.getWeight();
		beingExternallyInstantiatedVars.remove(seedVar);
		return new DefaultResult(world, weight);
	}

	abstract public void sampleStory();

	protected void setValueStoryPoint(VarWithDistrib var,
			DependencyModel.Distrib proposal) {
		Object value;
		if (instantiating) {
			value = proposal.sampleVal(var);
			world.setValue(var, value);
		} else {
			value = world.getValue(var);
		}

		blockVars.add(var);

		weight /= proposal.getProb(value);

		if (instantiating)
			var.ensureDetAndSupported(context); // seedVar already supported when
																					// uninstantiating.
		weight *= world.getProbOfValue(var);
	}

	protected void uninstantiateBlockVarsAfterMeasuringWhenUnsampling() {
		for (VarWithDistrib var : blockVars) {
			world.setValue(var, null);
		}
	}

	protected SamplerFactory fallbackFactory;
	protected boolean instantiating;
	protected double weight;
	protected Set beingExternallyInstantiatedVars;
	protected Collection<VarWithDistrib> blockVars;
	protected VarWithDistrib seedVar;
	protected PartialWorld world;
	protected EvalContext context;

	public class DefaultResult implements Sample {
		public DefaultResult(PartialWorld world, double weight) {
			this.world = world;
			this.weight = weight;
		}

		public PartialWorld getPartialWorld() {
			return world;
		}

		public double getWeight() {
			return weight;
		}

		public String toString() {
			return getWeight() + ": " + getPartialWorld();
		}

		private PartialWorld world;
		private double weight;
	}
}
