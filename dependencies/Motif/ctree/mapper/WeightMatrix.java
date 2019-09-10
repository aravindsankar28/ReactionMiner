package ctree.mapper;

import ctree.graph.*;

/**
 * <p> Closure-tree</p>
 *
 * @author Huahai He
 * @version 1.0
 */
public interface WeightMatrix extends java.io.Serializable {
  /**
   * Generate a weight matrix for two graphs. The matrix is to be used in
   * a graph mapping method, e.g. weighted bipartite mapper and weighted
   * extending mapper.
   * @param g1 Graph
   * @param g2 Graph
   * @return A n1 by n2 weight matrix M. If M[i][j]<=0 then i,j are not mappable
   */
  double[][] weightMatrix(Graph g1, Graph g2);
}
