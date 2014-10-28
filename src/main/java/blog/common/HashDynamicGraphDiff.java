/**
 * 
 */
package blog.common;


/**
 * @author David T
 * @since Sep 28, 2014
 * 
 */
public class HashDynamicGraphDiff extends HashDynamicGraph implements
    DynamicGraphDiff, Cloneable {

  public HashDynamicGraphDiff(DynamicGraph underlying) {
    this.underlying = underlying;
    this.nodeSet = new HashSetDiff(underlying.nodes());
    this.parents = new HashMultiMapDiff(underlying.getParentsMap());
    this.children = new HashMultiMapDiff(underlying.getChildrenMap());
    this.edgesAppearOn = new HashMultiMapDiff(underlying.getEdgesAppearOnMap());
    this.nodesRelatedTo = new HashMultiMapDiff(
        underlying.getNodesRelatedToMap());
  }

  protected DynamicGraph underlying;
  protected SetDiff nodeSet;
  protected MultiMapDiff parents;
  protected MultiMapDiff children;
  protected MultiMapDiff edgesAppearOn;
  protected MultiMapDiff nodesRelatedTo;
}
