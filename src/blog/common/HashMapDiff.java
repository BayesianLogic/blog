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

package blog.common;

import java.util.*;

/**
 * MapDiff implementation that uses a HashMap to store the values for changed
 * keys, and a HashSet to store the removed keys. This implementation does not
 * support null values.
 */
public class HashMapDiff extends AbstractMap implements MapDiff, Cloneable {
	/**
	 * Creates a new HashMapDiff with the given underlying map.
	 */
	public HashMapDiff(Map underlying) {
		this.underlying = underlying;
	}

	public boolean containsKey(Object key) {
		return (get(key) != null);
	}

	public Object get(Object key) {
		Object newValue = changedKeys.get(key);
		if (newValue == REMOVED) {
			return null;
		} else if (newValue != null) {
			return newValue;
		}
		return underlying.get(key);
	}

	public int size() {
		int size = underlying.size();

		for (Iterator iter = changedKeys.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			if (entry.getValue() == REMOVED) {
				--size;
			} else if (!underlying.containsKey(entry.getKey())) {
				++size;
			}
		}

		return size;
	}

	public Object put(Object key, Object value) {
		Object underlyingValue = underlying.get(key);
		Object oldChangedValue = changedKeys.get(key);

		if (value.equals(underlyingValue)) {
			// Same value as in underlying map, so just remove from changedKeys
			if (oldChangedValue != null) {
				changedKeys.remove(key);
			}
		} else {
			changedKeys.put(key, value);
		}

		// Return old value
		if (oldChangedValue == REMOVED) {
			return null;
		} else if (oldChangedValue != null) {
			return oldChangedValue;
		}
		return underlyingValue;
	}

	public Object remove(Object key) {
		Object underlyingValue = underlying.get(key);
		Object oldChangedValue = changedKeys.get(key);

		if (underlyingValue == null) {
			// Not in underlying map either, so just remove from changedKeys
			if (oldChangedValue != null) {
				changedKeys.remove(key);
			}
		} else {
			changedKeys.put(key, REMOVED);
		}

		// Return old value
		if (oldChangedValue == REMOVED) {
			return null;
		} else if (oldChangedValue != null) {
			return oldChangedValue;
		}
		return underlyingValue;
	}

	public Set entrySet() {
		return entrySet;
	}

	public Set getChangedKeys() {
		return Collections.unmodifiableSet(changedKeys.keySet());
	}

	public void changeUnderlying() {
		for (Iterator iter = changedKeys.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			if (entry.getValue() == REMOVED) {
				underlying.remove(entry.getKey());
			} else {
				underlying.put(entry.getKey(), entry.getValue());
			}
		}

		clearChanges();
	}

	public void clearChanges() {
		// changedKeys.clear();
		changedKeys = new HashMap();
	}

	public Object clone() {
		HashMapDiff clone = new HashMapDiff(underlying);
		clone.changedKeys = new HashMap(changedKeys);
		return clone;
	}

	// Private inner class; an object of this class is returned by entrySet.
	private class EntrySet extends AbstractSet {

		public int size() {
			return HashMapDiff.this.size();
		}

		public boolean contains(Object obj) {
			if (obj instanceof Map.Entry) {
				Map.Entry entry = (Map.Entry) obj;
				Object value = get(entry.getKey());
				if (value == null) {
					return false;
				}
				return value.equals(entry.getValue());
			}
			return false;
		}

		public Iterator iterator() {
			return new EntrySetIterator();
		}
	}

	// First iterates over the entry set of the underlying map,
	// skipping keys that have been removed and returning the changed values
	// for keys whose values have changed. Then iterates over the
	// changed keys, returning entries for those that are not in the
	// underlying map.
	private class EntrySetIterator implements Iterator {
		EntrySetIterator() {
			underlyingIter = underlying.entrySet().iterator();

			List addedEntries = new ArrayList();
			for (Iterator iter = changedKeys.entrySet().iterator(); iter.hasNext();) {
				Map.Entry changedEntry = (Map.Entry) iter.next();
				if ((changedEntry.getValue() != REMOVED)
						&& !underlying.containsKey(changedEntry.getKey())) {
					addedEntries.add(new Entry(changedEntry));
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

			HashMapDiff.this.remove(lastEntry.getKey());
			lastEntry = null;
		}

		void prepareNextEntry() {
			nextEntry = null;

			// Iterate over the underlying map until we find entry that
			// hasn't been removed. Use new value if it was changed.
			while (underlyingIter.hasNext()) {
				Map.Entry underlyingEntry = (Map.Entry) underlyingIter.next();
				Object key = underlyingEntry.getKey();
				Object changedValue = changedKeys.get(key);
				if (changedValue == REMOVED) {
					continue;
				} else if (changedValue != null) {
					// value changed, but not removed
					nextEntry = new Entry(key, changedValue);
					return;
				} else {
					// value unchanged, not removed
					nextEntry = new Entry(underlyingEntry);
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
	// map's entries because we don't want the setValue method to write
	// through to the underlying map; we want it to call the HashMapDiff's
	// put method instead. This put operation doesn't disrupt the iteration
	// because it doesn't change the underlying map.
	//
	// For iterating over added entries, we use our own Map.Entry objects
	// so that removals from the changedKeys map (which can happen if the
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
			HashMapDiff.this.put(getKey(), newValue);
			value = newValue;
			return oldValue;
		}
	}

	private static final Object REMOVED = new Object() {
	};

	private Map underlying;

	private Map changedKeys = new HashMap();

	private Set entrySet = new EntrySet();
}
