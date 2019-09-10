package grank.old;

import java.io.*;
import java.util.*;

import org.apache.commons.math.*;
import grank.graph.*;
import grank.transform.*;
import grank.pvalue.*;
import grank.mine.*;
import ctree.util.*;

/**
 * HistMine: Histogram mining
 *
 * Assuming p_1>=p_2>=...>=p_m
 *
 * HistMine5: lexicographical, depth-first, bottom up, flexible size,
 *             Enumerate trees and prune graph extension.
 *
 * @author Huahai He
 * @version 1.0
 */
public class HistMine5 {


  private static long cnt1 = 0; // count of accurate pvalue computations
  private static long cnt2 = 0; // count of fast pvalue pre-computations
  /**
   * GraphRank - the top level of the algorithm
   */
  private static void visit(int[] node, int pos0, int z, Vector < int[] > base,
                            int depth, Environment env) throws MathException {
    // compute pvalue

    eval(node, z, base.size(), env);

    // future lower bound


    Vector<int[]> base1 = new Vector<int[]> (base.size());
    int[] promote = new int[env.m];
    for (int pos = pos0; pos < env.m; pos++) {
      //for (int pos = m-1;pos>=pos0;pos--) {
      if (env.fbin[pos] == false) { // fbin: frequent bin
        continue;
      }
      int ground = node[pos]; // minimum value at pos
      Arrays.fill(promote, Integer.MAX_VALUE); // next minimum value at pos
      base1.clear();
      for (int i = 0; i < base.size(); i++) {
        int[] h = base.elementAt(i);
        if (h[pos] > ground) {
          base1.add(h);
          for (int j = 0; j < env.m; j++) {
            if (h[j] < promote[j]) {
              promote[j] = h[j];
            }
          }
        }
      }
      if (base1.size() < env.minSup) { // constraint of support
        continue;
      }
      boolean dup_flag = false;
      for (int j = 0; j < pos; j++) {
        if (promote[j] > node[j]) {
          dup_flag = true;
          break;
        }
      }
      if (dup_flag) {
        continue;
      }

      //node[pos] = promote; // step to the next minimum value at pos, support strictly decreases
      visit(promote, pos, PValue.sum(promote), base1, depth + 1, env);
      //node[pos] = ground;
    }

  }



  public static void eval(int[] node, int z, int sup,
                          Environment env) throws MathException {
    if (z > env.hZ) {
      return;
    }
    //double[] probs = PValue.probSubsetRecursiveArray(p, node, z, maxZ);

    // fast lower bound of pvalue
    double[] probs = new double[env.maxZ + 1];
    Arrays.fill(probs, 0, z, 0);
    for (int s = z; s <= env.maxZ; s++) {
      probs[s] = PValue.lowerProb(env.p, node, s);
    }
    double pvalue = PValue.computePvalue(probs, env.dbZ, env.dbN, env.nG, sup);

    boolean flag = false; // true if this candidate is in top-K
    if (pvalue < env.maxPvalue) {
      // accurate pvalue
      probs = PValue.probSubsetRecursiveArray(env.p, node, z, env.maxZ);
      pvalue = PValue.computePvalue(probs, env.dbZ, env.dbN, env.nG, sup);

      // add this node to answers
      if (pvalue <= env.maxPvalue) {
        int[] h = new int[env.m];
        System.arraycopy(node, 0, h, 0, env.m);
        Answer a = new Answer(h, sup, pvalue);
        env.ans.add(a);
        if (env.ans.size() > env.K) {
          env.ans.poll();
          env.maxPvalue = env.ans.peek().pvalue;
          System.err.printf("pvalue=%g, sup=%d, z=%d\n ", env.maxPvalue, sup, z);
        }
        flag = true;
        cnt1++;
      }
    }
    else {
      cnt2++;
    }
  }

