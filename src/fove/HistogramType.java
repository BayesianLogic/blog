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

package fove;

import java.util.*;
import java.io.PrintStream;

import blog.*;
import blog.common.CartesianProduct;
import blog.common.Util;

/**
 * A family of histograms with a certain set of buckets and a specified total
 * count. The set of buckets is represented as the cross product of the ranges
 * of a given list of types (called "dimensions").
 */
public class HistogramType extends Type {

	/**
	 * Creates a new HistogramType with the given dimensions (defining the set of
	 * buckets) and the given total count.
	 */
	public HistogramType(List<Type> dims, int total) {
		super("HType" + dims + "(" + total + ")");

		this.dims = new ArrayList<Type>(dims);

		List<List<?>> ranges = new ArrayList<List<?>>(dims.size());
		for (Type type : dims) {
			ranges.add(type.range());
		}
		buckets = new CartesianProduct(ranges);
		numBuckets = buckets.size();

		this.total = total;
		allHists = new HistList();
	}

	/**
	 * Returns an unmodifiable list of all the histograms of this type. The
	 * histograms are ordered so all the items are initially in the first bucket;
	 * they gradually "trickle down" to subsequent buckets. The histograms are
	 * constructed on the fly (the list is not stored explicitly), so two method
	 * calls that yield the same histogram according to <code>equals</code> will
	 * not necessarily return the same Java object.
	 */
	public List getGuaranteedObjects() {
		return allHists;
	}

	/**
	 * If the given object is a histogram whose number of buckets and total count
	 * are appropriate for this type, then this method returns the index of that
	 * histogram in the list returned by <code>getGuaranteedObjects</code>.
	 * Otherwise it returns -1.
	 */
	public int getGuaranteedObjIndex(Object o) {
		return allHists.indexOf(o);
	}

	/**
	 * Returns the histogram at the given index in the list returned by
	 * getGuaranteedObjects.
	 * 
	 * @throw IndexOutOfBoundsException if index is invalid
	 */
	public Object getGuaranteedObject(int index) {
		return allHists.get(index);
	}

	/**
	 * Returns the histogram with zero counts in all buckets.
	 */
	public Object getDefaultValue() {
		int[] counts = new int[numBuckets];
		Arrays.fill(counts, 0);
		return new Histogram(counts);
	}

	/**
	 * Returns the total count that is shared by all histograms of this type.
	 */
	public int getTotal() {
		return total;
	}

	/**
	 * Returns the types of the dimensions of this histogram.
	 */
	public List<Type> getDims() {
		return Collections.unmodifiableList(dims);
	}

	/**
	 * Returns the list of buckets in this histogram. Each bucket is represented
	 * as a list with an entry for each dimension. The bucket list is not stored
	 * explicitly and is not modifiable -- the elements are constructed on the
	 * fly.
	 * 
	 * <p>
	 * The list returned by this method allows you to get the index for any bucket
	 * (using the indexOf method) and get the bucket at any index (using the get
	 * method).
	 */
	public List<List<?>> getBuckets() {
		return buckets;
	}

	/**
	 * Inner class representing the list of all histograms of this type.
	 */
	private class HistList extends AbstractList<Histogram> {
		private HistList() {
			// The number of ways of making k choices from n buckets,
			// where the same bucket can be chosen more than once,
			// is n multichoose k = (n + k - 1) choose (n - 1)
			size = Util.multichoose(numBuckets, total);
		}

		public int size() {
			return size;
		}

		public Iterator<Histogram> iterator() {
			return new HistListIterator();
		}

		public ListIterator<Histogram> listIterator() {
			return new HistListIterator();
		}

		public ListIterator<Histogram> listIterator(int index) {
			return new HistListIterator(index);
		}

		public Histogram get(int index) {
			int[] counts = new int[numBuckets];
			if (HistogramType.setToNthHistogram(counts, 0, index, numBuckets, total)) {
				return new Histogram(counts);
			}
			throw new IndexOutOfBoundsException(String.valueOf(index));
		}

		public int indexOf(Object o) {
			if (o instanceof Histogram) {
				Histogram hist = (Histogram) o;
				if (hist.numBuckets() == numBuckets) {
					return HistogramType.getHistogramIndex(hist, 0, numBuckets, total);
				}
			}
			return -1;
		}

		/**
		 * Iterator over histograms.
		 */
		private class HistListIterator implements ListIterator<Histogram> {
			HistListIterator() {
				Arrays.fill(latestCounts, 0);
				latestCounts[0] = total;
				nextIndexToChange = 0;
			}

			HistListIterator(int index) {
				latestCounts = new int[numBuckets];
				if (!HistogramType.setToNthHistogram(latestCounts, 0, index,
						numBuckets, total)) {
					throw new IndexOutOfBoundsException();
				}
				nextIndexToChange = numBuckets - 2;
			}

