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
 * A map where keys are mapped to non-empty Set objects. Has the same behavior
 * as the standard Map interface, except that the value passed to
 * <code>put</code> must be a Set; <code>get</code> returns an empty set instead
 * of <code>null</code> when the given key is not in the map; and there are new
 * <code>add</code> and <code>remove</code> methods.
 */
public interface MultiMap extends Map {
	/**
	 * Returns the set associated with the given key. If the key is not in the
	 * map, returns an empty set. The set returned is modifiable and backed by
	 * this multi-map: if values are added for the given key, they will show up in
	 * the returned set. However, the returned set may lose its connection to this
	 * multi-map if the multi-map's <code>put</code> method is called or if all
	 * the values for the given key are removed.
	 */
	Object get(Object key);

	/**
	 * Associates the given key with the given value, which must be a Set. If the
	 * given set is empty, the key is removed from the map.
	 * 
	 * @return the set previously associated with this key, or an empty set if the
	 *         key was not in the map
	 * 
	 * @throws IllegalArgumentException
	 *           if <code>value</code> is not a Set
	 */
	Object put(Object key, Object value);

	/**
	 * Adds the given value to the set associated with the given key. If the key
	 * is not yet in the map, it is added.
	 * 
	 * @return true if the MultiMap changed as a result of this call
	 */
	boolean add(Object key, Object value);

	/**
	 * Adds all elements of the given set to the set associated with the given
	 * key. If the key is not yet in the map, it is added.
	 * 
	 * @return true if the MultiMap changed as a result of this call
	 */
	boolean addAll(Object key, Set values);

	/**
	 * Removes the given value from the set associated with the given key. Does
	 * nothing if the value is not in that set. If the set ends up being empty,
	 * then the key is removed from the map.
	 * 
	 * @return true if the MultiMap changed as a result of this call
	 */
	boolean remove(Object key, Object value);

	/**
	 * Removes all elements of the given set from the set associated with the
	 * given key. If the associated set ends up being empty, then the key is
	 * removed from the map.
	 * 
	 * @return true if the MultiMap changed as a result of this call
	 */
	boolean removeAll(Object key, Set values);

	static class EmptyMultiMap extends AbstractMap implements MultiMap {
		public Object get(Object key) {
			return Collections.EMPTY_SET;
		}

		public boolean add(Object key, Object value) {
			throw new UnsupportedOperationException();
		}

		public boolean addAll(Object key, Set values) {
			throw new UnsupportedOperationException();
		}

		public boolean remove(Object key, Object value) {
			throw new UnsupportedOperationException();
		}

		public boolean removeAll(Object key, Set values) {
			throw new UnsupportedOperationException();
		}

		public Set entrySet() {
			return Collections.EMPTY_SET;
		}
	}

	/**
	 * An unmodifiable multi-map that maps all keys to the empty set.
	 */
	public static final MultiMap EMPTY_MULTI_MAP = new EmptyMultiMap();
}
