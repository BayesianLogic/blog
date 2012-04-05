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
import blog.*;
import blog.bn.BasicVar;
import blog.bn.BayesNetVar;
import blog.common.Util;
import blog.model.Model;

public class VarElimEngine extends InferenceEngine {
	/**
	 * Creates a new VarElimEnginefor the given model. The properties argument is
	 * currently ignored.
	 */
	public VarElimEngine(Model model, Properties properties) {
		super(model);
		net = new MarkovNet(model);
		// net.print(System.out);
	}

	public void setEvidence(Evidence evidence) {
		super.setEvidence(evidence);

		if (!evidence.getSkolemConstants().isEmpty()) {
			throw new IllegalArgumentException(
					"VarElimEngine doesn't handle symbol evidence.");
		}

		// Accumulate evidence factors in a list separate from the Markov
		// net, because setEvidence could be called again with different
		// evidence.
		evidenceFactors = new ArrayList<Factor>();
		for (Iterator iter = evidence.getEvidenceVars().iterator(); iter.hasNext();) {
			BayesNetVar var = (BayesNetVar) iter.next();
			if (var instanceof BasicVar) {
				Object val = evidence.getObservedValue(var);
				List<BasicVar> factorVars = Collections.singletonList((BasicVar) var);
				List<Object> factorVals = Collections.singletonList(val);
				evidenceFactors.add(Factor.delta(factorVars, factorVals));
			} else {
				throw new IllegalArgumentException(
						"Can't handle non-basic evidence variable: " + var);
			}
		}
	}

	public void answerQueries() {
		long start = System.nanoTime();

		Collection<Factor> current_factors = new ArrayList<Factor>(net.factors);
		current_factors.addAll(evidenceFactors);

		// Eliminate everything but the query variables

		LinkedList<BasicVar> vars_to_eliminate = new LinkedList(net.rvs);

		ListIterator<Query> query_iter = queries.listIterator();
		while (query_iter.hasNext()) {
			Query query = query_iter.next();
			Collection<? extends BayesNetVar> queryVars = query.getVariables();
			for (BayesNetVar var : queryVars) {
				if (!(var instanceof BasicVar)) {
					throw new IllegalArgumentException(
							"VarElimEngine can't handle query " + query
									+ " involving non-basic random variable " + var);
				}
			}
			vars_to_eliminate.removeAll(queryVars);
		}

		// Make initial graph over the random variables
		Map<BasicVar, VarNode> var_nodes = new HashMap<BasicVar, VarNode>();
		for (BasicVar var : net.rvs) {
			var_nodes.put(var, new VarNode(var));
		}
		for (Factor factor : current_factors) {
			for (BasicVar var1 : factor.getRandomVars()) {
				VarNode node1 = var_nodes.get(var1);
				for (BasicVar var2 : factor.getRandomVars()) {
					if (!var2.equals(var1)) {
						node1.addNeighbor(var_nodes.get(var2));
					}
				}
			}
		}

		while (!vars_to_eliminate.isEmpty()) {
			BasicVar elimvar = chooseVarToElim(vars_to_eliminate, current_factors,
					var_nodes);
			if (Util.verbose()) {
				System.out.println("Summing out " + elimvar);
			}
			Iterator factor_iter = current_factors.iterator();

			Collection next_step_factors = new ArrayList(current_factors.size());
			List in_scope_factors = new ArrayList(current_factors.size());

			while (factor_iter.hasNext()) {

				Factor f = (Factor) factor_iter.next();
				// System.out.println(f.getPotential() instanceof MultiArrayPotential);
				if (f.inScope(elimvar))
					in_scope_factors.add(f);
				else
					next_step_factors.add(f);
			}

			if (in_scope_factors.size() > 0) {
				Factor product = Factor.multiply(in_scope_factors);

				ArrayList<BasicVar> elim_var_list = new ArrayList<BasicVar>(1);
				elim_var_list.add(elimvar);

				// Sum out the variable and add to next_step_factors
				Factor summedOut = Factor.sumOut(product, elim_var_list);
				next_step_factors.add(summedOut);

				// Update the graph
				VarNode elim_node = var_nodes.get(elimvar);
				for (VarNode neighbor : elim_node.neighbors) {
					neighbor.removeNeighbor(elim_node);
					for (VarNode other_neighbor : elim_node.neighbors) {
						if (other_neighbor != neighbor) {
							neighbor.addNeighbor(other_neighbor);
						}
					}
				}
			}

			vars_to_eliminate.remove(elimvar);
			current_factors = next_step_factors;
		}

		// Fold-right a multiply
		Iterator<Factor> f_iter = current_factors.iterator();
		Factor left = f_iter.next();
		while (f_iter.hasNext())
			left = Factor.multiply(left, f_iter.next());

		query_iter = queries.listIterator();
		while (query_iter.hasNext()) {
			Query query = ((Query) query_iter.next());
			Collection<BayesNetVar> query_vars = (Collection<BayesNetVar>) query
					.getVariables();
			ArrayList<BasicVar> vars_to_remove = new ArrayList<BasicVar>(
					left.getRandomVars());
			vars_to_remove.removeAll(query_vars);

			Factor marginal = Factor.sumOut(left, vars_to_remove);
			marginal.normalize();
			query.setPosterior(marginal);
		}

		long end = System.nanoTime();

		if (queries.isEmpty()) {
			System.out.println("Resulting zero-ary potential:");
			left.getPotential().print(System.out);
		}

		System.out.println("\n**TIME**" + (end - start));
	}

