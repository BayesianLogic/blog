/*
 * Copyright (c) 2006, Regents of the University of California
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

package blog.common;

import java.util.*;

/**
 * Implementation of SetDiff that uses hash sets to store additions and
 * removals.
 */
public class HashSetDiff extends AbstractSet implements SetDiff, Cloneable {
	/**
	 * Creates a new HashSetDiff with the given underlying set.
	 */
	public HashSetDiff(Set underlying) {
		this.underlying = underlying;
		additions = newSet();
		removals = newSet();
	}

	/**
	 * Creates a new HashSetDiff with the given underlying set. If
	 * <code>maintainOrder</code> is true, then this instance will use
	 * LinkedHashSets to keep the order of iteration predictable.
	 */
	public HashSetDiff(Set underlying, boolean maintainOrder) {
		this.underlying = underlying;
		this.maintainOrder = maintainOrder;

		additions = newSet();
		removals = newSet();
	}

	public int size() {
		return (underlying.size() + additions.size() - removals.size());
	}

	public boolean contains(Object o) {
		return (additions.contains(o) || (underlying.contains(o) && !removals
				.contains(o)));
	}

	public Iterator iterator() {
		return new SetDiffIterator();
	}

	public boolean add(Object o) {
		if (underlying.contains(o)) {
			return removals.remove(o);
		}
		return additions.add(o);
	}

	public boolean remove(Object o) {
		if (underlying.contains(o)) {
			return removals.add(o);
		}
		return additions.remove(o);
	}

	public Set getAdditions() {
		return Collections.unmodifiableSet(additions);
	}

	public Set getRemovals() {
		return Collections.unmodifiableSet(removals);
	}

	public void changeUnderlying() {
		underlying.addAll(additions);
		underlying.removeAll(removals);
		clearChanges();
	}

	public void clearChanges() {
		additions.clear();
		removals.clear();
	}

	/**
	 * Returns a shallow copy of this set diff, expressed relative to the same
	 * underlying set.
	 */
	public Object clone() {
		HashSetDiff clone = new HashSetDiff(underlying, maintainOrder);
		clone.additions = copySet(additions);
		clone.removals = copySet(removals);
		return clone;
	}

	protected Set newSet() {
		if (maintainOrder) {
			return new LinkedHashSet();
		}
		return new HashSet();
	}

	protected Set copySet(Set orig) {
		if (maintainOrder) {
			return new LinkedHashSet(orig);
		}
		return new HashSet(orig);
	}

	private class SetDiffIterator implements Iterator {
		SetDiffIterator() {
			underlyingIter = underlying.iterator();
			additionsIter = additions.iterator();
		}

		public boolean hasNext() {
			if (nextFromUnderlying == null) {
				nextFromUnderlying = getNextFromUnderlying();
			}
			return ((nextFromUnderlying != null) || additionsIter.hasNext());
		}

		public Object next() {
			if (nextFromUnderlying == null) {
				nextFromUnderlying = getNextFromUnderlying();
			}
			latestFromUnderlying = nextFromUnderlying;
			latestFromUnderlyingRemoved = false;

			if (nextFromUnderlying != null) {
				nextFromUnderlying = null; // so it's not returned again
				return latestFromUnderlying;
			}
			return additionsIter.next();
		}

		public void remove() {
			if (latestFromUnderlying != null) {
				if (latestFromUnderlyingRemoved) {
					throw new IllegalStateException(
							"next() has not been called since last call " + "to remove().");
				}
				removals.add(latestFromUnderlying);
				latestFromUnderlyingRemoved = true;
			} else {
				additionsIter.remove();
			}
		}

		private Object getNextFromUnderlying() {
			while (underlyingIter.hasNext()) {
				Object obj = underlyingIter.next();
				if (!removals.contains(obj)) {
					return obj;
				}
			}
			return null;
		}

		private Iterator underlyingIter;
		private Iterator additionsIter;

		private Object latestFromUnderlying = null;
		private boolean latestFromUnderlyingRemoved = false;
		private Object nextFromUnderlying = null;
	}

	private Set underlying;
	private boolean maintainOrder = false;

	private Set additions;
	private Set removals;
}
