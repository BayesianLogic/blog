/*
 * Copyright (c) 2007, Massachusetts Institute of Technology
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
 * Represents a map with preimages as a set of differences relative to 
 * an underlying map with preimages.  Uses a HashMapDiff for the forward 
 * mapping and a HashMultiMapDiff for the reverse mapping.  
 */
public class MapWithPreimagesDiff extends AbstractMapWithPreimages 
    implements MapDiff {

    /**
     * Creates a MapWithPreimagesDiff with the given underlying 
     * MapWithPreimages.  Initially, this object represents no differences 
     * relative to the underlying map.
     */
    public MapWithPreimagesDiff(MapWithPreimages underlying) {
	map = new HashMapDiff(underlying);
	preimages = new HashMultiMapDiff(underlying.getPreimages());
    }

    public Set getChangedKeys() {
	return ((HashMapDiff) map).getChangedKeys();
    }

    public void changeUnderlying() {
	// Only need to change the underlying MapWithPreimages through the 
	// MapDiff; it will update the preimages map itself.
	((HashMapDiff) map).changeUnderlying();
	((HashMultiMapDiff) preimages).clearChanges();
    }

    public void clearChanges() {
	((HashMapDiff) map).clearChanges();
	((HashMultiMapDiff) preimages).clearChanges();
    }
}