	static public Factor computeMarginal(Collection<Factor> factors,
			List<BasicVar> vars_to_eliminate) {
		ListIterator<BasicVar> elim_iter = vars_to_eliminate.listIterator();
		Collection<Factor> current_factors = new ArrayList<Factor>(factors);
		while (elim_iter.hasNext()) {
			BasicVar elimvar = elim_iter.next();
			Iterator factor_iter = current_factors.iterator();

			Collection next_step_factors = new ArrayList(current_factors.size());
			Collection in_scope_factors = new ArrayList(current_factors.size());

			while (factor_iter.hasNext()) {

				Factor f = (Factor) factor_iter.next();
				// System.out.println(f.getPotential() instanceof MultiArrayPotential);
				if (f.inScope(elimvar))
					in_scope_factors.add(f);
				else
					next_step_factors.add(f);
			}

			if (in_scope_factors.size() > 0) {
				// Fold-right a multiply.
				Iterator<Factor> f_iter = in_scope_factors.iterator();
				Factor left = f_iter.next();
				while (f_iter.hasNext())
					left = Factor.multiply(left, f_iter.next());

				ArrayList<BasicVar> elim_var_list = new ArrayList<BasicVar>(1);
				elim_var_list.add(elimvar);

				// Sum out the variable and add to next_step_factors
				next_step_factors.add(Factor.sumOut(left, elim_var_list));
			}
			current_factors = next_step_factors;

		}

		// Fold-right a multiply
		Iterator<Factor> f_iter = current_factors.iterator();
		Factor left = f_iter.next();
		while (f_iter.hasNext())
			left = Factor.multiply(left, f_iter.next());

		return left;

	}

	/**
	 * Chooses a variable to minimize the joint domain size of that variable and
	 * its neighbors.
	 */
	private BasicVar chooseVarToElim(Collection<BasicVar> varsToEliminate,
			Collection<Factor> factors, Map<BasicVar, VarNode> var_nodes) {
		BasicVar bestVar = null;
		double bestLogCost = Double.POSITIVE_INFINITY;
		for (BasicVar var : varsToEliminate) {
			double logCost = var_nodes.get(var).logNeighborsRangeSize;
			if (logCost < bestLogCost) {
				bestVar = var;
				bestLogCost = logCost;
			}
		}

		// System.out.println("Summing out " + bestVar + " with cost "
		// + Math.exp(bestLogCost));
		return bestVar;
	}

	protected static class VarNode {
		public VarNode(BasicVar var) {
			this.var = var;
			logRangeSize = Math.log(var.getType().range().size());
		}

		public void addNeighbor(VarNode neighbor) {
			if (neighbors.add(neighbor)) {
				logNeighborsRangeSize += neighbor.logRangeSize;
			}
		}

		public void removeNeighbor(VarNode neighbor) {
			if (neighbors.remove(neighbor)) {
				logNeighborsRangeSize -= neighbor.logRangeSize;
			}
		}

		public BasicVar var;
		public double logRangeSize;

		public Set<VarNode> neighbors = new HashSet<VarNode>();
		public double logNeighborsRangeSize = 1;
	}

	protected MarkovNet net;
	protected List<Factor> evidenceFactors = Collections.emptyList();
}
