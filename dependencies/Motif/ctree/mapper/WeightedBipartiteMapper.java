package ctree.mapper;

import ctree.alg.*;
import ctree.graph.*;

import ctree.index.*;

/**
 * Weighted bipartite graph mapper.
 *
 * @author Huahai He
 * @version 1.0
 */

public class WeightedBipartiteMapper implements GraphMapper {
  private WeightMatrix wmatrix;
  protected WeightedBipartiteMapper() {

  }

  public WeightedBipartiteMapper(WeightMatrix _wmatrix) {
    wmatrix = _wmatrix;
  }

  /**
   *
   * @param g1 AbstractGraph
   * @param g2 AbstractGraph
   * @param isSub boolean
   * @return int[]
   */
  public int[] map(Graph g1, Graph g2) {
    // compute weight matrix
    int n1 = g1.numV();
    int n2 = g2.numV();
    if (n1 > n2) {
      int[] rmap = map(g2, g1);
      return Util.get_rmap(rmap, n1);
    }
    double[][] W = wmatrix.weightMatrix(g1, g2);

    // maximum weight matching
    int[] map = Hungarian.maximumWeightMatching(W);

    // remove matchings of zero weight
    for (int i = 0; i < map.length; i++) {
      if (W[i][map[i]] == 0) {
        map[i] = -1;
      }
    }
    return map;
  }

}
