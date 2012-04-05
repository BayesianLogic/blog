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

package blog;

import java.util.*;

import blog.common.HashMultiMap;
import blog.common.MultiMap;
import blog.common.Util;
import blog.objgen.ObjGenGraph;

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
		return new ElementSet(context);
	}

	/**
	 * Returns an iterator over the objects specified by the underlying
	 * ObjGenGraphs. This iterator will return every object that satisfies this
	 * set specification, but it may return other objects as well.
	 */
	public ObjectIterator unfilteredIterator(EvalContext context) {
		return new UnfilteredIterator(context, context.getLogicalVarValues());
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

	private Boolean isFirstSatisfiedDisjunct(EvalContext context, Object obj,
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

	private class ElementSet extends AbstractObjectSet {
		ElementSet(EvalContext context) {
			this.context = context;
		}

		protected Boolean isEmptyInternal() {
			ObjectIterator iter = iterator(context.getLogicalVarValues());
			return (iter.canDetermineNext() ? Boolean.valueOf(!iter.hasNext()) : null);
		}

		protected Integer sizeInternal() {
			int size = 0;
			ObjectIterator iter = iterator(context.getLogicalVarValues());
			while (iter.hasNext()) {
				iter.next();
				++size;
				size += iter.skipIndistinguishable();
			}
			if (!iter.canDetermineNext()) {
				return null;
			}
			return new Integer(size);
		}

		public Boolean containsInternal(Object obj) {
			context.assign(var, obj);

			Boolean result = Boolean.FALSE;
			for (Iterator iter = disjuncts.iterator(); iter.hasNext();) {
				Formula disj = (Formula) iter.next();
				Boolean disjValue = (Boolean) disj.evaluate(context);
				if (disjValue == null) {
					result = null;
					break;
				}
				if (disjValue.booleanValue()) {
					// object satisfies this disjunct, so short-circuit
					result = Boolean.TRUE;
					break;
				}
			}

			context.unassign(var);
			return result;
		}

		public ObjectIterator iterator(Set externallyDistinguished) {
			externallyDistinguished = new HashSet(externallyDistinguished);
			externallyDistinguished.addAll(context.getLogicalVarValues());
			return new SatisfierIterator(externallyDistinguished);
		}

		public ObjectSet getExplicitVersion() {
			POPAppBasedSet set = new POPAppBasedSet();
			MultiMap popAppExceptions = null;

			// See if we can get the satisfiers explicitly regardless of
			// the POP application
			GenericObject genericObj = new GenericObject(var.getType());
			Set satisfiers = origFormula.getSatisfiersIfExplicit(context, var,
					genericObj);
			if (satisfiers == Formula.ALL_OBJECTS) {
				// we know each POP app is included with no exceptions
				popAppExceptions = MultiMap.EMPTY_MULTI_MAP;
			} else if (satisfiers != null) {
				for (Iterator iter = satisfiers.iterator(); iter.hasNext();) {
					Object obj = iter.next();
					set.addIndividual(obj, context.getPOPAppSatisfied(obj));
				}
				return set;
			} else {
				// See if we can find the non-satisfiers explicitly.
				// If so, we'll still enumerate the POP apps, but we'll know
				// the exceptions for each one.
				Set nonSatisfiers = origFormula.getNonSatisfiersIfExplicit(context,
						var, genericObj);
				if (nonSatisfiers == Formula.ALL_OBJECTS) {
					return set; // which is currently empty
				}
				if (nonSatisfiers != null) {
					popAppExceptions = new HashMultiMap();
					for (Iterator iter = nonSatisfiers.iterator(); iter.hasNext();) {
						Object obj = iter.next();
						popAppExceptions.add(context.getPOPAppSatisfied(obj), obj);
					}
				}
			}

			// Get ready to enumerate individuals and POP apps
			ObjectIterator[] graphIters = new ObjectIterator[objGenGraphs.length];
			BitSet active = new BitSet();
			for (int i = 0; i < objGenGraphs.length; ++i) {
				graphIters[i] = objGenGraphs[i].iterator(context,
						context.getLogicalVarValues(), true);
				active.set(i);
			}

			int nextDisjunctIndex = 0;
			while (active.cardinality() > 0) {
				if (!graphIters[nextDisjunctIndex].canDetermineNext()) {
					return null;
				}

				if (graphIters[nextDisjunctIndex].hasNext()) {
					Object nextObj = graphIters[nextDisjunctIndex].next();
					if (!maybeAddToSet(set, popAppExceptions, nextObj, nextDisjunctIndex)) {
						return null;
					}
					// continue round-robin over disjuncts
				} else {
					active.clear(nextDisjunctIndex);
				}

				nextDisjunctIndex = (nextDisjunctIndex + 1) % graphIters.length;
			}

			return set;
		}

		public Object sample(int n) {
			ObjectSet explicit = getExplicitVersion();
			if (explicit == null) {
				return null;
			}
			return explicit.sample(n);
		}

		public int indexOf(Object o) {
			ObjectSet explicit = getExplicitVersion();
			if (explicit == null) {
				return -1;
			}
			return explicit.indexOf(o);
		}

		public String toString() {
			return (CompiledSetSpec.this.toString() + " " + context
					.getAssignmentStr());
		}

		private boolean maybeAddToSet(POPAppBasedSet set,
				MultiMap popAppExceptions, Object obj, int disjunctIndex) {
			if (obj instanceof NumberVar) {
				return maybeAddPOPApp(set, popAppExceptions, (NumberVar) obj,
						disjunctIndex);
			}

			Boolean isFirst = isFirstSatisfiedDisjunct(context, obj, disjunctIndex);
			if (isFirst == null) {
				return false;
			}
			if (isFirst.booleanValue()) {
				NumberVar popApp = context.getPOPAppSatisfied(obj);
				set.addIndividual(obj, popApp);
			}
			return true; // successfully determined whether to add or not
		}

		private boolean maybeAddPOPApp(POPAppBasedSet set,
				MultiMap popAppExceptions, NumberVar popApp, int disjunctIndex) {
			ObjectSet satisfiers = context.getSatisfiers(popApp);
			if (popAppExceptions != null) {
				// We were able to enumerate exceptions explicitly
				// without even considering the generating functions
				set.addSatisfiers(popApp, satisfiers,
						(Set) popAppExceptions.get(popApp));
				return true;
			}

			// Try finding explicit representation by taking
			// generating functions into account
			GenericObject genericObj = new GenericPOPAppSatisfier(popApp.pop(),
					popApp.args());
			Set explicitSatisfiers = origFormula.getSatisfiersIfExplicit(context,
					var, genericObj);
			if (explicitSatisfiers == Formula.ALL_OBJECTS) {
				// this POP app is included with no exceptions
				set.addSatisfiers(popApp, satisfiers, Collections.EMPTY_SET);
				return true;
			} else if (explicitSatisfiers != null) {
				for (Iterator iter = explicitSatisfiers.iterator(); iter.hasNext();) {
					set.addIndividual(iter.next(), popApp);
				}
				return true;
			} else {
				// See if we can find the non-satisfiers explicitly.
				Set nonSatisfiers = origFormula.getNonSatisfiersIfExplicit(context,
						var, genericObj);
				if (nonSatisfiers == Formula.ALL_OBJECTS) {
					return true; // don't add anything
				}
				if (nonSatisfiers != null) {
					set.addSatisfiers(popApp, satisfiers, nonSatisfiers);
					return true;
				}
			}

			// If we get here, then we actually have to iterate over objects

			Set in = new LinkedHashSet();
			Set out = new HashSet();
			Boolean mostlyIncluded = null;

			for (ObjectIterator iter = satisfiers.iterator(context
					.getLogicalVarValues()); iter.hasNext();) {
				Object obj = iter.next();
				Boolean isFirst = isFirstSatisfiedDisjunct(context, obj, disjunctIndex);
				if (isFirst == null) {
					return false;
				}
				if (isFirst.booleanValue()) {
					in.add(obj);
				} else {
					out.add(obj);
				}

				if (mostlyIncluded == null) {
					if (iter.skipIndistinguishable() > 0) {
						mostlyIncluded = isFirst;
					}
				}
			}

			if ((mostlyIncluded != null) && mostlyIncluded.booleanValue()) {
				set.addSatisfiers(popApp, satisfiers, out);
			} else {
				for (Iterator iter = in.iterator(); iter.hasNext();) {
					set.addIndividual(iter.next(), popApp);
				}
			}

			return true;
		}

		private class SatisfierIterator extends AbstractObjectIterator {
			SatisfierIterator(Set externallyDistinguished) {
				graphIters = new ObjectIterator[objGenGraphs.length];
				for (int i = 0; i < objGenGraphs.length; ++i) {
					graphIters[i] = objGenGraphs[i].iterator(context,
							externallyDistinguished, false);
					active.set(i);
				}
			}

			protected int skipAfterNext() {
				return graphIters[nextDisjunctIndex].skipIndistinguishable();
			}

			protected Object findNext() {
				while (active.cardinality() > 0) {
					nextDisjunctIndex = (nextDisjunctIndex + 1) % graphIters.length;

					if (!graphIters[nextDisjunctIndex].canDetermineNext()) {
						canDetermineNext = false;
						return null;
					}

					if (graphIters[nextDisjunctIndex].hasNext()) {
						Object nextObj = graphIters[nextDisjunctIndex].next();
						Boolean isFirst = isFirstSatisfiedDisjunct(context, nextObj,
								nextDisjunctIndex);

						if (isFirst == null) {
							canDetermineNext = false;
							return null;
						}
						if (isFirst.booleanValue()) {
							return nextObj;
						}

						// current value of nextObj is not in this set
						graphIters[nextDisjunctIndex].skipIndistinguishable();
						// continue round-robin over disjuncts
					} else {
						active.clear(nextDisjunctIndex);
					}
				}

				return null;
			}

			private ObjectIterator[] graphIters;
			private BitSet active = new BitSet();
			private int nextDisjunctIndex = -1;
		}

		private EvalContext context;
	}

	private class UnfilteredIterator extends AbstractObjectIterator {
		UnfilteredIterator(EvalContext context, Set externallyDistinguished) {
			graphIters = new ObjectIterator[objGenGraphs.length];
			for (int i = 0; i < objGenGraphs.length; ++i) {
				graphIters[i] = objGenGraphs[i].iterator(context,
						externallyDistinguished, false);
				active.set(i);
			}
		}

		protected int skipAfterNext() {
			return graphIters[nextDisjunctIndex].skipIndistinguishable();
		}

		protected Object findNext() {
			while (active.cardinality() > 0) {
				nextDisjunctIndex = (nextDisjunctIndex + 1) % graphIters.length;

				if (!graphIters[nextDisjunctIndex].canDetermineNext()) {
					canDetermineNext = false;
					return null;
				}

				if (graphIters[nextDisjunctIndex].hasNext()) {
					return graphIters[nextDisjunctIndex].next();
				} else {
					active.clear(nextDisjunctIndex);
				}
			}

			return null;
		}

		private ObjectIterator[] graphIters;
		private BitSet active = new BitSet();
		private int nextDisjunctIndex = -1;
	}

	private LogicalVar var;
	private Formula origFormula;

	private List disjuncts; // of ConjFormula
	private ObjGenGraph[] objGenGraphs;
}
