package grank.graph;

import java.util.*;

/**
 * Generate a fingerprint or a unique code for a labeled graph.
 *
 * @author Huahai He
 * @version 1.0
 */
class GraphCode {
  /**
   * Generate the fingerprint of a graph
   * @param g LGraph
   * @return ByteArray
   */
  public static ByteArray finger(LGraph g) {
    int n = g.V.length;
    int m = g.E.length;
    byte[] tmp1 = new byte[n + m];
    for (int i = 0; i < n; i++) {
      tmp1[i] = (byte) g.V[i];
    }
    for (int j = 0; j < m; j++) {
      tmp1[n + j] = (byte) g.E[j].label;
    }
    Arrays.sort(tmp1, 0, n);
    Arrays.sort(tmp1, n, n + m);
    return new ByteArray(tmp1);
  }

  /**
   * Generate a unique code for a graph
   * Format:
   * Sorted V in ascending order of labels then degrees;
   * Then E (v1,v2,label) where v1<=v2 in ascending order
   * The sequence is minimal under all permuations of V
   * @param g LGraph
   * @return ByteArray
   */
  public static ByteArray ucode(LGraph g) {

    int n = g.V.length;
    int m = g.E.length;
    int[] map = new int[n];
    for (int i = 0; i < n; i++) {
      map[i] = i;
    }

    // Degrees
    int[] deg = new int[n];
    Arrays.fill(deg, 0);
    for (int i = 0; i < m; i++) {
      deg[g.E[i].v1]++;
      deg[g.E[i].v2]++;
    }

    // Sort V by labels then by degrees in ascending order
    for (int i = 0; i < n - 1; i++) {
      int x = map[i];
      for (int j = i + 1; j < n; j++) {
        int y = map[j];
        if (g.V[y] < g.V[x] || (g.V[y] == g.V[x] && deg[y] < deg[x])) {
          int tmp = map[i];
          map[i] = map[j];
          map[j] = tmp;
          x = y;
        }
      }
    }
    int[] rmap = new int[n]; // reverse map
    for (int i = 0; i < n; i++) {
      rmap[map[i]] = i;
    }

    // Find partitions that tie
    int pnum = 0; // number of partitions
    int[] plow = new int[n / 2]; // lower of the partition, inclusive
    int[] pup = new int[n / 2]; // upper of the partition, inclusive
    int low = 0;
    while (low < n - 1) {
      int x = map[low];
      int up = low + 1;
      for (; up < n; up++) {
        int y = map[up];
        if (g.V[x] != g.V[y] || deg[x] != deg[y]) {
          break;
        }
      }
      if (up > low + 1) { // low~up-1 is a partition
        plow[pnum] = low;
        pup[pnum] = up - 1;
        pnum++;
      }
      low = up;
    }

    // Permutate on all partitions
    int[] rmap2 = new int[n];
    for (int i = 0; i < n; i++) {
      rmap2[i] = i;
    }
    boolean[] visited = new boolean[n];
    Arrays.fill(visited, false);
    int[] eseq = genESeq(g.E, rmap, rmap2);

    if (pnum > 0) {
      perm(g, rmap, plow, pup, pnum, 0, 0, rmap2, visited, eseq);
    }

    // Assume that |V|<65536 && |Labels|<65536
    assert (n < 65536);

    short[] array = new short[n + m * 3];
    for (int i = 0; i < n; i++) { // g.V
      array[rmap2[rmap[i]]] = (short) g.V[i];
    }
    for (int i = 0; i < m * 3; i++) { //g.E
      array[n + i] = (short) eseq[i];
    }
    ByteArray ucode = new ByteArray(array);
    return ucode;
  }

