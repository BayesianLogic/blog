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

public class MultiArray {

	private Integer[] bases;
	private int[] index_multipliers;
	private double[] values;

	public static MultiArray marginalize(MultiArray a,
			List<Integer> sumout_indices) {
		boolean[] sum_out_mask = new boolean[a.bases.length];
		Arrays.fill(sum_out_mask, false);
		for (Integer index : sumout_indices) {
			sum_out_mask[index] = true;
		}

		return marginalize(a, sum_out_mask);
	}

	/**
	 * Returns the MultiArray obtained from <code>a</code> by summing out those
	 * dimensions <code>i</code> for which <code>sum_out_mask[i]</code> returns
	 * true.
	 */
	public static MultiArray marginalize(MultiArray a, boolean[] sum_out_mask) {
		List<Integer> marginBases = new ArrayList<Integer>();
		List<Integer> indicesInOrig = new ArrayList<Integer>();
		for (int i = 0; i < a.bases.length; ++i) {
			if (!sum_out_mask[i]) {
				marginBases.add(a.bases[i]);
				indicesInOrig.add(i);
			}
		}
		MultiArray margin = new MultiArray(marginBases, 0);
		DimMapping mapping = new DimMapping(indicesInOrig);
		MultiArray.TrackingPointer marginPtr = margin.trackingPointer(mapping);

		int[] origTuple = new int[a.bases.length];
		Arrays.fill(origTuple, 0);

		for (int origIndex = 0; origIndex < a.values.length; ++origIndex) {
			marginPtr.addToValue(a.values[origIndex]);

			// advance to next tuple
			for (int i = a.bases.length - 1; i >= 0; --i) {
				if (origTuple[i] < a.bases[i] - 1) {
					++(origTuple[i]);
					Arrays.fill(origTuple, i + 1, a.bases.length, 0);

					marginPtr.updateForIncrement(i);
					break;
				}
			}
		}

		return margin;
	}

	/*
	 * public static MultiArray marginalize(MultiArray a, List<Integer> a_points)
	 * { a_points = new ArrayList<Integer>(a_points); MultiArray margin; if
	 * (a_points.size() > 0) { Collections.sort(a_points); ListIterator<Integer>
	 * a_p_iter = a_points.listIterator(); Integer current = a_p_iter.next();
	 * 
	 * ArrayList<Integer> list_perm = new
	 * ArrayList<Integer>(a.bases.length-a_points.size()); for(int i = 0; i <
	 * a.bases.length; i++) { if (i != current) list_perm.add(i); else
	 * if(a_p_iter.hasNext()) current = a_p_iter.next(); }
	 * 
	 * DimMapping margin_p = new DimMapping(list_perm); ArrayList<Integer>
	 * sum_bases = margin_p.permuteObjectArray(a.bases); margin = new
	 * MultiArray(sum_bases);
	 * 
	 * TupleIterator margin_argiter = margin.allArgumentIterator();
	 * while(margin_argiter.hasNext())
	 * margin.setValue((List<Integer>)margin_argiter.next(), 0);
	 * 
	 * TupleIterator argiter = a.allArgumentIterator(); while(argiter.hasNext()) {
	 * List<Integer> args = (List<Integer>)argiter.next(); double value =
	 * a.getValue(args); List<Integer> marginargs =
	 * margin_p.permuteRandAccList(args); margin.setValue(marginargs,
	 * margin.getValue(marginargs)+value); }
	 * 
	 * a.last_dim_mapping = margin_p; } else { margin = new MultiArray(a);
	 * a.last_dim_mapping = DimMapping.identityDimMapping(a.bases.length); }
	 * 
	 * return margin; }
	 */

	public static MultiArray pointwiseProduct(MultiArray a, MultiArray b,
			DimMapping b_product_map) {

		List<Integer> product_bases = new ArrayList<Integer>();
		for (int i = 0; i < a.bases.length; ++i) {
			product_bases.add(a.bases[i]);
		}
		int[] b_indices_in_product = b_product_map.indicesInSuper();
		for (int i = 0; i < b.bases.length; ++i) {
			if (b_indices_in_product[i] >= a.bases.length) {
				product_bases.add(b.bases[i]);
			}
		}
		MultiArray product = new MultiArray(product_bases);

		DimMapping a_product_map = DimMapping.identityDimMapping(a.bases.length);
		MultiArray.TrackingPointer a_ptr = a.trackingPointer(a_product_map);
		MultiArray.TrackingPointer b_ptr = b.trackingPointer(b_product_map);

		int[] prodTuple = new int[product.bases.length];
		Arrays.fill(prodTuple, 0);
		for (int prodIndex = 0; prodIndex < product.values.length; ++prodIndex) {
			// Set this entry in the product
			product.values[prodIndex] = a_ptr.getValue() * b_ptr.getValue();

			// Advance prodTuple and the tracking pointers
			for (int i = prodTuple.length - 1; i >= 0; --i) {
				if (prodTuple[i] < product.bases[i] - 1) {
					// i is the index to increment
					++(prodTuple[i]);
					Arrays.fill(prodTuple, i + 1, prodTuple.length, 0);

					a_ptr.updateForIncrement(i);
					b_ptr.updateForIncrement(i);

					break;
				}
			}
		}

		return product;
	}

