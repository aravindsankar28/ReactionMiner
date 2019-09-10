package ctree.mapper;

import java.util.*;

import ctree.graph.*;

import ctree.index.*;

/**
 * State search graph mapping by Branch and Bound using the upper bound of
 * similarity. Assume uniform similarity scores.
 *
 * @author Huahai He
 * @version 1.0
 */

public class StateSearchMapper implements GraphMapper {
  private static final int VSIM = 1;
  private static final int ESIM = 1;

  private static long count;
  private static boolean exitFlag;
  private static long THRESHOLD = 1000000000; // maximum number of search states
//1000000000
  public StateSearchMapper() {
  }

  public StateSearchMapper(long _threshold) {
    THRESHOLD = _threshold;
  }

  public int[] map(Graph g1, Graph g2) {
    int[] map = new int[g1.numV()];
    map(g1, g2, map);
    return map;
  }

  public static int map(Graph g1, Graph g2, int[] maxmap) {
    Vertex[] V1 = g1.V();
    Vertex[] V2 = g2.V();
    int n1 = V1.length;
    int n2 = V2.length;
    int[][] alist1 = g1.adjList();
    int[][] adj2 = g2.adjMatrix();

    int[][] bilist = Util.getBipartiteList(g1, g2);
    int[] map = new int[n1];
    int[] rmap = new int[n2];
    //int[] maxmap = new int[n1];
    Arrays.fill(map, -1);
    Arrays.fill(rmap, -1);
    Arrays.fill(maxmap, -1);
    int sim_up = g1.numV() * VSIM + g1.numE() * ESIM; // norm of g1
    count = 0;
    exitFlag = false;
    int sim = stateSearch(0, sim_up, 0, alist1, adj2, bilist, map, rmap, maxmap,
                          n1);
    return sim;
  }

  /**
   * state search.
   * Note: ONLY for undirected graphs
   * @param sim Best similarity value found so far
   * @param sim_up Upper bound of sim
   * @param depth State search depth
   * @param alist1 Adjacency list of graph 1
   * @param adj2 Adjacency matrix of graph 2
   * @param bilist Bipartite list
   * @param map int[]
   * @param rmap int[]
   * @param maxmap int[]
   * @param n1 int
   * @return Maximum similarity value
   */
  private static int stateSearch(int sim, int sim_up, int depth,
                                 int[][] alist1, int[][] adj2, int[][] bilist,
                                 int[] map, int[] rmap, int[] maxmap, int n1) {
    count++;
    if (count > THRESHOLD) {
      exitFlag = true;
      return sim;
    }
    if (depth == n1) {
      if (sim < sim_up) {
        System.arraycopy(map, 0, maxmap, 0, n1);
        return sim_up;
      }
      else {
        return sim;
      }
    }
    int sim_up1;

    for (int i = 0; i < bilist[depth].length; i++) {
      int v = bilist[depth][i];
      if (rmap[v] == -1) {
        map[depth] = v;
        rmap[v] = depth;

        // update sim_up1
        sim_up1 = sim_up;
        for (int k = 0; k < alist1[depth].length; k++) {
          int p = alist1[depth][k];
          if (map[p] >= 0 && depth >= p) { //!!! only for undirected graphs
            if (adj2[v][map[p]] == 0) {
              sim_up1 -= ESIM;
            }
          }
        }

        if (sim_up1 > sim) {
          sim = stateSearch(sim, sim_up1, depth + 1,
                            alist1, adj2, bilist, map, rmap, maxmap, n1);

        }
        if (exitFlag) {
          return sim;
        }

        // restore
        rmap[v] = -1;
      }

    }
    // map to null
    map[depth] = -1;
    sim_up1 = sim_up - VSIM;
    for (int k = 0; k < alist1[depth].length; k++) {
      int p = alist1[depth][k];
      if (map[p] >= 0 || p >= depth) {
        sim_up1 -= ESIM;
      }
    }
    if (sim_up1 > sim) {
      sim = stateSearch(sim, sim_up1, depth + 1,
                        alist1, adj2, bilist, map, rmap, maxmap, n1);
    }
    if (exitFlag) {
      return sim;
    }

    return sim;
  }

  public static void setThreshold(long _threshold) {
    THRESHOLD = _threshold;
  }

}
