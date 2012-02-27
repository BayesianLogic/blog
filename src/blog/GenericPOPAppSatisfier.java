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
 * Represents a generic satisfier of some POP application.  Origin 
 * functions can be evaluated on this object, but other functions can't.  
 * This object cannot be used as an argument for any instantiated random 
 * variable.  
 */
public class GenericPOPAppSatisfier extends GenericObject {
    /**
     * Creates a new generic satisfier for the given POP.  
     */
    public GenericPOPAppSatisfier(POP pop, Object[] genObjs) {
	super(pop.type());
	this.pop = pop;
	this.genObjs = genObjs;
    }

    public POP getPOP() {
	return pop;
    }

    public Object[] getGenObjs() {
	return genObjs;
    }

    public Object getOriginFuncValue(OriginFunction g) {
	int index = pop.getOriginFuncIndex(g);
	if (index == -1) {
	    return Model.NULL;
	}
	return genObjs[index];
    }

    public boolean isConsistentInContext(EvalContext context, Object obj) {
	NumberVar objPOPApp = context.getPOPAppSatisfied(obj);
	return ((objPOPApp != null)
		&& (objPOPApp.pop() == pop) 
		&& Arrays.equals(objPOPApp.args(), genObjs));
    }

    public String toString() {
	StringBuffer buf = new StringBuffer();
	buf.append("Generic(");
	buf.append(pop.type());
	buf.append(" : ");
	for (int i = 0; i < genObjs.length; ++i) {
	    buf.append(pop.originFuncs()[i]);
	    buf.append(" = ");
	    buf.append(genObjs[i]);
	    if (i + 1 < genObjs.length) {
		buf.append(", ");
	    }
	}
	buf.append(")");
	return buf.toString();
    }

    private POP pop;
    private Object[] genObjs;
}
