/*
 * Copyright (c) 2007 Massachusetts Institute of Technology
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
 * * Neither the name of the Massachusetts Institute of Technology nor
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

package ve;

import java.util.*;
import java.io.PrintStream;

import blog.*;
import fove.*;
import common.TupleIterator;

/**
 * A propositional Markov net.  The MN is represented as a set of random 
 * variables and a set of factors.  
 *
 * <p>Note: BasicVar objects used in the factors may not be == to any of 
 * the BasicVar objects in the MN's collection of random variables.  When 
 * comparing BasicVar objects, one should always use the equals method.  
 */
public class MarkovNet {
    /**
     * Creates a new MarkovNet with no random variables and no factors.
     */
    public MarkovNet() {
    }

    /**
     * Creates a new Markov net corresponding to the given BLOG model.
     */
    public MarkovNet(Model model) {
	// Create a parameterized Markov net, then propositionalize it
	ParMarkovNet parMarkovNet = new ParMarkovNet(model);

	for (RandomFunction rf : parMarkovNet.getRandomFunctions()) {
	    addRandomVarsForFunction(rf);
	}

	for (Parfactor parfactor : parMarkovNet.getParfactors()) {
	    factors.addAll(parfactor.getFactors());
	}
    }

    /**
     * Prints a description of this Markov net to the given stream.
     */
    public void print(PrintStream out) {
	out.println("MN with random variables:");
	for (BasicVar rv : rvs) {
	    out.print('\t');
	    out.println(rv);
	}

	out.println();
	out.println("Factors:");
	out.println();
	for (Factor factor : factors) {
	    factor.print(out);
	    out.println();
	}
    }

    private void addRandomVarsForFunction(RandomFunction rf) {
	Type[] argTypes = rf.getArgTypes();
	List<Collection<Object>> argDomains 
	    = new ArrayList<Collection<Object>>(argTypes.length);
	for (int i = 0; i < argTypes.length; ++i) {
	    argDomains.add(argTypes[i].getGuaranteedObjects());
	}

	TupleIterator iter = new TupleIterator(argDomains);
	while (iter.hasNext()) {
	    List argValues = (List) iter.next();
	    BasicVar rv = new RandFuncAppVar(rf, argValues);
	    rvs.add(rv);
	}
    }

    Collection<BasicVar> rvs = new ArrayList<BasicVar>(); 
    Collection<Factor> factors = new ArrayList<Factor>();
}
