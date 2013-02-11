/*
 * Copyright (c) 2008 Massachusetts Institute of Technology
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
import blog.*;
import blog.model.LogicalVar;
import blog.model.Substitution;
import blog.model.Term;

public class Propositionalization extends LiftedInfOperator {

	private Set<Parfactor> parfactors;
	private Parfactor phi;
	private LogicalVar X;

	private Propositionalization(Set<Parfactor> parfactors, Parfactor phi,
			LogicalVar X) {
		this.parfactors = parfactors;
		this.phi = phi;
		this.X = X;
	}

	public double logCost() {
		return Math.log(phi.constraint().numAllowedConstants(X))
				+ Math.log(phi.potential().size());
	}

	public void operate() {
		parfactors.remove(phi);

		for (Term c : phi.constraint().allowedConstants(X)) {
			Substitution theta = new Substitution();
			theta.add(X, c);
			Parfactor propped = phi.applySubstitution(theta);
			parfactors.add(propped);
		}

		LiftedVarElim.shatter(parfactors, Collections.EMPTY_LIST);
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Propositionalization(");
		buf.append(X);
		buf.append(" in ");
		buf.append(phi);
		buf.append(")");
		return buf.toString();
	}

	public static Collection<LiftedInfOperator> opFactory(
			Set<Parfactor> parfactors, ElimTester query) {
		LinkedList<LiftedInfOperator> ops = new LinkedList<LiftedInfOperator>();

		for (Parfactor phi : parfactors) {
			for (LogicalVar X : phi.logicalVars()) {
				ops.add(new Propositionalization(parfactors, phi, X));
			}
		}

		return ops;
	}

}
