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
 * A HashSet class that uses weak references to its elements. This means that if
 * the only reference to an object is from this set, the object will be
 * garbage-collected and removed from this set.
 * 
 * The current implementation is built on WeakHashMap: it's just a map where
 * every key is mapped to Boolean.TRUE. It would be a bit more elegant (and save
 * some memory) to write this class from scratch. But it can't be built on top
 * of an ordinary HashSet, because we want to store WeakReference objects while
 * using the <code>equals</code> and <code>hashCode</code> methods of their
 * referents.
 */
public class WeakHashSet extends AbstractSet {
	/**
	 * Creates a new, empty WeakHashSet.
	 */
	public WeakHashSet() {
	}

	/**
	 * Creates a new WeakHashSet whose elements are weak references to the
	 * elements of the given collection.
	 */
	public WeakHashSet(Collection c) {
		for (Iterator iter = c.iterator(); iter.hasNext();) {
			add(iter.next());
		}
	}

	public int size() {
		return map.size();
	}

	public boolean contains(Object o) {
		return map.containsKey(o);
	}

	public Iterator iterator() {
		return map.keySet().iterator();
	}

	public boolean add(Object o) {
		return (map.put(o, Boolean.TRUE) == null);
	}

	public boolean remove(Object o) {
		return (map.remove(o) != null);
	}

	private WeakHashMap map = new WeakHashMap();
}
