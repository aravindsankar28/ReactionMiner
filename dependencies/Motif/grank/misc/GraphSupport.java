package grank.misc;

import grank.graph.*;
import java.io.*;
import ctree.util.*;

/**
 * Given a graph dataset D and a subgraph dataset A, compute the support of A in D.
 * @author Huahai He
 * @version 1.0
 */
public class GraphSupport {

  public static void main(String[] args) throws IOException {
    Opt opt = new Opt(args);
    if (opt.args() < 3) {
      System.err.println(
          "Usage: ... [options] graph_file subgraph_file output_file");
      System.err.println("  -map_file=FILE \t default=label.map");
      System.exit(1);
    }
    String graph_file = opt.getArg(0);
    String subgraph_file = opt.getArg(1);
    String output_file = opt.getArg(2);
    String map_file = opt.getString("map_file", "label.map");
    LabelMap map = new LabelMap(map_file);
    LGraph[] D = GraphFile.loadGraphs(graph_file, map);
    LGraph[] A = GraphFile.loadGraphs(subgraph_file, map);

    for (int i = 0; i < A.length; i++) {
      // Count support
      int cnt = 0;
      if (i % 100 == 0) {
        System.err.println(i);
      }
      for (int j = 0; j < D.length; j++) {
        if (A[i].E.length <= D[j].E.length && SubgraphIsom.subIsom(A[i], D[j])) {
          cnt++;
        }
      }

      // Modify the graph support in the subgraph ID
      String id = A[i].id;
      int idx = id.indexOf(',');
      String id1;
      if (idx >= 0) {
        id1 = id.substring(0, idx) + ", " + cnt;
      }
      else {
        id1 = id + ", " + cnt;
      }
      A[i].id = id1;
    }
    GraphFile.saveGraphs(A, output_file, map_file);
  }
}
