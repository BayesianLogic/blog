/*
 * Copyright (c) 2005, 2006, Regents of the University of California
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

package blog.distrib;

import blog.*;
import blog.common.Util;
import blog.model.Model;
import blog.model.Type;

import java.util.*;
import Jama.Matrix;

/**
 * CPD described by a table. The CPD can have any number of arguments, which are
 * the parents. The parent types must be either Boolean, or user-defined types
 * with some enumerated guaranteed objects and no number statements. The CPD
 * defines a distribution over the enumerated objects (or true and false) of the
 * child type, given any tuple of enumerated objects (or true/false) for the
 * parents. The child type can also be Integer or NaturalNum, in which case the
 * distribution is over some prefix of the natural numbers.
 * 
 * <p>
 * The CPD table has one row for each possible tuple of parent values (or just a
 * single row if there are no parents). The rows are ordered according to a
 * lexicographic ordering of tuples ("true" comes before "false", and enumerated
 * objects come in the order they were declared). The parameters of the CPD
 * should be a list of row vectors, each representing a row. Each row vector
 * consists of real numbers, one per possible value of the child variable.
 * Values of the child variable are ordered the same way as parent values.
 * 
 * <p>
 * It is also acceptable for a row to be shorter than the number of possible
 * child values: then if the entries sum to less than 1, the remaining
 * probability is allocated to the next child value (and any subsequent child
 * values get probability 0). This means that for a Boolean child variable, the
 * rows can have length one and just specify the probability of "true". If the
 * probabilities in a row sum to more than 1, the TabularCPD constructor throws
 * an IllegalArgumentException.
 * 
 * <p>
 * When one or more the parent values are <code>Model.NULL</code>, the child
 * distribution is also concentrated on <code>Model.NULL</code>.
 * 
 * <p>
 * When a TabularCPD object is created, it doesn't know the types of arguments
 * or their domain sizes. But it computes and stores these things the first time
 * the getProb or sampleVal method is called.
 */
public class TabularCPD extends AbstractCondProbDistrib {
	/**
	 * Creates a new tabular CPD with the given table rows.
	 */
	public TabularCPD(List params) {
		if (params.isEmpty()) {
			throw new IllegalArgumentException("Tabular CPD must have "
					+ "at least one row.");
		}

		// Determine how many columns we need in the probability table
		int maxRowLen = 0;
		for (Iterator iter = params.iterator(); iter.hasNext();) {
			Matrix row = (Matrix) iter.next();
			if (row.getRowDimension() > 1) {
				throw new IllegalArgumentException(
						"TabularCPD param is not a row vector.");
			}
			maxRowLen = Math.max(maxRowLen, row.getColumnDimension());
		}

		// Create table, leaving room for one more non-zero entry in case
		// some rows sum to less than 1
		table = new double[params.size()][maxRowLen + 1];

		// Copy entries into table, filling in an implicit last entry in
		// each row if necessary
		for (int i = 0; i < params.size(); ++i) {
			Matrix row = (Matrix) params.get(i);
			double rowSum = 0;
			for (int j = 0; j < row.getColumnDimension(); ++j) {
				double entry = row.getArray()[0][j];
				table[i][j] = entry;
				rowSum += entry;
			}

			if (Util.signifGreaterThan(rowSum, 1.0)) {
				throw new IllegalArgumentException(
						"TabularCPD row sum is greater than 1: "
								+ Arrays.asList(row.getArray()[0]));
			}

			int rowLen = row.getColumnDimension();
			double remaining = 1.0 - rowSum;
			if (Util.signifGreaterThan(remaining, 0.0)) {
				table[i][rowLen++] = remaining;
			}
			Arrays.fill(table[i], rowLen, table[i].length, 0.0);
		}
	}

