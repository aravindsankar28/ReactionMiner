package grank.mine;

import java.io.*;

import grank.graph.*;
import grank.transform.*;
import ctree.util.*;

/**
 * Sort prob. file in non-decreasing order.
 * Sort feature file and histogram file as well.
 * @author Huahai He
 * @version 1.0
 */
public class SortProb {

  public static void main(String[] args) throws IOException {
    Opt opt = new Opt(args);
    if (opt.args() < 3) {
      System.err.println("Usage: ... prob_file basis_file hist_file");
      System.err.println("  -map_file=FILE \t default=label.map");
      System.err.println("  -prob_out=FILE \t prob output, default=input");
      System.err.println("  -fea_out=FILE \t fea output, default=input");
      System.err.println("  -hist_out=FILE \t hist output, default=input");
      System.err.println(
          "  -order=+|- \t Ascendant or descendant order, default=+");
      System.exit(0);
    }

    // Input files
    String map_file = opt.getString("map_file", "label.map");
    String prob_file = opt.getArg(0);
    String basis_file = opt.getArg(1);
    String hist_file = opt.getArg(2);

    // Output files, by default, same as input files
    String prob_out = opt.getString("prob_out", prob_file);
    String fea_out = opt.getString("fea_out", basis_file);
    String hist_out = opt.getString("hist_out", hist_file);
    String orderopt = opt.getString("order", "+");
    boolean order = true;
    if (orderopt.equals("-")) {
      order = false;
    }

    // Sort prob and get map, map[i] is the index of the i^th smallest prob
    double[] prob = BasisProb.loadProb(prob_file);
    int m = prob.length;
    int[] map = new int[m];
    for (int i = 0; i < m; i++) {
      map[i] = i;
    }

    for (int i = 0; i < m - 1; i++) {
      for (int j = i + 1; j < m; j++) {
        if ( (order && prob[j] < prob[i]) || (!order && prob[j] > prob[i])) {
          double tmp = prob[i];
          prob[i] = prob[j];
          prob[j] = tmp;
          int idx = map[i];
          map[i] = map[j];
          map[j] = idx;
        }
      }
    }
    BasisProb.saveProb(prob, prob_out);

    // Feature set
    LGraph[] F = GraphFile.loadGraphs(basis_file, map_file);
    LGraph[] F1 = new LGraph[F.length];
    for (int i = 0; i < F.length; i++) {
      F1[i] = F[map[i]];
    }
    GraphFile.saveGraphs(F1, fea_out, map_file);

    // Histograms
    Hist[] H = Hist.loadHists(hist_file);
    for (int i = 0; i < H.length; i++) {
      int[] h = new int[m];
      for (int j = 0; j < m; j++) {
        h[j] = H[i].hist[map[j]];
      }
      H[i].hist = h;
    }
    Hist.saveHists(H, hist_out);

  }
}
