package grank.old;

import java.io.*;
import java.util.*;

import org.apache.commons.math.*;
import grank.transform.*;
import grank.pvalue.*;
import grank.mine.*;
import ctree.util.*;

/**
 * HistMine: Histogram mining
 *
 * Assuming p_1>=p_2>=...>=p_m
 *
 * HistMine7: lexicographical, depth-first, bottom up, flexible size,
 *             new best order search based on ClosedHist,
 *             gradually increase hZ
 *
 * @author Huahai He
 * @version 1.0
 */
public class HistMine7 {

  private static long cnt1 = 0; // count of eval (lower bound pvalue)
  private static long cnt2 = 0; // count of accurate pvalue computations
  private static long cnt3 = 0; // count of updating top-K
  /**
   * GraphRank - the top level of the algorithm
   */
  private static void closedHist(int[] hist, int pos0, int z,
                                 Vector < int[] > base,
                                 int depth, Environment env) throws
      MathException {

    /*if (!future(hist, pos0, base, env)) {
      return;
         }*/
    eval(hist, z, base.size(), env);

    Vector<int[]> base1 = new Vector<int[]> (base.size());

    int[] hist1 = new int[env.m];
    for (int pos = pos0; pos < env.m; pos++) {
      //for (int pos = m-1;pos>=pos0;pos--) {
      if (env.fbin[pos] == false) {
        continue;
      }
      int ground = hist[pos]; // minimum value at pos
      Arrays.fill(hist1, Integer.MAX_VALUE); // next minimum value at pos
      base1.clear();
      for (int i = 0; i < base.size(); i++) {
        int[] h = base.elementAt(i);
        if (h[pos] > ground) {
          base1.add(h);
          for (int j = 0; j < env.m; j++) {
            if (h[j] < hist1[j]) {
              hist1[j] = h[j];
            }
          }
        }
      }
      if (base1.size() < env.minSup) { // constraint of support
        continue;
      }

      // check if it violates lexicographical order
      boolean dup_flag = false;
      for (int j = 0; j < pos; j++) {
        if (hist1[j] > hist[j]) {
          dup_flag = true;
          break;
        }
      }
      if (dup_flag) {
        continue;
      }

      closedHist(hist1, pos, PValue.sum(hist1), base1, depth + 1, env);
    }
  }

  // Return false if future can be pruned
  public static boolean future(int[] hist, int pos0, Vector < int[] > base,
                               Environment env) throws MathException {
    int[] up = new int[env.m];
    System.arraycopy(hist, 0, up, 0, env.m);
    for (int i = 0; i < base.size(); i++) {
      int[] h = base.elementAt(i);
      for (int j = pos0; j < env.m; j++) {
        if (env.fbin[j] && h[j] > up[j]) {
          up[j] = h[j];
        }
      }
    }
    int z = PValue.sum(up);
    if (z > env.maxZ) {
      return true;
    }
    double[] probs = new double[env.maxZ + 1];
    Arrays.fill(probs, 0, z, 0);
    for (int s = z; s <= env.maxZ; s++) {
      probs[s] = PValue.lowerProb(env.p, up, s);
    }
    double pvalue = PValue.computePvalue(probs, env.dbZ, env.dbN, env.nG,
                                         base.size());
    if (pvalue < env.maxPvalue) {
      return true;
    }
    else {
      return false;
    }

  }

