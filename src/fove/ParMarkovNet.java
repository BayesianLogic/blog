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

package fove;

import java.util.*;
import java.io.PrintStream;

import blog.*;
import ve.*;

/**
 * A parameterized Markov net.
 */
public class ParMarkovNet {
    /**
     * Creates a new parameterized Markov net with no random functions and
     * no parfactors.
     */
    public ParMarkovNet() {
    }

    /**
     * Creates a new parameterized Markov net representing the same
     * distribution as the given BLOG model.
     */
    public ParMarkovNet(Model model) {
	Collection functions = model.getFunctions();
	for (Iterator iter = functions.iterator(); iter.hasNext(); ) {
	    Function f = (Function) iter.next();
	    if (f instanceof RandomFunction) {
		RandomFunction rf = (RandomFunction) f;
		addRandomFunction(rf);
		for (Parfactor pf : Parfactor.createForCPD(rf)) {
		    addParfactor(pf);
		}
	    }
	}

	parfactors.addAll(model.getParfactors());
    }

    /**
     * Returns an unmodifiable version of the collection of random
     * functions in this parameterized Markov net.
     */
    public Collection<RandomFunction> getRandomFunctions() {
	return Collections.unmodifiableCollection(functions);
    }

    /**
     * Adds the given random function to the set of random functions in
     * this ParMarkovNet.  A random function with argument types t1, ..., tK
     * represents a family of random variables, one for each element of
     * the cross product of t1, ..., tK.
     */
    public void addRandomFunction(RandomFunction f) {
	functions.add(f);
    }

    /**
     * Returns an unmodifiable version of the collection of parfactors
     * in this parameterized Markov net.
     */
    public Collection<Parfactor> getParfactors() {
	return Collections.unmodifiableCollection(parfactors);
    }

    /**
     * Adds the given parfactor to this parameterized Markov net.  Any
     * random functions that are used in this parfactor's terms are
     * automatically added to this Markov net's set of random functions.
     *
     * Assumes all formulas in the parfactor are FuncAppTerms or
     * CountingTerms, and that there's no nesting.
     */
    public void addParfactor(Parfactor phi) {
	parfactors.add(phi);

	// add the parfactor's random functions
	for (ArgSpec term : phi.dimTerms()) {
	    if (term instanceof FuncAppTerm) {
		this.addRandomFunctionFrom((FuncAppTerm)term);
	    } else if (term instanceof CountingTerm) {
		this.addRandomFunctionFrom
		    (((CountingTerm)term).singleSubTerm());
	    } else {
		throw new IllegalArgumentException
		    ("Can't handle term that isn't FuncAppTerm or "
		     + "CountingTerm.");
	    }
	}
    }

    // auxiliary procedure for addParfactor; if term contains a
    // RandomFunction, adds it to this.functions
    private void addRandomFunctionFrom(FuncAppTerm term) {
	Function f = term.getFunction();
	if (f instanceof RandomFunction) this.functions.add((RandomFunction)f);
    }

    /**
     * Prints a description of this parameterized Markov net to the given
     * stream.
     */
    public void print(PrintStream out) {
	out.println("Parameterized MN with random functions:");
	for (RandomFunction rf : functions) {
	    out.print('\t');
	    out.println(rf.getSig());
	}

	out.println();
	out.println("Parfactors:");
	out.println();
	for (Parfactor phi : parfactors) {
	    phi.print(out);
	    out.println();
	}
    }

    private Set<RandomFunction> functions = new LinkedHashSet();
    private List<Parfactor> parfactors = new ArrayList();
}
