package ctree.index;

import ctree.graph.*;

/**
 * Graph factory methods for C-Tree.
 *
 * @author Huahai He
 * @version 1.0
 */
 public interface GraphFactory extends java.io.Serializable {



  /**
   * Get the histogram of a graph
   * @param g Graph
   * @return Hist
   */
  Hist toHist(Graph g);

  /**
   * Construct a graph closure given two graphs
   * @param g1 Graph
   * @param g2 Graph
   * @param map mapping from g1 to g2
   * @return Graph
   */
  public Graph graphClosure(Graph g1, Graph g2, int[] map);


}
