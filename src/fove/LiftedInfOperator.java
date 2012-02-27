/*
 * Copyright (c) 2007 Massachusetts Institute of Technology
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
 * * Neither the name of the Massachusetts Institute of Technology nor
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

package fove;

import java.util.*;
import java.lang.*;
import java.lang.reflect.*;

import blog.*;

/**
 * Should be extended by the various "Lifted Inference Operations"
 * that are part of this FOVE algorithm. Note that all descendants
 * should implement a static method called opFactory, that takes a set
 * of parfactors and produces all valid operations as instantiations
 * with the type of operator on that set of parfactors.
 */
public abstract class LiftedInfOperator {

    /**
     * Returns the log of a "cost" that is proportional to the amout
     * of time that a call to do() will take
     */
    public abstract double logCost();

    /**
     * Actually carry out the operation specified by this instance of
     * LiftedInfOperator.  An instance stores the set of parfactors that 
     * it's operating on, and changes that set when this method is called.
     */
    public abstract void operate();

    /**
     * This returns a collection of instantiated LiftedInfOperators that are
     * valid on the given set of parfactors.
     */
    public static Collection<LiftedInfOperator> validOps
	(Set<Parfactor> parfactors, ElimTester query) 
    {
	Set<LiftedInfOperator> ops = new LinkedHashSet<LiftedInfOperator>();
	ops.addAll(Propositionalization.opFactory(parfactors, query));
	ops.addAll(CountExpansion.opFactory(parfactors, query));
	ops.addAll(Exponentiation.opFactory(parfactors, query));
	ops.addAll(SummingOut.opFactory(parfactors, query));
	ops.addAll(CountConversion.opFactory(parfactors, query));
	return ops;
    }

}
