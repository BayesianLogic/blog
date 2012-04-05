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

import blog.PartialWorld;
import blog.bn.BasicVar;
import blog.model.Type;

/**
 * A potential defined on a list of random variables. A Factor differs from a
 * Potential in that it includes a list of random variables, not just a list of
 * types.
 */
public class Factor {
	/**
	 * Creates a new Factor on the given random variables with the given
	 * potential.
	 */
	public Factor(List<BasicVar> rvs, Potential potential) {
		this.rvs = new ArrayList<BasicVar>(rvs);
		this.potential = potential;
	}

	public static Factor sumOut(Factor f, Collection<BasicVar> rvs) {
		List<BasicVar> remaining_rvs = new ArrayList<BasicVar>();
		boolean[] sum_out_mask = new boolean[f.rvs.size()];
		Arrays.fill(sum_out_mask, false);

		Iterator<BasicVar> rv_iter = f.rvs.iterator();
		for (int i = 0; rv_iter.hasNext(); ++i) {
			BasicVar rv = rv_iter.next();
			if (rvs.contains(rv)) {
				sum_out_mask[i] = true;
			} else {
				remaining_rvs.add(rv);
			}
		}

		Potential sum_p = f.potential.sumOut(sum_out_mask);
		return new Factor(remaining_rvs, sum_p);
	}

	public static Factor multiply(Factor f, Factor g) {
		// Product RVs will be RVs in f, followed by RVs in g that don't
		// occur in f.
		List<BasicVar> product_rvs = new ArrayList<BasicVar>(f.rvs);
		int[] g_indices_in_product = new int[g.rvs.size()];
		int g_index = 0;
		for (BasicVar rv : g.rvs) {
			int f_index = f.rvs.indexOf(rv);
			if (f_index == -1) {
				g_indices_in_product[g_index++] = product_rvs.size();
				product_rvs.add(rv);
			} else {
				g_indices_in_product[g_index++] = f_index;
			}
		}
		DimMapping g_product_map = new DimMapping(g_indices_in_product);

		Potential mult_p = f.potential.multiply(g.potential, g_product_map);

		return new Factor(product_rvs, mult_p);
	}

	public static Factor multiply(List<Factor> inputs) {
		List<BasicVar> product_rvs = new ArrayList<BasicVar>();
		Map<BasicVar, Integer> product_rv_indices = new HashMap<BasicVar, Integer>();
		List<Type> product_rv_types = new ArrayList<Type>();

		List<Potential> potentials = new ArrayList<Potential>(inputs.size());
		DimMapping[] dim_mappings = new DimMapping[inputs.size()];
		for (int i = 0; i < inputs.size(); ++i) {
			Factor f = inputs.get(i);
			potentials.add(f.getPotential());

			int[] indices_in_product = new int[f.rvs.size()];
			for (int j = 0; j < f.rvs.size(); ++j) {
				BasicVar rv = f.rvs.get(j);
				Integer index_in_product = product_rv_indices.get(rv);
				if (index_in_product == null) {
					index_in_product = new Integer(product_rvs.size());
					product_rvs.add(rv);
					product_rv_indices.put(rv, index_in_product);
					product_rv_types.add(rv.getType());
				}
				indices_in_product[j] = index_in_product.intValue();
			}
			dim_mappings[i] = new DimMapping(indices_in_product);
		}

		Potential product_pot = MultiArrayPotential.multiply(potentials,
				product_rv_types, dim_mappings);

		return new Factor(product_rvs, product_pot);
	}

	/**
	 * Returns a factor that on the given variables that assigns weight one to the
	 * given tuple of values, and weight zero to all other values.
	 */
	public static Factor delta(List<BasicVar> rvs, List<?> vals) {
		List<Type> dims = new ArrayList<Type>();
		for (BasicVar rv : rvs) {
			dims.add(rv.getType());
		}
		Potential pot = new MultiArrayPotential(dims, 0.0);
		pot.setValue(vals, 1.0);
		return new Factor(rvs, pot);
	}

	public boolean inScope(BasicVar rv) {
		return this.rvs.contains(rv);
	}

	/**
	 * Returns the random variables on which this factor is defined.
	 * 
	 * @return unmodifiable list
	 */
	public List<BasicVar> getRandomVars() {
		return Collections.unmodifiableList(rvs);
	}

	/**
	 * Returns the potential associated with this factor.
	 */
	public Potential getPotential() {
		return potential;
	}

	public void normalize() {
		potential.normalize();
	}

	/**
	 * Returns the weight defined by this factor for the given evaluation context.
	 * 
	 * @throws IllegalArgumentException
	 *           if the given partial world does not assign values to all the
	 *           random variables on which this factor is defined.
	 */
	public double getWeight(PartialWorld world) {
		List<Object> argValues = new ArrayList<Object>();
		for (BasicVar rv : rvs) {
			Object value = world.getValue(rv);
			if (value == null) {
				throw new IllegalArgumentException(
						"Can't evaluate factor because world does not assign "
								+ "value to variable " + rv);
			}
			argValues.add(value);
		}
		return potential.getValue(argValues);
	}

	/**
	 * Prints a human-readable representation of this factor to the given stream.
	 */
	public void print(PrintStream out) {
		out.print("random variables: ");
		out.println(rvs);
		potential.print(out);
	}

	public String toString() {
		return ("Factor" + rvs);
	}

	protected ArrayList<BasicVar> rvs;
	protected Potential potential;
}
