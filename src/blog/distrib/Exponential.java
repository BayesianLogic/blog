/*
 * Copyright (c) 2005, Regents of the University of California
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
 * An Exponential distribution with parameter lambda over non-negative reals.
 * The probability of x is lambda * e^(-lambda*x).
 */

public class Exponential extends AbstractCondProbDistrib {
    /** 
     * Creates a new Exponential with parameter lambda
     */
    public Exponential(List params) {
	if (!(params.get(0) instanceof Number)) {
	    throw new IllegalArgumentException
		("The first parameter to Exponential " 
		 + "must be of class Number, "
		 + "not " + params.get(0).getClass() + ".");
	}

	lambda = ((Number)params.get(0)).doubleValue() ;
    }
    
    /**
     * Returns the probability of x under this distribution
     */
    public double getProb(List args, Object value) {
	if (!(value instanceof Number)) {
	    throw new IllegalArgumentException
		("The Exponential CPD is a distribution over Numbers, "
		 + "not " + value.getClass() + ".");
	} else {
	    double x = ((Number)value).doubleValue();
	    return (lambda * Math.exp((- lambda) * x));
	}
    }

    /**
     * Returns the log of the probability of x under this distribution.
     */
    public double getLogProb(List args, Object value) {
	if (!(value instanceof Number)) {
	    throw new IllegalArgumentException
		("The Exponential CPD is a distribution over Numbers, "
		 + "not " + value.getClass() + ".");
	} else {
	    double x = ((Number)value).doubleValue();
	    return (Math.log(lambda) - (lambda * x));
	}
    }

    /**
     * Returns a double sampled according to this distribution.
     * Takes constant time.  (Reference: A Guide to Simulation, 2nd Ed.
     * Bratley, Paul, Bennett L. Fox and Linus E. Schrage.)
     */
    public Object sampleVal(List args, Type childType) {
	return new Double(-(Math.log(Util.random()) / lambda));
    }

    private double lambda;
}
