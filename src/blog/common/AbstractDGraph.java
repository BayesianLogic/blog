/*
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

package blog.common;

import java.util.*;
import java.io.PrintStream;

/**
 * Abstract implementation of the DGraph interface.
 */
public abstract class AbstractDGraph implements DGraph {

	/**
	 * Throws an UnsupportedOperationException.
	 */
	public boolean addNode(Object v) {
		throw new UnsupportedOperationException(
				"Tried to add node to unmodifiable graph.");
	}

	/**
	 * Throws an UnsupportedOperationException.
	 */
	public boolean removeNode(Object v) {
		throw new UnsupportedOperationException(
				"Tried to remove node from unmodifiable graph.");
	}

	/**
	 * Throws an UnsupportedOperationException.
	 */
	public void addEdge(Object parent, Object child) {
		throw new UnsupportedOperationException(
				"Tried to add edge to unmodifiable graph.");
	}

	/**
	 * Throws an UnsupportedOperationException.
	 */
	public void removeEdge(Object parent, Object child) {
		throw new UnsupportedOperationException(
				"Tried to remove edge from unmodifiable graph.");
	}

	/**
	 * Implements setParents in terms of addEdge and removeEdge.
	 */
	public void setParents(Object v, Set newParents) {
		Set oldParents = getParents(v);
		for (Iterator iter = oldParents.iterator(); iter.hasNext();) {
			Object parent = iter.next();
			if (!newParents.contains(parent)) {
				removeEdge(parent, v);
			}
		}
		for (Iterator iter = newParents.iterator(); iter.hasNext();) {
			Object parent = iter.next();
			if (!oldParents.contains(parent)) {
				addEdge(parent, v);
			}
		}
	}

	public Set getRoots() {
		Set roots = new HashSet();
		for (Iterator iter = nodes().iterator(); iter.hasNext();) {
			Object node = iter.next();
			if (getParents(node).isEmpty()) {
				roots.add(node);
			}
		}
		return Collections.unmodifiableSet(roots);
	}

	public Set getAncestors(Object v) {
		Set ancestors = new HashSet();

		// Do depth-first search, adding each node to the ancestor set
		// the first time it is seen.
		Stack stack = new Stack();
		stack.push(v);
		while (!stack.empty()) {
			Object u = stack.pop();
			Set parents = getParents(u);
			for (Iterator iter = parents.iterator(); iter.hasNext();) {
				Object parent = iter.next();
				if (ancestors.add(parent)) {
					stack.push(parent);
				}
			}
		}
		return ancestors;
	}

	public Set getDescendants(Object v) {
		Set descendants = new HashSet();

		// Do depth-first search, adding each node to the descendant set
		// the first time it is seen.
		Stack stack = new Stack();
		stack.push(v);
		while (!stack.empty()) {
			Object u = stack.pop();
			Set children = getChildren(u);
			for (Iterator iter = children.iterator(); iter.hasNext();) {
				Object child = iter.next();
				if (descendants.add(child)) {
					stack.push(child);
				}
			}
		}
		return descendants;
	}

	public void print(PrintStream s) {
		for (Iterator nodeIter = nodes().iterator(); nodeIter.hasNext();) {
			Object node = nodeIter.next();
			s.println(node);

			for (Iterator parentIter = getParents(node).iterator(); parentIter
					.hasNext();) {
				s.println("\t<- " + parentIter.next());
			}

			s.println();
		}
	}
}
