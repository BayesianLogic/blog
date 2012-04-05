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
import blog.*;
import blog.common.TupleIterator;

/**
 * A decision tree where each interior node is labeled with a SPLIT_LABEL, edges
 * are labeled with Objects, and the leaves are labeled with real-valued
 * weights.
 */
public class DecisionTree<SPLIT_LABEL> {

	/**
	 * Nested class for nodes in a decision tree.
	 */
	public static abstract class Node<SPLIT_LABEL> {
		/**
		 * Adds all the split labels used in this subtree to the given set.
		 */
		protected abstract void addSplitLabels(Set<SPLIT_LABEL> outLabels);

		/**
		 * Fills in that portion of <code>pot</code> corresponding to the non-null
		 * entries in <code>dimValues</code> to match the function represented by
		 * the decision tree rooted at this node.
		 */
		protected abstract void fillMultiArray(MultiArrayPotential pot,
				List<SPLIT_LABEL> dimensions, List<Object> dimValues);

		/**
		 * Prints a human-readable representation of the subtree rooted at this node
		 * to the given stream.
		 */
		public void print(PrintStream out) {
			print(out, 0);
		}

		/**
		 * Prints a human-readable representation of the subtree rooted at this node
		 * to the given stream, with indentation specified by the given depth.
		 */
		protected abstract void print(PrintStream out, int depth);

		protected static final String INDENT = "  ";
	}

	/**
	 * Nested class for internal nodes.
	 */
	public static class InternalNode<SPLIT_LABEL> extends Node<SPLIT_LABEL> {
		/**
		 * Returns the split label for this node, or null if no label has been
		 * specified yet.
		 */
		public SPLIT_LABEL getSplitLabel() {
			return splitLabel;
		}

		/**
		 * Sets the split label for this node.
		 */
		public void setSplitLabel(SPLIT_LABEL label) {
			splitLabel = label;
		}

		/**
		 * Returns the child corresponding to the given value, or null if this node
		 * has no child associated with that value.
		 */
		public Node getChildForValue(Object value) {
			return childMap.get(value);
		}

		/**
		 * Sets the given node to be the child associated with the given value. This
		 * replaces any node previously associated with that value.
		 */
		public void setChildForValue(Node<SPLIT_LABEL> node, Object value) {
			childMap.put(value, node);
		}

		protected void addSplitLabels(Set<SPLIT_LABEL> outLabels) {
			outLabels.add(splitLabel);
			for (Node child : childMap.values()) {
				child.addSplitLabels(outLabels);
			}
		}

		protected void fillMultiArray(MultiArrayPotential pot,
				List<SPLIT_LABEL> dimensions, List<Object> dimValues) {
			int dim = dimensions.indexOf(splitLabel);
			if (dim == -1) {
				throw new IllegalArgumentException(
						"No dimension corresponding to split: " + splitLabel);
			}

			for (Map.Entry<Object, Node> entry : childMap.entrySet()) {
				dimValues.set(dim, entry.getKey());
				entry.getValue().fillMultiArray(pot, dimensions, dimValues);
			}
			dimValues.set(dim, null);
		}

		protected void print(PrintStream out, int depth) {
			for (Map.Entry<Object, Node> entry : childMap.entrySet()) {
				for (int i = 0; i < depth; ++i) {
					out.print(Node.INDENT);
				}
				out.print(splitLabel);
				out.print(" = ");
				out.print(entry.getKey());
				out.println();
				entry.getValue().print(out, depth + 1);
			}
		}

		private SPLIT_LABEL splitLabel;
		private Map<Object, Node> childMap = new LinkedHashMap<Object, Node>();
	}

	/**
	 * Nested class for leaf nodes.
	 */
	public static class Leaf<SPLIT_LABEL> extends Node<SPLIT_LABEL> {
		/**
		 * Returns the weight associated with this node (initially zero).
		 */
		public double getWeight() {
			return weight;
		}

		/**
		 * Sets the weight associated with this node.
		 */
		public void setWeight(double weight) {
			this.weight = weight;
		}

		protected void addSplitLabels(Set outLabels) {
			// no label to add
		}

		protected void fillMultiArray(MultiArrayPotential pot,
				List<SPLIT_LABEL> dimensions, List<Object> dimValues) {
			// Figure out which dimensions don't have values set
			List<Integer> unsetDims = new ArrayList<Integer>();
			List<Collection> unsetDimRanges = new ArrayList<Collection>();
			for (int i = 0; i < dimValues.size(); ++i) {
				if (dimValues.get(i) == null) {
					unsetDims.add(i);
					List range = pot.getDims().get(i).range();
					unsetDimRanges.add(range);
				}
			}

			// For each assignment of values to unset dimensions,
			// set entry in potential.
			TupleIterator unsetTupleIter = new TupleIterator(unsetDimRanges);
			while (unsetTupleIter.hasNext()) {
				List unsetValues = (List) unsetTupleIter.next();
				for (int i = 0; i < unsetDims.size(); ++i) {
					dimValues.set(unsetDims.get(i), unsetValues.get(i));
				}
				pot.setValue(dimValues, weight);
			}

			// Clear unset dimensions in dimValues
			for (int i = 0; i < unsetDims.size(); ++i) {
				dimValues.set(unsetDims.get(i), null);
			}
		}

		protected void print(PrintStream out, int depth) {
			for (int i = 0; i < depth; ++i) {
				out.print(Node.INDENT);
			}
			out.print("weight ");
			out.print(weight);
			out.println();
		}

		private double weight;
	}

	/**
	 * Creates a new decision tree with the given root node.
	 */
	public DecisionTree(Node<SPLIT_LABEL> root) {
		this.root = root;
	}

	/**
	 * Returns a list of the split labels used in this decision tree (ordered by
	 * their first occurrences in a pre-order traversal).
	 */
	public List<SPLIT_LABEL> getSplitLabels() {
		LinkedHashSet<SPLIT_LABEL> labels = new LinkedHashSet<SPLIT_LABEL>();
		root.addSplitLabels(labels);
		return new ArrayList<SPLIT_LABEL>(labels);
	}

	/**
	 * Fills in the given multi-array potential so that it represents the same
	 * function as this decision tree. The dimensions of <code>pot</code> must be
	 * in one-to-one correspondence with the split labels in the
	 * <code>dimensions</code> list.
	 */
	public void fillMultiArray(MultiArrayPotential pot,
			List<SPLIT_LABEL> dimensions) {
		List<Object> dimValues = new ArrayList<Object>(Collections.nCopies(
				dimensions.size(), null));
		root.fillMultiArray(pot, dimensions, dimValues);
	}

	public void print(PrintStream out) {
		root.print(out, 0);
	}

	protected Node<SPLIT_LABEL> root;
}
