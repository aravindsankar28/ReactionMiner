package ctree.alg;

import java.util.*;

/**
 * This class implements the Hungarian algorithm for maximum weight matching
 * for bipartite graphs. The time complexity is O(n^3) where n is the number
 * of vertices.
 *
 *<pre>
 *
 * Reference:
 *   1. CMSC 651 Advanced Algorithms, Samir Khuller.
 *      {@link http://www.cs.umd.edu/class/fall2003/cmsc651/lec07.ps}
 *   2. Combinatorial Optimization: Algorithms and Complexity.
 *      Christos H. Papadimitriou and Kenneth Steiglitz.
 *
 *
 * Input:
 *    W[x,y]: complete bipartite weight matrix
 * Output:
 *    Maximum weight matching
 * Variables:
 *    label: labels for X and Y
 *    EQ: equality subgraph matrix
 *    S: the set of nodes in X encountered in the Hungarian tree
 *    T: the set of nodes in Y encountered in the Hungarian tree
 *    Z: the set of newly found nodes in Y
 *    slack(x,y): defined as label[x] + label[y] - W[x,y]
 *    lambda: the smallest slack(x,y) for x in S, y in ~T
 *    slack[]: for every y in ~T, slack[y]=min{slack(x,y)}, x in S
 *
 * MaximumWeightMatching {
 *   label[x] = max{W[x,y]}
 *   label[y] = 0
 *   EQ[x,y] = 1 iff W[x,y] == label[x] + label[y]
 *   M = MaximumCardinalityMatching(EQ)
 *   WHILE M is not perfect {
 *     S = free nodes in X
 *     T = {}
 *     Z <- y, for all y adjacent to S
 *     y = GrowHungarianTree
 *     Augment path along y
 *   }
 * }
 *
 * GrowHungarianTree {
 *   while true {
 *     while Z is not empty {
 *       remove y from Z
 *       if y is free then return y
 *       else
 *         T <- y
 *         x = mate(y)
 *         S <- x
 *         if it is not the first iteration
 *           for every y' in ~T
 *             slack[y'] = min{slack[y'], label[x]+label[y']-W[x,y']}
 *         Z <-y', y' adjacent to x and y' in ~T
 *     }
 *     if it is the first iteration
 *       for every y in ~T
 *         slack[y] = min{label[x]+label[y]-W[x,y]}, x in S
 *     Z = ReviseLabel;
 *   }
 * }
 *
 * ReviseLabel {
 *   lambda = min{slack[y]}, y in ~T
 *   for all x in S, label[x] = label[x] - lambda
 *   for all y in T, label[y] = label[y] + lambda
 *   for all y in ~T {
 *     slack[y] = slack[y] - lambda
 *     if slack[y]==0
 *       for x in S {
 *         if W[x,y]==label[x]+label[y]
 *           EQ[x,y] = 1
 *           Z <- y
 *       }
 *   }
 *   return Z
 * }
 *
 * </pre>
 * @author Huahai He
 * @version 1.0
 */

public class Hungarian {

  private static int n1; // size of X
  private static int n2; // size of Y
  private static double[][] W; //complete bipartite weight matrix

  private static double[] labelX; // labels in X
  private static double[] labelY; // labels in Y
  private static int[][] EQ; // equality subgraph

  // S is the set of nodes in X encountered in the Hungarian tree
  private static int[] S;

  // T is the set of nodes in Y encountered in the Hungarian tree
  // T[y]= preceding(y_j) in X in the Hungarian tree
  private static int[] T;

  private static double[] slack; // smallest slack for y in ~T

  private static int[] map, rmap; // map of X and Y

  private static int[] Z; // newly found nodes in Y

  /**
   * Maximum weight matching
   * @param _W complete bipartite weight matrix
   * @return map from X
   */
  public static int[] maximumWeightMatching(double[][] _W) {
    W = _W;
    n1 = W.length;
    n2 = W[0].length;
    assert (n1 <= n2);

    labelX = new double[n1];
    labelY = new double[n2];
    EQ = new int[n1][n2];
    S = new int[n1];
    T = new int[n2];
    Z = new int[n2];
    slack = new double[n2];

    // Initialize labels and equality subgraph
    Arrays.fill(labelY, 0); //labelY
    for (int x = 0; x < n1; x++) {
      labelX[x] = 0;
      for (int y = 0; y < n2; y++) { // labelX
        if (labelX[x] < W[x][y]) {
          labelX[x] = W[x][y];
        }
      }
      for (int y = 0; y < n2; y++) { // equality subgraph
        if (labelX[x] + labelY[y] == W[x][y]) {
          EQ[x][y] = 1;
        }
        else {
          EQ[x][y] = 0;
        }
      }
    }

    // Find a maximum cardinality matching in the equality subgraph
    map = new int[n1]; // map
    Arrays.fill(map, -1);
    HopcroftKarp.maximumMatching(EQ, n1, n2, map);

    int[] rmap = new int[n2]; //reverse map
    Arrays.fill(rmap, -1);
    for (int i = 0; i < n1; i++) {
      if (map[i] >= 0) {
        rmap[map[i]] = i;
      }
    }

    while (true) {
      // S = free nodes in X
      // T = adjacent(S)
      // Z = T
      Arrays.fill(S, 0);
      Arrays.fill(T, -1);
      int nZ = 0;
      int nFree = 0;
      for (int x = 0; x < n1; x++) {
        if (map[x] == -1) {
          S[x] = 1;
          nFree++;
          for (int y = 0; y < n2; y++) {
            if (EQ[x][y] == 1 && T[y] == -1) {
              T[y] = x;
              Z[nZ++] = y; // Z <-y, y adjacent to S
            }
          }
        }
      }

      if (nFree == 0) {
        break; // done
      }

      int y = GrowHungarianTree(nZ);

      // Augment path ended at y
      AugmentPath(y);
    }

    return map;
  }

