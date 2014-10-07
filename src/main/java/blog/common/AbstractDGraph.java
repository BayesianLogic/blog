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

import java.io.PrintStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

/**
 * Abstract implementation of the DGraph interface.
 */
public abstract class AbstractDGraph<T> implements DGraph<T> {

  /**
   * Throws an UnsupportedOperationException.
   */
  @Override
  public boolean addNode(T v) {
    throw new UnsupportedOperationException(
        "Tried to add node to unmodifiable graph.");
  }

  /**
   * Throws an UnsupportedOperationException.
   */
  @Override
  public boolean removeNode(T v) {
    throw new UnsupportedOperationException(
        "Tried to remove node from unmodifiable graph.");
  }

  /**
   * Throws an UnsupportedOperationException.
   */
  @Override
  public void addEdge(T parent, T child) {
    throw new UnsupportedOperationException(
        "Tried to add edge to unmodifiable graph.");
  }

  /**
   * Throws an UnsupportedOperationException.
   */
  @Override
  public void removeEdge(T parent, T child) {
    throw new UnsupportedOperationException(
        "Tried to remove edge from unmodifiable graph.");
  }

  /**
   * Implements setParents in terms of addEdge and removeEdge.
   */
  @Override
  public void setParents(T v, Set<T> newParents) {
    Set<T> oldParents = getParents(v);
    for (Iterator<T> iter = oldParents.iterator(); iter.hasNext();) {
      T parent = iter.next();
      if (!newParents.contains(parent)) {
        removeEdge(parent, v);
      }
    }
    for (Iterator<T> iter = newParents.iterator(); iter.hasNext();) {
      T parent = iter.next();
      if (!oldParents.contains(parent)) {
        addEdge(parent, v);
      }
    }
  }

  @Override
  public Set<T> getRoots() {
    Set<T> roots = new HashSet<T>();
    for (Iterator<T> iter = nodes().iterator(); iter.hasNext();) {
      T node = iter.next();
      if (getParents(node).isEmpty()) {
        roots.add(node);
      }
    }
    return Collections.unmodifiableSet(roots);
  }

  @Override
  public Set<T> getAncestors(T v) {
    Set<T> ancestors = new HashSet<T>();

    // Do depth-first search, adding each node to the ancestor set
    // the first time it is seen.
    Stack<T> stack = new Stack<T>();
    stack.push(v);
    while (!stack.empty()) {
      T u = stack.pop();
      Set<T> parents = getParents(u);
      for (Iterator<T> iter = parents.iterator(); iter.hasNext();) {
        T parent = iter.next();
        if (ancestors.add(parent)) {
          stack.push(parent);
        }
      }
    }
    return ancestors;
  }

  @Override
  public Set<T> getDescendants(T v) {
    Set<T> descendants = new HashSet<T>();

    // Do depth-first search, adding each node to the descendant set
    // the first time it is seen.
    Stack<T> stack = new Stack<T>();
    stack.push(v);
    while (!stack.empty()) {
      T u = stack.pop();
      Set<T> children = getChildren(u);
      for (Iterator<T> iter = children.iterator(); iter.hasNext();) {
        T child = iter.next();
        if (descendants.add(child)) {
          stack.push(child);
        }
      }
    }
    return descendants;
  }

  @Override
  public void print(PrintStream s) {
    for (Iterator<T> nodeIter = nodes().iterator(); nodeIter.hasNext();) {
      T node = nodeIter.next();
      s.println(node);

      for (Iterator<T> parentIter = getParents(node).iterator(); parentIter
          .hasNext();) {
        s.println("\t<- " + parentIter.next());
      }

      s.println();
    }
  }
}
