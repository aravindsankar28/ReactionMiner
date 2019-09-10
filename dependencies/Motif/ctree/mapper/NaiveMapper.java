package ctree.mapper;

import ctree.graph.*;

import ctree.index.*;

/**
 * A simple but extremely slow optimal distance based graph mapper.
 *
 * @author Huahai He
 * @version 1.0
 */
public class NaiveMapper implements GraphMapper {
  GraphSim graphSim;

  public NaiveMapper(GraphSim _graphSim) {
    graphSim = _graphSim;
  }

  public int[] map(Graph g1, Graph g2) {
    int m = g1.numV();
    int n = g2.numV();
    int[] maxmap = new int[m];
    int[] map = new int[m];
    int[] cand = new int[n];
    int maxSim = stateSearch(g1, g2, map, 0, cand, Integer.MAX_VALUE,
                             maxmap);
    return maxmap;
  }

  private int stateSearch(Graph gc1, Graph gc2, int[] map,
                          int depth, int[] marked, int maxSim,
                          int[] maxmap) {
    if (depth == gc1.numV()) {
      int sim = graphSim.sim(gc1, gc2, map);
      if (sim > maxSim) {
        System.arraycopy(map, 0, maxmap, 0, map.length);
        maxSim = sim;
      }
      return maxSim;
    }
    for (int j = 0; j < marked.length; j++) {
      if (marked[j] == 0) {
        marked[j] = 1;
        map[depth] = j;
        maxSim = stateSearch(gc1, gc2, map, depth + 1, marked, maxSim, maxmap);
        marked[j] = 0;
      }
    }
    // neglect this vertex
    map[depth] = -1;
    maxSim = stateSearch(gc1, gc2, map, depth + 1, marked, maxSim, maxmap);
    return maxSim;
  }

}
