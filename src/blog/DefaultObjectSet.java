/*
 * Copyright (c) 2007, Massachusetts Institute of Technology
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
 * * Neither the name of the Massachusetts Institute of Technology nor
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

package blog;

import java.util.*;

import blog.common.IndexedHashSet;
import blog.common.IndexedSet;

/**
 * ObjectSet implementation that can be constructed using an ordinary
 * Collection. A DefaultObjectSet is not backed by this collection; it just
 * reflects the contents of that collection at the time of construction.
 */
public class DefaultObjectSet extends AbstractObjectSet {
	/**
	 * Creates an ObjectSet whose contents are the same as the current contents of
	 * the given collection.
	 */
	public DefaultObjectSet(Collection c) {
		this.elements = new IndexedHashSet(c);
	}

	protected Integer sizeInternal() {
		return new Integer(elements.size());
	}

	protected Boolean containsInternal(Object o) {
		return Boolean.valueOf(elements.contains(o));
	}

	public ObjectSet getExplicitVersion() {
		return this;
	}

	public ObjectIterator iterator(Set externallyDistinguished) {
		return new DefaultObjectIterator(elements.iterator());
	}

	public Object sample(int n) {
		return elements.get(n);
	}

	public int indexOf(Object o) {
		return elements.indexOf(o);
	}

	private IndexedSet elements;
}
