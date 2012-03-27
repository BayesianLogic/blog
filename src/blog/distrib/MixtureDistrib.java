/*
 * Copyright (c) 2006, Regents of the University of California
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

package blog.distrib;

import blog.*;
import java.util.*;
import common.Util;

/**
 * A mixture of conditional probability distributions. The distributions being
 * mixed together can have arguments, but the mixing probabilities cannot depend
 * on the arguments. Currently this class does not have the standard constructor
 * taking a list of parameters.
 */
public class MixtureDistrib extends AbstractCondProbDistrib {
	/**
	 * Creates a new MixtureDistrib that combines the given distributions
	 * according to the given probabilities. The arrays of distributions and
	 * probabilities must have the same length.
	 */
	public MixtureDistrib(CondProbDistrib[] distribs, double[] probs) {
		this.distribs = (CondProbDistrib[]) distribs.clone();
		this.mixDistrib = new Categorical(probs);
	}

	public double getProb(List args, Object value) {
		double prob = 0;
		for (int i = 0; i < distribs.length; ++i) {
			prob += (mixDistrib.getProb(i) * distribs[i].getProb(args, value));
		}
		return prob;
	}

	public double getLogProb(List args, Object value) {
		double logProb = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < distribs.length; ++i) {
			logProb = Util.logSum(logProb,
					(mixDistrib.getLogProb(i) + distribs[i].getLogProb(args, value)));
		}
		return logProb;
	}

	public Object sampleVal(List args, Type childType) {
		int index = mixDistrib.sampleVal();
		return distribs[index].sampleVal(args, childType);
	}

	private CondProbDistrib[] distribs;
	private Categorical mixDistrib;
}
