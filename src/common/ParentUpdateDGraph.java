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
 * Directed graph that is backed by an underlying DGraph, but 
 * represents changes to the set of nodes and to the parent sets of some 
 * existing nodes (and thus to the child sets of some nodes as well).
 */
public class ParentUpdateDGraph extends AbstractDGraph implements Cloneable {
    /**
     * Creates a new ParentUpdateDGraph that represents no changes to the 
     * given underlying graph.
     */
    public ParentUpdateDGraph(DGraph underlying) {
	this.underlying = underlying;
    }

    public Set nodes() {
	return nodeSet;
    }

    /**
     * Adds the given node to the graph with an empty parent set.  Does 
     * nothing if the node is already in the graph.  If a node is removed 
     * and then added again, it gets an empty parent set (and an empty 
     * child set, since no node has this removed node as a parent anymore).  
     *
     * @return true if the node was actually added; false if it was already 
     *              in the graph
     */
    public boolean addNode(Object v) {
	if (removedNodes.remove(v)) {
	    // Node was previously removed; re-add with no neighbors
	    newParentSets.put(v, new HashSet());
	    Set oldChildren = underlying.getChildren(v);
	    if (oldChildren != null) { // in case underlying was modified
		childSetRemove.addAll(v, oldChildren);
	    }
	    return true;
	}

	if (underlying.nodes().contains(v) || newParentSets.containsKey(v)) {
	    return false; // node already in graph
	}

	newParentSets.put(v, new HashSet());
	return true;
    }

    /**
     * Removes the given node from the graph, along with all incident edges.
     * 
     * @return true if the node was actually removed; false if it was not 
     *              in the graph
     */
    public boolean removeNode(Object v) {
	// Remove from child sets of all parents
	Set curParents = getParents(v);
	if (curParents == null) {
	    return false; // node is not in graph
	}
	for (Iterator iter = curParents.iterator(); iter.hasNext(); ) {
	    Object parent = iter.next();
	    removeEdge(parent, v);
	}

	// Remove from parent sets of all children
	for (Iterator iter = getChildren(v).iterator(); iter.hasNext(); ) {
	    Object child = iter.next();
	    removeEdge(v, child);
	}

	newParentSets.remove(v);
	childSetAdd.remove(v);
	childSetRemove.remove(v);
	if (underlying.nodes().contains(v)) {
	    removedNodes.add(v);
	}

	return true;
    }

    public void addEdge(Object parent, Object child) {
	addNode(child);
	Set curParents = getParents(child);
	if (!curParents.contains(parent)) {
	    addNode(parent);

	    Set newParents = new HashSet(curParents);
	    newParents.add(parent);
	    newParentSets.put(child, newParents);

	    addChild(parent, child);
	}
    }

    public void removeEdge(Object parent, Object child) {
	Set curParents = getParents(child);
	if ((curParents != null) && curParents.contains(parent)) {
	    Set newParents = new HashSet(curParents);
	    newParents.remove(parent);
	    newParentSets.put(child, newParents);
	    
	    removeChild(parent, child);
	}
    }

    public Set getParents(Object v) {
	Set newParentSet = (Set) newParentSets.get(v);
	if (newParentSet != null) {
	    return Collections.unmodifiableSet(newParentSet);
	} 
	if (removedNodes.contains(v)) {
	    return null;
	} 
	return underlying.getParents(v);
    }

    /**
     * Changes the parent set of the given node to equal the given set.  
     * If any elements of newParents were not in the graph, this method 
     * adds them as nodes.  
     */
    public void setParents(Object v, Set newParents) {
	addNode(v); // only adds if not already present
	Set curParents = getParents(v);
	if (curParents.equals(newParents)) {
	    return;
	}
	
	for (Iterator iter = newParents.iterator(); iter.hasNext(); ) {
	    Object parent = iter.next();
	    addNode(parent); // only adds if not already present
	    if (!curParents.contains(parent)) {
		addChild(parent, v);
	    }
	}

	for (Iterator iter = curParents.iterator(); iter.hasNext(); ) {
	    Object parent = iter.next();
	    if (!newParents.contains(parent)) {
		removeChild(parent, v);
	    }
	}

	newParentSets.put(v, new HashSet(newParents));
    }

    public Set getChildren(Object v) {
	Set oldChildren = underlying.getChildren(v);
	if (oldChildren == null) {
	    // Node is not in underlying graph
	    if (!newParentSets.containsKey(v)) {
		return null;
	    }

	    // New children are just those that were added
	    return Collections.unmodifiableSet((Set) childSetAdd.get(v));
	} 

	// Node is in underlying graph
	if (removedNodes.contains(v)) {
	    return null;
	}

	Set curChildren = new HashSet(oldChildren);
	curChildren.removeAll((Set) childSetRemove.get(v));
	curChildren.addAll((Set) childSetAdd.get(v));
	return Collections.unmodifiableSet(curChildren);
    }

