/**
 * 
 */
package blog.sample.modular;

import java.util.LinkedHashMap;
import java.util.List;

import blog.ObjectIdentifier;
import blog.bn.VarWithDistrib;
import blog.distrib.CondProbDistrib;
import blog.model.DependencyModel;
import blog.sample.ClassicInstantiatingEvalContext;
import blog.world.PartialWorld;

/**
 * @author leili
 * 
 */
public class BlockInstantiatingEvalContextImpl extends
		ClassicInstantiatingEvalContext implements BlockInstantiatingEvalContext {

	/**
	 * @param world
	 */
	public BlockInstantiatingEvalContextImpl(PartialWorld world) {
		super(world);
	}

	protected BlockInstantiatingEvalContextImpl(PartialWorld world,
			LinkedHashMap respVarsAndContexts) {
		super(world, respVarsAndContexts);
	}

	protected Object instantiate(VarWithDistrib var) {
		var.ensureStable();

		/*
		 * if (Util.verbose()) { System.out.println("Need to instantiate: " + var);
		 * }
		 */

		if (respVarsAndContexts.containsKey(var)) {
			cycleError(var);
		}

		// Create a new "child" context and get the distribution for
		// var in that context.
		respVarsAndContexts.put(var, this);
		ClassicInstantiatingEvalContext spawn = new BlockInstantiatingEvalContextImpl(
				world, respVarsAndContexts);
		spawn.afterSamplingListener = afterSamplingListener;
		DependencyModel.Distrib distrib = var.getDistrib(spawn);
		logProb += spawn.getLogProbability();
		respVarsAndContexts.remove(this);

		// Sample new value for var
		CondProbDistrib cpd = distrib.getCPD();
		List cpdArgs = distrib.getArgValues();
		Object newValue = cpd.sampleVal(cpdArgs, var.getType());
		double probForThisValue = cpd.getProb(cpdArgs, newValue);
		double logProbForThisValue = Math.log(probForThisValue);
		logProb += logProbForThisValue;

		// Assert any identifiers that are used by var
		Object[] args = var.args();
		for (int i = 0; i < args.length; ++i) {
			if (args[i] instanceof ObjectIdentifier) {
				world.assertIdentifier((ObjectIdentifier) args[i]);
			}
		}
		if (newValue instanceof ObjectIdentifier) {
			world.assertIdentifier((ObjectIdentifier) newValue);
		}

		// Actually set value
		world.setValue(var, newValue);

		if (afterSamplingListener != null)
			afterSamplingListener.evaluate(var, newValue, probForThisValue);

		if (staticAfterSamplingListener != null)
			staticAfterSamplingListener.evaluate(var, newValue, probForThisValue);

		/*
		 * if (Util.verbose()) { System.out.println("Instantiated: " + var); }
		 */

		return newValue;
	}

}
