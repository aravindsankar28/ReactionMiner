package ctree.tool;

import ctree.graph.*;

/**
 * This class represents entries of the priority queue used in the
 * incremental ranking (RTreeRanker). The entry can be an index node,
 * a leaf node, or an object.
 *
 * @author Huahai He
 * @version 1.0
 */
public class RankerEntry implements Comparable {
  private double dist; // distance to the query
  private Object obj; // Graph or CTreeNode

  public RankerEntry(double _dist, Object _obj) {
    dist = _dist;
    obj = _obj;
  }

  public int compareTo(Object obj) {
    RankerEntry e1 = (RankerEntry) obj;
    if (dist < e1.dist) {
      return -1;
    }
    else if (dist == e1.dist) {
      return 0;
    }
    else {
      return 1;
    }
  }

  public double getDist() {
    return dist;
  }

  public void setDist(double _dist) {
    dist = _dist;
  }

  public Object getObject() {
    return obj;
  }

  public Graph getGraph() {
    return (Graph) obj;
  }

}
