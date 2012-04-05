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
 * An extension of the Map interface that provides efficient access to the
 * pre-image of a value in the map. The pre-image of a value y is the set of
 * keys that map to y.
 */
public interface MapWithPreimages extends Map {
	/**
	 * An empty MapWithPreimages.
	 */
	static final MapWithPreimages EMPTY_MAP_WITH_PREIMAGES = new EmptyMapWithPreimages();

	static class EmptyMapWithPreimages extends AbstractMapWithPreimages {
		EmptyMapWithPreimages() {
			map = Collections.EMPTY_MAP;
			preimages = MultiMap.EMPTY_MULTI_MAP;
		}
	}

	/**
	 * Returns the set of values in the map. This method differs from the
	 * <code>values</code> method in the <code>Map</code> interface in that it
	 * returns a Set rather than a Collection.
	 */
	Set valueSet();

	/**
	 * Returns the set of keys that map to <code>v</code>. If no keys map to
	 * <code>v</code>, this is the empty set.
	 * 
	 * @return an unmodifiable Set of keys
	 */
	Set getPreimage(Object v);

	/**
	 * Returns a MultiMap view of the inverse of this map. The MultiMap is backed
	 * by this MapWithPreimages, so it will change as this MapWithPreimages
	 * changes. It should be treated as unmodifiable.
	 */
	MultiMap getPreimages();

	/**
	 * Returns true if <code>k1</code> and <code>k2</code> are coreferent, that
	 * is, they are both keys that map to the same value.
	 */
	boolean isCorefPair(Object k1, Object k2);

	/**
	 * Returns the number of two-element sets of objects {k1, k2} such that
	 * isCorefPair(k1, k2) returns true. This can be computed by summing, over all
	 * values v in the map, the number of two-element subsets of getPreimage(v).
	 * The number of two-element subsets of a set of size n is n-choose-2, or n *
	 * (n-1) / 2.
	 * 
	 * <p>
	 * Note that this is different from computing the number of <b>ordered
	 * pairs</b> (k1, k2) such that isCorefPair(k1, k2) returns true; then we
	 * wouldn't divide by 2 in the formula above.
	 */
	int numCorefPairs();
}
