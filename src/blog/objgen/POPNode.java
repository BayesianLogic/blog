package blog.objgen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import blog.AbstractObjectIterator;
import blog.EvalContext;
import blog.NumberVar;
import blog.ObjectIterator;
import blog.ObjectSet;
import blog.POP;

import common.TupleIterator;
import common.Util;

/**
 * Node satisfied by the set of objects that satisfy a given potential object
 * pattern (POP).
 */
public class POPNode extends Node {
	POPNode(POP pop, List parents) {
		this.pop = pop;
		this.parents = new ArrayList(parents);
	}

	public Set getParents() {
		return new HashSet(parents);
	}

	public ObjectIterator iterator(EvalContext context,
			Set externallyDistinguished, boolean returnPOPApps,
			Map desiredPOPParentObjs, Map otherPOPParentObjs,
			boolean includeGuaranteed) {
		return new POPNodeIterator(context, externallyDistinguished,
				returnPOPApps, desiredPOPParentObjs, otherPOPParentObjs,
				includeGuaranteed);
	}

	public boolean isFinite() {
		return true;
	}

	public boolean dependsOnIdOrder(EvalContext context) {
		return context.usesIdentifiers(pop.type());
	}

	public String toString() {
		return ("POP[" + pop + "]");
	}

	/**
	 * To return objects generated by POP applications that involve at least one
	 * desired parent object, we iterate over tuples of generating objects where
	 * the first position with a desired object is position 1, then likewise for
	 * position 2, and so on.
	 */
	private class POPNodeIterator extends AbstractObjectIterator {
		POPNodeIterator(EvalContext context, Set externallyDistinguished,
				boolean returnPOPApps, Map desiredPOPParentObjs,
				Map otherPOPParentObjs, boolean includeGuaranteed) {
			this.context = context;
			this.externallyDistinguished = externallyDistinguished;
			this.returnPOPApps = returnPOPApps;
			this.desiredPOPParentObjs = desiredPOPParentObjs;
			this.otherPOPParentObjs = otherPOPParentObjs;
			this.includeGuaranteed = includeGuaranteed;
		}

		protected int skipAfterNext() {
			if (satisfyingObjIter == null) {
				return 0;
			}
			return satisfyingObjIter.skipIndistinguishable();
		}

		protected Object findNext() {
			while ((satisfyingObjIter == null) || !satisfyingObjIter.hasNext()) {

				// Need to move on to next parent tuple -- that is,
				// next tuple of generating objects
				while ((parentTupleIter == null) || !parentTupleIter.hasNext()) {

					// Need to move on to next first-desired-object index
					if (parents.isEmpty() && includeGuaranteed && !doneEmptyTuple) {
						parentTupleIter = new TupleIterator(Collections.EMPTY_LIST);
						doneEmptyTuple = true;
					} else if (firstDesiredObjIndex < parents.size()) {
						parentTupleIter = new TupleIterator(getParentObjLists());
						++firstDesiredObjIndex;
					} else {
						return null;
					}
				}

				// Have tuple of generating objects;
				// see what they generate
				List genObjs = (List) parentTupleIter.next();
				NumberVar nv = new NumberVar(pop, genObjs);
				if (returnPOPApps) {
					if (context.getValue(nv) == null) {
						canDetermineNext = false;
						return null;
					}
					return nv;
					// } else {
					// satisfyingObjIter = context.getSatisfiers(nv)
					// .iterator(externallyDistinguished);
					// if (satisfyingObjIter == null) {
					// canDetermineNext = false;
					// return null;
					// }
					// }
				} else {
					ObjectSet satisfyingObjs = context.getSatisfiers(nv);
					if (satisfyingObjs != null)
						satisfyingObjIter = context.getSatisfiers(nv).iterator(
								externallyDistinguished);
					if (satisfyingObjs == null || satisfyingObjIter == null) {
						canDetermineNext = false;
						return null;
					}
				}
			}

			return satisfyingObjIter.next();
		}

		private List getParentObjLists() {
			List objLists = new ArrayList(); // List of Lists
			for (int i = 0; i < parents.size(); ++i) {
				Node parent = (Node) parents.get(i);
				if (i < firstDesiredObjIndex) {
					objLists.add(otherPOPParentObjs.get(parent));
				} else if (i == firstDesiredObjIndex) {
					objLists.add(desiredPOPParentObjs.get(parent));
				} else {
					objLists.add(Util.concat((List) desiredPOPParentObjs.get(parent),
							(List) otherPOPParentObjs.get(parent)));
				}
			}
			return objLists;
		}

		EvalContext context;
		Set externallyDistinguished;
		boolean returnPOPApps;
		Map desiredPOPParentObjs;
		Map otherPOPParentObjs;
		boolean includeGuaranteed;

		boolean doneEmptyTuple = false;
		int firstDesiredObjIndex = 0;
		Iterator parentTupleIter = null;
		ObjectIterator satisfyingObjIter = null;
	}

	POP pop;
	List parents;
}