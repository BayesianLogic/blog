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
 * Abstract implementation of the MapWithPreimages interface. A concrete
 * subclass only needs to implement a constructor that initializes the protected
 * variables <code>map</code> and <code>preimages</code>. The implemented
 * methods do not support null keys or null values.
 */
public abstract class AbstractMapWithPreimages extends AbstractMap implements
		MapWithPreimages {

	public int size() {
		return map.size();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	public boolean containsValue(Object v) {
		return preimages.containsKey(v);
	}

	public Object get(Object key) {
		return map.get(key);
	}

	public Object put(Object key, Object newVal) {
		if (key == null) {
			throw new IllegalArgumentException("Null keys not supported.");
		}
		if (newVal == null) {
			throw new IllegalArgumentException("Null values not supported.");
		}

		Object oldVal = map.put(key, newVal);
		if (!newVal.equals(oldVal)) {
			if (oldVal != null) {
				preimages.remove(oldVal, key);
			}

			preimages.add(newVal, key);
		}

		return oldVal;
	}

	public Object remove(Object key) {
		Object oldVal = map.remove(key);
		updatePreimagesForRemoval(key, oldVal);
		return oldVal;
	}

	public void clear() {
		map.clear();
		preimages.clear();
	}

	public Set keySet() {
		return keySet;
	}

	public Collection values() {
		return values;
	}

	public Set entrySet() {
		return entrySet;
	}

	public Set valueSet() {
		return Collections.unmodifiableSet(preimages.keySet());
	}

	public Set getPreimage(Object value) {
		Set preimage = (Set) preimages.get(value);
		if (preimage == null) {
			return Collections.EMPTY_SET;
		}
		return Collections.unmodifiableSet(preimage);
	}

	public MultiMap getPreimages() {
		return preimages;
	}

	public boolean isCorefPair(Object k1, Object k2) {
		Object value1 = map.get(k1);
		return ((value1 != null) && (value1.equals(map.get(k2))));
	}

	public int numCorefPairs() {
		int sum = 0;
		for (Iterator iter = valueSet().iterator(); iter.hasNext();) {
			Object value = iter.next();
			int preimageSize = ((Set) preimages.get(value)).size();
			sum += ((preimageSize * (preimageSize - 1)) / 2);
		}
		return sum;
	}

	private void updatePreimagesForRemoval(Object key, Object oldVal) {
		if (oldVal != null) {
			preimages.remove(oldVal, key);
		}
	}

	/**
	 * Set that wraps around the underlying map and calls
	 * updatePreimagesForRemoval when a key is removed.
	 */
	private Set keySet = new AbstractSet() {
		public int size() {
			return map.size();
		}

		public boolean isEmpty() {
			return map.isEmpty();
		}

		public boolean contains(Object key) {
			return map.containsKey(key);
		}

		public boolean remove(Object key) {
			Object oldVal = map.remove(key);
			updatePreimagesForRemoval(key, oldVal);
			return (oldVal != null);
		}

		public Iterator iterator() {
			return new Iterator() {
				private Iterator iter = map.keySet().iterator();
				private Object lastKey = null;

				public boolean hasNext() {
					return iter.hasNext();
				}

				public Object next() {
					lastKey = iter.next();
					return lastKey;
				}

				public void remove() {
					if (lastKey == null) {
						throw new IllegalStateException("Nothing to remove.");
					}
					Object oldVal = map.get(lastKey);
					iter.remove();
					updatePreimagesForRemoval(lastKey, oldVal);
					lastKey = null;
				}
			};
		}
	};

	/**
	 * Collection that wraps around the underlying map and calls
	 * updatePreimagesForRemoval when a value is removed.
	 */
	private Collection values = new AbstractCollection() {
		public int size() {
			return map.size();
		}

		public boolean isEmpty() {
			return map.isEmpty();
		}

		public boolean contains(Object value) {
			return preimages.containsKey(value);
		}

		public boolean remove(Object value) {
			for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
				Map.Entry entry = (Map.Entry) iter.next();
				if (entry.getValue().equals(value)) {
					iter.remove();
					updatePreimagesForRemoval(entry.getKey(), entry.getValue());
					return true;
				}
			}
			return false;
		}

		public Iterator iterator() {
			return new Iterator() {
				private Iterator iter = map.entrySet().iterator();
				private Map.Entry lastEntry = null;

				public boolean hasNext() {
					return iter.hasNext();
				}

				public Object next() {
					lastEntry = (Map.Entry) iter.next();
					return lastEntry.getValue();
				}

				public void remove() {
					if (lastEntry == null) {
						throw new IllegalStateException("Nothing to remove.");
					}
					iter.remove();
					updatePreimagesForRemoval(lastEntry.getKey(), lastEntry.getValue());
					lastEntry = null;
				}
			};
		}
	};

	/**
	 * Set that wraps around the underlying map and calls
	 * updatePreimagesForRemoval when an entry is removed.
	 */
	private Set entrySet = new AbstractSet() {
		public int size() {
			return map.size();
		}

		public boolean isEmpty() {
			return map.isEmpty();
		}

		public boolean contains(Object entry) {
			return map.entrySet().contains(entry);
		}

		public boolean remove(Object o) {
			if (map.entrySet().remove(o)) {
				Map.Entry entry = (Map.Entry) o;
				updatePreimagesForRemoval(entry.getKey(), entry.getValue());
				return true;
			}
			return false;
		}

		public Iterator iterator() {
			return new Iterator() {
				private Iterator iter = map.entrySet().iterator();
				private Map.Entry lastEntry = null;

				public boolean hasNext() {
					return iter.hasNext();
				}

				public Object next() {
					lastEntry = (Map.Entry) iter.next();
					return lastEntry;
				}

				public void remove() {
					if (lastEntry == null) {
						throw new IllegalStateException("Nothing to remove.");
					}
					iter.remove();
					updatePreimagesForRemoval(lastEntry.getKey(), lastEntry.getValue());
					lastEntry = null;
				}
			};
		}
	};

	/**
	 * The map itself.
	 */
	protected Map map;

	/**
	 * MultiMap from values in <code>map</code> to the keys for which they serve
	 * as the value.
	 */
	protected MultiMap preimages;
}