	public double getProb(List args, Object value) {
		if (args.contains(Model.NULL)) {
			return ((value == Model.NULL) ? 1 : 0);
		}

		int rowNum = getRowNum(args);

		if ((rowNum >= 0) && (rowNum < table.length)) {
			// TODO: get child domain size based on value, check if
			// first childDomainSize entries in row sum to 1 (not less)

			int valNum = Model.getObjectIndex(value);

			if (valNum == -1)
				Util.fatalError("Value " + value
						+ " of uncountable type passed to TabularCPD");

			if ((valNum >= 0) && (valNum < table[rowNum].length)) {
				return table[rowNum][valNum];
			}
			return 0; // remaining values get probability 0
		}

		if (Util.verbose()) {
			System.err.println("TabularCPD " + this
					+ " Warning: invalid parent tuple: " + args);
		}
		// Put all probability on the child being null
		return ((value == Model.NULL) ? 1 : 0);
	}

	static int counter = 0;

	public Object sampleVal(List args, Type childType) {
		if (args.contains(Model.NULL)) {
			return Model.NULL;
		}

		int rowNum = getRowNum(args);
		if ((rowNum >= 0) && (rowNum < table.length)) {
			int childValNum = Util.sampleWithProbs(table[rowNum]);
			Object result = childType.getGuaranteedObject(childValNum);
			if (result == null) {
				System.err.println("TabularCPD Warning: probabilities "
						+ "for guaranteed objects of type " + childType
						+ " sum to less than " + "one in row " + rowNum + ".");
				return Model.NULL;
			}
			return result;
		}

		System.err.println("TabularCPD " + this + " Warning: invalid parent tuple "
				+ args);
		return Model.NULL;
	}

	private int getRowNum(List args) {
		if (productDomSizes == null) {
			if (!initDomainSizes(args)) {
				return -1;
			}
		}

		int rowNum = 0;
		for (int i = 0; i < args.size(); ++i) {
			Object parentVal = args.get(i);
			int parentValNum = Model.getObjectIndex(parentVal);
			if (parentValNum < 0) {
				return -1; // invalid argument;
			}
			rowNum += (parentValNum * productDomSizes[i]);
		}

		return rowNum;
	}

	private boolean initDomainSizes(List args) {
		productDomSizes = new int[args.size()];
		int overallProd;

		if (args.isEmpty()) {
			overallProd = 1;
		} else {
			productDomSizes[productDomSizes.length - 1] = 1;
			for (int i = productDomSizes.length - 1; i > 0; --i) {
				int domSize = getDomainSize(args.get(i));
				if (domSize < 0) {
					return false; // invalid argument
				}
				productDomSizes[i - 1] = domSize * productDomSizes[i];
			}

			// Commenting out: allowing table to have less rows than its domain.
			// This allows us to have CPDs on integers when we know they are within a
			// range.
			// In particular, this will allow us to have CPD on cardinalities.
			// If we ever need a row we don't have, we can have a warning (which is
			// the behavior I am commenting out anyway) -- Rodrigo.
			// int domZeroSize = getDomainSize(args.get(0));
			// if (domZeroSize < 0) {
			// return false; // invalid argument
			// }
			// overallProd = domZeroSize * productDomSizes[0];
		}

		// if (overallProd != table.length) {
		// System.err.println("TabularCPD warning: table has "
		// + table.length
		// + " rows, but product of domain sizes is "
		// + overallProd + ".");
		// }

		return true;
	}

	private int getDomainSize(Object o) {
		if (o instanceof Boolean) {
			return 2;
		} else if (o instanceof EnumeratedObject) {
			Type t = ((EnumeratedObject) o).getType();
			return t.getGuaranteedObjects().size();
		}
		return -1;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer("[");
		for (int i = 0; i != table.length; i++) {
			buffer.append(Arrays.asList(table[i]));
			if (i != table.length - 1)
				buffer.append(", ");
		}
		buffer.append("]");
		return buffer.toString();
	}

	double[][] table;

	// productDomSizes[i] is product of sizes of domains of arguments
	// *after* i in the argument list
	int[] productDomSizes;
}
