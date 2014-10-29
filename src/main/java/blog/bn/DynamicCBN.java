/**
 * 
 */
package blog.bn;

import java.util.Iterator;

import blog.common.HashDynamicGraph;

/**
 * @author David T
 * @since Oct 28, 2014
 * 
 */
public class DynamicCBN extends HashDynamicGraph implements CBN {
  public DynamicCBN(CBN underlying) {
    for (Iterator iter = underlying.nodes().iterator(); iter.hasNext();) {
      BayesNetVar var = (BayesNetVar) iter.next();
      Node curNode = new Node(var);
      addNode(curNode);
      for (Iterator iter2 = underlying.getParents(var).iterator(); iter2
          .hasNext();) {
        BayesNetVar par = (BayesNetVar) iter2.next();
        Node parNode = new Node(par);
        addEdge(new Edge(parNode, curNode));
      }
    }
  }
}
