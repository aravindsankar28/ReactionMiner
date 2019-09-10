package grank.old;

import java.io.*;
import java.util.*;

import org.apache.commons.math.*;
import grank.transform.*;
import grank.pvalue.*;
import grank.mine.*;
import ctree.util.*;
import grank.old.*;

/**
 * HistMine: Histogram mining
 *
 * Assuming p_1>=p_2>=...>=p_m
 *
 * HistMine6: lexicographical, depth-first, bottom up, flexible size,
 *             BEST ORDER search, using a double-linked heap
 *
 * @author Huahai He
 * @version 1.0
 */
public class HistMine6 {

  private static long cnt1 = 0; // count of accurate pvalue computations
  private static long cnt2 = 0; // count of fast pvalue pre-computations
  private static long cnt3 = 0; // count of all candidates
  /**
   * GraphRank - the top level of the algorithm
   */
  private static void visit(int depth, Environment env, DoubleHeap<InterAnswer>
      heap) throws MathException, IOException {

    // compute pvalue
    InterAnswer ia = heap.remove();
    int z = PValue.sum(ia.hist);

    if (z > env.hZ) {
      return;
    }
    Vector<Integer> base = new Vector<Integer> (ia.sup);
    for (int i = 0; i < env.HD.length; i++) {
      if (HistMine3.subHist(ia.hist, env.HD[i], env.m)) {
        base.add(i);
      }
    }
    assert (base.size() == ia.sup);

    boolean flag = false; // true if this candidate is in top-K
    if (ia.pvalue < env.maxPvalue) {
      // accurate pvalue
      double[] probs = PValue.probSubsetRecursiveArray(env.p, ia.hist, z,
          env.maxZ);
      ia.pvalue = PValue.computePvalue(probs, env.dbZ, env.dbN, env.nG, ia.sup);

      // add this node to answers
      if (ia.pvalue <= env.maxPvalue) {
        env.ans.add(ia);
        if (env.ans.size() > env.K) {
          env.ans.poll();
          env.maxPvalue = env.ans.peek().pvalue;
          System.err.printf("pvalue=%g, depth=%d, sup=%d, z=%d\n ",
                            env.maxPvalue,
                            depth, ia.sup, z);
        }
        flag = true;
        cnt1++;
      }
      else {
        cnt2++;
      }
    }

    for (int pos = ia.pos; pos < env.m; pos++) {
      if (env.fbin[pos] == false) {
        continue;
      }
      int ground = ia.hist[pos]; // minimum value at pos
      int[] promote = new int[env.m];
      Arrays.fill(promote, Integer.MAX_VALUE); // next minimum value at pos
      //base1.clear();
      int sup1 = 0;
      for (int i = 0; i < base.size(); i++) {
        int[] h = env.HD[base.elementAt(i)];
        if (h[pos] > ground) {
          //base1.add(h);
          sup1++;
          for (int j = 0; j < env.m; j++) {
            if (h[j] < promote[j]) {
              promote[j] = h[j];
            }
          }
        }
      }
      if (sup1 < env.minSup) { // constraint of support
        continue;
      }
      boolean dup_flag = false;
      for (int j = 0; j < pos; j++) {
        if (promote[j] > ia.hist[j]) {
          dup_flag = true;
          break;
        }
      }
      if (dup_flag) {
        continue;
      }

      // fast lower bound of pvalue
      double[] probs1 = new double[env.maxZ + 1];
      Arrays.fill(probs1, 0, z, 0);
      for (int s = PValue.sum(promote); s <= env.maxZ; s++) {
        probs1[s] = PValue.lowerProb(env.p, promote, s);
      }
      double pvalue1 = PValue.computePvalue(probs1, env.dbZ, env.dbN, env.nG, sup1);
      InterAnswer ia1 = new InterAnswer(pos, promote, sup1, pvalue1);
      heap.insert(ia1);

      cnt3++;
      /*if (cnt3 % 1000 == 0) {
        System.err.printf("cnt3 = %d\n", cnt3);
        intermediateResults(env.ans, "temp" + cnt3 + ".hist");
             }*/
      visit(depth + 1, env, heap);
      //node[pos] = ground;

    }

  }

  public static Hist[] graphRank6(double maxPvalue, int K, int minSup,
                                  int[][] H, double[] p, int hZ, int heapSize,
                                  int period) throws
      MathException, IOException {

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

    // answer set stored in a priority queue, reverse order, at most K answers
    PriorityQueue<Answer>
        ans = new PriorityQueue<Answer> (K);
    DoubleHeap<InterAnswer>
        heap = new DoubleHeap<InterAnswer> (heapSize, new ReverseComparator());
    final Environment env = new Environment(m, p, null, maxZ, H, nG, dbZ, dbN,
                                            maxPvalue,
                                            minSup, K, hZ, ans, fbin);

    InterAnswer ia = new InterAnswer(0, root, H.length, 1);
    heap.insert(ia);

    Timer timer = new Timer(false);
    TimerTask task = new TimerTask() {
      PriorityQueue<Answer> ans = env.ans;
      int cnt = 0;

      public void run() {
        try {
          intermediateResults(ans, "temp" + cnt + ".hist");
          cnt++;
        }
        catch (IOException ex) {
          ex.printStackTrace();
        }
      }
    };

    timer.scheduleAtFixedRate(task, 0, period * 1000);
    // Start searching
    visit(0, env, heap);

    timer.cancel();

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

  static void intermediateResults(PriorityQueue<Answer> ans,
      String file_name) throws IOException {
    int K = ans.size();
    Answer[] array = new Answer[K];
    ans.toArray(array);
    Arrays.sort(array, ans.comparator());
    Hist[] results = new Hist[K];
    for (int i = 0; i < K; i++) {
      Answer a = array[i];
      int size = PValue.sum(a.hist);
      int i1 = results.length - i - 1;
      String id = size + "-" + i1 + "," + a.sup + "," + a.pvalue;
      Hist h = new Hist(id, a.hist);
      results[i1] = h;
    }
    Hist.saveHists(results, file_name);

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
          "  -heapSize=NUMBER \t Maximum double heap size, default=1000");
      System.err.println(
          "  -period=NUMBER \t Time period in seconds to report results, default=30");
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
    int heapSize = opt.getInt("heapSize", 1000);

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
    int period = opt.getInt("period", 30);

    Hist[] results = graphRank6(maxPvalue, K, minSup, H, p, hZ, heapSize,
                                period);

    String sig_hist = opt.getString("sig_hist", "sig.hist");
    Hist.saveHists(results, sig_hist);

    long time1 = System.currentTimeMillis();
    System.err.printf("Time: %.2f sec\n", (time1 - time0) / 1000.0);
    System.err.printf("cnt1=%d, cnt2=%d\n", cnt1, cnt2);
  }

}
