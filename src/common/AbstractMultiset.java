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

package common;

import java.util.*;

/**
 * Abstract implementation of the Multiset class.
 */
public abstract class AbstractMultiset extends AbstractCollection 
                                       implements Multiset {
    /**
     * Returns the number of occurrences of the given element in this multiset.
     */
    public abstract int count(Object o);

    /**
     * Returns the set of entries in the multiset.  An entry is a pair 
     * (e, n) where e is an element of the multiset and n is the number of 
     * times e occurs in the multiset.  The returned set contains exactly 
     * one entry for each distinct element e.  Thus, entrySet.size() 
     * returns the number of distinct elements in the multiset.  
     *
     * The returned set supports element removal: removing an entry 
     * corresponds to deleting all occurrences of the corresponding element 
     * from the multiset.  However, the returned set does not support 
     * the <code>add</code> or <code>addAll</code> methods.  
     */
    public abstract Set entrySet();

    /**
     * Returns true if <code>count(o)</code> returns a number greater 
     * than zero.
     */
    public boolean contains(Object o) {
	return (count(o) > 0);
    }

    /**
     * Two multisets are equal if they have the same elements with 
     * the same occurrence counts.
     */
    public boolean equals(Object o) {
	if (o instanceof Multiset) {
	    Multiset other = (Multiset) o;
	    return entrySet().equals(other.entrySet());
	}
	return false;
    }

    public int hashCode() {
	return entrySet().hashCode();
    }

    /**
     * Abstract implementation of the Multiset.Entry interface.
     */
    public static abstract class Entry implements Multiset.Entry {
	/**
	 * Returns the element in this entry.
	 */
	public abstract Object getElement();

	/**
	 * Returns the count of this element.
	 */
	public abstract int getCount();
		
	public boolean equals(Object o) {
	    if (o instanceof Multiset.Entry) {
		Multiset.Entry other = (Multiset.Entry) o;
		return (other.getElement().equals(getElement())
			&& (other.getCount() == getCount()));
	    }
	    return false;
	}
		
	public int hashCode() {
	    return (getElement().hashCode() ^ getCount());
	}
		
	public String toString() {
	    return ("(" + getElement() + ", " + getCount() + ")");
	}
    }
}	
