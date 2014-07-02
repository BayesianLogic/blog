/*
 * Copyright (c) 2005, 2006, Regents of the University of California
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

package blog.objgen;

import java.util.*;

/**
 * Iterator over an {@link blog.objgen.ObjectSet ObjectSet}. This interface extends the
 * Java Iterator interface in two ways:
 * <ul>
 * <li><b>Indistinguishable objects</b>: Two objects are indistinguishable in a
 * partial world if they satisfy the same POP application and are not used as
 * values or arguments in any instantiated basic random variable. This interface
 * includes a <code>skipIndistinguishable</code> method that skips objects that
 * are indistinguishable from the last returned object, and returns the number
 * of objects skipped.
 * 
 * <li><b>Partially defined sets</b>: A partial world may be complete enough to
 * determine whether some objects are in a given set, but not complete enough to
 * determine all the set's elements. The <code>canDetermineNext</code> method
 * checks whether the world underlying an iterator is complete enough to
 * determine the iterator's next element.
 * </ul>
 */
public interface ObjectIterator extends Iterator {
	/**
	 * Skips remaining objects that are indistinguishable from the object returned
	 * by the last call to <code>next</code>. Two objects are indistinguishable if
	 * the underlying partial world implies that they both satisfy the same POP
	 * application, neither one serves as a value or argument for any instantiated
	 * basic random variable, and neither one occurs in the set of
	 * "externally distinguished" objects passed to the <code>iterator</code> call
	 * that produced this iterator. Typically, the externally distinguished
	 * objects are the values of logical variables in the current scope.
	 * 
	 * <p>
	 * This method can only be called immediately after a call to
	 * <code>next</code>: that is, there can be no intervening calls to any
	 * methods on this iterator. Even calling <code>hasNext</code> may cause some
	 * implementations to lose internal state that is necessary for skipping
	 * indistinguishable objects.
	 * 
	 * @return the number of objects skipped (may be zero). The return value is
	 *         always zero if canDetermineNext would return false.
	 * 
	 * @throws IllegalStateException
	 *           if <code>next</code> has not been called yet, or if any other
	 *           method has been called on this iterator since the last call to
	 *           <code>next</code>
	 */
	int skipIndistinguishable();

	/**
	 * Returns true if this ObjectIterator is running on a partial world that is
	 * complete enough to determine whether there is a next object, and if so,
	 * what that object is. If <code>hasNext</code> is called when
	 * <code>canDetermineNext</code> would return false, then <code>hasNext</code>
	 * returns false.
	 */
	boolean canDetermineNext();
}