	/**
	 * Returns the element-wise product of the given multi-arrays.
	 * 
	 * @param inputs
	 *          the multi-arrays to multiply
	 * 
	 * @param productDims
	 *          size for each dimension in the product
	 * 
	 * @param dimMappings
	 *          for each input multi-array, a mapping from argument tuples in the
	 *          product to argument tuples in that multi-array
	 */
	public static MultiArray pointwiseProduct(List<MultiArray> inputs,
			List<Integer> productDims, DimMapping[] dimMappings) {
		MultiArray product = new MultiArray(productDims);

		if (inputs.isEmpty()) {
			Arrays.fill(product.values, 1);
			return product;
		}

		MultiArray.TrackingPointer[] ptrs = new MultiArray.TrackingPointer[inputs
				.size()];
		for (int i = 0; i < inputs.size(); ++i) {
			ptrs[i] = inputs.get(i).trackingPointer(dimMappings[i]);
		}

		int[] prodTuple = new int[product.bases.length];
		Arrays.fill(prodTuple, 0);
		for (int prodIndex = 0; prodIndex < product.values.length; ++prodIndex) {
			// Compute this entry in the product
			double p = ptrs[0].getValue();
			for (int i = 1; i < ptrs.length; ++i) {
				p *= ptrs[i].getValue();
			}
			product.values[prodIndex] = p;

			// Advance prodTuple and the tracking pointers
			for (int i = prodTuple.length - 1; i >= 0; --i) {
				if (prodTuple[i] < product.bases[i] - 1) {
					// i is the index to increment
					++(prodTuple[i]);
					Arrays.fill(prodTuple, i + 1, prodTuple.length, 0);

					for (int j = 0; j < ptrs.length; ++j) {
						ptrs[j].updateForIncrement(i);
					}

					break;
				}
			}
		}

		return product;
	}

	/*
	 * public static MultiArray pointwiseProduct(List<MultiArray> inputs,
	 * List<Integer> productDims, DimMapping[] dimMappings) { MultiArray product =
	 * new MultiArray(productDims); for (TupleIterator entryIter =
	 * product.allArgumentIterator(); entryIter.hasNext(); ) { List prodIndices =
	 * (List) entryIter.next();
	 * 
	 * // Compute this entry in the product double p = 1; for (int i = 0; i <
	 * inputs.size(); ++i) { MultiArray input = inputs.get(i); List inputIndices =
	 * dimMappings[i].permuteRandAccList(prodIndices); p *=
	 * input.getValue(inputIndices); } product.setValue(prodIndices, p); } return
	 * product; }
	 */

	public MultiArray(List<Integer> baseList) {
		bases = new Integer[baseList.size()];
		int i = 0;
		for (Integer base : baseList) {
			bases[i++] = base.intValue();
		}

		index_multipliers = new int[bases.length];
		int blockSize = 1;
		for (i = bases.length - 1; i >= 0; --i) {
			index_multipliers[i] = blockSize;
			blockSize *= bases[i];
		}

		values = new double[blockSize];
	}

	public MultiArray(List<Integer> bases, double initialValue) {
		this(bases);
		Arrays.fill(values, initialValue);
	}

	public MultiArray(MultiArray a) {
		this.bases = new Integer[a.bases.length];
		for (int i = 0; i < this.bases.length; i++)
			this.bases[i] = a.bases[i];

		this.index_multipliers = new int[a.index_multipliers.length];
		for (int i = 0; i < this.index_multipliers.length; i++)
			this.index_multipliers[i] = a.index_multipliers[i];

		this.values = new double[a.values.length];
		for (int i = 0; i < this.values.length; i++)
			this.values[i] = a.values[i];
	}

	public MultiArray copy() {
		return new MultiArray(this);
	}

