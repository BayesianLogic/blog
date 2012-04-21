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

package blog.model;

import java.util.*;


/**
 * Represents a skolem constant -- a constant introduced by symbol evidence
 * statements.
 * 
 * @see blog.model.SymbolEvidenceStatement
 */
public class SkolemConstant extends RandomFunction {

	/**
	 * Creates a new SkolemConstant with the given name and return type. The
	 * dependency model is created automatically.
	 * 
	 * @param name
	 *          String representation of the SkolemConstant
	 * 
	 * @param setSpec
	 *          specification for set of objects from which this SkolemConstant's
	 *          value is chosen
	 * 
	 * @param predecessors
	 *          List of SkolemConstant objects that are listed earlier in the same
	 *          symbol evidence statement
	 */
	public SkolemConstant(String name, ImplicitSetSpec setSpec, List predecessors) {
		super(name, Collections.EMPTY_LIST, setSpec.getType(), null);

		// Create formula that excludes objects denoted by the predecessors
		// from the set of eligible objects
		LogicalVar x = setSpec.getGenericSetElt();
		Formula cond = setSpec.getCond();
		for (Iterator iter = predecessors.iterator(); iter.hasNext();) {
			SkolemConstant pred = (SkolemConstant) iter.next();
			Term predTerm = new FuncAppTerm(pred, Collections.EMPTY_LIST);
			Formula eq = new EqualityFormula(x, predTerm);
			cond = new ConjFormula(cond, new NegFormula(eq));
		}

		// Create dependency model that selects uniformly from objects
		// that satisfy this new formula

		ImplicitSetSpec newSetSpec = new ImplicitSetSpec(x, cond);
		List cpdArgs = new ArrayList();
		cpdArgs.add(newSetSpec);

		Clause clause = new Clause(TrueFormula.TRUE,
				new blog.distrib.UniformChoice(), cpdArgs);
		List clauseList = new ArrayList();
		clauseList.add(clause);

		setArgVars(Collections.EMPTY_LIST); // no arguments
		setDepModel(new DependencyModel(clauseList, setSpec.getType(), Model.NULL));
	}
}