  public static void eval(int[] node, int z, int sup,
                          Environment env) throws MathException {
    // compute pvalue

    if (z > env.hZ) {
      return;
    }

    // fast lower bound of pvalue
    double[] probs = new double[env.maxZ + 1];
    Arrays.fill(probs, 0, z, 0);
    for (int s = z; s <= env.maxZ; s++) {
      probs[s] = PValue.lowerProb(env.p, node, s);
    }
    double pvalue = PValue.computePvalue(probs, env.dbZ, env.dbN, env.nG, sup);
    cnt1++;

    if (pvalue <= env.maxPvalue) {
      // accurate pvalue
      probs = PValue.probSubsetRecursiveArray(env.p, node, z, env.maxZ);
      pvalue = PValue.computePvalue(probs, env.dbZ, env.dbN, env.nG, sup);
      cnt2++;

      // add this node to answers
      if (pvalue <= env.maxPvalue) {
        int[] h = new int[env.m];
        System.arraycopy(node, 0, h, 0, env.m);
        Answer a = new Answer(h, sup, pvalue);
        env.ans.add(a);
        if (env.ans.size() > env.K) {
          env.ans.poll();
          env.maxPvalue = env.ans.peek().pvalue;
          System.err.printf("pvalue=%g, sup=%d, z=%d\n ",
                            env.maxPvalue, sup, z);
        }
        cnt3++;
      }
    }
  }

  public static Hist[] graphRank4(double maxPvalue, int K, int minSup,
                                  int[][] H, double[] p, int hZ) throws
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

    // prepare parameters
    int[] root = new int[m];
    Arrays.fill(root, 0);
    Vector<int[]> base0 = new Vector<int[]> ();
    for (int[] h : H) {
      base0.add(h);
    }

    // answer set stored in a priority queue, reverse order, at most K answers
    PriorityQueue<Answer>
        ans = new PriorityQueue<Answer> (Math.min(K, 1000));

    Environment env = new Environment(m, p, null, maxZ, H, nG, dbZ, dbN, maxPvalue,
                                      minSup, K, hZ, ans, fbin);

    // Gradually increase hZ
    int h_Z = 5;
    env.hZ = h_Z;
    closedHist(root, 0, 0, base0, 0, env);

    while (h_Z < hZ) {
      h_Z += 5;
      env.hZ = h_Z;
      System.err.printf("hZ=%d\n", h_Z);
      expand(env);
    }

    // output
    Hist[] results = new Hist[ans.size()];
    for (int i = 0; i < results.length; i++) {
      Answer a = ans.poll();
      int size = PValue.sum(a.hist);
      int i1 = results.length - i - 1;
      String id = size + "-" + i1 + "," + a.sup + "," + a.pvalue;
      Hist h = new Hist(id, a.hist);
      results[i1] = h;
    }
    return results;
  }

  public static void expand(Environment env) throws MathException {
    // Copy out top-K histograms from the last round
    int k = env.ans.size();
    Answer[] buf = new Answer[k];

    for (int i = 0; i < k; i++) {
      buf[k - i - 1] = env.ans.poll();
    }

    // Use each histogram as root, expand search states
    // Search trees might be duplicate
    Vector<int[]> base = new Vector<int[]> ();
    for (Answer ans : buf) {
      base.clear();
      for (int[] h : env.HD) {
        if (subHist(ans.hist, h, env.m)) {
          base.add(h);
        }
      }
      closedHist(ans.hist, 0, PValue.sum(ans.hist), base, 0, env);
    }
  }

  // check if a histogram is a sub-histogram of another
  public static boolean subHist(int[] h1, int[] h2, int m) {
    for (int i = 0; i < m; i++) {
      if (h1[i] > h2[i]) {
        return false;
      }
    }
    return true;
  }

  public static void main(String[] args) throws IOException, MathException {
    Opt opt = new Opt(args);
    if (opt.args() < 2) {
      System.err.println(
          "Usage: ... [options] hist_file prob_file");
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

    long time0 = System.currentTimeMillis();

    Hist[] results = graphRank4(maxPvalue, K, minSup, H, p, hZ);

    String sig_hist = opt.getString("sig_hist", "sig.hist");
    Hist.saveHists(results, sig_hist);

    long time1 = System.currentTimeMillis();
    System.err.printf("Time: %.2f sec\n", (time1 - time0) / 1000.0);
    System.err.printf("cnt1=%d, cnt2=%d, cnt3=%d\n", cnt1, cnt2, cnt3);
  }

}
