/*
 * Copyright (c) 2012, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.  
 *
 * * Neither the name of the University of California, Berkeley nor
 *   the names of its contributors may be used to endorse or promote
 *   products derived from this software without specific prior 
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package blog.sample;

import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import blog.bn.BayesNetVar;
import blog.bn.VarWithDistrib;
import blog.model.DependencyModel;
import blog.model.Model;
import blog.world.PartialWorld;
import blog.world.PartialWorldDiff;

/**
 * A proposer specifically written for MCMC inference on hurricane.blog.
 * This model contains an RV whose value changes the distributions of
 * its children, a situation where GenericProposer does not account for
 * the resulting changes in probability and therefore does not satisfy
 * the detailed balance equations. 
 * 
 * @author awong
 * @date December 17, 2012
 */
public class HurricaneProposer extends GenericProposer {
	/**
	 * Creates a new HurricaneProposer, configured in the same manner
	 * as GenericProposer.
	 */
	public HurricaneProposer(Model model, Properties properties) {
		super(model, properties);
	}
	
	@Override
	protected void sampleValue(VarWithDistrib varToSample, PartialWorld world) {
		// Save child set before graph becomes out of date
		Set children = world.getCBN().getChildren(varToSample);
		
		if (children.size() < 2) {
			super.sampleValue(varToSample, world);
		}
		else {
			DependencyModel.Distrib distrib = varToSample
					.getDistrib(new DefaultEvalContext(world, true));
			Object oldValue = world.getValue(varToSample);
			logProbBackward += Math.log(distrib.getCPD().getProb(
					distrib.getArgValues(), oldValue));
			
			// Children
			Iterator nextChild = children.iterator();
			while (nextChild.hasNext()) {
				VarWithDistrib child = (VarWithDistrib) nextChild.next();
				DependencyModel.Distrib childDistrib = child
						.getDistrib(new DefaultEvalContext(world, true));
				Object value = world.getValue(child);
				logProbBackward += Math.log(childDistrib.getCPD().getProb(
						childDistrib.getArgValues(), value));
			}
			
			Object newValue = distrib.getCPD().sampleVal(distrib.getArgValues());
			world.setValue(varToSample, newValue);
			logProbForward += Math.log(distrib.getCPD().getProb(distrib.getArgValues(),
					newValue));
			
			// Children
			nextChild = children.iterator();
			while (nextChild.hasNext()) {
				VarWithDistrib child = (VarWithDistrib) nextChild.next();
				DependencyModel.Distrib childDistrib = child
						.getDistrib(new DefaultEvalContext(world, true));
				Object value = world.getValue(child);
				logProbForward += Math.log(childDistrib.getCPD().getProb(
						childDistrib.getArgValues(), value));
			}
	
			// Make the world self-supporting. The only variables whose active
			// parent sets could have changed are the children of varToSample.
			ClassicInstantiatingEvalContext instantiator = new ClassicInstantiatingEvalContext(
					world);
	
			for (Iterator childrenIter = children.iterator(); childrenIter.hasNext();) {
				BayesNetVar child = (BayesNetVar) childrenIter.next();
				if (!world.isInstantiated(child)) // NOT SURE YET THIS IS THE RIGHT THING
																					// TO DO! CHECKING WITH BRIAN.
					continue;
				child.ensureDetAndSupported(instantiator);
			}
	
			logProbForward += instantiator.getLogProbability();
		}
	}
}