    /**
     * Returns the set of nodes that are barren in this graph but are
     * not barren nodes in the underlying graph.  A barren node is one
     * with no children.
     */
    public Set getNewlyBarrenNodes() {
	Set newlyBarren = new HashSet();
	
	// One way to get a new barren node is to add a node, but not add 
	// any children to it.
	for (Iterator iter = newParentSets.keySet().iterator(); 
	     iter.hasNext(); ) {
	    Object v = iter.next();
	    if (!underlying.nodes().contains(v)
		  && !childSetAdd.containsKey(v)) {
		newlyBarren.add(v);
	    }
	}

	// The other way is to remove children from a node.
	for (Iterator iter = childSetRemove.keySet().iterator(); 
	     iter.hasNext(); ) {
	    Object v = iter.next();
	    if (getChildren(v).isEmpty()) {
		// Make sure it wasn't already barren
		if (!(underlying.nodes().contains(v) 
		      && underlying.getChildren(v).isEmpty())) {
		    newlyBarren.add(v);
		}
	    }
	}

	return newlyBarren;
    }

    /**
     * Changes the underlying graph so it is equal to this graph.  
     */
    public void changeUnderlying() {
	// Add nodes that were added (and not removed again)
	for (Iterator iter = newParentSets.keySet().iterator(); 
	     iter.hasNext(); ) {
	    underlying.addNode(iter.next());
	}

	// Remove nodes that were removed (and not added again)
	for (Iterator iter = removedNodes.iterator(); iter.hasNext(); ) {
	    underlying.removeNode(iter.next());
	}

	// Change parent sets
	for (Iterator iter = newParentSets.entrySet().iterator(); 
	         iter.hasNext(); ) {
	    Map.Entry entry = (Map.Entry) iter.next();
	    Object child = entry.getKey();
	    Set newParents = (Set) entry.getValue();
	    underlying.setParents(child, newParents);
	}

	clearChanges();
    }

    public void clearChanges() {
	//removedNodes.clear();
	//newParentSets.clear();
	//childSetAdd.clear();
	//childSetRemove.clear();
	removedNodes = new HashSet();
	newParentSets = new HashMap();
	childSetAdd = new HashMultiMap();
	childSetRemove = new HashMultiMap();
    }	

    public Object clone() {
	ParentUpdateDGraph clone = new ParentUpdateDGraph(underlying);
	clone.removedNodes = (Set) ((HashSet) removedNodes).clone();

	clone.newParentSets = (Map) ((HashMap) newParentSets).clone();
	for (Iterator iter = clone.newParentSets.entrySet().iterator(); 
	         iter.hasNext(); ) {
	    Map.Entry entry = (Map.Entry) iter.next();
	    entry.setValue(((HashSet) entry.getValue()).clone());
	}

	clone.childSetAdd = (MultiMap) ((HashMultiMap) childSetAdd).clone();
	clone.childSetRemove 
	    = (MultiMap) ((HashMultiMap) childSetRemove).clone();
	return clone;
    }

    private void addChild(Object parent, Object child) {
	// If this child was previously removed, just reverse that operation
	if (!childSetRemove.remove(parent, child)) {
	    // Really an addition
	    childSetAdd.add(parent, child);
	}
    }

    private void removeChild(Object parent, Object child) {
	// If this child was previously added, just reverse that operation
	if (!childSetAdd.remove(parent, child)) {
	    // Really a removal
	    childSetRemove.add(parent, child);
	}
    }

    private class NodeSet extends AbstractSet {
	public int size() {
	    int size = underlying.nodes().size();
	    for (Iterator iter = newParentSets.keySet().iterator(); 
		     iter.hasNext(); ) {
		if (!underlying.nodes().contains(iter.next())) {
		    ++size;
		}
	    }
	    size -= removedNodes.size();
	    return size;
	}

	public boolean contains(Object o) {
	    return ((underlying.nodes().contains(o) 
		         || newParentSets.containsKey(o)) 
		    && !removedNodes.contains(o));
	}

	public Iterator iterator() {
	    Set nodes = new HashSet(underlying.nodes());
	    nodes.addAll(newParentSets.keySet());
	    nodes.removeAll(removedNodes);
	    return Collections.unmodifiableSet(nodes).iterator();
	}
    }

    // Underlying graph
    DGraph underlying;

    // Set of nodes removed from the graph.
    Set removedNodes = new HashSet();

    // Map from node objects to Set objects representing their new 
    // parent sets.  Nodes that are not in the underlying graph are 
    // also included here.  
    Map newParentSets = new HashMap();
   
    // Multi-map from node objects to the sets of nodes added to their 
    // child sets.
    MultiMap childSetAdd = new HashMultiMap();

    // Multi-map from node objects to the sets of nodes removed from their 
    // child sets.
    MultiMap childSetRemove = new HashMultiMap();

    // Instance of inner class representing node set
    Set nodeSet = new NodeSet();
}
