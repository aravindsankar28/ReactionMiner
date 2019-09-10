package ctree.mapper;

import ctree.graph.*;

/**
 * Find a mapping between two graphs
 *
 * @author Huahai He
 * @version 1.0
 */

public interface GraphMapper extends java.io.Serializable {
  /**
   * Find a mapping between g1 and g2
   * @param g1 Graph
   * @param g2 Graph
   *
   * @return if V1[i] is mapped to V2[j], then map[i]=j, or -1 if V1[i] has no mapping.
   */
  int[] map(Graph g1, Graph g2);

}
