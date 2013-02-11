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

package blog.world;

import java.util.*;

import blog.ObjectIdentifier;
import blog.bn.BasicVar;
import blog.bn.NumberVar;

/**
 * Interface for objects that can be notified of changes to the values of random
 * variables in a PartialWorld.
 */
public interface WorldListener {
	/**
	 * Called when the value of a basic random variable has changed. If the
	 * variable was not previously instantiated, <code>oldValue</code> is null. If
	 * it has just been uninstantiated, then <code>newValue</code> is null.
	 */
	void varChanged(BasicVar rv, Object oldValue, Object newValue);

	/**
	 * Called when the assertion that a partial world makes about the given object
	 * identifier has changed. If the identifier was not previously asserted to
	 * satisfy any POP application, then <code>oldPOPApp</code> is null. If the
	 * identifier has just been unasserted, then <code>newPOPApp</code> is null.
	 */
	void identifierChanged(ObjectIdentifier id, NumberVar oldPOPApp,
			NumberVar newPOPApp);
}
