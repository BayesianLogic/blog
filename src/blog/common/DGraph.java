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
 * A directed graph.
 */
public interface DGraph {
	/**
	 * An empty directed graph.
	 */
	static final DGraph EMPTY_GRAPH = new AbstractDGraph() {
		public Set nodes() {
			return Collections.EMPTY_SET;
		}

		public Set getParents(Object v) {
			throw new IllegalArgumentException("Object not in graph: " + v);
		}

		public Set getChildren(Object v) {
			throw new IllegalArgumentException("Object not in graph: " + v);
		}
	};

	/**
	 * Returns an unmodifiable set consisting of the nodes in this graph.
	 */
	Set nodes();

	/**
	 * Adds the given object as a node in the graph. Has no effect if the node was
	 * already in the graph.
	 * 
	 * @return true if the node was actually added; false if it was already in the
	 *         graph
	 */
	boolean addNode(Object v);

	/**
	 * Removes the given node from the graph, along with all the edges incident on
	 * it. Has no effect if the node was not in the graph.
	 * 
	 * @return true if the node was actually removed; false if it was not in the
	 *         graph
	 */
	boolean removeNode(Object v);

	/**
	 * Adds to the graph an edge from <code>parent</code> to <code>child</code>.
	 * Automatically adds these nodes to the graph if they were not already
	 * present.
	 */
	void addEdge(Object parent, Object child);

	/**
	 * Removes the edge from <code>parent</code> to <code>child</code> from the
	 * graph. This method has no effect if the edge was not present; it is safe to
	 * call even if the given nodes are not in the graph.
	 */
	void removeEdge(Object parent, Object child);

	/**
	 * Returns an unmodifiable set consisting of the given object's parents, or
	 * null if the object is not in the graph.
	 */
	Set getParents(Object v);

	/**
	 * Changes the graph so that the parent set of the given node is the given
	 * set. Automatically adds the child node and any parent nodes if they were
	 * not already in the graph.
	 */
	void setParents(Object v, Set parents);

	/**
	 * Returns an unmodifiable set consisting of the given object's children, or
	 * null if the object is not in the graph.
	 */
	Set getChildren(Object v);

	/**
	 * Returns the set of nodes in this graph that have no parents.
	 * 
	 * @return unmodifiable Set of Object
	 */
	Set getRoots();

	/**
	 * Returns the set of ancestors of the given node, including the node itself.
	 * 
	 * @return unmodifiable Set of Object
	 * 
	 * @throws NullPointerException
	 *           if the given object is not in the graph
	 */
	Set getAncestors(Object v);

	/**
	 * Returns the set of descendants of the given node, including the node
	 * itself.
	 * 
	 * @return unmodifiable Set of Object
	 * 
	 * @throws NullPointerException
	 *           if the given object is not in the graph
	 */
	Set getDescendants(Object v);

	/**
	 * Prints a description of this graph to the given stream.
	 */
	void print(PrintStream s);
}
