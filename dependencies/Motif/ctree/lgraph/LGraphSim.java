package ctree.lgraph;

import java.util.*;

import ctree.graph.*;

import ctree.index.*;

/**
 *
 * @author Huahai He
 * @version 1.0
 */

public class LGraphSim implements GraphSim {
  private static final int VSIM = 1;
  private static final int ESIM = 1;

  public int norm(Graph g) {
    return VSIM * g.numV() + g.numE() * ESIM;
  }

  public int sim(Graph g1, Graph g2, int[] map) {
    int sim = 0;
    for (int i = 0; i < map.length; i++) {
      if (map[i] >= 0) {
        sim += VSIM;
      }

    }
    Edge[] E1 = g1.E();
    int[][] adj2 = g2.adjMatrix();
    for (int i = 0; i < E1.length; i++) {
      Edge e = E1[i];
      if (map[e.v1()] >= 0 && map[e.v2()] >= 0 && adj2[map[e.v1()]][map[e.v2()]] > 0) {
        sim += ESIM;
      }
    }
    return sim;
  }

  public int simLower(Graph g1, Graph g2) {
    int[][] B = Util.getBipartiteMatrix(g1, g2);
    int[] map = new int[g1.numV()];
    Arrays.fill(map, -1);
    ctree.alg.HopcroftKarp.maximumMatching(B, map);
    return sim(g1, g2, map);
  }

  public int simUpper(Graph g1, Graph g2) {
    LabelMap labelMap = new LabelMap();
    int[] vlabels1=labelMap.importGraph2((LGraph)g1);
    int[] vlabels2=labelMap.importGraph2((LGraph)g2);
    int L = labelMap.size();

    // histogram of vertices
    Vertex[] V1 = g1.V();
    int[] vhist = new int[L];
    Arrays.fill(vhist, 0);
    for (int i = 0; i < V1.length; i++) {
      vhist[vlabels1[i]]++;
    }
    // histogram of edges
    Edge[] E1 = g1.E();
    HashMap<Integer, Integer> edgeMap = new HashMap();
    int cnt = 0;
    int[] ehist = new int[E1.length];
    Arrays.fill(ehist,0);
    for (int i = 0; i < E1.length; i++) {
      int x1 = vlabels1[E1[i].v1()];
      int x2 = vlabels1[E1[i].v2()];
      if(x1>x2) {
        int temp=x1;
        x1=x2;
        x2=temp;
      }
      int x= (x1<<16)+x2;
      Integer I = edgeMap.get(x);
      if (I == null) {
        edgeMap.put(x, cnt);
        ehist[cnt] = 1;
        cnt++;
      }
      else {
        ehist[I.intValue()]++;
      }
    }

    int sim = 0;
    Vertex[] V2 = g2.V();
    for (int i = 0; i < V2.length; i++) {
      int x = vlabels2[i];
      if (vhist[x] > 0) {
        vhist[x]--;
        sim += VSIM;
      }
    }
    Edge[] E2 = g2.E();
    for (int i = 0; i < E2.length; i++) {
      int x1 = vlabels2[E2[i].v1()];
      int x2 = vlabels2[E2[i].v2()];
      if(x1>x2) {
        int temp=x1;
        x1=x2;
        x2=temp;
      }
      int x = (x1<<16)+x2;
      Integer I = edgeMap.get(x);
      if (I != null && ehist[I.intValue()] > 0) {
        ehist[I.intValue()]--;
        sim += ESIM;
      }
    }
    return sim;
  }

}
