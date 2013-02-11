package fove;

import java.util.*;
import blog.*;
import blog.model.LogicalVar;

public class Exponentiation extends LiftedInfOperator {

	private Set<Parfactor> parfactors;
	private Parfactor phi;
	private LogicalVar X;

	private Exponentiation(Set<Parfactor> parfactors, Parfactor phi, LogicalVar X) {
		this.parfactors = parfactors;
		this.phi = phi;
		this.X = X;
	}

	public double logCost() {
		return 0; // never makes more table entries..
	}

	public void operate() {
		// TODO: handle counting formulas that have inequalities with X
		parfactors.remove(phi);
		parfactors.add(phi.exponentiate(X));
	}

	public static Collection<LiftedInfOperator> opFactory(
			Set<Parfactor> parfactors, ElimTester query) {
		LinkedList<LiftedInfOperator> ops = new LinkedList<LiftedInfOperator>();

		for (Parfactor phi : parfactors) {
			for (LogicalVar X : phi.getUnusedVars()) {
				ops.add(new Exponentiation(parfactors, phi, X));
			}
		}
		return ops;
	}

}