  /**
   * Grow the Hungarian tree to search for an augmenting path
   * @param nZ |Z|
   * @return the end node in Y in the augmenting path
   */
  private static int GrowHungarianTree(int nZ) {
    boolean firstIteration = true;
    while (true) {
      // Starting of a phase
      while (nZ > 0) {
        int y = Z[--nZ];
        if (rmap[y] == -1) {
          return y; // found a free node in Y
        }
        else {
          int x = rmap[y]; // mate(y)
          S[x] = 1; // S <- x
          if (!firstIteration) {
            for (int y1 = 0; y1 < n2; y1++) {
              if (T[y1] == -1) {
                double temp = labelX[x] + labelY[y1] - W[x][y1];
                if (slack[y1] > temp) {
                  slack[y1] = temp;
                }
              }
            }
          } // end of if

          for (int y1 = 0; y1 < n2; y1++) {
            if (T[y1] == -1 && EQ[x][y1] == 1) {
              T[y1] = x; // T <- y1
              Z[nZ++] = y1; // Z <- y1
            }
          } // end of for

        } // end of else

      } // end of while nZ>0

      if (firstIteration) {
        firstIteration = false;
        Arrays.fill(slack, Double.MAX_VALUE);
        for (int y1 = 0; y1 < n2; y1++) {
          if (T[y1] == -1) {
            for (int x1 = 0; x1 < n1; x1++) {
              if (S[x1] == 1) {
                double temp = labelX[x1] + labelY[y1] - W[x1][y1];
                if (slack[y1] > temp) {
                  slack[y1] = temp;
                }
              }
            }
          }

        } // end of for y1

      } // end of if

      nZ = ReviseLabel();
      assert (nZ > 0);
    } // end of while

  }

  /**
   * Revise labels of X and Y
   * @return |Z|
   */
  private static int ReviseLabel() {
    // lambda = min{slack[y]}, y in ~T
    double lambda = Double.MAX_VALUE;
    for (int y = 0; y < n2; y++) {
      if (T[y] == -1) {
        if (lambda > slack[y]) {
          lambda = slack[y];
        }
      }
    }

    int nZ = 0;
    // revise S and T
    for (int x = 0; x < n1; x++) {
      if (S[x] == 1) {
        labelX[x] -= lambda;
      }
    }
    for (int y = 0; y < n2; y++) {
      if (T[y] >= 0) { // y in T
        labelY[y] += lambda;
      }
      else { // y in ~T
        slack[y] -= lambda;
        if (slack[y] == 0) {
          for (int x = 0; x < n1; x++) {
            if (S[x] == 1 &&
                Math.abs(labelX[x] + labelY[y] - W[x][y]) < 0.000001) {
              EQ[x][y] = 1;
              if (T[y] == -1) { // not yet processed
                T[y] = x; // T <- y
                Z[nZ++] = y; // Z <- y
              }
            }
          } // end of for
        }
      }

    } // end of for

    return nZ;
  }

  /**
   * Augment a path ended at y
   * @param y int
   */
  private static void AugmentPath(int y) {
    while (true) {
      int x = T[y];
      rmap[y] = x;
      if (map[x] == -1) {
        map[x] = y;
        break;
      }
      else {
        int y1 = map[x];
        map[x] = y;
        y = y1;
      }
    }
  }

  public static void main(String[] args) {
    //double[][] W = {{0,1,0},{1,0,0},{0,3,1}};
    double[][] W = { {1, 2, 3, 4, 5}
        , {6, 7, 8, 7, 2}
        , {1, 3, 4, 4, 5}
        , {3, 6, 2, 8, 7}
        , {4, 1, 3, 5, 4}
    }; // The maximum weight should be 28
    int[] map = maximumWeightMatching(W);
    double w = 0;
    for (int i = 0; i < map.length; i++) {
      assert (map[i] >= 0);
      w += W[i][map[i]];
      System.out.print(map[i] + " ");
    }
    System.out.println("\nw = " + w);
  }
}
