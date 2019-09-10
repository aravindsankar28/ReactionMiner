package ctree.lgraph;

import java.util.*;
import ctree.graph.*;
import ctree.index.*;
import ctree.mapper.*;


/**
 * <p> Closure-tree</p>
 *
 * @author Huahai He
 * @version 1.0
 */
public class LGraphWeightMatrix implements WeightMatrix {
  public LGraphWeightMatrix() {
  }

  /**
   * weightMatrix
   *
   * @param g1 Graph
   * @param g2 Graph
   * @return double[][]
   */
  public double[][] weightMatrix(Graph g1, Graph g2) {
    int n1 = g1.numV();
    int n2 = g2.numV();

    // prepare data to compute the initial weight matrix W
    int[][] alist1 = g1.adjList();
    int[][] alist2 = g2.adjList();
    LabelMap labelMap = new LabelMap();
    int[] VL1 = labelMap.importGraph2((LGraph)g1);
    int[] VL2 = labelMap.importGraph2((LGraph)g2);
    int L = labelMap.size();
    int[][] llist1 = labelList(alist1, VL1, L);
    int[][] llist2 = labelList(alist2, VL2, L);
    double[][] W = new double[n1][n2];

    // compute W
    int c=0;
    for (int u = 0; u < n1; u++) {
      Arrays.fill(W[u], 0);
      for (int v = 0; v < n2; v++) {
        if (VL1[u] == VL2[v]) {
        	c++;
          int temp1 = intersection(llist1[u], llist2[v]);
          int temp2 = union(llist1[u], llist2[v]);
          if (temp2 == 0) {
            W[u][v] = 1;
          }
          else {
            W[u][v] = 1 + (double) (temp1 * temp1) / temp2;
          }
        }
      }
    }
    // Note commented here
    //System.out.println(c);
    return W;
  }

  private static int[][] labelList(int[][] alist, int[] vlabels, int L) {
    int[][] llist = new int[alist.length][L];
    for (int i = 0; i < alist.length; i++) {
      Arrays.fill(llist[i], 0);
      for (int j = 0; j < alist[i].length; j++) {
        int v = alist[i][j]; // v is a neighbor of i
        int l = vlabels[v]; // l is the label index of v
        llist[i][l]++;
      }
    }
    return llist;
  }

  private static int intersection(int[] list1, int[] list2) {
    int sum = 0;
    for (int i = 0; i < list1.length; i++) {
      sum += Math.min(list1[i], list2[i]);
    }
    return sum;
  }

  private static int union(int[] list1, int[] list2) {
    int sum = 0;
    for (int i = 0; i < list1.length; i++) {
      sum += Math.max(list1[i], list2[i]);
    }
    return sum;
  }

}
