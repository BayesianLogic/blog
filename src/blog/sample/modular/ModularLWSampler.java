/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package blog.sample.modular;

import java.util.List;
import java.util.Properties;

import blog.BLOGUtil;
import blog.bn.BayesNetVar;
import blog.bn.VarWithDistrib;
import blog.common.Util;
import blog.distrib.CondProbDistrib;
import blog.model.DependencyModel;
import blog.model.Model;
import blog.model.Query;
import blog.model.Type;
import blog.sample.LWSampler;
import blog.world.PartialWorld;
import blog.world.UninstVarIterator;
import blog.world.WorldInProgress;

/**
 * 
 * @author leili
 * @date Apr 22, 2012
 */
public class ModularLWSampler extends LWSampler {

	private static final int intBound = -1; // TODO parse from Configuration
	private static final int depthBound = -1; // TODO parse from Configuration

	/**
	 * @param model
	 * @param properties
	 */
	public ModularLWSampler(Model model, Properties properties) {
		super(model, properties);
	}

	@Override
	public void nextSample() {
		WorldInProgress curWorld = (WorldInProgress) getBaseWorld();
		if (curWorld == null) {
			curWorld = new WorldInProgress(model, evidence, intBound, depthBound);
			Util.debug("Creating initial possible world");
		}
		this.curWorld = curWorld;

		while (!isCurWorldSufficient(curWorld)) {
			boolean varInst = false;
			for (UninstVarIterator iter = curWorld.uninstVarIterator(); iter
					.hasNext();) {
				VarWithDistrib var = iter.next();
				DependencyModel.Distrib distrib = var
						.getDistrib(new BlockInstantiatingEvalContextImpl(curWorld));
				Util.debug("Instantiating: " + var);
				Type varType = var.getType();
				CondProbDistrib cpd = distrib.getCPD();
				List args = distrib.getArgValues();
				Object value = cpd.sampleVal(args, varType);

				iter.setValue(value);
				varInst = true;
				break;
				// TODO special treatment for number variables
			}

			if (!varInst) {
				System.out.println("World is not complete, but no basic random "
						+ "variable is supported.  Please check for "
						+ "a possible cycle in your model.");
			}
		}

		// TODO compute weight
		if (evidence.isTrue(curWorld)) {
			weight = 1;
		} else
			weight = 0;
		// weight = supportEvidenceAndCalculateWeight();
		BLOGUtil.ensureDetAndSupportedWithListener(queryVars, curWorld,
				afterSamplingListener);

		if (Util.verbose()) {
			System.out.println("Generated world:");
			curWorld.print(System.out);
			System.out.println("Weight: " + weight);
		}

		++totalNumSamples;
		++numSamplesThisTrial;
		if (weight > 0) {
			++totalNumConsistent;
			++numConsistentThisTrial;
		}
		sumWeightsThisTrial += weight;
	}

	private boolean isCurWorldSufficient(PartialWorld world) {
		// TODO leili: find more efficient ways to check sufficient
		if (!evidence.isDetermined(world)) {
			return false;
		}

		for (Query q : queries) {
			for (BayesNetVar var : q.getVariables()) {
				if (!var.isDetermined(world)) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * instantiate
	 * calculate weight of
	 * 
	 * @see blog.sample.LWSampler#supportEvidenceAndCalculateWeight()
	 */
	@Override
	protected double supportEvidenceAndCalculateWeight() {
		// TODO modify to block sampling
		BLOGUtil.setBasicVars(evidence, curWorld);
		BlockInstantiatingEvalContextImpl context = new BlockInstantiatingEvalContextImpl(
				curWorld);
		BLOGUtil.ensureDetAndSupported(evidence.getEvidenceVars(), context);
		return evidence.getEvidenceProb(curWorld);
	}

}
