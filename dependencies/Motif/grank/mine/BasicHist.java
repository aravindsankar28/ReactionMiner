package grank.mine;

import java.io.*;
import java.util.*;

import org.apache.commons.math.*;
import grank.transform.*;
import grank.pvalue.*;
import ctree.util.*;

/**
 * The basic histogram mining algorithm
 *
 * Assuming p_1>=p_2>=...>=p_m
 *
 * BasicHist: lexicographical, depth-first, bottom up, flexible size
 *
 *
 * @author Huahai He
 * @version 1.0
 */
public class BasicHist {

  private static long cntEval = 0; // count of eval (lower bound pvalue)
  private static long cntAccurate = 0; // count of accurate pvalue computations
  private static long cntUpdate = 0; // count of updating top-K
  private static int numFreqBin = 0; // number of frequent bins
  /**
   * GraphRank - the top level of the algorithm
   */
  private static void mineHist(int[] hist, int pos0, int z,
                                 Vector < int[] > base,
                                 int depth, Environment env) throws
      MathException {

    /*if (!future(hist, pos0, base, env)) {
      return;
         }*/
    if (env.toEval) {
      eval(hist, z, base.size(), env);
    }
    cntEval++;

    Vector<int[]> base1 = new Vector<int[]> (base.size());

    for (int pos = pos0; pos < env.m; pos++) {
      //for (int pos = m-1;pos>=pos0;pos--) {
      if (env.fbin[pos] == false) {
        continue;
      }
      int ground=hist[pos];
      base1.clear();
      for (int i = 0; i < base.size(); i++) {
        int[] H = base.elementAt(i);  // a supporting database histogram
        if (H[pos] > ground) {
          base1.add(H);
        }
      }
      if (base1.size() < env.minSup) { // constraint of support
        continue;
      }

      int z1 = z+1;
      if (z1 > env.hZ) {
        continue;
      }
      hist[pos]++;
      mineHist(hist, pos, z1, base1, depth + 1, env);
      hist[pos]--;
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

  public static void eval(int[] h, int z, int sup,
                          Environment env) throws MathException {
    assert (z <= env.hZ);
    // compute pvalue

    // fast lower bound of pvalue
    double[] dbP = new double[env.dbZ.length];
    for (int i = 0; i < env.dbZ.length; i++) {
      if (env.dbZ[i] >= z) {
        dbP[i] = PValue.lowerProb(env.p, h, env.dbZ[i]);
      }
      else {
        dbP[i] = 0;
      }
    }
    double pvalue = PValue.computePvalue(dbP, env.dbN, env.nG, sup);

    if (pvalue <= env.maxPvalue) {
      // accurate pvalue
      double[] probs = new double[env.maxZ + 1];
      probs = PValue.probSubsetRecursiveArray(env.p, h, z, env.maxZ);
      pvalue = PValue.computePvalue(probs, env.dbZ, env.dbN, env.nG, sup);
      cntAccurate++;

      // add this node to answers
      if (pvalue <= env.maxPvalue) {
        int[] h1 = new int[env.m];
        System.arraycopy(h, 0, h1, 0, env.m);
        Answer a = new Answer(h1, sup, pvalue);
        env.ans.add(a);
        if (env.ans.size() > env.K) {
          env.ans.poll();
          env.maxPvalue = env.ans.peek().pvalue;
          System.err.printf("pvalue=%g, sup=%d, z=%d\n",
                            env.maxPvalue, sup, z);
        }
        cntUpdate++;
      }
    }
  }

  public static Hist[] graphRank4(double maxPvalue, int K, int minSup,
                                  int[][] H, double[] p, int hZ, boolean toEval) throws
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
        //System.err.printf("%d ", cnt);
      }
      else {
        fbin[i] = false;
      }
    }
    numFreqBin = fcnt;
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
                                      minSup, K, hZ, ans, fbin, toEval,true,true);
    mineHist(root, 0, 0, base0, 0, env);

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
      System.err.println(
          "  -eval=[yes|no] \t Whether to evaluate p-values, default=yes");
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

    boolean toEval = opt.getString("eval", "yes").equalsIgnoreCase("yes");

    long time0 = System.currentTimeMillis();

    Hist[] results = graphRank4(maxPvalue, K, minSup, H, p, hZ, toEval);

    String sig_hist = opt.getString("sig_hist", "sig.hist");
    Hist.saveHists(results, sig_hist);

    long time1 = System.currentTimeMillis();
    double runtime = (time1 - time0) / 1000.0;
    System.err.printf("Time: %.2f sec\n", runtime);
    System.err.printf(
        "# of eval: %d, # of accurate pvalue: %d, # of top-K updates: %d\n",
        cntEval, cntAccurate, cntUpdate);

    System.out.printf("%.1f %d %.2f %d %d %d\n", 100.0 * minSup / DB.length,
                      numFreqBin, runtime, cntEval, cntAccurate,
                      cntUpdate);
  }

}
