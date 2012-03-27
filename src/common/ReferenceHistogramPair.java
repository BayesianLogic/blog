/*
 * Copyright (c) 2005, Regents of the University of California
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

package common;

import java.util.*;

/**
 * Represents the partitions of two sets of references, called the foreground
 * set and the background set. There are methods for moving a reference from one
 * set to another; the foreground and background histograms are both updated, so
 * that the total number of references referring to each object always remains
 * the same. There are also methods for getting the foreground and background
 * histograms, which implement the ReferenceHistogram interface and are backed
 * by the ReferenceHistogramPair object.
 * 
 * A ReferenceHistogramPair object does not maintain two separate histograms
 * explicitly. Instead, it starts with one master histogram representing the
 * full set of references. It represents the foreground histogram explicitly,
 * and represents the background histogram implicitly as the difference between
 * the master and foreground histograms. The point is that we don't have to
 * explicitly construct the background histogram, which may be quite large. The
 * master histogram is never changed.
 */
public class ReferenceHistogramPair {
	/**
	 * Creates a new ReferenceHistogramPair with this given master histogram,
	 * where all citations are initially in the background set.
	 */
	public ReferenceHistogramPair(ReferenceHistogram master) {
		this.master = master;
		bgNumReferenced = master.numReferencedObjects();
	}

	/**
	 * Moves one reference referring to the given object from the background to
	 * the foreground. Throws an IllegalStateException if there is no such
	 * reference in the background.
	 * 
	 * @param obj
	 *          the ID of the object referred to by the reference being moved
	 */
	public void moveToForeground(Long obj) {
		int numBgReferences = background.getNumReferences(obj);
		if (numBgReferences == 0) {
			throw new IllegalStateException("No such reference in background");
		}
		if (numBgReferences == 1) {
			// As soon as we move this reference, there will be no more
			// references to this object in the background set.
			bgNumReferenced--;
		}

		foreground.addReference(obj);
	}

	/**
	 * Moves one reference referring to the given object from the foreground to
	 * the background. Throws an IllegalStateException if there is no such
	 * reference in the foreground.
	 * 
	 * @param obj
	 *          the ID of the object referred to by the reference being moved.
	 */
	public void moveToBackground(Long obj) {
		int numFgReferences = foreground.getNumReferences(obj);
		if (numFgReferences == 0) {
			throw new IllegalStateException("No such reference in foreground");
		}
		if (numFgReferences == master.getNumReferences(obj)) {
			// There are currently 0 references to this object in the bg set.
			// After the move, there will be 1 such reference.
			bgNumReferenced++;
		}

		foreground.removeReference(obj);
	}

	/**
	 * Returns a ReferenceHistogram object for the foreground set.
	 */
	public ReferenceHistogram getForegroundHistogram() {
		return foreground;
	}

	/**
	 * Returns a ReferenceHistogram object for the background set.
	 */
	public ReferenceHistogram getBackgroundHistogram() {
		return background;
	}

	/**
	 * Inner class that implements the ReferenceHistogram interface for the
	 * background set of references.
	 */
	class BackgroundHistogram implements ReferenceHistogram {
		public int numReferencedObjects() {
			return bgNumReferenced;
		}

		public int totalReferences() {
			return (master.totalReferences() - foreground.totalReferences());
		}

		public int getNumReferences(Long obj) {
			return (master.getNumReferences(obj) - foreground.getNumReferences(obj));
		}

		public Iterator iterator() {
			return new BgHistIterator();
		}

		class BgHistIterator implements Iterator {
			Iterator masterIter;
			Object nextObj = null;

			BgHistIterator() {
				masterIter = master.iterator();
			}

			public boolean hasNext() {
				if (nextObj != null) {
					return true;
				}

				while (masterIter.hasNext()) {
					Long nextInMaster = (Long) masterIter.next();
					if (getNumReferences(nextInMaster) > 0) {
						nextObj = nextInMaster;
						return true;
					}
				}
				return false;
			}

			public Object next() {
				if (!hasNext()) { // also loads next object into nextObj
					throw new NoSuchElementException();
				}
				Object toReturn = nextObj;
				nextObj = null;
				return toReturn;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		}
	}

	ReferenceHistogram master;
	DefaultReferenceHistogram foreground = new DefaultReferenceHistogram();
	BackgroundHistogram background = new BackgroundHistogram();
	int bgNumReferenced;
}
