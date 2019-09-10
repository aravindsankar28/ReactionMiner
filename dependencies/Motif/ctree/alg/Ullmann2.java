package ctree.alg;

import java.util.*;
import ctree.graph.*;

/**
 * Ullmann's algorithm for subgraph isomorphism test.
 * Similiar to Ullmann except assuming attributes on edges.
 * Reference: J. R. Ullmann, "An Algorithm for Subgraph Isomorphism", J of ACM, 1976
 *
 * @author Huahai He
 * @version 1.0
 */

public class Ullmann2 {

  // common used variables
  private static int n1; // number of vertices in graph 1
  private static int n2; // number of vertices in graph 2
  private static Edge[][] adj1; // adjacency matrix of graph 1
  private static Edge[][] adj2; // adjacency matrix of graph 2
  private static int[][] M; // possible mapping matrix
  private static int[] map; // current mapping in state search space
  private static int[] rmap; // reverse map

  /**
   *
   * @param _adj1 adjacency matrix of graph 1
   * @param _adj2 adjacency matrix of graph 2
   * @param _M possible mapping matrix of graph 1 to graph 2.
   *           I.e., if M[i][j]==1 then i can be mapped to j
   * @return an isomorphic mapping from graph 1 to graph 2 or null
   */
  public static int[] subgraphIsomorphism(Edge[][] _adj1, Edge[][] _adj2,
                                          int[][] _M) {
    adj1 = _adj1;
    adj2 = _adj2;
    M = _M;
    n1 = adj1.length;
    n2 = adj2.length;
    if(n1>n2) return null;
    assert (M.length == n1 && M[0].length == n2);
    map = new int[n1];
    rmap = new int[n2];
    Arrays.fill(map, -1);
    Arrays.fill(rmap, -1);
    if (stateSearch(0)) {
      return map;
    } else {
      return null;
    }
  }

  /**
   * @param depth search depth. It is also the index of the active vertex in graph 1
   * @return boolean
   */
  private static boolean stateSearch(int depth) {
    // backup the current row
    int[] backupRow = new int[n2];
    System.arraycopy(M[depth], 0, backupRow, 0, n2);
    Arrays.fill(M[depth], 0); // only one cell in a row can be 1
    for (int v = 0; v < n2; v++) {
      if (backupRow[v] > 0 && rmap[v] == -1) {
        if (checkMapping(depth, v)) { // check if depth can be mapped to v
          // map depth to v
          map[depth] = v;
          rmap[v] = depth;
          M[depth][v] = 1;
          if (depth == n1 - 1) {
            return true;
          } else if (stateSearch(depth + 1)) {
            return true;
          }
          // restore mapping
          map[depth] = -1;
          rmap[v] = -1;
          M[depth][v] = 0;
        }
      }
    }
    //restore row at depth
    System.arraycopy(backupRow, 0, M[depth], 0, n2);
    return false;
  }

  /**
   * Check if vertex at depth can be mapped to v
   * @param depth int
   * @param v int
   * @return boolean
   */
  private static boolean checkMapping(int depth, int v) {
    for (int j = 0; j < n1; j++) {
      // if j is adjacent to depth, then
      //   if j is mapped to j', then j' must be adjacent to v
      //   else there must exists a possible mapping j=>l, l is adjacent to v
      if (adj1[depth][j] !=null) {
        if (map[j] != -1) {
          if (adj2[v][map[j]] == null || !adj1[depth][j].mappable(adj2[v][map[j]])) {
            return false;
          }
        } else {
          boolean flag = false;
          for (int l = 0; l < n2; l++) {
            if (M[j][l] == 1 && rmap[l]==-1&& adj2[v][l]!=null && adj1[depth][j].mappable(adj2[v][l]) ) {
              flag = true;
              break;
            }
          }
          if (flag == false) {
            return false;
          }
        }
      }
    }
    return true;
  }

  public static void main(String[] args) {

    class LabeledEdge implements Edge {
      int label;
      public LabeledEdge(int _label) {
        label=_label;
      }
      public boolean mappable(Edge e) {
        return ((LabeledEdge)e).label==label;
      }
      public int v1() {return -1;}
      public int v2() {return -1;}
	@Override
	public int w() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public String stereo() {
		// TODO Auto-generated method stub
		return "";
	}
    }

    LabeledEdge e1=new LabeledEdge(1);
    LabeledEdge e2=new LabeledEdge(2);
    Edge[][] adj1 = { {null, e1, e2}
                   , {e1, null, e1}
                   , {e2, e1, null}
    };
    Edge[][] adj2 = { {null, e1, null, null}
                   , {e1, null, e1, e2}
                   , {null, e1, null, e1}
                   , {null, e2, e1, null}
    };
    int[][] M = { {0, 1, 1, 1}
                , {0, 1, 1, 1}
                , {0, 1, 1, 1}
    };

    // a simple test
    int[] map = subgraphIsomorphism(adj1, adj2, M);
    if (map == null) {
      System.out.println("Not matched");
    } else {
      System.out.print("map = ");
      for (int i = 0; i < map.length; i++) {
        System.out.print(map[i] + " ");
      }
      System.out.println();
    }
  }
}
