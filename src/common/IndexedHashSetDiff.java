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
 * Implementation of the IndexedSet interface that is represented as a 
 * set of differences from an underlying IndexedSet.  
 *
 * <p>An IndexedHashSetDiff will behave correctly if objects are added
 * to the underlying set after the IndexedHashSetDiff is created, as
 * long as these objects have not also been added to the
 * IndexedHashSetDiff (then they would be double-counted).  Removing
 * objects from the underlying set may cause an IndexedHashSetDiff to
 * behave incorrectly, because it may change the indices of elements
 * of the underlying set.  
 */
public class IndexedHashSetDiff extends AbstractSet 
                                implements IndexedSetDiff, Cloneable {
    /**
     * Creates a new IndexedHashSetDiff with the given underlying IndexedSet.
     */
    public IndexedHashSetDiff(IndexedSet underlying) {
	this.underlying = underlying;
    }

    public int size() {
	return (underlying.size() - removedObjs.size() + additions.size());
    }

    /**
     * Returns an iterator that is robust to additions to this 
     * IndexedSetDiff and additions to the underlying set.  It is also 
     * robust to removals from this IndexedSetDiff if the removed object 
     * was in the underlying set at the time of removal.
     */
    public Iterator iterator() {
	return new IndexedSetDiffIterator();
    }

    public boolean contains(Object o) {
	return (additions.contains(o)
		|| (underlying.contains(o) && !removedObjs.contains(o)));
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

	additions.add(o);
	return true;
    }

    /**
     * Removes the given object from this IndexedSet if it was present.  
     * If the object was in the set but not at the end of the ordering, 
     * this operation may change the indices of some elements.  
     *
     * @return true if the object was in this set
     */
    public boolean remove(Object o) {
	if (underlying.contains(o)) {
	    // Removing an object that is in the underlying set
	    boolean removed = removedObjs.add(o);
	    if (removed) {
		int index = underlying.indexOf(o);
		removalIndices.add(new Integer(index));
	    }
	    return removed;
	} 

	return additions.remove(o);
    }

    /**
     * Returns the object with the specified index in this IndexedSet.  
     * Currently, if the given index corresponds to an element of the 
     * underlying set, then this method takes time linear in the number of 
     * objects removed from the underlying set.  
     *
     * @throws IndexOutOfBoundsException if the index is out of range
     *                              (<code>index < 0 || index >= size()</code>)
     */
    public Object get(int index) {
	int curUnderlyingSize = underlying.size() - removalIndices.size();
	if (index < curUnderlyingSize) {
	    for (Iterator iter = removalIndices.iterator(); iter.hasNext(); ) {
		Integer removalIndex = (Integer) iter.next();
		if (removalIndex.intValue() <= index) {
		    ++index; // skip over this removed index
		} else {
		    break; 
		}
	    }
	    return underlying.get(index);
	}

	return additions.get(index - curUnderlyingSize);
    }

    /**
     * Returns the index of the given object in this IndexedSet, or -1 if 
     * this IndexedSet does not contain the given object.
     */
    public int indexOf(Object o) {
	if (underlying.contains(o) && !removedObjs.contains(o)) {
	    int index = underlying.indexOf(o);

	    // Because removedObjs does not contain o, we know index is 
	    // not in removedIndices.
	    int numRemovalsBefore 
		= removalIndices.headSet(new Integer(index)).size();
	    return (index - numRemovalsBefore);
	}

	int index = additions.indexOf(o);
	if (index == -1) {
	    return -1;
	}
	int curUnderlyingSize = underlying.size() - removalIndices.size();
	return (curUnderlyingSize + index);
    }

    /**
     * Returns the set of objects that are in this set and not the 
     * underlying set.
     */
    public Set getAdditions() {
	return Collections.unmodifiableSet(additions);
    }

    /**
     * Returns the set of objects that are in the underlying set but not 
     * in this set.
     */
    public Set getRemovals() {
	return Collections.unmodifiableSet(removedObjs);
    }

    /**
     * Changes the underlying IndexedSet so it is equal to this one, and 
     * clears the changes in this IndexedSetDiff.  Note that his operation 
     * may change the indices of some elements.
     */
    public void changeUnderlying() {
	for (Iterator iter = removedObjs.iterator(); iter.hasNext(); ) {
	    underlying.remove(iter.next());
	}
	for (Iterator iter = additions.iterator(); iter.hasNext(); ) {
	    underlying.add(iter.next());
	}

	clearChanges();
    }

    /**
     * Clears the changes in this IndexedSetDiff so it is once again equal 
     * to the underlying IndexedSet.
     */
    public void clearChanges() {
	additions.clear();
	removedObjs.clear();
	removalIndices.clear();
    }

    public Object clone() {
	IndexedHashSetDiff clone = new IndexedHashSetDiff(underlying);
	clone.additions = (IndexedSet) ((IndexedHashSet) additions).clone();
	clone.removalIndices = (SortedSet) ((TreeSet) removalIndices).clone();
	clone.removedObjs = (Set) ((HashSet) removedObjs).clone();
	return clone;
    }

    private class IndexedSetDiffIterator implements Iterator {
	IndexedSetDiffIterator() {
	    underlyingIter = underlying.iterator();
	    additionsIter = additions.iterator();
	}

	public boolean hasNext() {
	    if (nextFromUnderlying == null) {
		loadNextFromUnderlying(); 
	    }
	    return ((nextFromUnderlying != null) || additionsIter.hasNext());
	}

	public Object next() {
	    if (nextFromUnderlying == null) {
		loadNextFromUnderlying();
	    }

	    latestFromUnderlying = nextFromUnderlying;
	    nextFromUnderlying = null; // so it isn't returned again
	    if (latestFromUnderlying != null) {
		latestFromUnderlyingRemoved = false;
		return latestFromUnderlying;
	    } else {
	        return additionsIter.next();
	    }
	}

	public void remove() {
	    if (latestFromUnderlying != null) {
		if (latestFromUnderlyingRemoved) {
		    throw new IllegalStateException
			("remove() has already been called since last call "
			 + "to next().");
		} else {
		    // Removing an object that was in the underlying set.
		    // Don't need to worry about iterator because we aren't 
		    // actually modifying the underlying set.
		    int index = underlying.indexOf(latestFromUnderlying);
		    removalIndices.add(new Integer(index));
		    removedObjs.add(latestFromUnderlying);
		    latestFromUnderlyingRemoved = true;
		}
	    } else {
		additionsIter.remove();
	    }
	}

	private void loadNextFromUnderlying() {
	    while (underlyingIter.hasNext()) {
		nextFromUnderlying = underlyingIter.next();
		if (!removedObjs.contains(nextFromUnderlying)) {
		    return; // got a valid object
		}
	    }
	    
	    // must have reached end of underlying set
	    nextFromUnderlying = null;
	}

	private Iterator underlyingIter;
	private Iterator additionsIter;
	private Object latestFromUnderlying = null;
	private Object nextFromUnderlying = null;
	private boolean latestFromUnderlyingRemoved = false;
    }
    

    private IndexedSet underlying;
    private IndexedSet additions = new IndexedHashSet();
    private SortedSet removalIndices = new TreeSet();
    private Set removedObjs = new HashSet();

    /**
     * Test program.
     */
    public static void main(String[] args) {
	IndexedSet underlying = new IndexedHashSet();
	for (int i = 0; i < 5; ++i) {
	    underlying.add(new Integer(i));
	}
	System.out.println("underlying: " + underlying); // [0 1 2 3 4]

	IndexedSetDiff diff = new IndexedHashSetDiff(underlying);
	System.out.println("diff: " + diff); // [0 1 2 3 4]

	diff.add(new Integer(5));
	diff.add(new Integer(6));
	System.out.println("diff: " + diff); // [0 1 2 3 4 5 6]
	System.out.println("underlying: " + underlying); // [0 1 2 3 4]
	
	diff.remove(new Integer(5));
	System.out.println("diff: " + diff); // [0 1 2 3 4 6]
	
	diff.remove(new Integer(1));
	diff.remove(new Integer(3));
	System.out.println("diff: " + diff); // [0 2 4 6]
	
	System.out.println();
	System.out.println("indexOf(0): " 
			   + diff.indexOf(new Integer(0))); // 0
	System.out.println("indexOf(2): " 
			   + diff.indexOf(new Integer(2))); // 1
	System.out.println("indexOf(4): " 
			   + diff.indexOf(new Integer(4))); // 2
	System.out.println("indexOf(6): " 
			   + diff.indexOf(new Integer(6))); // 3
	System.out.println("indexOf(1): " 
			   + diff.indexOf(new Integer(1))); // -1

	System.out.println();
	System.out.println("get(0): " + diff.get(0)); // 0
	System.out.println("get(1): " + diff.get(1)); // 2
	System.out.println("get(2): " + diff.get(2)); // 4
	System.out.println("get(3): " + diff.get(3)); // 6
    }
}
