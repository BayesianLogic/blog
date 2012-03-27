package fove;

import java.util.Set;

/**
 * Says what lifted inference operator to do next.
 */
public interface LiftedInfOperatorOracle {

	/**
	 * Make a recommendation for which inference operator should next be applied,
	 * in order to perform an efficient elimination.
	 * 
	 * This method is not static so that oracles that wish to maintain state on
	 * the ordering of the operators they have performed thus far may do so.
	 */
	LiftedInfOperator nextOperator(Set<Parfactor> factors, ElimTester query);

}
