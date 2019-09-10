package grank.transform;

import java.util.*;
import grank.graph.*;

/**
 * A naive state search approach to find the maximum similarity between
 * two graphs. Assuming uniform similarity measures.
 *
 * @author Huahai He
 * @version 1.0
 */
public class NaiveGraphSim {
  private static class Environment {
    LGraph g1, g2;
    int n1, n2;
    int[] map, rmap, maxmap;
    int maxSim;
    Environment(LGraph _g1, LGraph _g2, int[] _map, int[] _rmap, int[] _maxmap,
                int _maxSim) {
      g1 = _g1;
      g2 = _g2;
      n1 = g1.V.length;
      n2 = g2.V.length;
      map = _map;
      rmap = _rmap;
      maxmap = _maxmap;
      maxSim = _maxSim;
    }
  }

  // Naive state search for graph similarity
  // For small graphs only
  public static int graphSim(LGraph g1, LGraph g2) {
    int n1 = g1.V.length;
    int n2 = g2.V.length;
    int[] map = new int[n1];
    int[] rmap = new int[n2];
    int[] maxmap = new int[n1];
    Arrays.fill(map, -1);
    Arrays.fill(rmap, -1);
    Arrays.fill(maxmap, -1);
    Environment env = new Environment(g1, g2, map, rmap, maxmap,
                                      Integer.MIN_VALUE);
    stateSearch(env, 0);
    return env.maxSim;
  }

  private static void stateSearch(Environment env, int depth) {
    if (depth >= env.n1) { // End of mapping vertices
      int sim = 0;
      for (int i = 0; i < env.n1; i++) { // vertex similarity
        int j = env.map[i];
        if (j >= 0) {
          if (env.g1.V[i] == env.g2.V[j]) {
            sim++;
          }
        }
      }

      int[][] matrix2 = env.g2.adjmatrix();
      for (LEdge e : env.g1.E) {
        int v1 = env.map[e.v1];
        int v2 = env.map[e.v2];
        if (v1 >= 0 && v2 >= 0) {
          if (e.label + 1 == matrix2[v1][v2]) {
            sim++;
          }
        }
      }
      if (sim > env.maxSim) {
        env.maxSim = sim;
        System.arraycopy(env.map, 0, env.maxmap, 0, env.n1);
      }

    }
    else {
      for (int j = 0; j < env.n2; j++) { // map V1[depth] to a free node in V2
        if (env.rmap[j] < 0) {
          env.map[depth] = j;
          env.rmap[j] = depth;
          stateSearch(env, depth + 1);
          env.rmap[j] = -1;
        }
      }
      env.map[depth] = -1; // map V1[depth] to a dummy node
      stateSearch(env, depth + 1);
    }
  }

  public static double norm(LGraph g) {
    return g.V.length+g.E.length;
  }
  public static void main(String[] args) {

  }
}