  /**
   * Permutation on all partitions
   * @param g LGraph
   * @param rmap int[]
   * @param plow Partition lower bounds
   * @param pup Partition upper bounds
   * @param pnum Number of partitions
   * @param pcnt The current partition
   * @param depth int
   * @param rmap2 int[]
   * @param visited boolean[]
   * @param minESeq int[]
   */
  private static void perm(LGraph g, int[] rmap, int[] plow, int[] pup,
                           int pnum, int pcnt,
                           int depth, int[] rmap2, boolean[] visited,
                           int[] minESeq) {
    int low = plow[pcnt];
    int up = pup[pcnt];
    if (depth > up - low) {   // end of permutation of a partition
      if (pcnt == pnum - 1) {   // end of a global permutation
        int[] eseq = genESeq(g.E, rmap, rmap2);
        boolean flag = false;
        for (int i = 0; i < eseq.length; i++) {  // compare eseq's
          if (eseq[i] > minESeq[i]) {
            break;
          }
          else if (eseq[i] < minESeq[i]) {
            flag = true;
            break;
          }
        }
        if (flag) {
          System.arraycopy(eseq, 0, minESeq, 0, eseq.length);
        }
      }
      else {  // permutation on the next partition
        perm(g, rmap, plow, pup, pnum, pcnt + 1, 0, rmap2, visited, minESeq);
      }
    }
    else { // continue permutation of the current partition
      for (int i = low; i <= up; i++) {
        if (!visited[i]) {
          visited[i] = true;
          rmap2[i] = low + depth;
          perm(g, rmap, plow, pup, pnum, pcnt, depth + 1, rmap2, visited,
               minESeq);
          visited[i] = false;
        }
      }
    }
  }

  /**
   * Generate the minimum E sequence for the given permutation.
   * E is sorted in ascending order.
   * An edge is in the form (v1,v2,label) where v1<=v2.
   * @param E LEdge[]
   * @param rmap int[]
   * @param rmap2 int[]
   * @return int[]
   */
  private static int[] genESeq(LEdge[] E, int[] rmap, int[] rmap2) {
    int m = E.length;
    int[] seq = new int[m * 3];

    // Copy E with mapped vertices
    for (int i = 0; i < m; i++) {
      LEdge e = E[i];
      int v1 = rmap2[rmap[e.v1]];
      int v2 = rmap2[rmap[e.v2]];
      if (v1 > v2) {
        int tmp = v1;
        v1 = v2;
        v2 = tmp;
      }
      seq[i * 3] = v1;
      seq[i * 3 + 1] = v2;
      seq[i * 3 + 2] = e.label;
    }

    // Sort E in ascending order
    for (int i = 0; i < m - 1; i++) {
      int x1 = seq[i * 3];
      int x2 = seq[i * 3 + 1];
      int x3 = seq[i * 3 + 2];
      for (int j = i + 1; j < m; j++) {
        int y1 = seq[j * 3];
        int y2 = seq[j * 3 + 1];
        int y3 = seq[j * 3 + 2];
        if (y1 < x1 || y1 == x1 && (y2 < x2 || y2 == x2 && (y3 < x3))) {
          seq[i * 3] = y1;
          seq[i * 3 + 1] = y2;
          seq[i * 3 + 2] = y3;
          seq[j * 3] = x1;
          seq[j * 3 + 1] = x2;
          seq[j * 3 + 2] = x3;
          x1 = y1;
          x2 = y2;
          x3 = y3;
        }
      }
    }

    return seq;
  }

  public static void main(String[] args) {
    /*int[] V = {1, 2, 3, 2};
         LEdge e01 = new LEdge(0, 3, 1);
         LEdge e02 = new LEdge(1, 2, 1);
         LEdge e12 = new LEdge(1, 3, 1);
         //LEdge e23 = new LEdge(2, 3, 1);
     */
    int[] V = {5, 0, 3, 3};
    LEdge e1 = new LEdge(0, 1, 1);
    LEdge e2 = new LEdge(0, 2, 0);
    LEdge e3 = new LEdge(0, 3, 1);
    LEdge[] E = {e1, e2, e3};
    LGraph g = new LGraph("test", V, E);
    System.out.println(g.ucode().toString());
  }
}
