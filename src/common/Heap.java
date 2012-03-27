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
 * Collection with an <code>extractMin</code> for efficiently getting the
 * minimum-cost element. Rather than just holding objects and using a comparator
 * to compare them (like java.util.TreeSet does), implementations of this
 * interface hold Heap.Entry objects, which consist of an object and a
 * real-valued cost. The <code>decreaseCost</code> method can be used to
 * decrease an entry's cost and update its position in the heap.
 */
public interface Heap extends Collection {
	/**
	 * Interface for entries in a heap. An entry consists of an element, an
	 * associated cost, and a pointer to a location in the heap (which is not
	 * accessible through the interface, but is used by the
	 * <code>decreaseCost</code> method).
	 */
	static interface Entry {
		Object getElement();

		double getCost();
	}

	/**
	 * Returns the minimum-cost entry in this heap, without modifying the heap.
	 * 
	 * @throws NoSuchElementException
	 *           if the heap is empty
	 */
	Heap.Entry peekMin();

	/**
	 * Returns the minimum-cost entry in this heap and removes it from the heap.
	 * 
	 * @throws NoSuchElementException
	 *           if the heap is empty
	 */
	Heap.Entry extractMin();

	/**
	 * Adds an entry to this heap for the given object and cost. This does not
	 * affect any other entries for the same object. Returns the entry that was
	 * added.
	 */
	Heap.Entry add(Object o, double cost);

	/**
	 * Decreases the cost for the given entry. The entry must already be in this
	 * heap, and the new cost must be less than or equal to its current cost.
	 * 
	 * <p>
	 * Warning: If the new cost is greater than the current one, this method will
	 * not produce an error message, but the heap may behave incorrectly.
	 */
	void decreaseCost(Heap.Entry entry, double newCost);

	/**
	 * Changes the cost for the given entry. The entry must already be in this
	 * heap.
	 */
	void changeCost(Heap.Entry entry, double newCost);
}
