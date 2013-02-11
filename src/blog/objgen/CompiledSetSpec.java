/*
 * Copyright (c) 2005, 2006, Regents of the University of California
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

package blog.objgen;

import java.util.List;

import blog.model.ConjFormula;
import blog.model.Formula;
import blog.model.LogicalVar;
import blog.sample.EvalContext;

/**
 * Data structure that facilitates iterating over the set of objects <i>x</i>
 * such that a given formula <i>phi(x)</i> is true. A CompiledSetSpec consists
 * of a DNF version of <i>phi</i>, with an ObjGenGraph for each disjunct. To
 * iterate over the objects that satisfy <i>phi</i>, we create an iterator for
 * each ObjGenGraph and get objects from these iterators in a round-robin
 * fashion. When we get an object from an ObjGenGraph, we return it if it
 * satisfies the corresponding disjunct and no earlier disjuncts. Thus, each
 * object that satisfies <i>phi</i> is returned exactly once: when it is
 * returned by the iterator for the <em>first</em> disjunct that it satisfies.
 * The round-robin iteration over disjuncts ensures that even if the ObjGenGraph
 * iterator for, say, the first disjunct returns infinitely many objects, we
 * will still return objects that satisfy the other disjuncts after a finite
 * amount of time.
 */
public class CompiledSetSpec {
	/**
	 * Creates a new CompiledSetSpec for iterating over all bindings for
	 * <code>var</code> that satisfy <code>phi</code>.
	 */
	public CompiledSetSpec(LogicalVar var, Formula phi) {
		this.var = var;
		this.origFormula = phi;

		disjuncts = phi.getPropDNF().getDisjuncts();
		objGenGraphs = new ObjGenGraph[disjuncts.size()];
		for (int i = 0; i < disjuncts.size(); ++i) {
			// TODO leili: List may be inefficient for get(i), use ArrayList instead.
			ConjFormula disjunct = (ConjFormula) disjuncts.get(i);
			objGenGraphs[i] = new ObjGenGraph(var.getType(), var,
					disjunct.getConjuncts());
		}

		/*
		 * if (Util.verbose()) { System.out.println("Compiled version of {" + var +
		 * ": " + phi + "}:"); for (int i = 0; i < objGenGraphs.length; ++i) {
		 * System.out.println("Disjunct " + i + ": " + disjuncts.get(i));
		 * System.out.println("ObjGenGraph for disjunct " + i + ":");
		 * objGenGraphs[i].print(System.out); } System.out.println(); }
		 */
	}

	/**
	 * Returns an ObjectSet representing the objects that, when bound to
	 * <code>var</code>, make <code>orgFormula</code> true in the given context.
	 */
	public ObjectSet elementSet(EvalContext context) {
		return new ElementSet(this, context);
	}

	/**
	 * Returns an iterator over the objects specified by the underlying
	 * ObjGenGraphs. This iterator will return every object that satisfies this
	 * set specification, but it may return other objects as well.
	 */
	public ObjectIterator unfilteredIterator(EvalContext context) {
		return new UnfilteredIterator(this, context, context.getLogicalVarValues());
	}

	/**
	 * Returns true if the iteration order for the set returned by
	 * <code>elementSet</code> is affected by the iteration order for object
	 * identifiers in the given context.
	 */
	public boolean dependsOnIdOrder(EvalContext context) {
		for (int i = 0; i < objGenGraphs.length; ++i) {
			if (objGenGraphs[i].dependsOnIdOrder(context)) {
				return true;
			}
		}
		return false;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("CompiledSetSpec {");
		buf.append(var.getType());
		buf.append(" ");
		buf.append(var.getName());
		buf.append(" : ");

		for (int i = 0; i < disjuncts.size(); ++i) {
			buf.append(disjuncts.get(i));
			if (i + 1 < disjuncts.size()) {
				buf.append(" | ");
			}
		}

		buf.append("}");
		return buf.toString();
	}

	Boolean isFirstSatisfiedDisjunct(EvalContext context, Object obj,
			int index) {
		context.assign(var, obj);
		Boolean result;

		Object value = ((Formula) disjuncts.get(index)).evaluate(context);
		if (value == null) {
			result = null;
		} else if (((Boolean) value).booleanValue()) {
			// Satisfies this disjunct; see if it satisfies an earlier one
			result = Boolean.TRUE;
			for (int i = 0; i < index; ++i) {
				value = ((Formula) disjuncts.get(i)).evaluate(context);
				if (value == null) {
					result = null;
					break;
				}
				if (((Boolean) value).booleanValue()) {
					result = Boolean.FALSE;
					break;
				}
			}
		} else {
			result = Boolean.FALSE;
		}

		context.unassign(var);
		return result;
	}

	LogicalVar var;
	Formula origFormula;

	List disjuncts; // of ConjFormula
	ObjGenGraph[] objGenGraphs;
}
