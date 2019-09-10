package grank.misc;

import java.io.*;
import java.util.*;
import grank.graph.*;
import ctree.util.*;

/**
 * Shrink a set of frequent subgraphs into closed subgraphs
 * The support is contained in the graph ID.
 * @author Huahai He
 * @version 1.0
 */
public class ClosedGraph {

  public static void main(String[] args) throws IOException {
    Opt opt = new Opt(args);
    if (opt.args() < 2) {
      System.err.println("Usage: ... [options] sig_file sig1_file");
      System.err.println("  -map_file=FILE \t default=label.map");
      System.exit(1);
    }

    String map_file = opt.getString("map_file", "label.map");
    String sig_file = opt.getArg(0);
    String sig1_file = opt.getArg(1);
    LGraph[] graphs = GraphFile.loadGraphs(sig_file, map_file);

    int nG = graphs.length;
    boolean[] mark = new boolean[nG];
    Arrays.fill(mark, false);
    int num = nG;
    for (int i = nG - 1; i > 0; i--) {
      if (mark[i]) {
        continue;
      }
      LGraph g1 = graphs[i];
      // Extract support from ID
      int sup1 = Integer.parseInt(g1.id.substring(g1.id.indexOf(' ') + 1));
      boolean flag = false;
      for (int j = i - 1; j >= 0; j--) {
        if (mark[j]) {
          continue;
        }
        LGraph g2 = graphs[j];
        int sup2 = Integer.parseInt(g2.id.substring(g2.id.indexOf(' ') + 1));
        // Check if g2 is sub-isomorphic to g1 and their supports are equal
        if (g2.E.length < g1.E.length) {
          if (sup2 == sup1 && SubgraphIsom.subIsom(g2, g1)) {
            mark[j] = true;
            num--;
          }
        }
        else if (g2.E.length > g1.E.length && !flag) {
          if (sup2 == sup1 && SubgraphIsom.subIsom(g1, g2)) {
            flag = true;
          }
        }
      }
      if (flag) {
        mark[i] = true;
        num--;
      }
    }
    LGraph[] graphs1 = new LGraph[num];
    int cnt = 0;
    for (int i = 0; i < nG; i++) {
      if (!mark[i]) {
        graphs1[cnt++] = graphs[i];
      }
    }
    GraphFile.saveGraphs(graphs1, sig1_file, map_file);
  }
}
