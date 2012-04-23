/**
 * 
 */
package blog.objgen;

import java.util.BitSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import blog.AbstractObjectIterator;
import blog.AbstractObjectSet;
import blog.GenericObject;
import blog.GenericPOPAppSatisfier;
import blog.ObjectIterator;
import blog.ObjectSet;
import blog.POPAppBasedSet;
import blog.bn.NumberVar;
import blog.common.HashMultiMap;
import blog.common.MultiMap;
import blog.model.Formula;
import blog.sample.EvalContext;

class ElementSet extends AbstractObjectSet {
	/**
	 * 
	 */
	private final CompiledSetSpec compiledSetSpec;

	ElementSet(CompiledSetSpec compiledSetSpec, EvalContext context) {
		this.compiledSetSpec = compiledSetSpec;
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
		context.assign(this.compiledSetSpec.var, obj);

		Boolean result = Boolean.FALSE;
		for (Iterator iter = this.compiledSetSpec.disjuncts.iterator(); iter
				.hasNext();) {
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

		context.unassign(this.compiledSetSpec.var);
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
		GenericObject genericObj = new GenericObject(
				this.compiledSetSpec.var.getType());
		Set satisfiers = this.compiledSetSpec.origFormula.getSatisfiersIfExplicit(
				context, this.compiledSetSpec.var, genericObj);
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
			Set nonSatisfiers = this.compiledSetSpec.origFormula
					.getNonSatisfiersIfExplicit(context, this.compiledSetSpec.var,
							genericObj);
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
		ObjectIterator[] graphIters = new ObjectIterator[this.compiledSetSpec.objGenGraphs.length];
		BitSet active = new BitSet();
		for (int i = 0; i < this.compiledSetSpec.objGenGraphs.length; ++i) {
			graphIters[i] = this.compiledSetSpec.objGenGraphs[i].iterator(context,
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
		return (this.compiledSetSpec.toString() + " " + context.getAssignmentStr());
	}

	private boolean maybeAddToSet(POPAppBasedSet set, MultiMap popAppExceptions,
			Object obj, int disjunctIndex) {
		if (obj instanceof NumberVar) {
			return maybeAddPOPApp(set, popAppExceptions, (NumberVar) obj,
					disjunctIndex);
		}

		Boolean isFirst = this.compiledSetSpec.isFirstSatisfiedDisjunct(context,
				obj, disjunctIndex);
		if (isFirst == null) {
			return false;
		}
		if (isFirst.booleanValue()) {
			NumberVar popApp = context.getPOPAppSatisfied(obj);
			set.addIndividual(obj, popApp);
		}
		return true; // successfully determined whether to add or not
	}

	private boolean maybeAddPOPApp(POPAppBasedSet set, MultiMap popAppExceptions,
			NumberVar popApp, int disjunctIndex) {
		ObjectSet satisfiers = context.getSatisfiers(popApp);
		if (popAppExceptions != null) {
			// We were able to enumerate exceptions explicitly
			// without even considering the generating functions
			set.addSatisfiers(popApp, satisfiers, (Set) popAppExceptions.get(popApp));
			return true;
		}

		// Try finding explicit representation by taking
		// generating functions into account
		GenericObject genericObj = new GenericPOPAppSatisfier(popApp.pop(),
				popApp.args());
		Set explicitSatisfiers = this.compiledSetSpec.origFormula
				.getSatisfiersIfExplicit(context, this.compiledSetSpec.var, genericObj);
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
			Set nonSatisfiers = this.compiledSetSpec.origFormula
					.getNonSatisfiersIfExplicit(context, this.compiledSetSpec.var,
							genericObj);
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
			Boolean isFirst = this.compiledSetSpec.isFirstSatisfiedDisjunct(context,
					obj, disjunctIndex);
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
			graphIters = new ObjectIterator[ElementSet.this.compiledSetSpec.objGenGraphs.length];
			for (int i = 0; i < ElementSet.this.compiledSetSpec.objGenGraphs.length; ++i) {
				graphIters[i] = ElementSet.this.compiledSetSpec.objGenGraphs[i]
						.iterator(context, externallyDistinguished, false);
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
					Boolean isFirst = ElementSet.this.compiledSetSpec
							.isFirstSatisfiedDisjunct(context, nextObj, nextDisjunctIndex);

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