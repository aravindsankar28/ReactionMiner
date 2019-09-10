package grank.misc;

import java.io.*;
import java.util.*;
import grank.graph.*;
import ctree.util.*;
import grank.pvalue.*;


/**
 * Verify how many real significant subgraphs have been discovered.
 * It is used for synthetic graph experiments.
 *
 * @author Huahai He
 * @version 1.0
 */
public class RecallSigGraph {

  public static void main(String[] args) throws IOException {
    Opt opt = new Opt(args);
    if (opt.args() < 3) {
      System.err.println("Usage: ... [-option] sig_file1 sig_file2 rank_file");
      System.err.println("  -map_file=FILE \t default=label.map");
      System.exit(0);
    }
    String map_file = opt.getString("map_file", "label.map");
    String sig_file1 = opt.getArg(0);
    String sig_file2 = opt.getArg(1);
    String rank_file = opt.getArg(2);

    // Read significant subgraphs
    LGraph[] sig1 = GraphFile.loadGraphs(sig_file1, map_file);
    LGraph[] sig2 = GraphFile.loadGraphs(sig_file2, map_file);
    HashMap<String, LGraph> sig2Map = mapGraphs(sig2);

    // read the ranking results
    REntry2[] ranks = loadRankings(rank_file);

    // Get the ranked graphs
    LGraph[] rankedGraphs = new LGraph[ranks.length];
    for (int i = 0; i < ranks.length; i++) {
      LGraph g = sig2Map.get(ranks[i].id);
      assert (g != null);
      rankedGraphs[i] = g;
    }

    // Verify sig1 and sig2
    for (int j = 0; j < rankedGraphs.length; j++) {
      LGraph g2 = rankedGraphs[j];
      for (int i = 0; i < sig1.length; i++) {
        LGraph g1 = sig1[i];
        assert (g1 != null && g2 != null);
        if (g1.V.length == g2.V.length
            && g1.E.length == g2.E.length
            && g1.equals(g2)) {
          System.out.println(j); // print the rank
          break;
        }
      }
    }
  }

  // Map graph id to graph
  public static HashMap<String, LGraph> mapGraphs(LGraph[] sig) {
    HashMap<String, LGraph> sig2Map = new HashMap<String, LGraph> ();
    for (int i = 0; i < sig.length; i++) {
      LGraph g2 = sig[i];
      String id = g2.id.substring(0, g2.id.indexOf(','));
      assert (!sig2Map.containsKey(id));
      sig2Map.put(id, g2);
    }
    return sig2Map;
  }

  // Load ranking results
  public static REntry2[] loadRankings(String rank_file) throws IOException {
    // read the ranking results
    Vector<REntry2> ranks = new Vector<REntry2> ();
    BufferedReader in = new BufferedReader(new FileReader(rank_file));

    while (true) {
      String line = in.readLine();
      if (line == null) {
        break;
      }
      String[] list = line.split(" +");
      String id = list[1];
      double pvalue = Double.parseDouble(list[2]);
      int histMu0 = Integer.parseInt(list[3]);
      int graphMu0 = Integer.parseInt(list[4]);
      double mean = Double.parseDouble(list[5]);
      int hsize=Integer.parseInt(list[6]);
      REntry2 entry = new REntry2(id, pvalue, histMu0, graphMu0, mean,hsize);
      ranks.add(entry);
    }
    REntry2[] array = new REntry2[ranks.size()];
    ranks.toArray(array);
    return array;
  }
}
