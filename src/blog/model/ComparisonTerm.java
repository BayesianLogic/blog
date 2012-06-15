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
package blog.model;

import java.util.*;
import blog.Substitution;
import blog.bn.BayesNetVar;
import blog.bn.DerivedVar;
import blog.bn.RandFuncAppVar;
import blog.GenericObject;
import blog.common.Util;
import blog.sample.EvalContext;

/**
 * @author rbharath
 *
 * A term consisting of two terms and a comparison between them. Comparison terms
 * are inserted in the model by transExpr.
 */
public class ComparisonTerm extends Formula {
    /**
     * Creates a Comparison Term with the two given left and right terms and 
     * comparison type. The possible comparison type values are given as static
     * constants in OpExpr.
     */
    public ComparisonTerm(Term l, Term r, int t) {
        left = l;
        right = r;
        compType = t;
    }

	public Set getSatisfiersIfExplicit(EvalContext context,
			LogicalVar subject, GenericObject genericObj) {
        return null;
    }

    public boolean checkTypesAndScope(Model model, Map scope) {
        // TODO: Fill this out with something more reasonable.
        return true;
    }

    public Term getCanonicalVersion() {
        // TODO: Put something more reasonable here.
        return null;
    }

    public boolean makeOverlapSubst(Term t, Substitution theta) {
        // this shouldn't ever get called...
        return false;
    }

    public Type getType() {
        return null;
    }

    public ArgSpec getSubstResult(Substitution subst, Set<LogicalVar> boundVars) {
        return null;
    }

    public ArgSpec replace(Term t, ArgSpec another) {
        Util.fatalError("replace not supported for ComparisonTerm.");
        return null;
    }

    public boolean containsRandomSymbol() {
        return false;
    }

    public Object evaluate(EvalContext context) {
        return null;
    }

    private Term left;
    private Term right;
    private int compType;
}

