/**
 * 
 */
package blog.common;

import java.util.Set;

import blog.bn.BayesNetVar;

/**
 * @author David T
 * @since Oct 16, 2014
 * 
 */
public interface DynamicGraph extends DGraph {

  MultiMap getParentsMap();

  MultiMap getChildrenMap();

  MultiMap getEdgesAppearOnMap();

  MultiMap getNodesRelatedToMap();

  Set getBarrenNodeSet();

  public class Edge {
    public Edge(Node src, Node dst) {
      this.src = src;
      this.dst = dst;
    }

    public void setSrc(Node src) {
      this.src = src;
    }

    public void setDst(Node dst) {
      this.dst = dst;
    }

    public Node getSrc() {
      return src;
    }

    public Node getDst() {
      return dst;
    }

    private Node src, dst;
  }

  public class Node {
    public Node(Object var) {
      this.var = (BayesNetVar) var;
    }

    public void setNode(Object var) {
      this.var = (BayesNetVar) var;
    }

    public BayesNetVar getNode() {
      return var;
    }

    public void incOutDegree() {
      outDegree++;
    }

    public void decOutDegree() {
      outDegree--;
    }

    public boolean isBarren() {
      return (outDegree == 0);
    }

    private BayesNetVar var;
    private int outDegree = 0;
  }
}
