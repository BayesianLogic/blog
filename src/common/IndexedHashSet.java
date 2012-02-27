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
 * Implementation of the IndexedSet interface that stores objects in a
 * list, and uses a HashMap to map objects to their indices.  Indices
 * are assigned to objects consecutively as they are added.  When an
 * object is removed, the last element is swapped in to fill its
 * index; thus, removing an element that is not the last one may
 * change the index of the last element.  Getting the element at a 
 * given index takes constant time.  
 */
public class IndexedHashSet extends AbstractSet implements IndexedSet,
                                                           Cloneable {
    /**
     * Creates a new, empty IndexedSet.
     */
    public IndexedHashSet() {
    }

    /**
     * Creates a new IndexedSet whose elements are those in the given 
     * collection, ordered according to the collection's iterator order.
     */
    public IndexedHashSet(Collection c) {
	addAll(c);
    }

    public int size() {
	return elements.size();
    }

    public Iterator iterator() {
	return new IndexedSetIterator();
    }

    public boolean contains(Object o) {
	return indices.containsKey(o);
    }

    /**
     * Adds the given object to this IndexedSet, with an index equal to the 
     * previous size of the set.  Does nothing if the object was already in 
     * this set.
     *
     * @return true if the object was not already in this set
     */
    public boolean add(Object o) {
	if (contains(o)) {
	    return false;
	}

	int index = size();
	elements.add(o);
	indices.put(o, new Integer(index));
	return true;
    }

    /**
     * Removes the given object from this IndexedSet if it was present.  
     * If the given object was in the set but not at the end, this operation 
     * changes the index of the last element so it is equal to the previous 
     * index of this element.
     *
     * @return true if the object was in this set
     */
    public boolean remove(Object o) {
	Integer index = (Integer) indices.get(o);
	if (index == null) {
	    return false;
	}

	int lastIndex = size() - 1;
	if (index.intValue() != lastIndex) {
	    Object last = elements.get(lastIndex);
	    elements.set(index.intValue(), last);
	    indices.put(last, index);
	    ++internalRemovalCount;
	}
	elements.remove(lastIndex);
	indices.remove(o);
	return true;
    }

    public void clear() {
	elements.clear();
	indices.clear();
    }

    /**
     * Returns the object with the specified index in this IndexedSet.  
     *
     * @throws IndexOutOfBoundsException if the index is out of range
     *                              (<code>index < 0 || index >= size()</code>)
     */
    public Object get(int index) {
	return elements.get(index);
    }

    /**
     * Returns the index of the given object in this IndexedSet, or -1 if 
     * this IndexedSet does not contain the given object.
     */
    public int indexOf(Object o) {
	Integer index = (Integer) indices.get(o);
	if (index == null) {
	    return -1;
	}
	return index.intValue();
    }

    public Object clone() {
	IndexedHashSet clone = new IndexedHashSet();
	clone.indices = (Map) ((HashMap) indices).clone();
	clone.elements = (List) ((ArrayList) elements).clone();
	return clone;
    }

    private class IndexedSetIterator implements Iterator {
	IndexedSetIterator() {
	    initialInternalRemovalCount = internalRemovalCount;
	}

	public boolean hasNext() {
	    checkModification();
	    return (index < size());
	}

	public Object next() {
	    checkModification();
	    if (index >= size()) {
		throw new NoSuchElementException
		    ("No more elements in IndexedSet.");
	    }

	    prevIndex = index;
	    return elements.get(index++);
	}

	public void remove() {
	    checkModification();
	    if (prevIndex < 0) {
		throw new IllegalStateException
		    ("No object to remove in IndexSet iterator.");
	    }

	    Object toRemove = elements.get(prevIndex);
	    int lastIndex = size() - 1;
	    if (prevIndex != lastIndex) {
		Object last = elements.get(lastIndex);
		elements.set(prevIndex, last);
		indices.put(last, new Integer(prevIndex));
	    }
	    elements.remove(lastIndex);
	    indices.remove(toRemove);

	    index = prevIndex; // that object hasn't been returned yet
	    prevIndex = -1;    
	}

	private void checkModification() {
	    if (internalRemovalCount > initialInternalRemovalCount) {
		throw new ConcurrentModificationException
		    ("IndexedSet iterator is invalid because an element "
		     + "not at the end of the ordering has been removed.");
	    }
	}

	private int index = 0;
	private int prevIndex = -1;
	private int initialInternalRemovalCount;
    }

    private Map indices = new HashMap(); // from Object to Integer
    private List elements = new ArrayList(); // of Object

    private int internalRemovalCount = 0;

    /**
     * Test program.
     */
    public static void main(String[] args) {
	IndexedSet s = new IndexedHashSet();
	s.add("A");
	s.add("B");
	s.add("C");
	s.add("D");
	System.out.println(s); // [A, B, C, D]

	System.out.println("size(): " + s.size()); // 4
	System.out.println("contains(\"A\"): " + s.contains("A")); // true
	System.out.println("contains(\"Z\"): " + s.contains("Z")); // false
	System.out.println("add(\"A\"): " + s.add("A")); // false
	System.out.println("add(\"Z\"): " + s.add("Z")); // true
	System.out.println(s);                           // [A, B, C, D, Z]
	System.out.println("remove(\"C\"): " + s.remove("C")); // true
	System.out.println(s);                           // [A, B, Z, D]
	System.out.println("remove(\"D\"): " + s.remove("D")); // true
	System.out.println(s);                           // [A, B, Z]
	System.out.println("remove(\"D\"): " + s.remove("D")); // false
	System.out.println("contains(\"C\"): " + s.contains("C")); // false

	System.out.println();
	System.out.println("Iteration:");
	Iterator iter = s.iterator();
	System.out.println(iter.next()); // A
	System.out.println("add(\"C\"): " + s.add("C")); // true
	while (iter.hasNext()) {
	    System.out.println(iter.next()); // B   Z   C
	}
	System.out.println("remove(\"B\"): " + s.remove("B")); // true
	iter.hasNext(); // IllegalStateException
    }
}