  public static Hist[] graphRank5(double maxPvalue, int K, int minSup,
                                  int[][] H, LGraph[] graphs, double[] p,
                                  int hZ, int zB,
                                  HashMap<LGraph, Integer> pcMap, int VL,
      int EL) throws
      MathException {
    int m = H[0].length;
    int nG = H.length;

    // frequency of each bin
    int fcnt = 0;
    boolean[] fbin = new boolean[m]; // true if fbin[i] is frequent
    for (int i = 0; i < m; i++) {
      int cnt = 0;
      for (int j = 0; j < nG; j++) {
        if (H[j][i] > 0) {
          cnt++;
        }
      }
      if (cnt >= minSup) {
        fcnt++;
        fbin[i] = true;
        System.err.printf("%d ", cnt);
      }
      else {
        fbin[i] = false;
      }
    }
    System.err.printf("\nFrequent bins: %d\n", fcnt);

    // Get dbZ, dbN, maxZ
    int[] dbsizes = new int[nG];
    for (int i = 0; i < nG; i++) {
      dbsizes[i] = PValue.sum(H[i]);
    }
    int[][] tmp = PValue.dbSizes(dbsizes);
    int[] dbZ = tmp[0];
    int[] dbN = tmp[1];
    int maxZ = 0;
    for (int Z : dbZ) {
      if (Z > maxZ) {
        maxZ = Z;
      }
    }

    TreeGrow.initLinks(graphs, VL, EL, minSup);

    // prepare parameters
    int[] root = new int[m];
    Arrays.fill(root, 0);
    Vector<Integer> base0 = new Vector<Integer> ();
    for (int i = 0; i < nG; i++) {
      base0.add(i);
    }

    // answer set stored in a priority queue, reverse order, at most K answers
    PriorityQueue<Answer>
        ans = new PriorityQueue<Answer> (K);
    Environment5 env = new Environment5(m, p, maxZ, H, nG, dbZ, dbN, maxPvalue,
                                        minSup, K, hZ, ans, fbin, graphs,
                                        zB, pcMap);

    //visit(root, 0, 0, base0, 0, env);
    for (int vlab = 0; vlab < VL; vlab++) {
      if (TreeGrow.links[vlab].length == 0) {
        continue;
      }
      Tree tree0 = new Tree(vlab);
      TreeGrow.grow(tree0, root, base0, env);
    }

    // output
    Hist[] results = new Hist[env.ans.size()];
    for (int i = 0; i < results.length; i++) {
      Answer a = env.ans.poll();
      int size = PValue.sum(a.hist);
      int i1 = results.length - i - 1;
      String id = size + "-" + i1 + "," + a.sup + "," + a.pvalue;
      Hist h = new Hist(id, a.hist);
      results[i1] = h;
    }
    return results;
  }

  public static void main(String[] args) throws IOException, MathException {
    Opt opt = new Opt(args);
    if (opt.args() < 4) {
      System.err.println(
          "Usage: ... [options] hist_file prob_file basis_file graph_file");
      System.err.println("  -pvalue=DOUBLE \t Maximum p-value, default=1");
      System.err.println(
          "  -K=NUMBER \t\t Top-K significant subgraphs, default=MAX_INT");
      System.err.println("  -hZ=NUMBER \t\t Maximum sub-histogram size, default=maximum database histogram size");
      System.err.println(
          "  -hz=NUMBER \t\t Minimum sub-histogram size, default=1");
      System.err.println(
          "  -minSup=NUMBER[%] \t Minimum support, either number or percentage, default=1");
      System.err.println("  -mu0=[graph|hist] \t Use graphMu0 or histMu0 as the real support, default=graph");
      System.err.println(
          "  -sig_hist=FILE \t Significant histograms file, default=sig.hist");
      System.err.println("  -map_file=FILE \t default=label.map");
      System.err.println("  -zB=NUMBER \t Size of PCs, default=3");
      System.exit(0);
    }

    double maxPvalue = opt.getDouble("pvalue", 1);
    int K = opt.getInt("K", Integer.MAX_VALUE);
    int hZ = opt.getInt("hZ", Integer.MAX_VALUE);
    int hz = opt.getInt("hz", 1);
    boolean mu0Flag = true; // if true, then use graphMu0, o/w use histMu0
    if (opt.hasOpt("mu0") && opt.getString("mu0").equals("hist")) {
      mu0Flag = false;
    }

    Hist[] DB = Hist.loadHists(opt.getArg(0)); // database histograms
    int[][] H = new int[DB.length][DB[0].hist.length];
    for (int i = 0; i < H.length; i++) {
      H[i] = DB[i].hist;
    }

    // minimum support, either integer or percentage
    String tmp = opt.getString("minSup", "1");
    int minSup;
    if (tmp.endsWith("%")) {
      double ratio = Double.parseDouble(tmp.substring(0, tmp.length() - 1));
      minSup = (int) Math.ceil(DB.length * ratio / 100);
    }
    else {
      minSup = Integer.parseInt(tmp);
    }

    int maxZ = Hist.maxSize(DB); // maximum histogram size
    if (hZ > maxZ) {
      hZ = maxZ;
    }

    double[] p = BasisProb.loadProb(opt.getArg(1)); // feature probabilities
    /*
     for (int i = 0; i < p.length - 1; i++) {
      if (p[i] < p[i + 1]) {
        throw new RuntimeException("Assertion error: prob. are not sorted");
      }
         }
     */
    String basis_file = opt.getArg(2);
    String map_file = opt.getString("map_file", "label.map");
    int zB = opt.getInt("zB", 3);
    HashMap<LGraph, Integer>
        pcMap = Graph2Hist.loadBasis(basis_file, map_file);

    String graph_file = opt.getArg(3);
    LGraph[] graphs = GraphFile.loadGraphs(graph_file, map_file);
    LabelMap labelMap = new LabelMap(map_file);
    int VL = labelMap.vlab.length;
    int EL = labelMap.elab.length;

    long time0 = System.currentTimeMillis();

    Hist[] results = graphRank5(maxPvalue, K, minSup, H, graphs, p, hZ, zB,
                                pcMap, VL, EL);

    String sig_hist = opt.getString("sig_hist", "sig.hist");
    Hist.saveHists(results, sig_hist);

    long time1 = System.currentTimeMillis();
    System.err.printf("Time: %.2f sec\n", (time1 - time0) / 1000.0);
    System.err.printf("cnt1=%d, cnt2=%d\n", cnt1, cnt2);
  }

}
