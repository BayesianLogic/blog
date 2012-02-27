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

package blog;

import java.util.*;

/**
 * Abstract implementation of the ObjectIterator interface.  Subclasses 
 * must implement the findNext method, and may also implement 
 * skipAfterNext.  
 */
public abstract class AbstractObjectIterator implements ObjectIterator {
    public boolean hasNext() {
	latestObj = null; // skipIndistinguishable may not work anymore
	if (canDetermineNext && (nextObj == null)) {
	    nextObj = findNext();
	}
	return (nextObj != null);
    }

    public Object next() {
	if (!hasNext()) {
	    throw new NoSuchElementException();
	}

	latestObj = nextObj;
	nextObj = null; // so it's not returned again
	return latestObj;
    }

    public void remove() {
	throw new UnsupportedOperationException
	    ("Can't remove from ObjectIterator.");
    }

    public int skipIndistinguishable() {
	if (latestObj == null) {
	    throw new IllegalStateException
		("Call to skipIndistinguishable was not immediately after "
		 + "a call to next.");
	}

	int numSkipped = skipAfterNext();
	latestObj = null;
	return numSkipped;
    }

    public boolean canDetermineNext() {
	hasNext(); // make sure canDetermineNext field is set properly
	return canDetermineNext;
    }

    /**
     * Behaves like skipIndistinguishable, except it can assume that this 
     * call to skipIndistinguishable comes immediately after a successful 
     * call to <code>next</code>, with no intervening calls to 
     * <code>findNext</code> or any other methods.  
     *
     * <p>This default implementation just returns zero.
     */
    protected int skipAfterNext() {
	return 0;
    }

    /**
     * Returns the next object to be returned by this iterator, or null if 
     * there are no more objects.  If the next object cannot be determined, 
     * this method sets the protected field <code>canDetermineNext</code> 
     * to <code>false</code> and returns null.
     *
     * <p>This method will not be called when <code>canDetermineNext</code>
     * is false.  However, it may be called again after it has returned null, 
     * in which case it should just return null again.
     */
    protected abstract Object findNext();

    private Object nextObj = null;
    private Object latestObj = null;
    
    /**
     * Should be set to false by findNext when the next object cannot 
     * be determined.
     */
    protected boolean canDetermineNext = true;
}
