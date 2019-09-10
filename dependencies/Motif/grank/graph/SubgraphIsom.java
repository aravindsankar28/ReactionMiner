package grank.graph;

import ctree.alg.*;

/**
 * Subgraph isomorphism test using Ullmann's algorithm
 * @author Huahai He
 * @version 1.0
 */

public class SubgraphIsom {

  // common used variables
  private static int n1; // number of vertices in graph 1
  private static int n2; // number of vertices in graph 2
  private static int[][] adj1; // adjacency matrix of graph 1
  private static int[][] adj2; // adjacency matrix of graph 2
  private static int[][] M; // possible mapping matrix
  private static int[] map; // current mapping in state search space
  private static int[] rmap; // reverse map


  public static boolean subIsom(LGraph g1, LGraph g2) {
    int[][] adj1=g1.adjmatrix();
    int[][] adj2=g2.adjmatrix();
    int[][] M = bimatrix(g1,g2);
    int[] map=Ullmann3.subgraphIsomorphism(adj1,adj2,M);
    return map!=null;
  }

  private static int[][] bimatrix(LGraph g1, LGraph g2) {
    int n1 = g1.V.length;
    int n2 = g2.V.length;
    int[][] M = new int[n1][n2];
    for (int i = 0; i < n1; i++) {
      for (int j = 0; j < n2; j++) {
        if (g1.V[i] == g2.V[j]) {
          M[i][j] = 1;
        }
        else {
          M[i][j] = 0;
        }
      }
    }
    return M;
  }

}
