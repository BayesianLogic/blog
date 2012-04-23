/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package blog.sample.modular;

import java.util.Properties;

import blog.BLOGUtil;
import blog.common.Util;
import blog.model.Model;
import blog.sample.LWSampler;
import blog.world.DefaultPartialWorld;

/**
 * 
 * @author leili
 * @date Apr 22, 2012
 */
public class ModularLWSampler extends LWSampler {

	/**
	 * @param model
	 * @param properties
	 */
	public ModularLWSampler(Model model, Properties prop) {
		super(model, prop);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void nextSample() {
		// TODO Auto-generated method stub
		curWorld = getBaseWorld();
		if (curWorld == null)
			curWorld = new DefaultPartialWorld(idTypes);

		weight = supportEvidenceAndCalculateWeight();
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
