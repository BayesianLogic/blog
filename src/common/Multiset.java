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
 * A multiset is like a set, except that it can include multiple copies of 
 * an element.  It differs from a list in that the element occurrences are 
 * not in any order.  Formally, a multiset consists of a set S and a function 
 * m(x) that maps each element x of S to the number of times it occurs in the 
 * multiset.
 * <p>
 * The Multiset interface extends the Collection interface, adding methods 
 * for getting the number of distinct elements and the number of times a 
 * given element occurs in the multiset.  Its size() method returns the 
 * sum of the occurrence counts of all the elements.  The iterator returned 
 * by the iterator() method on a multiset may return multiple copies of the 
 * same element.  To get an iterator over the distinct elements, use 
 * distinctIterator().
 */
public interface Multiset extends Collection {
    /**
     * Returns the number of occurrences of the given element in this multiset.
     */
    int count(Object o);

    /**
     * Returns the set of entries in the multiset.  An entry is a pair 
     * (e, n) where e is an element of the multiset and n is the number of 
     * times e occurs in the multiset.  The returned set contains exactly 
     * one entry for each distinct element e.  Thus, entrySet.size() 
     * returns the number of distinct elements in the multiset.  
     *
     * <p> The returned set supports element removal: removing an entry 
     * corresponds to deleting all occurrences of the corresponding element 
     * from the multiset.  However, the returned set does not support 
     * the <code>add</code> or <code>addAll</code> methods.  
     *
     * @return a Set of Multiset.Entry objects
     */
    Set entrySet();

    /**
     * Interface for objects that represent entries in a multiset.
     */
    interface Entry {
	/**
	 * Returns the element in this entry.
	 */
	Object getElement();

	/**
	 * Returns the count of this element.
	 */
	int getCount();
    }
}
