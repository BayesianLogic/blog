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

import blog.common.DGraph;
import blog.common.DefaultDGraph;
import blog.common.HashMapWithPreimages;
import blog.common.HashMultiMap;
import blog.common.IndexedHashMultiMap;
import blog.common.IndexedMultiMap;
import blog.common.MultiMap;
import blog.distrib.ListInterp;
import blog.model.FunctionSignature;
import blog.bn.DefaultCBN;


/**
 * Straightforward implementation of AbstractPartialWorld.
 */
public class DefaultPartialWorld extends AbstractPartialWorld implements
		Cloneable {

	/**
	 * Creates a new DefaultPartialWorld with no instantiated variables. This
	 * world will not use object identifiers for any types.
	 */
	public DefaultPartialWorld() {
		this(Collections.EMPTY_SET);
	}

	/**
	 * Creates a new DefaultPartialWorld with no instantiated variables and no
	 * identifiers.
	 * 
	 * @param idTypes
	 *          Set of Type objects for types that will be represented with object
	 *          identifiers
	 */
	public DefaultPartialWorld(Set idTypes) {
		super(idTypes);
		basicVarToValue = new HashMap();
		objToUsesAsValue = new HashMultiMap();
		objToUsesAsArg = new HashMultiMap();
		assertedIdToPOPApp = new HashMap();
		popAppToAssertedIds = new IndexedHashMultiMap();
		commIdToPOPApp = new HashMap();
		popAppToCommIds = new IndexedHashMultiMap();
        cbn = new DefaultCBN();
		varToUninstParent = new HashMapWithPreimages();
		varToLogProb = new HashMap();
		derivedVarToValue = new HashMap();
		
		/*added by cheng*/
		decisionToInterp = new HashSet();
		
	}

	public Object clone() {
		DefaultPartialWorld newWorld = new DefaultPartialWorld();
		cloneFields(newWorld);
		return newWorld;
	}

	public Set getDecisionInterp() {
		return decisionToInterp; 
	}
}
