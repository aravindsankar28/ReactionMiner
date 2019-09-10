package ctree.mapper;

import java.util.*;

import ctree.alg.*;
import ctree.graph.*;

import ctree.index.*;

/**
 * Find a mapping between two graphs using the bolean bipartite method.
 *
 * @author Huahai He
 * @version 1.0
 */

public class BipartiteMapper implements GraphMapper {
  public BipartiteMapper() {
  }

  public int[] map(Graph g1, Graph g2) {

    int[][] bimatrix = Util.getBipartiteMatrix(g1, g2);

    int[] map = new int[g1.numV()];
    Arrays.fill(map, -1);
    HopcroftKarp.maximumMatching(bimatrix, map);
    return map;
  }

}
