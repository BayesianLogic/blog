/**
 * 
 */
package blog.objgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import blog.sample.EvalContext;

class SatisfierIterator extends AbstractObjectIterator {
	/**
	 * 
	 */
	private final ObjGenGraph objGenGraph;

	SatisfierIterator(ObjGenGraph objGenGraph, EvalContext context, Set externallyDistinguished,
			boolean returnPOPApps) {
		this.objGenGraph = objGenGraph;
		this.context = context;
		this.externallyDistinguished = externallyDistinguished;
		this.returnPOPAppsForTarget = returnPOPApps;

		// For each POPNode, add its parents to the POP parent maps,
		// associated with empty lists
		for (Iterator nodeIter = this.objGenGraph.nodes.iterator(); nodeIter.hasNext();) {
			Node node = (Node) nodeIter.next();
			if (node instanceof POPNode) {
				for (Iterator parentIter = node.getParents().iterator(); parentIter
						.hasNext();) {
					Node parent = (Node) parentIter.next();
					popParentCurRoundObjs.put(parent, new ArrayList());
					popParentPrevRoundObjs.put(parent, new ArrayList());
					popParentEarlierRoundObjs.put(parent, new ArrayList());
				}
			}
		}

		targetIter = getNodeIterator(this.objGenGraph.targetNode, returnPOPAppsForTarget);
	}

	protected int skipAfterNext() {
		return targetIter.skipIndistinguishable();
	}

	protected Object findNext() {
		// If the target iterator has no more elements, we need to
		// finish this round and start a new one. Do this
		// repeatedly until the target node iterator has a next
		// element, or until we complete a round where no objects
		// are added for any POP parents.
		while (!targetIter.hasNext()) {
			if (!targetIter.canDetermineNext()) {
				canDetermineNext = false;
				// System.out.println("Iter for target node " + targetNode
				// + " can't determine next.");
				return null;
			}

			// Process all the POP parents in this round -- except
			// the target node, whose curRoundObjs were collected over
			// the course of this round.
			//
			// Note that within a round, the order in which nodes
			// are processed doesn't matter.
			for (Iterator iter = popParentCurRoundObjs.keySet().iterator(); iter
					.hasNext();) {
				Node popParent = (Node) iter.next();
				if (popParent != this.objGenGraph.targetNode) {
					if (!processPOPParent(popParent)) {
						return null;
					}
				}
			}

			// Try to start new round.
			if (!startNewRound()) {
				return null;
			}

			// Don't include guaranteed objects in new iterators after
			// the first round
			includeGuaranteed = false;

			// We will process the target node first in the new round
			targetIter = getNodeIterator(this.objGenGraph.targetNode, returnPOPAppsForTarget);
		}

		// Continue processing the target node in the current round
		Object newObj = targetIter.next();
		if (popParentCurRoundObjs.containsKey(this.objGenGraph.targetNode)) {
			((List) popParentCurRoundObjs.get(this.objGenGraph.targetNode)).add(newObj);
		}
		return newObj;
	}

	private boolean startNewRound() {
		// TODO: limit ourselves to parents of POPs that are still
		// active, i.e., POPs that don't have a permanently empty
		// parent. A node is permanently empty if it has no
		// objects and none of its ancestors generated any objects
		// in the current round.

		boolean haveNewObjs = false;
		for (Iterator iter = popParentCurRoundObjs.entrySet().iterator(); iter
				.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			Node popParent = (Node) entry.getKey();
			List curRoundObjs = (List) entry.getValue();
			if (!curRoundObjs.isEmpty()) {
				haveNewObjs = true;
			}

			List prevRoundObjs = (List) popParentPrevRoundObjs.get(popParent);
			List earlierRoundObjs = (List) popParentEarlierRoundObjs.get(popParent);
			earlierRoundObjs.addAll(prevRoundObjs);

			prevRoundObjs.clear();
			prevRoundObjs.addAll(curRoundObjs);
			curRoundObjs.clear();
		}

		return haveNewObjs;
	}

	private boolean processPOPParent(Node node) {
		List curRoundObjs = (List) popParentCurRoundObjs.get(node);
		ObjectIterator iter = getNodeIterator(node, false);
		if (node.isFinite()) {
			// Get all objects from iterator
			while (iter.hasNext()) {
				curRoundObjs.add(iter.next());
			}
		} else {
			// Get only one object from iterator
			if (iter.hasNext()) {
				curRoundObjs.add(iter.next());
			}
		}

		if (!iter.canDetermineNext()) {
			canDetermineNext = false;
			// System.out.println("POP parent node " + node
			// + " can't determine next.");
			return false;
		}
		return true;
	}

	private ObjectIterator getNodeIterator(Node node, boolean returnPOPApps) {
		if (rootIters.containsKey(node)) {
			return (ObjectIterator) rootIters.get(node);
		}

		ObjectIterator iter = node.iterator(context, externallyDistinguished,
				returnPOPApps, popParentPrevRoundObjs, popParentEarlierRoundObjs,
				includeGuaranteed);
		if (node.getParents().isEmpty()) {
			rootIters.put(node, iter);
		}
		return iter;
	}

	EvalContext context;
	Set externallyDistinguished;
	boolean returnPOPAppsForTarget;

	boolean includeGuaranteed = true;
	Map popParentCurRoundObjs = new HashMap(); // from Node to List
	Map popParentPrevRoundObjs = new HashMap(); // from Node to List
	Map popParentEarlierRoundObjs = new HashMap(); // from Node to List
	Map rootIters = new HashMap(); // from Node to ObjectIterator

	ObjectIterator targetIter;
}