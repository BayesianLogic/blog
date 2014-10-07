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

package blog.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of the DGraph interface. Represents both the parent
 * set and the child set for each node (even though this is redundant) so both
 * kinds of sets can be accessed quickly.
 */
public class DefaultDGraph<T> extends AbstractDGraph<T> implements Cloneable {
  /**
   * Creates a new, empty graph.
   */
  public DefaultDGraph() {
  }

  /**
   * Returns an unmodifiable set consisting of the nodes in this graph.
   */
  @Override
  public Set<T> nodes() {
    return Collections.unmodifiableSet(nodeInfo.keySet());
  }

  @Override
  public boolean addNode(T v) {
    if (nodeInfo.containsKey(v)) {
      return false;
    }
    nodeInfo.put(v, new NodeInfo<T>());
    return true;
  }

  @Override
  public boolean removeNode(Object v) {
    NodeInfo<T> info = nodeInfo.remove(v);
    if (info == null) {
      return false;
    }

    for (Iterator<T> iter = info.parents.iterator(); iter.hasNext();) {
      T parent = iter.next();
      NodeInfo<T> parentInfo = nodeInfo.get(parent);
      if (parentInfo != null) {
        parentInfo.children.remove(v);
      }
    }
    for (Iterator<T> iter = info.children.iterator(); iter.hasNext();) {
      Object child = iter.next();
      NodeInfo<T> childInfo = nodeInfo.get(child);
      if (childInfo != null) {
        childInfo.parents.remove(v);
      }
    }

    return true;
  }

  @Override
  public void addEdge(T parent, T child) {
    NodeInfo<T> parentInfo = ensureNodePresent(parent);
    parentInfo.children.add(child);

    NodeInfo<T> childInfo = ensureNodePresent(child);
    childInfo.parents.add(parent);
  }

  @Override
  public void removeEdge(T parent, T child) {
    NodeInfo<T> parentInfo = nodeInfo.get(parent);
    if (parentInfo != null) {
      parentInfo.children.remove(child);
    }

    NodeInfo<T> childInfo = nodeInfo.get(child);
    if (childInfo != null) {
      childInfo.parents.remove(parent);
    }
  }

  public Set<T> getParents(Object v) {
    NodeInfo<T> info = nodeInfo.get(v);
    if (info == null) {
      return null;
    }
    return Collections.unmodifiableSet(info.parents);
  }

  @Override
  public void setParents(T v, Set<T> newParents) {
    NodeInfo<T> vInfo = ensureNodePresent(v);
    Set<T> oldParents = vInfo.parents;
    vInfo.parents = new HashSet<T>(newParents);

    // Remove v from child sets of old parents
    for (Iterator<T> iter = oldParents.iterator(); iter.hasNext();) {
      T parent = iter.next();
      if (!newParents.contains(parent)) {
        NodeInfo<T> parentInfo = nodeInfo.get(parent);
        if (parentInfo != null) {
          parentInfo.children.remove(v);
        }
      }
    }

    // Add v to child sets of new parents
    for (Iterator<T> iter = newParents.iterator(); iter.hasNext();) {
      T parent = iter.next();
      if (!oldParents.contains(parent)) {
        NodeInfo<T> parentInfo = ensureNodePresent(parent);
        parentInfo.children.add(v);
      }
    }
  }

  @Override
  public Set<T> getChildren(Object v) {
    NodeInfo<T> info = nodeInfo.get(v);
    if (info == null) {
      return null;
    }
    return Collections.unmodifiableSet(info.children);
  }

  @Override
  public Object clone() {
    DefaultDGraph<T> clone = new DefaultDGraph<T>();
    clone.nodeInfo = (Map<T, NodeInfo<T>>) ((HashMap) nodeInfo).clone();
    for (Iterator<Map.Entry<T, NodeInfo<T>>> iter = clone.nodeInfo.entrySet()
        .iterator(); iter.hasNext();) {
      Map.Entry<T, NodeInfo<T>> entry = iter.next();
      entry.setValue((NodeInfo<T>) (entry.getValue()).clone());
    }
    return clone;
  }

  public static class NodeInfo<T> implements Cloneable {
    HashSet<T> parents = new HashSet<T>();
    HashSet<T> children = new HashSet<T>();

    public Object clone() {
      NodeInfo<T> clone = new NodeInfo<T>();

      clone.parents = (HashSet<T>) parents.clone();
      clone.children = (HashSet<T>) children.clone();
      return clone;
    }
  }

  private NodeInfo<T> ensureNodePresent(T v) {
    NodeInfo<T> info = nodeInfo.get(v);
    if (info == null) {
      info = new NodeInfo<T>();
      nodeInfo.put(v, info);
    }
    return info;
  }

  protected Map<T, NodeInfo<T>> nodeInfo = new HashMap<T, NodeInfo<T>>(); // from
                                                                          // Object
                                                                          // to
                                                                          // NodeInfo
}
