package grank.old;



import java.io.*;
import java.util.*;
import grank.graph.*;
import ctree.util.*;

import grank.transform.*;

/**
 * Transform graphs into feature vectors, i.e., histograms of a given feature set.
 * Same as Graph2Hist, but use EnumFeatures2, i.e., each subgraph has diameter of 2.
 * @author Huahai He
 * @version 1.0
 */
public class Graph2Hist2 {

  /*
   public static Hist[] graphs2hists(HashMap<LGraph, Integer> pcMap, LGraph[] D,
                                    int zB) {
    Hist[] hists = new Hist[D.length];
    for (int i = 0; i < D.length; i++) {
      hists[i] = graph2Hist(pcMap, D[i], zB);
    }
    return hists;
  }
  */

  public static void graphs2hists(HashMap<LGraph, Integer> pcMap, LGraph[] D,
                                   PrintStream out) {
    for (int i = 0; i < D.length; i++) {
      //if(i%100==0) System.err.println(i);
      Hist h = graph2Hist(pcMap, D[i]);
      out.println(h.toString());
    }
  }

  /**
   * Transform a graph into a feature vector.
   * The graph is enumerated and all features of the given size are generated.
   * The features are compared with the feature set to obtain their indices,
   * then the histogram is generated.
   * @param pcMap HashMap
   * @param g LGraph
   * @param zB int
   * @return FeatureVector
   */
  public static Hist graph2Hist(HashMap<LGraph, Integer> pcMap, LGraph g) {
    Vector<LGraph> F = GenPC2.genFeature(g);
    int m = pcMap.size();
    int[] hist = new int[m];
    Arrays.fill(hist, 0);
    for (LGraph f : F) {
      Integer idx = pcMap.get(f);
      if (idx != null) {
        assert (idx < m);
        hist[idx]++;
      }
    }
    return new Hist(g.id, hist);
  }

  /**
   * Load and index a feature set
   * @param basis_file String
   * @param map_file String
   * @return HashMap
   * @throws IOException
   */
  public static HashMap<LGraph, Integer> loadFeatures(String basis_file,
      String map_file) throws IOException {
    LGraph[] features = GraphFile.loadGraphs(basis_file, map_file);
    HashMap<LGraph, Integer> pcMap = new HashMap<LGraph, Integer> ();
    int fcnt = 0;
    for (int i = 0; i < features.length; i++) {
      if (!pcMap.containsKey(features[i])) {
        pcMap.put(features[i], fcnt);
        fcnt++;
      }
    }
    return pcMap;
  }

  public static void main(String[] args) throws IOException {
    Opt opt = new Opt(args);
    if (opt.args() < 3) {
      System.err.println("Usage: ... [options] graph_file basis_file hist_file");
      System.err.println("  -map_file=FILE \t default=label.map");
      System.exit(0);
    }

    String map_file = opt.getString("map_file", "label.map");
    String graph_file = opt.getArg(0);
    String basis_file = opt.getArg(1);
    String hist_file = opt.getArg(2);
    LGraph[] graphs = GraphFile.loadGraphs(graph_file, map_file);

    HashMap<LGraph, Integer> pcMap = loadFeatures(basis_file, map_file);

    //Hist[] hists = graphs2hists(pcMap, graphs, zB);
    PrintStream out = new PrintStream(hist_file);

    /*for (Hist I : hists) {
      out.println(I.toString());
         }*/
    graphs2hists(pcMap, graphs, out);
    out.close();
  }
}
