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
import blog.common.TupleIterator;
import blog.common.Util;
import blog.model.Type;
import Jama.Matrix;

public class MultiArrayPotential implements Potential {

	private ArrayList<Type> arguments;
	public MultiArray multi_array;

	public MultiArrayPotential(List<Type> args) {
		arguments = new ArrayList<Type>(args.size());
		ListIterator<Type> argiter = args.listIterator();
		ArrayList<Integer> bases = new ArrayList<Integer>(args.size());
		while (argiter.hasNext()) {
			Type arg = argiter.next();
			arguments.add(arg);
			bases.add(arg.range().size());
		}

		multi_array = new MultiArray(bases);
	}

	public MultiArrayPotential(List<Type> args, double initialValue) {
		arguments = new ArrayList<Type>(args.size());
		ListIterator<Type> argiter = args.listIterator();
		ArrayList<Integer> bases = new ArrayList<Integer>(args.size());
		while (argiter.hasNext()) {
			Type arg = argiter.next();
			arguments.add(arg);
			bases.add(arg.range().size());
		}

		multi_array = new MultiArray(bases, initialValue);
	}

	public MultiArrayPotential(List<Type> args, MultiArray initial) {
		if (args instanceof ArrayList)
			arguments = (ArrayList<Type>) args;
		else
			arguments = new ArrayList<Type>(args);
		multi_array = initial;
	}

	public MultiArrayPotential(List<Type> args, List params) {
		arguments = new ArrayList<Type>(args);
		ArrayList<Integer> bases = new ArrayList<Integer>(args.size());
		int numEntries = 1;
		for (Type arg : arguments) {
			int size = arg.range().size();
			bases.add(size);
			numEntries *= size;
		}

		if ((params.size() != 1) || !(params.get(0) instanceof Matrix)) {
			throw new IllegalArgumentException(
					"MultiArrayPotential requires a single parameter of "
							+ "class Matrix.");
		}
		Matrix m = (Matrix) params.get(0);
		if ((m.getColumnDimension() != numEntries) || (m.getRowDimension() != 1)) {
			throw new IllegalArgumentException("Parameter matrix should be "
					+ numEntries + "x1, not " + m.getColumnDimension() + "x"
					+ m.getRowDimension());
		}

		multi_array = new MultiArray(bases);
		multi_array.setValues(m.getRowPackedCopy());
	}

	public MultiArrayPotential copy() {
		return new MultiArrayPotential(arguments, multi_array.copy());
	}

	public List<Type> getDims() {
		return Collections.unmodifiableList(arguments);
	}

	public int size() {
		return multi_array.size();
	}

	private List<Integer> argValuesToIndices(List<?> arg_values) {
		List<Integer> indices = new ArrayList<Integer>(arg_values.size());
		ListIterator<Type> argiter = arguments.listIterator();
		ListIterator<?> valueiter = arg_values.listIterator();
		while (argiter.hasNext()) {
			Type arg = argiter.next();
			Object value = valueiter.next();
			int index = arg.range().indexOf(value);
			if (index == -1) {
				throw new IllegalArgumentException("Object " + value
						+ " is not one of a finite set of "
						+ " guaranteed objects of type " + arg);
			}
			indices.add(index);
		}
		return indices;
	}

	public double getValue(List<?> arg_values) {
		return multi_array.getValue(argValuesToIndices(arg_values));
	}

	public void setValue(List<?> arg_values, double value) {
		multi_array.setValue(argValuesToIndices(arg_values), value);
	}

	public void setValues(double[] values) {
		multi_array.setValues(values);
	}

	public void pow(double exp) {
		multi_array.pow(exp);
	}

	public void normalize() {
		multi_array.normalize();
	}

	public boolean isZero() {
		return multi_array.isConstant(0);
	}

	public Potential multiply(Potential second, DimMapping second_product_map) {
		if (!(second instanceof MultiArrayPotential)) {
			throw new IllegalArgumentException(
					"Can't multiply by potential of class " + second.getClass());
		}
		MultiArrayPotential casted_second = ((MultiArrayPotential) second);

		ArrayList<Type> product_args = new ArrayList<Type>(arguments);
		int[] second_indices_in_product = second_product_map.indicesInSuper();
		int i = 0;
		for (Type argType : casted_second.arguments) {
			if (second_indices_in_product[i++] >= arguments.size()) {
				product_args.add(argType);
			}
		}

		MultiArray product_array = MultiArray.pointwiseProduct(multi_array,
				casted_second.multi_array, second_product_map);

		return new MultiArrayPotential(product_args, product_array);
	}

	public Potential sumOut(List<Integer> sum_points) {
		boolean[] sum_out_mask = new boolean[arguments.size()];
		Arrays.fill(sum_out_mask, false);
		for (int i : sum_points) {
			sum_out_mask[i] = true;
		}

		return sumOut(sum_out_mask);
	}

	public Potential sumOut(boolean[] sum_out_mask) {
		MultiArray marginalized_array = MultiArray.marginalize(multi_array,
				sum_out_mask);

		List<Type> marginalized_args = new ArrayList<Type>();
		for (int i = 0; i < arguments.size(); ++i) {
			if (!sum_out_mask[i]) {
				marginalized_args.add(arguments.get(i));
			}
		}

		return new MultiArrayPotential(marginalized_args, marginalized_array);
	}

	public void print(PrintStream out) {
		List<List<String>> valueStrings = new ArrayList<List<String>>(
				arguments.size());
		for (Type type : arguments) {
			List<String> strs = new ArrayList<String>();
			for (Object o : type.range()) {
				strs.add(o.toString());
			}
			valueStrings.add(strs);
		}

		multi_array.print(out, valueStrings);
	}

	public boolean withinTol(Potential other) {
		if (!arguments.equals(other.getDims())) {
			return false;
		}

		if (other instanceof MultiArrayPotential) {
			return multi_array.withinTol(((MultiArrayPotential) other).multi_array);
		}

		List<List<Object>> ranges = new ArrayList<List<Object>>();
		for (Type type : arguments) {
			ranges.add(type.range());
		}
		for (Iterator iter = new TupleIterator(ranges); iter.hasNext();) {
			List argValues = (List) iter.next();
			if (!Util.withinTol(getValue(argValues), other.getValue(argValues))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the element-wise product of the given potentials.
	 * 
	 * @param pots
	 *          the potentials to multiply
	 * 
	 * @param productArgs
	 *          argument type for each dimension in the product
	 * 
	 * @param dimMappings
	 *          for each input potential, a mapping from product argument tuples
	 *          to argument tuples in that potential
	 */
	public static MultiArrayPotential multiply(List<Potential> pots,
			List<Type> product_args, DimMapping[] dim_mappings) {
		List<MultiArray> inputs = new ArrayList<MultiArray>(pots.size());
		for (Potential pot : pots) {
			inputs.add(((MultiArrayPotential) pot).multi_array);
		}

		List<Integer> product_dims = new ArrayList<Integer>(product_args.size());
		for (Type type : product_args) {
			product_dims.add(type.range().size());
		}

		MultiArray product_array = MultiArray.pointwiseProduct(inputs,
				product_dims, dim_mappings);

		return new MultiArrayPotential(product_args, product_array);
	}
}
