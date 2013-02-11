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
 * Implementation of the MultiMap interface that uses a HashMap for the map and
 * HashSets for automatically created sets.
 * 
 * <p>
 * Technically, a MultiMap implementation should put a wrapper around the
 * iterators for the underlying data structure so the values associated with
 * keys are <b>unmodifiable</b> sets. Also, its Map.Entry objects should have a
 * <code>setValue</code> method that removes the key when the value is set to an
 * empty set. For now, we don't bother with these issues.
 */
public class HashMultiMap extends AbstractMap implements MultiMap, Cloneable {
	/**
	 * Creates a new, empty HashMultiMap.
	 */
	public HashMultiMap() {
		map = new HashMap();
	}

	/**
	 * Creates a new, empty HashMultiMap. If <code>maintainOrder</code> is true,
	 * then this instance uses a LinkedHashMap and LinkedHashSets so that the
	 * iteration orders for keys, and for elements of value sets, are predictable.
	 */
	public HashMultiMap(boolean maintainOrder) {
		this.maintainOrder = maintainOrder;
		if (maintainOrder) {
			map = new LinkedHashMap();
		} else {
			map = new HashMap();
		}
	}

	/**
	 * Creates a new HashMultiMap that is equal to the given MultiMap.
	 */
	public HashMultiMap(MultiMap orig) {
		map = new HashMap();
		for (Iterator iter = orig.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			map.put(entry.getKey(), copySet((Set) entry.getValue()));
		}
	}

	public int size() {
		return map.size();
	}

	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	public Object get(Object key) {
		return new ValueSet(key);
	}

	public Object put(Object key, Object value) {
		if (!(value instanceof Set)) {
			throw new IllegalArgumentException(
					"Values stored in MultiMap must be sets.");
		}

		Object oldValue = map.get(key);
		map.put(key, copySet((Set) value)); // in case value unmodifiable
		return ((oldValue == null) ? emptySet() : oldValue);
	}

	public boolean add(Object key, Object value) {
		Set s = addKeyInternal(key);
		return s.add(value);
	}

	public boolean addAll(Object key, Set values) {
		if (values.isEmpty()) {
			return false;
		}

		Set s = addKeyInternal(key);
		return s.addAll(values);
	}

	public Object remove(Object key) {
		Object oldValue = map.get(key);
		map.remove(key);
		return ((oldValue == null) ? emptySet() : oldValue);
	}

	public boolean remove(Object key, Object value) {
		Set s = (Set) map.get(key);
		if (s == null) {
			return false;
		}

		if (s.remove(value)) {
			if (s.isEmpty()) {
				map.remove(key);
			}
			return true;
		}
		return false;
	}

	public boolean removeAll(Object key, Set values) {
		Set s = (Set) map.get(key);
		if (s == null) {
			return false;
		}

		if (s.removeAll(values)) {
			if (s.isEmpty()) {
				map.remove(key);
			}
			return true;
		}
		return false;
	}

	public void clear() {
		map.clear();
	}

	public Set entrySet() {
		return map.entrySet();
	}

	public Set keySet() {
		return map.keySet();
	}

	public boolean equals(Object o) {
		return map.equals(o);
	}

	public int hashCode() {
		return map.hashCode();
	}

	/**
	 * Returns a shallow copy of this multi-map. he copy is "shallow" in that it
	 * does not create new copies of the keys or elements; however, it does make
	 * copies of the Set objects used to store elements in the map.
	 */
	public Object clone() {
		HashMultiMap clone = new HashMultiMap(maintainOrder);
		if (maintainOrder) {
			clone.map = new HashMap(map);
		} else {
			clone.map = new LinkedHashMap(map);
		}

		// Copy each set in the map
		for (Iterator iter = clone.map.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			entry.setValue(copySet((Set) entry.getValue()));
		}
		return clone;
	}

	protected Set emptySet() {
		return Collections.EMPTY_SET;
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

	private Set addKeyInternal(Object key) {
		Set s = (Set) map.get(key);
		if (s == null) {
			s = newSet();
			map.put(key, s);
		}
		return s;
	}

	protected class ValueSet extends AbstractSet {
		protected ValueSet(Object key) {
			this.key = key;
		}

		public int size() {
			if (set == null) {
				set = (Set) map.get(key);
				if (set == null) {
					return 0;
				}
			}
			return set.size();
		}

		public boolean contains(Object o) {
			if (set == null) {
				set = (Set) map.get(key);
				if (set == null) {
					return false;
				}
			}
			return set.contains(o);
		}

		public Iterator iterator() {
			if (set == null) {
				set = (Set) map.get(key);
				if (set == null) {
					return emptySet().iterator();
				}
			}
			return new ValueSetIterator();
		}

		public boolean add(Object o) {
			if (set == null) {
				set = (Set) map.get(key);
				if (set == null) {
					set = newSet();
					map.put(key, set);
				}
			}
			return set.add(o);
		}

		public boolean remove(Object o) {
			if (set == null) {
				set = (Set) map.get(key);
				if (set == null) {
					return false; // can't remove from empty set
				}
			}

			boolean removed = set.remove(o);
			if (set.isEmpty()) {
				map.remove(key);
				set = null;
			}
			return removed;
		}

		// We implement our own iterator class just so we can detect
		// when a call to the iterator's remove method makes the set empty.
		protected class ValueSetIterator implements Iterator {
			protected ValueSetIterator() {
				iter = set.iterator();
			}

			public boolean hasNext() {
				return iter.hasNext();
			}

			public Object next() {
				return iter.next();
			}

			public void remove() {
				iter.remove();
				if (set.isEmpty()) {
					map.remove(key);
					set = null;
				}
			}

			protected Iterator iter;
		}

		protected Object key;
		protected Set set = null;
	}

	protected HashMap map;
	protected boolean maintainOrder = false;
}
