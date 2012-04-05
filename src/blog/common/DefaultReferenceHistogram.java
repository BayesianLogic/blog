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
import java.io.Serializable;

/**
 * Default implementation of the ReferenceHistogram interface. It uses a HashMap
 * to store the mapping from objects to numbers of references.
 */
public class DefaultReferenceHistogram implements ReferenceHistogram,
		Serializable {
	/**
	 * Creates a new DefaultReferenceHistogram with no references (and hence no
	 * referenced objects).
	 */
	public DefaultReferenceHistogram() {
		map = new HashMap();
		totalRefs = 0;
	}

	/**
	 * Adds to this histogram a reference referring to the given object. This adds
	 * 1 to the number of references for the given object.
	 * 
	 * @param obj
	 *          a Long value representing the ID of the object that the added
	 *          reference refers to
	 */
	public void addReference(Long obj) {
		map.put(obj, new Integer(getNumReferences(obj) + 1));
		totalRefs++;
	}

	/**
	 * Removes from this histogram a reference referring to the given object. This
	 * subtracts 1 from the number of references for the given object. If the
	 * histogram contains no reference referring to the object, an
	 * IllegalStateException is thrown.
	 * 
	 * @param obj
	 *          a Long value representing the ID of the object that the removed
	 *          citation refers to
	 */
	public void removeReference(Long obj) {
		int oldNumRefs = getNumReferences(obj);
		if (oldNumRefs == 0) {
			throw new IllegalStateException("No such reference to remove");
		} else if (oldNumRefs == 1) {
			map.remove(obj);
		} else {
			map.put(obj, new Integer(oldNumRefs - 1));
		}

		totalRefs--;
	}

	/**
	 * Takes one of the references in this histogram that refers to oldObj, and
	 * makes it refer to newObj instead. Throws an IllegalStateException if the
	 * histogram contains no reference that refers to oldObj.
	 */
	public void changeReference(Long oldObj, Long newObj) {
		removeReference(oldObj);
		addReference(newObj);
	}

	public int numReferencedObjects() {
		return map.keySet().size();
	}

	public int totalReferences() {
		return totalRefs;
	}

	public int getNumReferences(Long obj) {
		if (map.containsKey(obj)) {
			return ((Integer) map.get(obj)).intValue();
		}
		return 0;
	}

	public Iterator iterator() {
		return Collections.unmodifiableSet(map.keySet()).iterator();
	}

	Map map;
	int totalRefs;
}
