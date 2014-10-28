/**
 * 
 */
package blog.common;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author David T
 * @since Oct 16, 2014
 * 
 */
public class HashDynamicGraph extends AbstractDGraph implements DynamicGraph,
    Cloneable {

  public HashDynamicGraph() {
    nodeSet = new HashSet();
    parents = new HashMultiMap();
    children = new HashMultiMap();
    edgesAppearOn = new HashMultiMap();
    nodesRelatedTo = new HashMultiMap();
    barrenNodeSet = new HashSet();
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.common.DGraph#nodes()
   */
  @Override
  public Set nodes() {
    return Collections.unmodifiableSet(nodeSet);
  }

  public MultiMap getParentsMap() {
    return parents;
  }

  public MultiMap getChildrenMap() {
    return children;
  }

  public MultiMap getEdgesAppearOnMap() {
    return edgesAppearOn;
  }

  public MultiMap getNodesRelatedToMap() {
    return nodesRelatedTo;
  }

  public boolean addNode(Node v) {
    return nodeSet.add(v);
  }

  public boolean removeNode(Node v) {
    if (nodeSet.contains(v)) {
      for (Iterator iter = ((Set) parents.get(v)).iterator(); iter.hasNext();) {
        Node curNode = (Node) iter.next();
        ((Set) children.get(curNode)).remove(v);
      }
      for (Iterator iter = ((Set) children.get(v)).iterator(); iter.hasNext();) {
        Node curNode = (Node) iter.next();
        ((Set) parents.get(curNode)).remove(v);
      }
      parents.remove(v);
      children.remove(v);
      if (edgesAppearOn.containsKey(v)) {
        for (Iterator iter = ((Set) edgesAppearOn.get(v)).iterator(); iter
            .hasNext();) {
          Edge curEdge = (Edge) iter.next();
          ((Set) nodesRelatedTo.get(curEdge)).remove(v);
        }
        edgesAppearOn.remove(v);
      }
    }
    return nodeSet.remove(v);
  }

  public boolean addEdge(Edge e) {
    if (nodeSet.contains(e.getSrc()) && nodeSet.contains(e.getDst())
        && !((Set) parents.get(e.getDst())).contains(e.getSrc())) {
      ((Set) parents.get(e.getDst())).add(e.getSrc());
      ((Set) children.get(e.getSrc())).add(e.getDst());
      e.getSrc().incOutDegree();
      if (!e.getSrc().isBarren()) {
        barrenNodeSet.remove(e.getSrc());
      }
      return true;
    }
    return false;
  }

  public boolean removeEdge(Edge e) {
    if (nodeSet.contains(e.getSrc()) && nodeSet.contains(e.getDst())
        && ((Set) children.get(e.getSrc())).contains(e.getDst())) {
      ((Set) parents.get(e.getDst())).remove(e.getSrc());
      ((Set) children.get(e.getSrc())).remove(e.getDst());
      e.getSrc().decOutDegree();
      if (e.getSrc().isBarren()) {
        barrenNodeSet.add(e.getSrc());
      }
      if (nodesRelatedTo.containsKey(e)) {
        for (Iterator iter = ((Set) nodesRelatedTo.get(e)).iterator(); iter
            .hasNext();) {
          Node curNode = (Node) iter.next();
          ((Set) edgesAppearOn.get(curNode)).remove(e);
        }
        nodesRelatedTo.remove(e);
      }
      return true;
    }
    return false;
  }

  public boolean addLabel(Edge e, Node labelNode) {
    if (!((Set) edgesAppearOn.get(labelNode)).contains(e)) {
      ((Set) edgesAppearOn.get(labelNode)).add(e);
      ((Set) nodesRelatedTo.get(e)).add(labelNode);
      return true;
    }
    return false;
  }

  public Set getBarrenNodeSet() {
    return barrenNodeSet;
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.common.DGraph#getParents(java.lang.Object)
   */
  @Override
  public Set getParents(Object v) {
    return Collections.unmodifiableSet((Set) parents.get(v));
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.common.DGraph#getChildren(java.lang.Object)
   */
  @Override
  public Set getChildren(Object v) {
    return Collections.unmodifiableSet((Set) children.get(v));
  }

  protected Set nodeSet;
  protected MultiMap parents;
  protected MultiMap children;
  protected MultiMap edgesAppearOn;
  protected MultiMap nodesRelatedTo;
  protected Set barrenNodeSet;
}
