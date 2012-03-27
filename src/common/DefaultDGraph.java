/*
 * Copyright (c) 2007, Massachusetts Institute of Technology
 * Copyright (c) 2005, Regents of the University of California
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

package common;

import java.util.*;

/**
 * Default implementation of the DGraph interface. Represents both the parent
 * set and the child set for each node (even though this is redundant) so both
 * kinds of sets can be accessed quickly.
 */
public class DefaultDGraph extends AbstractDGraph implements Cloneable {
	/**
	 * Creates a new, empty graph.
	 */
	public DefaultDGraph() {
	}

	/**
	 * Returns an unmodifiable set consisting of the nodes in this graph.
	 */
	public Set nodes() {
		return Collections.unmodifiableSet(nodeInfo.keySet());
	}

	public boolean addNode(Object v) {
		if (nodeInfo.containsKey(v)) {
			return false;
		}
		nodeInfo.put(v, new NodeInfo());
		return true;
	}

	public boolean removeNode(Object v) {
		NodeInfo info = (NodeInfo) nodeInfo.remove(v);
		if (info == null) {
			return false;
		}

		for (Iterator iter = info.parents.iterator(); iter.hasNext();) {
			Object parent = iter.next();
			NodeInfo parentInfo = (NodeInfo) nodeInfo.get(parent);
			if (parentInfo != null) {
				parentInfo.children.remove(v);
			}
		}
		for (Iterator iter = info.children.iterator(); iter.hasNext();) {
			Object child = iter.next();
			NodeInfo childInfo = (NodeInfo) nodeInfo.get(child);
			if (childInfo != null) {
				childInfo.parents.remove(v);
			}
		}

		return true;
	}

	public void addEdge(Object parent, Object child) {
		NodeInfo parentInfo = ensureNodePresent(parent);
		parentInfo.children.add(child);

		NodeInfo childInfo = ensureNodePresent(child);
		childInfo.parents.add(parent);
	}

	public void removeEdge(Object parent, Object child) {
		NodeInfo parentInfo = (NodeInfo) nodeInfo.get(parent);
		if (parentInfo != null) {
			parentInfo.children.remove(child);
		}

		NodeInfo childInfo = (NodeInfo) nodeInfo.get(child);
		if (childInfo != null) {
			childInfo.parents.remove(parent);
		}
	}

	public Set getParents(Object v) {
		NodeInfo info = (NodeInfo) nodeInfo.get(v);
		if (info == null) {
			return null;
		}
		return Collections.unmodifiableSet(info.parents);
	}

	public void setParents(Object v, Set newParents) {
		NodeInfo vInfo = ensureNodePresent(v);
		Set oldParents = vInfo.parents;
		vInfo.parents = new HashSet(newParents);

		// Remove v from child sets of old parents
		for (Iterator iter = oldParents.iterator(); iter.hasNext();) {
			Object parent = iter.next();
			if (!newParents.contains(parent)) {
				NodeInfo parentInfo = (NodeInfo) nodeInfo.get(parent);
				if (parentInfo != null) {
					parentInfo.children.remove(v);
				}
			}
		}

		// Add v to child sets of new parents
		for (Iterator iter = newParents.iterator(); iter.hasNext();) {
			Object parent = iter.next();
			if (!oldParents.contains(parent)) {
				NodeInfo parentInfo = ensureNodePresent(parent);
				parentInfo.children.add(v);
			}
		}
	}

	public Set getChildren(Object v) {
		NodeInfo info = (NodeInfo) nodeInfo.get(v);
		if (info == null) {
			return null;
		}
		return Collections.unmodifiableSet(info.children);
	}

	public Object clone() {
		DefaultDGraph clone = new DefaultDGraph();
		clone.nodeInfo = (Map) ((HashMap) nodeInfo).clone();
		for (Iterator iter = clone.nodeInfo.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			entry.setValue(((NodeInfo) entry.getValue()).clone());
		}
		return clone;
	}

	private static class NodeInfo implements Cloneable {
		Set parents = new HashSet();
		Set children = new HashSet();

		public Object clone() {
			NodeInfo clone = new NodeInfo();
			clone.parents = (Set) ((HashSet) parents).clone();
			clone.children = (Set) ((HashSet) children).clone();
			return clone;
		}
	}

	private NodeInfo ensureNodePresent(Object v) {
		NodeInfo info = (NodeInfo) nodeInfo.get(v);
		if (info == null) {
			info = new NodeInfo();
			nodeInfo.put(v, info);
		}
		return info;
	}

	private Map nodeInfo = new HashMap(); // from Object to NodeInfo
}
