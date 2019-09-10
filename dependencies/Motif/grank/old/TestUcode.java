package grank.old;

import java.util.*;
import grank.graph.*;

//Obsolete
public class TestUcode {

  public static void main(String[] args) throws Exception {
    LGraph[] D = GraphFile.loadGraphs(args[0], args[1]);

    Random rand = new Random(1);
    for (int i = 0; i < D.length; i++) {
      LGraph g = D[i];
      int n = g.V.length;

      for (int t = 0; t < 50; t++) {
        // random map
        int[] map = new int[n];
        Arrays.fill(map, -1);
        int cnt = 0;
        while (cnt < n) {
          int k;
          do {
            k = rand.nextInt(n);
          }
          while (map[k] >= 0);
          map[k] = cnt;
          cnt++;
        }

        // permutation on V
        int[] V1 = new int[n];
        for (int j = 0; j < n; j++) {
          V1[map[j]] = g.V[j];
        }
        int m = g.E.length;
        LEdge[] E1 = new LEdge[m];
        Arrays.fill(E1, null);
        for (int j = 0; j < m; j++) {
          int k;
          do {
            k = rand.nextInt(m);
          }
          while (E1[k] != null);
          LEdge e = g.E[j];
          LEdge e1 = new LEdge(map[e.v1], map[e.v2], e.label);
          E1[k] = e1;
        }
        LGraph g1 = new LGraph("", V1, E1);
        if (g.id.equals("B0")) {
          int test = 0;
        }
        if (!g.equals(g1)) {
          System.err.println(g.id);
          break;
        }
      }
    }
  }
}
