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
 * A map that is represented as a set of changes to some underlying map. A
 * MapDiff object can be modified, but changes to the MapDiff do not affect the
 * underlying map unless <code>changeUnderlyingMap</code> is called. The
 * advantages of using a MapDiff rather than creating another Map object are:
 * <ul>
 * <li>We do not need to copy the underlying map, which would take time linear
 * in the size of the map.
 * <li>We can enumerate the changes between the new map (represented by the
 * MapDiff object) and the underlying map using MapDiff methods, without doing
 * an exhaustive comparison which would take time linear in the size of the
 * underlying map.
 * </ul>
 * 
 * <p>
 * A MapDiff behaves correctly if entries are placed in the underlying map after
 * the MapDiff is created. It also behaves correctly if entries are removed from
 * the underlying map, as long as those keys have not also been removed from the
 * MapDiff (then they may be double-counted).
 */
public interface MapDiff extends Map {
	/**
	 * Returns the set of keys that map to different values in this map than in
	 * the underlying map. This includes any keys that have values in this map but
	 * not the underlying map, or vice versa.
	 * 
	 * <p>
	 * The returned set may or may not be kept up to date as changes are made to
	 * this MapDiff.
	 * 
	 * @return unmodifiable Set of Object
	 */
	Set getChangedKeys();

	/**
	 * Changes the underlying map so it is equal to this map.
	 * 
	 * @throws UnsupportedOperationException
	 *           if the underlying map is not modifiable
	 */
	void changeUnderlying();

	/**
	 * Resets this map so it is equal to the underlying map.
	 */
	void clearChanges();
}
