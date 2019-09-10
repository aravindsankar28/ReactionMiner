package grank.graph;

import java.util.*;


/**
 * Labeled graph specific for gmine.
 * Both vertices and edges have labels.
 * Labels start at 0.
 * In the adjacency matrix, each entry is the edge_label + 1
 * or 0 if there is no edge.
 *
 * @author Huahai He
 * @version 1.0
 */
public class LGraph {
  public String id;
  public int[] V;
  public LEdge[] E;

  // These are computed just once
  private ByteArray finger = null; // finger code
  private ByteArray ucode = null; // unique code
  private int[][] adj = null; // adjacency matrix

  public LGraph(String _id, int[] _V, LEdge[] _E) {
    id = _id;
    V = _V;
    E = _E;
  }

  /**
   * Adjacency matrix where each entry is the edge_label+1
   * or 0 if there is no edge.
   * @return int[][]
   */
  public int[][] adjmatrix() {
    if (adj != null) {
      return adj;
    }
    int n = V.length;
    int[][] matrix = new int[n][n];
    for (int[] row : matrix) {
      Arrays.fill(row, 0);
    }
    for (LEdge e : E) {
      matrix[e.v1][e.v2] = e.label+1;
      matrix[e.v2][e.v1] = e.label+1;
    }
    adj = matrix;
    return matrix;
  }

  /**
   * Fingerprint of the graph
   * @return ByteArray
   */
  public ByteArray finger() {
    if (finger != null) {
      return finger;
    }
    else {
      finger = GraphCode.finger(this);
      return finger;
    }
  }

  /**
   * Generate unique code for a graph
   * Format:
   * Sorted V in ascending order of labels then degrees;
   * Then E (v1,v2,label) where v1<=v2;
   * The sequence is minimal under all permuations of V subject to the order
   * @return ByteArray
   */
  public ByteArray ucode() {
    if (ucode != null) {
      return ucode;
    }
    else {
      ucode = GraphCode.ucode(this);
      return ucode;
    }
  }

  public int hashCode() {
    return ucode().hashCode();
  }

  public boolean equals(Object o) {
    LGraph g1=(LGraph)o;
    return ucode().equals(g1.ucode());
    //return E.length==g1.E.length&&Ullmann.subIsom(this,g1);
  }

}
