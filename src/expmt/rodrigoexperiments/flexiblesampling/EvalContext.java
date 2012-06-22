package expmt.rodrigoexperiments.flexiblesampling;

import java.util.*;

import expmt.rodrigoexperiments.flexiblesampling.Sampler.Sample;

import blog.bn.BasicVar;
import blog.bn.VarWithDistrib;
import blog.common.HashSetDiff;
import blog.sample.DefaultEvalContext;
import blog.sample.InstantiatingEvalContext;
import blog.world.PartialWorld;

public class EvalContext extends DefaultEvalContext implements
		InstantiatingEvalContext {
	public EvalContext(PartialWorld world, Set beingExternallyInstantiatedVars,
			SamplerFactory fallbackFactory) {
		super(world);
		this.beingExternallyInstantiatedVars = beingExternallyInstantiatedVars;
		this.fallbackFactory = fallbackFactory;
	}

	public Object getValue(BasicVar var) {
		Object value = world.getValue(var);
		if (value == null) {
			if (var instanceof VarWithDistrib) {
				value = instantiate((VarWithDistrib) var);
			} else {
				throw new IllegalArgumentException("Don't know how to instantiate: "
						+ var);
			}
		}
		return value;
	}

	private Object instantiate(VarWithDistrib var) {
		if (beingExternallyInstantiatedVars.contains(var))
			throw new Error("Instantiation of " + var
					+ " requested in order to instantiate itself.");// TODO: maybe a good
																													// idea to tell which
																													// samplers requested
																													// both times.
		Sampler fallback = fallbackFactory.make(var, world,
				beingExternallyInstantiatedVars);
		if (fallback == null)
			throw new Error("Fallback of " + this + " does not know how to sample "
					+ var + " in " + world);
		Sample fallbackResult = fallback.sample();
		weight *= fallbackResult.getWeight(); // bring in whatever importance
																					// sampling went on there.
		world = fallbackResult.getPartialWorld();
		return world.getValue(var);
	}

	public double getWeight() {
		return weight;
	}

	public boolean isInstantiated(BasicVar var) {
		return (world.getValue(var) != null);
	}

	private Set beingExternallyInstantiatedVars;
	private SamplerFactory fallbackFactory;
	private double weight = 1;
}
