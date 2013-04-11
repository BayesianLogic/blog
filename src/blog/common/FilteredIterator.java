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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Abstract class for Iterator objects that filter or transform the objects
 * returned by an underlying iterator. Subclasses must implement the
 * <code>filter</code> method, which takes an object returned by the underlying
 * iterator, and either:
 * <ul>
 * <li>returns null, in which case the FilteredIterator skips this object;
 * <li>returns a non-null object, which will be returned by the
 * FilteredIterator.
 * </ul>
 */
public abstract class FilteredIterator implements Iterator {
	/**
	 * Creates a new FilteredIterator on the given underlying iterator.
	 */
	public FilteredIterator(Iterator underlying) {
		this.underlying = underlying;
	}

	private boolean loadNextObject() {
		if (nextObj == null) {
			while (underlying.hasNext()) {
				nextObj = filter(index++, underlying.next());
				if (nextObj != null) {
					return true; // found next object
				}
			}
			return false; // didn't find next object
		}
		return true; // next object already loaded
	}

	public boolean hasNext() {
		return loadNextObject();
	}

	public Object next() {
		if (!loadNextObject()) {
			throw new NoSuchElementException();
		}

		Object toReturn = nextObj;
		nextObj = null;
		return toReturn;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	protected abstract Object filter(int index, Object obj);

	private Iterator underlying;
	private Object nextObj;
	int index;
}
