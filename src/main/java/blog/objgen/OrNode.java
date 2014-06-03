package blog.objgen;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import blog.sample.EvalContext;


public class OrNode extends Node {
	/**
	 * Creates a new OrNode with no parents.
	 */
	OrNode() {
		this.parents = new HashSet();
	}

	/**
	 * Creates a new OrNode with the given nodes as parents.
	 * 
	 * @param parents
	 *          Set of Node objects
	 */
	private OrNode(Set parents) {
		this.parents = new HashSet(parents);
	}

	Set getParents() {
		return Collections.unmodifiableSet(parents);
	}

	/**
	 * Adds the given node as a parent of this node.
	 */
	void addParent(Node parent) {
		parents.add(parent);
	}

	public ObjectIterator iterator(EvalContext context,
			Set externallyDistinguished, boolean returnPOPApps,
			Map desiredPOPParentObjs, Map otherPOPParentObjs,
			boolean includeGuaranteed) {
		return new OrNodeIterator(context, externallyDistinguished,
				returnPOPApps, desiredPOPParentObjs, otherPOPParentObjs,
				includeGuaranteed);
	}

	public boolean isFinite() {
		return true; // assumes no IntegerNodes as parents
	}

	public boolean dependsOnIdOrder(EvalContext context) {
		for (Iterator iter = parents.iterator(); iter.hasNext();) {
			Node parent = (Node) iter.next();
			if (parent.dependsOnIdOrder(context)) {
				return true;
			}
		}
		return false;
	}

	public String toString() {
		return ("OrNode" + parents);
	}

	private class OrNodeIterator extends AbstractObjectIterator {
		OrNodeIterator(EvalContext context, Set externallyDistinguished,
				boolean returnPOPApps, Map desiredPOPParentObjs,
				Map otherPOPParentObjs, boolean includeGuaranteed) {
			this.context = context;
			this.externallyDistinguished = externallyDistinguished;
			this.returnPOPApps = returnPOPApps;
			this.desiredPOPParentObjs = desiredPOPParentObjs;
			this.otherPOPParentObjs = otherPOPParentObjs;
			this.includeGuaranteed = includeGuaranteed;

			parentsIter = parents.iterator();
		}

		protected int skipAfterNext() {
			return curParentIter.skipIndistinguishable();
		}

		protected Object findNext() {
			while ((curParentIter == null) || !curParentIter.hasNext()) {
				if ((curParentIter != null) && !curParentIter.canDetermineNext()) {
					canDetermineNext = false;
					return null;
				}

				// Move on to next parent
				if (parentsIter.hasNext()) {
					Node parent = (Node) parentsIter.next();
					if (!parent.isFinite()) {
						throw new IllegalStateException(
								"Parent of OrNode returned iterator over " + "infinite set.");
					}
					curParentIter = parent.iterator(context, externallyDistinguished,
							returnPOPApps, desiredPOPParentObjs, otherPOPParentObjs,
							includeGuaranteed);
				} else {
					return null;
				}
			}

			return curParentIter.next();
		}

		EvalContext context;
		Set externallyDistinguished;
		boolean returnPOPApps;
		Map desiredPOPParentObjs;
		Map otherPOPParentObjs;
		boolean includeGuaranteed;

		Iterator parentsIter;
		ObjectIterator curParentIter = null;
	}

	Set parents;
}