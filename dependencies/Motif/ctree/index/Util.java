package ctree.index;

import java.util.*;

import ctree.alg.*;
import ctree.graph.*;

/**
 *
 * @author Huahai He
 * @version 1.0
 */

public final class Util {
  public static int[] get_rmap(int[] map, int n2) {
    int[] rmap = new int[n2];
    Arrays.fill(rmap, -1);
    for (int i = 0; i < map.length; i++) {
      if (map[i] != -1) {
        rmap[map[i]] = i;
      }
    }
    return rmap;
  }

  /**
   * Return the next Poisson value
   * @param lambda the mean value
   * @param rand Random
   * @return int
   */
  public static int nextPoisson(double lambda, Random rand) {
    double elambda = Math.exp( -1 * lambda);
    int x = 0;
    double product = 1;
    while (product >= elambda) {
      product *= rand.nextDouble();
      x++;
    }
    return x > 0 ? x - 1 : 0;
  }

  /**
   * Check if g1 is isomorphic to a subgraph of g2.
   * Assuming no attributes on edges.
   * @param g1 Graph
   * @param g2 Graph
   * @return boolean
   */
  public static boolean subIsomorphic(Graph g1, Graph g2) {

    // check subgraph isomorphism
    int[][] B = getBipartiteMatrix2(g1, g2);
    int[] map = Ullmann.subgraphIsomorphism(g1.adjMatrix(), g2.adjMatrix(), B);
    if (map == null) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * Check if g1 is pseudo subisomorphic to g2.
   * Assuming no attributes on edges.
   * @param g1 Graph
   * @param g2 Graph
   * @param level int
   * @param checkEdges boolean
   * @return boolean
   */
  public static boolean pseudoSubIsomorphic(Graph g1, Graph g2, int level) {
    if (g1.numV() > g2.numV()) {
      return false;
    }
    int[][] M = getBipartiteMatrix(g1, g2);
    return PseudoSubgraphIsomorphism.pseudoSubIsomorphic(g1.adjList(),
            g2.adjList(), M, level);
  }

  /**
   * Return a bipartite matrix for two graphs based on the mappability of vertices.
   * @param g1 Graph
   * @param g2 Graph
   * @return int[][]
   */
  public static int[][] getBipartiteMatrix(Graph g1, Graph g2) {
    Vertex[] V1 = g1.V();
    Vertex[] V2 = g2.V();
    int n1 = V1.length;
    int n2 = V2.length;
    int[][] B = new int[n1][n2];
    for (int i = 0; i < n1; i++) {
      for (int j = 0; j < n2; j++) {
        if (V1[i].mappable(V2[j])) {
          B[i][j] = 1;
        } else {
          B[i][j] = 0;
        }
      }
    }
    return B;
  }

  /**
   * Return a bipartite list for two graphs based on the mappability of vertices.
   * @param g1 Graph
   * @param g2 Graph
   * @return int[][]
   */
  public static int[][] getBipartiteList(Graph g1, Graph g2) {
    Vertex[] V1 = g1.V();
    Vertex[] V2 = g2.V();
    int n1 = V1.length;
    int n2 = V2.length;
    LinkedList<Integer> [] llist = new LinkedList[n1];
    for (int i = 0; i < n1; i++) {
      llist[i] = new LinkedList<Integer>();
      for (int j = 0; j < n2; j++) {
        if (V1[i].mappable(V2[j])) {
          llist[i].add(j);
        }
      }
    }
    int[][] bilist = new int[n1][];
    for (int i = 0; i < n1; i++) {
      bilist[i] = new int[llist[i].size()];
      Iterator<Integer> it = llist[i].listIterator();
      int cnt = 0;
      while (it.hasNext()) {
        bilist[i][cnt++] = it.next();
      }
    }
    return bilist;
  }

  /**
   * Same as getBipartiteMatrix(), but checking mappability of neighbor vertices as well.
   * @param g1 Graph
   * @param g2 Graph
   * @return int[][]
   */
  public static int[][] getBipartiteMatrix2(Graph g1, Graph g2) {
    int[][] adj1 = g1.adjMatrix();
    int[][] adj2 = g2.adjMatrix();
    Vertex[] V1 = g1.V();
    Vertex[] V2 = g2.V();
    int n1 = V1.length;
    int n2 = V2.length;

// generate matching matrix
    int[][] B = new int[n1][n2];
    for (int i = 0; i < n1; i++) {
      for (int j = 0; j < n2; j++) {
        if (checkPair(adj1, adj2, V1, V2, n1, n2, i, j)) {
          B[i][j] = 1;
        } else {
          B[i][j] = 0;
        }
      }
    }
    return B;
  }

  /**
   * Check if a mapping from i to j is valid
   * @param adj1 int[][]
   * @param adj2 int[][]
   * @param V1 Vertex[]
   * @param V2 Vertex[]
   * @param n1 int
   * @param n2 int
   * @param i int
   * @param j int
   * @return boolean
   */
  private static boolean checkPair(int[][] adj1, int[][] adj2,
                                   Vertex[] V1,
                                   Vertex[] V2, int n1, int n2,
                                   int i, int j) {
    // check the two vertices
    if (!V1[i].mappable(V2[j])) {
      return false;
    }
    // check degree
    int cnt1 = 0;
    for (int p = 0; p < n1; p++) {
      if (adj1[i][p] > 0) {
        cnt1++;
      }
    }
    int cnt2 = 0;
    for (int q = 0; q < n2; q++) {
      if (adj2[j][q] > 0) {
        cnt2++;
      }
    }
    if (cnt1 > cnt2) {
      return false;
    }

    // check neighbors
    for (int p = 0; p < n1; p++) {
      if (adj1[i][p] > 0) {
        boolean flag = false;
        for (int q = 0; q < n2; q++) {
          if (adj2[j][q] > 0 && V1[p].mappable(V2[q])) {
            flag = true;
            break;
          }
        }
        if (flag == false) {
          return false;
        }
      }
    }

    return true;

  }

  /**
   * This method is suspended.
   * @param g1 Graph
   * @param g2 Graph
   * @return int[][]
   */
  public static int[][] getBipartiteMatrixWithEdge(Graph g1, Graph g2) {
    Vertex[] V1 = g1.V();
    Vertex[] V2 = g2.V();
    int n1 = V1.length;
    int n2 = V2.length;
    int[][] B = new int[n1][n2];
    for (int i = 0; i < n1; i++) {
      for (int j = 0; j < n2; j++) {
        if (V1[i].mappable(V2[j])) {
          B[i][j] = 1;
        } else {
          B[i][j] = 0;
        }
      }
    }

    int[][] alist1 = g1.adjList();
    Edge[][] edges1 = g1.adjEdges();
    int[][] alist2 = g2.adjList();
    Edge[][] edges2 = g2.adjEdges();
    int[][] B1 = new int[n1][n2]; // local bipartite
    int[] map = new int[n1];
    int[] rmap = new int[n2];
    for (int i = 0; i < n1; i++) {
      for (int j = 0; j < n2; j++) {
        if (B[i][j] > 0) {
          // check local mappability
          for (int p = 0; p < alist1[i].length; p++) {
            int u1 = alist1[i][p];
            Edge e1 = edges1[i][p];
            // construct local bipartite for (i,j)
            for (int q = 0; q < alist2[j].length; q++) {
              int v1 = alist2[j][q];
              Edge e2 = edges2[j][q];
              if (B[u1][v1] > 0 && e1.mappable(e2)) {
                B1[p][q] = 1;
              } else {
                B1[p][q] = 0;
              }

            } // for q

          } // for p

          // check if B1 has a semi-perfect matching
          Arrays.fill(map, 0, alist1[i].length, -1);
          Arrays.fill(rmap, 0, alist2[j].length, -1);
          int matches = MaximumBipartiteMatching.maximumMatching(B1,
                  alist1[i].length, alist2[j].length, map, rmap, true);
          if (matches < alist1[i].length) {
            // if not, then i is not mappable onto j
            B[i][j] = 0;
          }

        }
      }
    }
    return B;
  }

  /**
   * Test if a graph is connected.
   * @param g Graph
   * @return boolean
   */
  public static boolean isConnected(Graph g) {
    int[][] alist = g.adjList();
    boolean[] visited = new boolean[alist.length];
    Arrays.fill(visited, false);
    Stack<Integer> stack = new Stack();
    visited[0] = true;
    stack.push(0);
    while (!stack.isEmpty()) {
      int i = stack.pop();
      for (int j : alist[i]) {
        if (!visited[j]) {
          visited[j] = true;
          stack.push(j);
        }
      }
    }
    for (int i = 0; i < visited.length; i++) {
      if (!visited[i]) {
        return false;
      }
    }
    return true;
  }


}
