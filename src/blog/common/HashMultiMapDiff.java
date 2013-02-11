/*
 * Copyright (c) 2007, Massachusetts Institute of Technology
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

package blog.common;

import java.util.*;

/**
 * Implementation of MultiMapDiff that uses a hash map from objects
 * HashSetDiffs.
 */
public class HashMultiMapDiff extends AbstractMap implements MultiMapDiff,
		Cloneable {
	/**
	 * Creates a new HashMultiMapDiff with the given underlying MultiMap.
	 */
	public HashMultiMapDiff(MultiMap underlying) {
		this.underlying = underlying;
		diffs = new HashMap();
		removedKeys = new HashSet();
	}

	/**
	 * Creates a new HashMultiMapDiff with the given underlying MultiMap. If
	 * <code>maintainOrder</code> is true, uses linked data structures so that the
	 * order of iteration is predictable.
	 */
	public HashMultiMapDiff(MultiMap underlying, boolean maintainOrder) {
		this.underlying = underlying;
		this.maintainOrder = maintainOrder;
		if (maintainOrder) {
			diffs = new LinkedHashMap();
			removedKeys = new LinkedHashSet();
		} else {
			diffs = new HashMap();
			removedKeys = new HashSet();
		}
	}

	public int size() {
		int size = underlying.size();

		// Add 1 for each key that has a non-empty set here and an empty
		// set in the underlying multi-map.
		for (Iterator iter = diffs.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			if (!underlying.containsKey(entry.getKey())) {
				++size;
			}
		}

		// Subtract the number of keys whose sets were removed completely
		size -= removedKeys.size();

		return size;
	}

	public boolean containsKey(Object key) {
		return (diffs.containsKey(key) || (underlying.containsKey(key) && !removedKeys
				.contains(key)));
	}

	public Object get(Object key) {
		return new ValueSet(key);
	}

	public Object put(Object key, Object value) {
		if (!(value instanceof Set)) {
			throw new IllegalArgumentException(
					"Values stored in MultiMap must be sets.");
		}
		Set newSet = (Set) value;
		Set oldSet = getCurrent(key);

		if (newSet.isEmpty()) {
			diffs.remove(key);
			removedKeys.add(key);
		} else {
			Set orig = (Set) underlying.get(key);
			SetDiff diff = newDiff((Set) underlying.get(key));
			diff.addAll(newSet);
			for (Iterator iter = orig.iterator(); iter.hasNext();) {
				Object origObj = iter.next();
				if (!newSet.contains(origObj)) {
					diff.remove(origObj);
				}
			}

			diffs.put(key, diff);
			removedKeys.remove(key);
		}

		return oldSet;
	}

	public boolean add(Object key, Object value) {
		SetDiff diff = ensureHasDiff(key);
		return diff.add(value);
	}

	public boolean addAll(Object key, Set values) {
		if (values.isEmpty()) {
			return false;
		}

		SetDiff diff = ensureHasDiff(key);
		return diff.addAll(values);
	}

	public Object remove(Object key) {
		Set diff = (Set) diffs.remove(key);
		if (diff == null) {
			// We'll return the old set, unless key was already removed
			Set oldSet = (Set) underlying.get(key);
			if (!oldSet.isEmpty()) {
				// ensure that key is in removedKeys
				if (!removedKeys.add(key)) {
					return emptySet(); // key was already removed
				}
			}
			return oldSet;
		}

		if (underlying.containsKey(key)) {
			removedKeys.add(key);
		}
		return diff;
	}

	public boolean remove(Object key, Object value) {
		SetDiff diff = ensureHasDiffOrEmpty(key);
		if ((diff != null) && diff.remove(value)) {
			if (diff.isEmpty()) {
				remove(key);
			}
			return true;
		}
		return false;
	}

	public boolean removeAll(Object key, Set values) {
		SetDiff diff = ensureHasDiffOrEmpty(key);
		if ((diff != null) && diff.removeAll(values)) {
			if (diff.isEmpty()) {
				remove(key);
			}
			return true;
		}
		return false;
	}

	public void clear() {
		diffs.clear();
		removedKeys.addAll(underlying.keySet());
	}

	public Set entrySet() {
		return entrySet;
	}

	public Set getChangedKeys() {
		Set changedKeys = new HashSet(diffs.keySet());
		changedKeys.addAll(removedKeys);
		return Collections.unmodifiableSet(changedKeys);
	}

	public Set getAddedValues(Object key) {
		SetDiff diff = (SetDiff) diffs.get(key);
		return ((diff == null) ? Collections.EMPTY_SET : diff.getAdditions());
	}

	public Set getRemovedValues(Object key) {
		if (removedKeys.contains(key)) {
			return (Set) underlying.get(key);
		}

		SetDiff diff = (SetDiff) diffs.get(key);
		return ((diff == null) ? Collections.EMPTY_SET : diff.getRemovals());
	}

	/**
	 * Changes the underlying multi-map to equal this multi-map.
	 */
	public void changeUnderlying() {
		for (Iterator iter = removedKeys.iterator(); iter.hasNext();) {
			underlying.remove(iter.next());
		}

		for (Iterator iter = diffs.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			SetDiff diff = (SetDiff) entry.getValue();
			diff.changeUnderlying();
		}

		clearChanges();
	}

	/**
	 * Resets this multi-map to be equal to the underlying multi-map.
	 */
	public void clearChanges() {
		// diffs.clear();
		// removedKeys.clear();
		diffs = new HashMap();
		removedKeys = new HashSet();
	}

	/**
	 * Returns a shallow copy of this multi-map diff, expressed relative to the
	 * same underlying multi-map. The copy is "shallow" in that it does not create
	 * new copies of the keys or elements; however, it does make copies of the Set
	 * objects used to store elements in the map.
	 */
	public Object clone() {
		HashMultiMapDiff clone = new HashMultiMapDiff(underlying);
		clone.diffs = new HashMap(diffs);
		for (Iterator iter = clone.diffs.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			entry.setValue(((HashSetDiff) entry.getValue()).clone());
		}
		clone.removedKeys = new HashSet(removedKeys);
		return clone;
	}

	protected Set emptySet() {
		return Collections.EMPTY_SET;
	}

	protected SetDiff newDiff(Set underlying) {
		return new HashSetDiff(underlying, maintainOrder);
	}

	protected Set getCurrent(Object key) {
		Set diff = (Set) diffs.get(key);
		if (diff != null) {
			return diff;
		}

		if (removedKeys.contains(key)) {
			return emptySet();
		}

		return (Set) underlying.get(key);
	}

	private SetDiff ensureHasDiff(Object key) {
		SetDiff diff = (SetDiff) diffs.get(key);
		if (diff == null) {
			Set orig = (Set) underlying.get(key);
			diff = newDiff(orig);
			diffs.put(key, diff);
			if (removedKeys.remove(key)) {
				// Key was previously removed, so remove all old values
				// from value set.
				diff.removeAll(orig);
			}
		}
		return diff;
	}

	// returns null if key maps to empty set
	private SetDiff ensureHasDiffOrEmpty(Object key) {
		SetDiff diff = (SetDiff) diffs.get(key);
		if ((diff == null) && !removedKeys.contains(key)) {
			Set orig = (Set) underlying.get(key);
			if (!orig.isEmpty()) {
				diff = newDiff(orig);
				diffs.put(key, diff);
			}
		}
		return diff;
	}

	protected class ValueSet extends AbstractSet {
		ValueSet(Object key) {
			this.key = key;
		}

		public int size() {
			return getSetToUse().size();
		}

		public boolean contains(Object o) {
			return getSetToUse().contains(o);
		}

		public Iterator iterator() {
			if (diff == null) {
				diff = (SetDiff) diffs.get(key);
				if (diff == null) {
					// need to be robust to diff being added during iteration
					return new RobustValueSetIterator();
				}
			}

			return new SimpleValueSetIterator();
		}

		public boolean add(Object o) {
			if (diff == null) {
				diff = ensureHasDiff(key);
			}
			return diff.add(o);
		}

		public boolean remove(Object o) {
			if (diff == null) {
				diff = ensureHasDiff(key);
			}
			boolean removed = diff.remove(o);
			if (diff.isEmpty()) {
				HashMultiMapDiff.this.remove(key);
				diff = null;
			}
			return removed;
		}

		// Like getCurrent(), but caches results
		protected Set getSetToUse() {
			if (diff == null) {
				diff = (SetDiff) diffs.get(key);
				if (diff == null) {
					if (removedKeys.contains(key)) {
						return emptySet();
					}
					if (underlyingSet == null) {
						underlyingSet = (Set) underlying.get(key);
					}
					return underlyingSet;
				}
			}
			return diff;
		}

		// Iterator that is used if diff is non-null when iterator() is
		// called. We just detect when the diff becomes empty.
		private class SimpleValueSetIterator implements Iterator {
			private SimpleValueSetIterator() {
				iter = diff.iterator();
			}

			public boolean hasNext() {
				return iter.hasNext();
			}

			public Object next() {
				return iter.next();
			}

			public void remove() {
				iter.remove();
				if (diff.isEmpty()) {
					HashMultiMapDiff.this.remove(key);
					diff = null;
				}
			}

			private Iterator iter;
		}

		// Iterator that is used if diff is null when iterator() is called.
		// It is robust to a diff being added during iteration.
		// It essentially mimics SetDiff.SetDiffIterator: its next() method
		// returns a non-removed object from the underlying set if possible,
		// and if not, it returns an object in the diff that is not in
		// the underlying set.
		private class RobustValueSetIterator implements Iterator {
			private RobustValueSetIterator() {
				if (removedKeys.contains(key)) {
					underlyingIter = emptySet().iterator();
				} else {
					underlyingIter = ((Set) underlying.get(key)).iterator();
				}
			}

			public boolean hasNext() {
				if (nextFromUnderlying == null) {
					nextFromUnderlying = getNextFromUnderlying();
					if (nextFromUnderlying == null) {
						nextFromDiff = getNextFromDiff();
						return (nextFromDiff != null);
					}
				}
				return (nextFromUnderlying != null);
			}

			public Object next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				latestFromUnderlying = nextFromUnderlying;
				latestFromUnderlyingRemoved = false;

				if (nextFromUnderlying != null) {
					nextFromUnderlying = null;
					return latestFromUnderlying;
				}

				return nextFromDiff;
			}

			public void remove() {
				if (latestFromUnderlying != null) {
					if (latestFromUnderlyingRemoved) {
						throw new IllegalStateException(
								"next() has not been called since last call " + "to remove().");
					}
					if (diff == null) {
						diff = ensureHasDiff(key);
					}
					diff.remove(latestFromUnderlying);
					latestFromUnderlyingRemoved = true;
				} else if (diffIter != null) {
					diffIter.remove();
					if (diff.isEmpty()) {
						HashMultiMapDiff.this.remove(key);
						diff = null;
					}
				} else {
					throw new IllegalStateException("No object to remove.");
				}
			}

			private Object getNextFromUnderlying() {
				while (underlyingIter.hasNext()) {
					Object obj = underlyingIter.next();
					if ((diff == null) || diff.contains(obj)) {
						// We aren't robust to removals -- we don't check
						// to see if diff should be non-null now
						return obj;
					}
				}
				return null;
			}

			private Object getNextFromDiff() {
				if (diffIter == null) {
					diff = (SetDiff) diffs.get(key);
					if (diff == null) {
						return null; // still no added (or removed) values
					}
					diffIter = diff.iterator();
				}

				while (diffIter.hasNext()) {
					Object obj = diffIter.next();
					if (underlyingSet == null) {
						underlyingSet = (Set) underlying.get(key);
					}
					if (!underlyingSet.contains(obj)) {
						return obj;
					}
				}
				return null;
			}

			private Iterator underlyingIter;
			private Iterator diffIter = null;

			private Object latestFromUnderlying = null;
			private boolean latestFromUnderlyingRemoved = false;
			private Object nextFromUnderlying = null;

			protected Object nextFromDiff = null;
		}

		protected Object key;
		private SetDiff diff = null;
		private Set underlyingSet = null;
	}

	private class EntrySet extends AbstractSet {
		public int size() {
			return HashMultiMapDiff.this.size();
		}

		public boolean contains(Object o) {
			if (o instanceof Map.Entry) {
				Map.Entry entry = (Map.Entry) o;
				return get(entry.getKey()).equals(entry.getValue());
			}
			return false;
		}

		public Iterator iterator() {
			return new EntrySetIterator();
		}
	}

	private class EntrySetIterator implements Iterator {
		EntrySetIterator() {
			underlyingIter = underlying.entrySet().iterator();

			List addedEntries = new ArrayList();
			for (Iterator iter = diffs.entrySet().iterator(); iter.hasNext();) {
				Map.Entry entry = (Map.Entry) iter.next();
				if (!underlying.containsKey(entry.getKey())) {
					addedEntries.add(new Entry(entry));
					// Don't need to worry about removals because the set
					// associated with this key in the underlying multi-map
					// is empty.
				}
			}
			addedEntriesIter = addedEntries.iterator();

			prepareNextEntry();
		}

		public boolean hasNext() {
			return (nextEntry != null);
		}

		public Object next() {
			lastEntry = nextEntry;
			prepareNextEntry();
			return lastEntry;
		}

		public void remove() {
			if (lastEntry == null) {
				throw new IllegalStateException("Nothing to remove.");
			}

			HashMultiMapDiff.this.remove(lastEntry.getKey());
			lastEntry = null;
		}

		void prepareNextEntry() {
			nextEntry = null;

			// Iterate over the underlying map until we find key that
			// hasn't been removed. Use its new associated set.
			while (underlyingIter.hasNext()) {
				Map.Entry underlyingEntry = (Map.Entry) underlyingIter.next();
				Object key = underlyingEntry.getKey();
				if (!removedKeys.contains(key)) {
					nextEntry = new Entry(key, getCurrent(key));
					return;
				}
			}

			// Use the next added entry
			if (addedEntriesIter.hasNext()) {
				nextEntry = (Map.Entry) addedEntriesIter.next();
			}
		}

		Iterator underlyingIter;
		Iterator addedEntriesIter;
		Map.Entry lastEntry = null;
		Map.Entry nextEntry;
	}

	// We need to use our own Map.Entry objects instead of the underlying
	// multi-map's entries because we don't want the setValue method to write
	// through to the underlying multi-map; we want it to call the
	// MultiMapDiff's put method instead. This put operation doesn't
	// disrupt the iteration because it doesn't change the underlying map.
	//
	// For iterating over added keys, we use our own Map.Entry objects
	// so that removals from the diffs map (which can happen if the
	// iterator's remove method is called) don't disrupt our iteration.
	private class Entry extends DefaultMapEntry {
		Entry(Object key, Object value) {
			super(key, value);
		}

		Entry(Map.Entry entry) {
			super(entry);
		}

		public Object setValue(Object newValue) {
			Object oldValue = getValue();
			HashMultiMapDiff.this.put(getKey(), newValue);
			value = newValue;
			return oldValue;
		}
	}

	private MultiMap underlying;
	private boolean maintainOrder = false;
	private Map diffs = new HashMap(); // from Object to SetDiff
	private Set removedKeys = new HashSet();

	EntrySet entrySet = new EntrySet();
}