	public TupleIterator allArgumentIterator() {
		List<Collection> possible_arguments = new LinkedList<Collection>();
		for (int b = 0; b < bases.length; b++) {
			int size = bases[b];
			Collection possible_args_for_index = new ArrayList(size);
			for (int k = 0; k < size; k++)
				possible_args_for_index.add(k);
			possible_arguments.add(possible_args_for_index);
		}
		return new TupleIterator(possible_arguments);
	}

	public double getValue(List<Integer> dimIndex) {
		return values[indexIntoValues(dimIndex)];
	}

	public double getValue(int[] dimIndex) {
		return values[indexIntoValues(dimIndex)];
	}

	public void setValue(List<Integer> dimIndex, double value) {
		values[indexIntoValues(dimIndex)] = value;
	}

	public void setValue(int[] dimIndex, double value) {
		values[indexIntoValues(dimIndex)] = value;
	}

	public void setValues(double[] vs) {
		for (int i = 0; i < values.length && i < vs.length; i++) {
			values[i] = vs[i];
		}
	}

	public int size() {
		return values.length;
	}

	/**
	 * Creates a mew multiarray where each entry <code>x</code> is replaces with
	 * <code>x^exp</code>.
	 */
	public void pow(double exp) {
		for (int i = 0; i < values.length; i++) {
			values[i] = Math.pow(values[i], exp);
		}
	}

	public void normalize() {
		double total = 0.0;
		for (int i = 0; i < values.length; i++) {
			total += values[i];
		}
		for (int i = 0; i < values.length; i++) {
			values[i] /= total;
		}

	}

