package grank.pvalue;

import java.io.*;
import java.util.*;

import grank.graph.*;
import ctree.util.*;
import grank.misc.*;

/**
 * Reduce the rankings, i.e., remove all g2 where rank(g2)<rank(g1) and
 * g2 \subseteq g1.
 *
 * @author Huahai He
 * @version 1.0
 */
public class ReduceRank {

  public static void main(String[] args) throws IOException {
    Opt opt = new Opt(args);
    if (opt.args() < 2) {
      System.err.println("Usage: ... [-option] sig_file rank_file");
      System.err.println("  -map_file=FILE \t default=label.map");
      System.err.println("  -closed \t Reduce non-closed graphs only");
      System.err.println("  -shrink \t Shrink the rankings");
      System.exit(0);
    }
    String map_file = opt.getString("map_file", "label.map");
    String sig_file = opt.getArg(0);
    String rank_file = opt.getArg(1);
    boolean closed = opt.hasOpt("closed");
    boolean shrink = opt.hasOpt("shrink");

    LGraph[] sig = GraphFile.loadGraphs(sig_file, map_file);
    HashMap<String, LGraph> sig2Map = RecallSigGraph.mapGraphs(sig);

    REntry2[] ranks = RecallSigGraph.loadRankings(rank_file);

    // Get the ranked graphs
    LGraph[] rankedGraphs = new LGraph[ranks.length];
    for (int i = 0; i < ranks.length; i++) {
      LGraph g = sig2Map.get(ranks[i].id);
      assert (g != null);
      rankedGraphs[i] = g;
    }

    // Remove all g2's where rank(g2)<rank(g1) and g2 \subseteq g1
    boolean[] mark = new boolean[ranks.length];
    Arrays.fill(mark, false);
    for (int i = 0; i < ranks.length - 1; i++) {
      if (mark[i]) {
        continue;
      }
      LGraph g = rankedGraphs[i];
      for (int j = i + 1; j < ranks.length; j++) {
        if (mark[j]) {
          continue;
        }
        LGraph g2 = rankedGraphs[j];
        if (SubgraphIsom.subIsom(g2, g)) {
          if (!closed || closed && ranks[j].graphMu0 == ranks[i].graphMu0) {
            mark[j] = true;
          }
        }
      }
    }

    // Remove all g2's where rank(g2)<rank(g1) and g2 \supseteq g1
    /*
     for (int i = 0; i < ranks.length - 1; i++) {
      if (mark[i]) {
        continue;
      }
      LGraph g = rankedGraphs[i];
      for (int j = i + 1; j < ranks.length; j++) {
        if (mark[j]) {
          continue;
        }
        LGraph g2 = rankedGraphs[j];
        if (Ullmann.subIsom(g, g2)) {
          mark[j] = true;
        }
      }
         }
    */

    // Output reduced rankings
    int cnt=0;
    for (int i = 0; i < ranks.length; i++) {
      if (!mark[i]) {
        System.out.printf("%d %s %g %d %d %f\n", shrink?cnt:i, ranks[i].id,
                          ranks[i].pvalue, ranks[i].histMu0, ranks[i].graphMu0,
                          ranks[i].mean);
        cnt++;
      }
    }

  }
}
