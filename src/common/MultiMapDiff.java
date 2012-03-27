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
 * A MultiMap that is represented as a set of changes to some underlying
 * MultiMap. The changes are additions and removals on the sets of objects
 * associated with various keys. When using a MultiMapDiff, we do not need to
 * copy any information for keys whose associated sets do not change. And for a
 * keys whose set does change, we still do not need to copy all the objects that
 * are in that set in both the underlying world and the MultiMapDiff.
 */
public interface MultiMapDiff extends MultiMap {
	/**
	 * Returns the set of keys whose associated value sets are different in this
	 * multi-map and the underlying multi-map. This includes keys that were added
	 * or removed.
	 * 
	 * <p>
	 * The returned set may or may not be kept up to date as changes are made to
	 * this MultiMapDiff.
	 * 
	 * @return unmodifiable Set of Objects
	 */
	Set getChangedKeys();

	/**
	 * Returns the set of values that are associated with the given key in this
	 * multi-map and not in the underlying multi-map. Returns an empty set if the
	 * key is not in this multi-map.
	 * 
	 * <p>
	 * The returned set may or may not be kept up to date as changes are made to
	 * this MultiMapDiff.
	 * 
	 * @return unmodifiable Set of Objects
	 */
	Set getAddedValues(Object key);

	/**
	 * Returns the set of values that are associated with the given key in the
	 * underlying multi-map but not in this multi-map. Returns an empty set if the
	 * key is not in the underlying multi-map.
	 * 
	 * <p>
	 * The returned set may or may not be kept up to date as changes are made to
	 * this MultiMapDiff.
	 * 
	 * @return unmodifiable Set of Objects
	 */
	Set getRemovedValues(Object key);

	/**
	 * Changes the underlying multi-map to equal this multi-map.
	 */
	void changeUnderlying();

	/**
	 * Resets this multi-map to be equal to the underlying multi-map.
	 */
	void clearChanges();
}