	/**
	 * Returns true if all the entries in this MultiArray are equal to the given
	 * value.
	 */
	public boolean isConstant(double c) {
		for (int i = 0; i < values.length; ++i) {
			if (values[i] != c) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns true if this MultiArray has the same dimensions as the given
	 * MultiArray, and each pair of corresponding entries differs by no more than
	 * Util.TOLERANCE.
	 */
	public boolean withinTol(MultiArray other) {
		if (!Arrays.equals(bases, other.bases)) {
			return false;
		}

		for (int i = 0; i < values.length; ++i) {
			if (!Util.withinTol(values[i], other.values[i])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Prints a human-readable representation of this MultiArray to the given
	 * stream. Simply uses numerals to represent the values along each dimension.
	 */
	public void print(PrintStream out) {
		print(out, null);
	}

	/**
	 * Prints a human-readable representation of this MultiArray to the given
	 * stream. For each dimension <code>j</code>, uses the strings in
	 * <code>valueStrings.get(j)</code> to represent the values along that
	 * dimension. If <code>valueStrings</code> is null, just uses numerals to
	 * represent the values.
	 */
	public void print(PrintStream out, List<List<String>> valueStrings) {
		int[] curIndices = new int[bases.length];
		Arrays.fill(curIndices, 0);

		String[] curStrings = new String[bases.length];
		for (int i = 0; i < bases.length; ++i) {
			curStrings[i] = (valueStrings == null ? "0" : valueStrings.get(i).get(0));
		}

		for (int offset = 0; offset < values.length; ++offset) {
			printEntry(out, curStrings, offset);

			// advance curIndices; last index cycles the fastest
			for (int j = bases.length - 1; j >= 0; --j) {
				if (curIndices[j] < bases[j] - 1) {
					++curIndices[j];
					curStrings[j] = (valueStrings == null ? String.valueOf(curIndices[j])
							: valueStrings.get(j).get(curIndices[j]));
					break;
				}
				curIndices[j] = 0;
				curStrings[j] = (valueStrings == null ? "0" : valueStrings.get(j)
						.get(0));
			}
		}
	}

	private void printEntry(PrintStream out, String[] curStrings, int offset) {
		for (int i = 0; i < curStrings.length; ++i) {
			out.print(curStrings[i]);
			out.print('\t');
		}
		out.println(values[offset]);
	}

	private int indexIntoValues(List<Integer> args) {
		int index = 0;
		int dimIndex = 0;
		for (Integer arg : args) {
			index += (index_multipliers[dimIndex++] * arg.intValue());
		}
		return index;
	}

	private int indexIntoValues(int[] args) {
		int index = 0;
		for (int i = 0; i < args.length; ++i) {
			index += (index_multipliers[i] * args[i]);
		}
		return index;
	}

	private TrackingPointer trackingPointer(DimMapping dimMapping) {
		return new TrackingPointer(dimMapping);
	}

	private class TrackingPointer {
		private TrackingPointer(DimMapping dimMapping) {
			int[] indicesInSub = dimMapping.indicesInSub();
			changeAmounts = new int[indicesInSub.length];

			int changeFromReset = 0;
			for (int i = indicesInSub.length - 1; i >= 0; --i) {
				if (indicesInSub[i] == -1) {
					// Incrementing index i just resets to zero those
					// dimensions in this multi-array that come after
					// index i in the superset multi-array.
					changeAmounts[i] = changeFromReset;
				} else {
					int localDim = indicesInSub[i];

					// Incrementing index i increments localDim
					// and resets later dimensions to zero.
					int blockSize = index_multipliers[localDim];
					changeAmounts[i] = blockSize + changeFromReset;

					// When this dimension is reset, it will go from
					// bases[localDim] - 1 to zero.
					changeFromReset -= ((bases[localDim] - 1) * blockSize);
				}
			}
		}

		private void updateForIncrement(int superDimIncremented) {
			if (superDimIncremented < changeAmounts.length) {
				curIndex += changeAmounts[superDimIncremented];
			}
			// Else the increment just changed dimensions after the
			// last local dimension in the superset multi-array, which
			// doesn't change the local index.
		}

		private double getValue() {
			return values[curIndex];
		}

		private void addToValue(double addition) {
			values[curIndex] += addition;
		}

		private int curIndex = 0;
		private int[] changeAmounts;
	}

	public static void main(String[] args) {
		List<Integer> bases = new ArrayList<Integer>();
		bases.add(2);
		bases.add(4);
		bases.add(6);
		MultiArray a = new MultiArray(bases);
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 4; j++) {
				for (int k = 0; k < 6; k++) {
					int[] loopargs = { i, j, k };
					a.setValue(loopargs, i * 1000 + j * 100 + k * 10);
				}
			}
		}
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 4; j++) {
				for (int k = 0; k < 6; k++) {
					int[] loopargs = { i, j, k };
					double val = a.getValue(loopargs);
					// System.out.print(loopargs);
					// System.out.print(" : ");
					// System.out.print(val);
					if (val != i * 1000 + j * 100 + k * 10) {
						System.out.println("invalid value at: ");
						System.out.println(loopargs);
					}
				}
			}
		}
		System.out.println("Indexing works");

		List<Integer> ab_bases = new ArrayList<Integer>();
		ab_bases.add(3);
		ab_bases.add(2);
		List<Integer> bc_bases = new ArrayList<Integer>();
		bc_bases.add(2);
		bc_bases.add(2);
		MultiArray ab = new MultiArray(ab_bases);
		MultiArray bc = new MultiArray(bc_bases);
		int[] _00 = { 0, 0 };
		int[] _01 = { 0, 1 };
		int[] _10 = { 1, 0 };
		int[] _11 = { 1, 1 };
		int[] _20 = { 2, 0 };
		int[] _21 = { 2, 1 };
		ab.setValue(_00, 0.5);
		ab.setValue(_01, 0.8);
		ab.setValue(_10, 0.1);
		ab.setValue(_11, 0);
		ab.setValue(_20, 0.3);
		ab.setValue(_21, 0.9);

		bc.setValue(_00, 0.5);
		bc.setValue(_01, 0.7);
		bc.setValue(_10, 0.1);
		bc.setValue(_11, 0.2);

		List<Integer> _0 = Collections.singletonList(0);
		List<Integer> _1 = Collections.singletonList(1);

		int[] bc_indices_in_prod = { 1, 2 };
		MultiArray abc = pointwiseProduct(ab, bc,
				new DimMapping(bc_indices_in_prod));

		int[] _000 = { 0, 0, 0 };
		int[] _001 = { 0, 0, 1 };
		int[] _010 = { 0, 1, 0 };
		int[] _011 = { 0, 1, 1 };
		int[] _100 = { 1, 0, 0 };
		int[] _101 = { 1, 0, 1 };
		int[] _110 = { 1, 1, 0 };
		int[] _111 = { 1, 1, 1 };
		int[] _200 = { 2, 0, 0 };
		int[] _201 = { 2, 0, 1 };
		int[] _210 = { 2, 1, 0 };
		int[] _211 = { 2, 1, 1 };

		if (abc.getValue(_000) != 0.5 * 0.5 || abc.getValue(_001) != 0.5 * 0.7
				|| abc.getValue(_010) != 0.8 * 0.1 || abc.getValue(_011) != 0.8 * 0.2
				|| abc.getValue(_100) != 0.1 * 0.5 || abc.getValue(_101) != 0.1 * 0.7
				|| abc.getValue(_110) != 0 || abc.getValue(_111) != 0
				|| abc.getValue(_200) != 0.3 * 0.5 || abc.getValue(_201) != 0.3 * 0.7
				|| abc.getValue(_210) != 0.9 * 0.1 || abc.getValue(_211) != 0.9 * 0.2)
			System.out.println("pointwise product failed!");
		else
			System.out.println("Pointwise Product works");

		List<Integer> abc_bases = new ArrayList<Integer>();
		abc_bases.add(3);
		abc_bases.add(2);
		abc_bases.add(2);
		int[] ab_to_prod = { 0, 1 };
		int[] bc_to_prod = { 1, 2 };
		DimMapping[] abc_mappings = { new DimMapping(ab_to_prod),
				new DimMapping(bc_to_prod) };
		List<MultiArray> inputs = new ArrayList<MultiArray>(2);
		inputs.add(ab);
		inputs.add(bc);
		MultiArray abc_from_list = pointwiseProduct(inputs, abc_bases, abc_mappings);
		if (Arrays.equals(abc.values, abc_from_list.values)) {
			System.out.println("n-ary and binary products same");
		} else {
			System.out.println("n-ary and binary products different!");
		}

		MultiArray presum = new MultiArray(abc_bases);

		presum.setValue(_000, 0.25);
		presum.setValue(_001, 0.35);
		presum.setValue(_010, 0.08);
		presum.setValue(_011, 0.16);
		presum.setValue(_100, 0.05);
		presum.setValue(_101, 0.07);
		presum.setValue(_110, 0);
		presum.setValue(_111, 0);
		presum.setValue(_200, 0.15);
		presum.setValue(_201, 0.21);
		presum.setValue(_210, 0.09);
		presum.setValue(_211, 0.18);

		List<Integer> dcb_bases = new ArrayList<Integer>();
		dcb_bases.add(2);
		dcb_bases.add(2);
		dcb_bases.add(2);
		MultiArray dcb_ma = new MultiArray(dcb_bases);

		dcb_ma.setValue(_000, 0.56);
		dcb_ma.setValue(_001, 0.23);
		dcb_ma.setValue(_010, 0.35);
		dcb_ma.setValue(_011, 0.24);
		dcb_ma.setValue(_100, 0);
		dcb_ma.setValue(_101, 0.87);
		dcb_ma.setValue(_110, 0);
		dcb_ma.setValue(_111, 0.9);

		List<Integer> list12 = new ArrayList<Integer>();
		list12.add(1);
		list12.add(2);
		List<Integer> list21 = new ArrayList<Integer>();
		list21.add(2);
		list21.add(1);

		int[] dcb_indices_in_prod = { 3, 2, 1 };
		MultiArray joint_product = pointwiseProduct(presum, dcb_ma, new DimMapping(
				dcb_indices_in_prod));

		int[] _0000 = { 0, 0, 0, 0 };
		int[] _2110 = { 2, 1, 1, 0 };

		if (joint_product.getValue(_0000) != 0.25 * 0.56
				|| joint_product.getValue(_2110) != 0.18 * 0.24)

			System.out.println("Product Failed!");
		else
			System.out.println("Product works");

		int[] dcb_indices_in_prod2 = { 3, 4, 5 };
		MultiArray nullProduct = pointwiseProduct(presum, dcb_ma, new DimMapping(
				dcb_indices_in_prod2));

		int[] _201101 = { 2, 0, 1, 1, 0, 1 };
		int[] _010111 = { 0, 1, 0, 1, 1, 1 };

		if (nullProduct.getValue(_201101) != 0.21 * 0.87
				|| nullProduct.getValue(_010111) != 0.08 * 0.9)
			System.out.println("null product failed");
		else
			System.out.println("null product works");

		List<Integer> mult_args_to_sum = new ArrayList<Integer>();
		mult_args_to_sum.add(0);
		mult_args_to_sum.add(2);
		MultiArray summed = marginalize(presum, mult_args_to_sum);

		System.out.print("Should be around 1.08: ");
		System.out.println(summed.getValue(_0));

		System.out.print("Should be around 0.51: ");
		System.out.println(summed.getValue(_1));

		List<Integer> args_to_sum = Collections.singletonList(1);
		summed = marginalize(presum, args_to_sum);

		if (summed.getValue(_00) != 0.33 || summed.getValue(_01) != 0.51
				|| summed.getValue(_10) != 0.05 || summed.getValue(_11) != 0.07
				|| summed.getValue(_20) != 0.24 || summed.getValue(_21) != 0.39)

			System.out.println("Marginalization Failed!");
		else
			System.out.println("Marginalization works");
	}
}
