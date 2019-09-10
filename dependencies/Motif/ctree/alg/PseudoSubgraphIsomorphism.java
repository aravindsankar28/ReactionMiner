package ctree.alg;

import java.util.*;

/**
 * Pseudo subgraph isomorphism test.
 * Assuming no attributes on edges.
 *
 * <pre>
 * Reference:
 *   Huahai He and Ambuj K. Singh. Closure-Tree: An Index Structure for Graph
 *   Queries. ICDE 2006.
 *
 * Algorithm description:
 * Input:
 * G1,G2: adjacency matrices of G1 and G2
 * B: Initial level compatible matrix between G1 and G2
 *
 * Ouput: Bipartite matrix B and whether G1 and G2 are pseudo isomorphic
 *
 * RefineBipartiteMatrix(G1, G2, B) {
 *   for level = 1 to n-1 {
 *     uniform = true;
 *     for u in G1, v in G2 and B[u,v]>=level {
 *       N_G(u), N_H(v): neighbors of u and v
 *       B': bipartite matrix between N_G(u) and N_H(v)
 *       for u' in N_G(u), v' in N_H(v) {
 *         if B[u',v']>=B[u,v] then B'[u',v']=1
 *         else B'[u',v']=0
 *       }
 *       M=bipartite_maximum_matching(B');
 *       if |M|==|N_G(u)| then B[u,v]++;
 *       else uniform = false;
 *     }
 *     if uniform then {
 *       for all B[u,v]>level, B[u,v] = n
 *       break;
 *     }
 *   }
 * }
 *
 * pseudo_subisomorphic(G1, G2){
 *   generate initial compatible matrix B: B[u,v]=1 iff label[u]==label[v]
 *   compute_compatible_matrix(G1,G2,B)
 *   construct C from B, C[u,v]=1 iff B[u,v]==n
 *   M=maximum_bipartite_matching(C);
 *   if |M|==|G1| then return true;
 *   else return false;
 * }
 *
 * The time complexity is O(L*n1*n2*(d1*d2+M(d1,d2))+M(n1,n2)) where L is the
 * pseudo level, n1 and n2 are the number of vertices, d1 and d2 are the
 * maximum degrees of vertices, M() is the time complexity of bipartite matching.
 *
 * </pre>
 *
 * @author Huahai He
 * @version 1.0
 */

public class PseudoSubgraphIsomorphism {

  /**
   * Check if a graph is pseudo sub-isomorphic to another graph.
   * @param G1 adjacency list of G1
   * @param G2 adjacency list of G2
   * @param B initial compatible matrix, will be changed
   * @param level Target compatible level. 0<=level<=n1-1 or level==-1. n1=|G1|
   * If level==0 then no computation is performed. If level==-1 then level=n1-1.
   * @return true if G1 is pseudo isomorphic to a subgraph of G2.
   */
  public static boolean pseudoSubIsomorphic(int[][] G1, int[][] G2, int[][] B,
                                            int level) {
    //long time0 = System.currentTimeMillis();
    int n1 = G1.length;
    int n2 = G2.length;
    refineBipartiteMatrix(G1, G2, B, level);

    // check if G1 has a perfect matching to G2

    int[] map = new int[n1];
    Arrays.fill(map, -1);
    int[] rmap = new int[n2];
    Arrays.fill(rmap, -1);
    //int matches = HopcroftKarp.maximumMatching(B, map);
    int matches = MaximumBipartiteMatching.maximumMatching(B, n1, n2, map,
        rmap, true);

    //time += System.currentTimeMillis() - time0;
    //cnt++;

    if (matches < n1) {
      return false;
    }
    else {
      return true;
    }
  }

  //!!!
  //public static long time = 0, timeA = 0, cnt = 0;

  /**
   * Compute the boolean compatible matrix. The compatible level of two
   * vertices (u,v) ranges from 0 to n1-1. After the call, B[u][v]>0 if
   * its compatible level is at least the given parameter, otherwise B[u][v]=0.
   * @param G1 int[n1][n1] It is not required that n1<=n2
   * @param G2 int[n2][n2]
   * @param B Initial compatible matrix (level 0)
   * @param level Target compatible level. 0<=level<=n1-1 or level==-1.
   * If level==0 then no computation is performed. If level==-1 then level=n1-1.
   */
  public static void refineBipartiteMatrix(int[][] G1, int[][] G2,
                                             int[][] B, int level) {
    int n1 = G1.length;
    int n2 = G2.length;
    if (level == -1) {
      //level = n1 - 1;
      //level = Math.max(n1, n2) - 1;
      level = n1 * n2;
    }

    int[][] B1 = new int[n1][n2];   // local bipartite matrix
    int[] map = new int[n1];
    int[] rmap = new int[n2];
    boolean changed = false;
    for (int lev = 0; lev < level; lev++) { // loop of levels
      changed = false;
      for (int u = 0; u < n1; u++) { // check if u is compatible to v
        for (int v = 0; v < n2; v++) {

          if (B[u][v] == 0) {
            continue;
          }

          //long timeA0 = System.currentTimeMillis();
          // generate bipartite of N_G(u) and N_H(v)
          for (int p = 0; p < G1[u].length; p++) {
            int u1 = G1[u][p];

            for (int q = 0; q < G2[v].length; q++) {
              int v1 = G2[v][q];
              if (B[u1][v1] > 0) {
                B1[p][q] = 1;
              }
              else {
                B1[p][q] = 0;
              }
            } // for NH[v]
          } // for NG[u]

          // check if N_G(u) has a perfect matching to N_H(v)
          for (int i = 0; i < G2[v].length; i++) {
            rmap[i] = -1;
          }
          for (int i = 0; i < G1[u].length; i++) {
            map[i] = -1;
          }
          //int matches = HopcroftKarp.maximumMatching(B1, NG[u].length,
          //    NH[v].length, map);
          int matches = MaximumBipartiteMatching.maximumMatching(B1,
              G1[u].length,
              G2[v].length, map, rmap, true);

          if (matches < G1[u].length) {
            B[u][v] = 0;
            changed = true;
            if (lev >= Math.max(n1, n2) - 1) {
              throw new RuntimeException("counter example");
            }
          }
          //timeA += System.currentTimeMillis() - timeA0;

        } //for v
      } // for u

      if (!changed) {
        break;
      }
    } // for level
  }

}