			public boolean hasNext() {
				return (initialHist || (latestCounts[numBuckets - 1] < total));
			}

			public Histogram next() {
				if (initialHist) {
					initialHist = false;
					return new Histogram(latestCounts);
				}

				loadNextChangeableIndex();
				if (nextIndexToChange < 0) {
					throw new NoSuchElementException();
				}

				// Remove one object from this bucket
				--latestCounts[nextIndexToChange];

				if (nextIndexToChange == numBuckets - 2) {
					// Put object in last bucket; the current bucket
					// remains the next one to be changed.
					++latestCounts[numBuckets - 1];
				} else {
					// Put object in next bucket, along with all the other
					// objects to the right, which we know have accumulated
					// in the last bucket. This next bucket is now the
					// next one to change.
					++nextIndexToChange;
					latestCounts[nextIndexToChange] = latestCounts[numBuckets - 1] + 1;
					latestCounts[numBuckets - 1] = 0;
				}

				return new Histogram(latestCounts);
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}

			public void add(Histogram e) {
				throw new UnsupportedOperationException();
			}

			public void set(Histogram e) {
				throw new UnsupportedOperationException();
			}

			public boolean hasPrevious() {
				throw new UnsupportedOperationException();
			}

			public Histogram previous() {
				throw new UnsupportedOperationException();
			}

			public int nextIndex() {
				throw new UnsupportedOperationException();
			}

			public int previousIndex() {
				throw new UnsupportedOperationException();
			}

			protected void loadNextChangeableIndex() {
				while ((nextIndexToChange >= 0)
						&& ((nextIndexToChange >= numBuckets - 1) || (latestCounts[nextIndexToChange] == 0))) {
					--nextIndexToChange;
				}
			}

			protected int[] latestCounts = new int[numBuckets];
			protected int nextIndexToChange;
			protected boolean initialHist = true;
		}

		private int size;
	}

	// Note: the running time of this method (the number of calls to
	// multichoose) depends on the sum of the entries in the resulting
	// histogram.
	private static boolean setToNthHistogram(int[] counts, int start, int index,
			int nBins, int sum) {
		if (nBins == 1) {
			counts[start] = sum;
			return (index == 0);
		}

		for (int c = sum; c >= 0; --c) {
			int numHistsWithPrefix = Util.multichoose(nBins - 1, sum - c);
			if (index < numHistsWithPrefix) {
				counts[start] = c;
				return setToNthHistogram(counts, start + 1, index, nBins - 1, sum - c);
			}
			index -= numHistsWithPrefix;
		}

		return false;
	}

	private static int getHistogramIndex(Histogram hist, int start, int nBins,
			int sum) {
		if (nBins == 1) {
			if (hist.getCount(start) == sum) {
				return 0;
			}
			return -1; // histogram has wrong sum
		}

		if (hist.getCount(start) > sum) {
			return -1; // count is too large
		}

		int index = 0;
		for (int c = sum; c > hist.getCount(start); --c) {
			index += Util.multichoose(nBins - 1, sum - c);
		}

		int indexOffset = getHistogramIndex(hist, start + 1, nBins - 1,
				sum - hist.getCount(start));
		if (indexOffset == -1) {
			return -1; // histogram has wrong sum
		}
		return (index + indexOffset);
	}

	private List<Type> dims;
	private int total;
	private List<List<?>> buckets;
	private int numBuckets;

	private List<Histogram> allHists;

	public static void main(String[] args) {
		// Make histogram type with two Boolean dimensions (4 buckets)
		// and a total count of 5.
		List<Type> dims = new ArrayList<Type>();
		dims.add(BuiltInTypes.BOOLEAN);
		dims.add(BuiltInTypes.BOOLEAN);
		HistogramType ht = new HistogramType(dims, 5);

		List hists = ht.range();
		System.out.println(hists.size() + " histograms of type " + ht);
		int num = 0;
		for (Iterator iter = hists.iterator(); iter.hasNext();) {
			Histogram hist = (Histogram) iter.next();
			System.out.print(hist);
			System.out.print("\t");
			System.out.println(hists.indexOf(hist));
			++num;
		}
		System.out.println("Histograms printed: " + num);

		System.out.println();
		System.out.println("Accessing histograms by index...");
		for (int i = 0; i < hists.size(); ++i) {
			System.out.print(i);
			System.out.print(". ");
			System.out.println(hists.get(i));
		}

		System.out.println();
		System.out.println("Printing from index 46 with ListIterator...");
		for (Iterator iter = hists.listIterator(46); iter.hasNext();) {
			Histogram hist = (Histogram) iter.next();
			System.out.print(hist);
			System.out.print("\t");
			System.out.println(hists.indexOf(hist));
		}
	}
}
