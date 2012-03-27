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

package common;

import java.util.*;

/**
 * Implementation of the IndexedMultiMap interface.
 */
public class IndexedHashMultiMap extends HashMultiMap implements
		IndexedMultiMap {
	/**
	 * Creates a new, empty IndexedMultiMap.
	 */
	public IndexedHashMultiMap() {
		super();
	}

	/**
	 * Creates a new IndexedMultiMap that is initially equal to the given
	 * IndexedMultiMap.
	 */
	public IndexedHashMultiMap(IndexedMultiMap orig) {
		super(orig);
	}

	public Object get(Object key) {
		return new IndexedValueSet(key);
	}

	protected Set emptySet() {
		return IndexedSet.EMPTY_INDEXED_SET;
	}

	protected Set newSet() {
		return new IndexedHashSet();
	}

	protected Set copySet(Set orig) {
		return new IndexedHashSet(orig);
	}

	protected class IndexedValueSet extends ValueSet implements IndexedSet {
		protected IndexedValueSet(Object key) {
			super(key);
		}

		public int indexOf(Object o) {
			if (set == null) {
				set = (IndexedSet) map.get(key);
				if (set == null) {
					return -1;
				}
			}
			return ((IndexedSet) set).indexOf(o);
		}

		public Object get(int i) {
			if (set == null) {
				set = (IndexedSet) map.get(key);
				if (set == null) {
					// throw exception
					return IndexedSet.EMPTY_INDEXED_SET.get(i);
				}
			}
			return ((IndexedSet) set).get(i);
		}
	}
}
