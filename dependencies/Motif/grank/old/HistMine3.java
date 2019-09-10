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
 * HistMine3: lexicographical, depth-first, bottom up, flexible size,
 *              Reduce dimensions
 *
 * @author Huahai He
 * @version 1.0
 */
public class HistMine3 {

  private static long cnt1 = 0; // count of accurate pvalue computations
  private static long cnt2 = 0; // count of fast pvalue pre-computations

  /**
   * GraphRank - the top level of the algorithm
   */
  private static double visit(int m, double[] p, int[][] H, int maxZ, int nG,
                              int[] dbZ, int[] dbN,
                              double maxPvalue, int K, int minSup, int[] node,
                              int pos0, int z, Vector < int[] > base, int hZ,
                              int depth,
                              PriorityQueue<Answer> ans) throws MathException {

    // compute pvalue
    if (z > hZ) {
      return maxPvalue;
    }
    int sup = base.size(); // support
    //double[] probs = PValue.probSubsetRecursiveArray(p, node, z, maxZ);

    // fast lower bound of pvalue
    double[] probs = new double[maxZ + 1];
    Arrays.fill(probs, 0, z, 0);
    for (int s = z; s <= maxZ; s++) {
      probs[s] = PValue.lowerProb(p, node, s);
    }
    double pvalue = PValue.computePvalue(probs, dbZ, dbN, nG, sup);

    boolean flag = false; // true if this candidate is in top-K
    if (pvalue <= maxPvalue) {
      // accurate pvalue
      probs = PValue.probSubsetRecursiveArray(p, node, z, maxZ);
      pvalue = PValue.computePvalue(probs, dbZ, dbN, nG, sup);

      // add this node to answers
      if (pvalue <= maxPvalue) {
        int[] h = new int[m];
        System.arraycopy(node, 0, h, 0, m);
        Answer a = new Answer(h, sup, pvalue);
        ans.add(a);
        if (ans.size() > K) {
          ans.poll();
          maxPvalue = ans.peek().pvalue;
          System.err.printf("pvalue=%g, depth=%d, sup=%d, z=%d\n ", maxPvalue,
                            depth, sup, z);
        }
        flag = true;
        cnt1++;
      }
    }
    else {
      cnt2++;
    }

    // future lower bound


    Vector<int[]> base1 = new Vector<int[]> (base.size());
    for (int pos = pos0; pos < m; pos++) {

      int ground = node[pos]; // minimum value at pos
      int promote = Integer.MAX_VALUE; // next minimum value at pos
      base1.clear();
      for (int i = 0; i < base.size(); i++) {
        int[] h = base.elementAt(i);
        if (h[pos] > ground) {
          base1.add(h);
          if (h[pos] < promote) {
            promote = h[pos];
          }
        }
      }
      if (base1.size() < minSup) { // constraint of support
        continue;
      }
      //node[pos] = promote; // step to the next minimum value at pos, support strictly decreases
      node[pos]++;
      maxPvalue = visit(m, p, H, maxZ, nG, dbZ, dbN, maxPvalue, K, minSup, node,
                        pos, z + 1, base1, hZ, depth + 1, ans);
      node[pos] = ground;
    }
    return maxPvalue;
  }

  public static int[][] screen(double maxPvalue, int K, int minSup,
                               int[][] H, double[] p, int hZ) throws
      MathException {

    int m = H[0].length;
    int nG = H.length;

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
    PriorityQueue<Answer> ans = new PriorityQueue<Answer> (K);
    visit(m, p, H, maxZ, nG, dbZ, dbN, maxPvalue, K,
          minSup, root, 0, 0, base0, hZ, 0, ans);

    // output
    int[][] results = new int[ans.size()][];
    for (int i = 0; i < results.length; i++) {
      Answer a = ans.poll();
      results[i] = a.hist;
    }
    return results;
  }

  /**
   * GraphRank - the top level of the algorithm
   */
  private static double visitSketch(int m, double[] p, int[][] H, int maxZ,
                                    int nG, int[] dbZ, int[] dbN,
                                    double maxPvalue, int K, int minSup,
                                    int[] node, int pos0, int z, int[] image,
                                    Vector < int[] > base,
                                    int hZ, int depth,
                                    PriorityQueue<Answer> ans, int m1,
      int[] map, int[] sketch, int sketch_z) throws MathException {

    // Leaf node, compute pvalue
    if (z == sketch_z) {
      if (z > hZ) {
        return maxPvalue;
      }
      int sup = base.size(); // support
      //double[] probs = PValue.probSubsetRecursiveArray(p, node, z, maxZ);

      // fast lower bound of pvalue
      double[] probs = new double[maxZ + 1];
      Arrays.fill(probs, 0, z, 0);
      for (int s = z; s <= maxZ; s++) {
        probs[s] = PValue.lowerProb(p, node, s);
      }
      double pvalue = PValue.computePvalue(probs, dbZ, dbN, nG, sup);

      boolean flag = false; // true if this candidate is in top-K
      if (pvalue <= maxPvalue) {
        // accurate pvalue
        probs = PValue.probSubsetRecursiveArray(p, node, z, maxZ);
        pvalue = PValue.computePvalue(probs, dbZ, dbN, nG, sup);

        // add this node to answers
        if (pvalue <= maxPvalue) {
          int[] h = new int[m];
          System.arraycopy(node, 0, h, 0, m);
          Answer a = new Answer(h, sup, pvalue);
          ans.add(a);
          if (ans.size() > K) {
            ans.poll();
            maxPvalue = ans.peek().pvalue;
            System.err.printf("pvalue=%g, depth=%d, sup=%d, z=%d\n ", maxPvalue,
                              depth, sup, z);
          }
          flag = true;
          cnt1++;
        }
      }
      else {
        cnt2++;
      }
      return maxPvalue;
    }

    // future lower bound, suspended


    Vector<int[]> base1 = new Vector<int[]> (base.size());
    for (int pos = pos0; pos < m; pos++) {
      int ground = node[pos]; // minimum value at pos
      int promote = Integer.MAX_VALUE; // next minimum value at pos
      base1.clear();
      for (int i = 0; i < base.size(); i++) {
        int[] h = base.elementAt(i);
        if (h[pos] > ground) {
          base1.add(h);
          if (h[pos] < promote) {
            promote = h[pos];
          }
        }
      }
      if (base1.size() < minSup) { // constraint of support
        continue;
      }
      //node[pos] = promote; // step to the next minimum value at pos, support strictly decreases
      node[pos]++;
      image[map[pos]]++;
      maxPvalue = visitSketch(m, p, H, maxZ, nG, dbZ, dbN, maxPvalue, K, minSup,
                              node, pos, z + 1, image, base1, hZ, depth + 1,
                              ans, m1, map,
                              sketch, sketch_z);
      node[pos] = ground;
      image[map[pos]]--;
    }
    return maxPvalue;
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

  public static Hist[] refine(double maxPvalue, int K, int minSup,
                              int[][] H, double[] p, int hZ, int m1, int[] map,
                              int[][] sketches) throws
      MathException {

    int m = H[0].length;
    int nG = H.length;

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

    // answer set stored in a priority queue, reverse order, at most K answers
    PriorityQueue<Answer> ans = new PriorityQueue<Answer> (K);
    int[] buf = new int[m1];
    int[] image = new int[m1];
    for (int[] sketch : sketches) {
      base0.clear();
      for (int[] h : H) {
        transform(h, m, map, m1, buf);
        if (subHist(sketch, buf, m1)) {
          base0.add(h);
        }
      }
      Arrays.fill(image, 0);
      int sketch_z = PValue.sum(sketch);
      maxPvalue = visitSketch(m, p, H, maxZ, nG, dbZ, dbN, maxPvalue, K,
                              minSup, root, 0, 0, image, base0, hZ, 0, ans, m1,
                              map,
                              sketch, sketch_z);
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

  // keep first m1-1 bins, merge rest bins
  public static int[] mergeRest(double[] p, int m1) {
    int m = p.length;
    int[] map = new int[m];
    for (int i = 0; i < m1 - 1; i++) {
      map[i] = i;
    }
    for (int i = m1 - 1; i < m; i++) {
      map[i] = m1 - 1;
    }
    return map;
  }

  // each bucket has roughly the same frequency
  public static int[] equiHeight(double[] p, int m1) {
    int m = p.length;
    int[] map = new int[m];

    double part = 0; // sum of current bucket
    double rest = 1; // sum of rest bins
    int idx = 0; // index of current bucket
    double avg = rest / (m1 - idx); // averge of rest buckets
    for (int i = 0; i < m; i++) {
      map[i] = idx;
      part += p[i];
      rest -= p[i];
      if (part >= avg) { // current bucket exceeds the average, use next bucket
        idx++;
        avg = rest / (m1 - idx);
        part = 0;
      }
    }
    assert (idx == m1);
    return map;
  }

  /**
   * Transform a histogram into a reduced histogram
   */
  private static void transform(int[] h, int m, int[] map, int m1, int[] h1) {
    Arrays.fill(h1, 0);
    for (int i = 0; i < m; i++) {
      h1[map[i]] += h[i];
    }
  }

  public static Hist[] graphRank3(double maxPvalue, int K, int minSup,
                                  int[][] H, double[] p, int hZ, int m1, int K1,
                                  int reduceFlag) throws
      MathException {
    // reduce dimensions
    int m = p.length;
    int[] map = null;
    switch (reduceFlag) {
      case 1:
        map = mergeRest(p, m1);
        break;
      case 2:
        map = equiHeight(p, m1);
        break;
      case 3:
        int[] boundary = VOptimal.V_Optimal(p, m, m1);
        for (int i = 0; i < boundary.length; i++) {
          System.err.printf("%4d", boundary[i]);
        }
        System.err.println();
        map = VOptimal.boundary2map(boundary, m);
        break;
      default:
        System.err.printf("Unknown reduceFlag: %d\n", reduceFlag);
        System.exit(1);
    }

    // get reduced prob. and histograms
    double[] p1 = new double[m1];
    Arrays.fill(p1, 0);
    for (int i = 0; i < m; i++) {
      p1[map[i]] += p[i];
    }

    try {
      BasisProb.saveProb(p1, "reduced.prob");
    }
    catch (IOException ex) {
    }

    int[][] H1 = new int[H.length][m1];
    for (int i = 0; i < H.length; i++) {
      transform(H[i], m, map, m1, H1[i]);
    }

    long time0 = System.currentTimeMillis();

    System.err.println("Screening...");
    int[][] sketches = screen(maxPvalue, K1, minSup, H1, p1, hZ);

    long time1 = System.currentTimeMillis();
    System.err.printf("\nScreen time: %.2f sec\n\n", (time1 - time0) / 1000.0);

    System.err.println("Refining...");
    Hist[] results = refine(maxPvalue, K, minSup, H, p, hZ, m1, map, sketches);
    long time2 = System.currentTimeMillis();
    System.err.printf("\nRefine time: %.2f sec \n", (time2 - time1) / 1000.0);

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
      System.err.println("  -m1=NUMBER \t\t Reduced dimensions, default=m");
      System.err.println("  -K1=NUMBER \t\t Top-K for screening, default=K");
      System.err.println(
          "  -reduce=NUMBER \t Reduce dimensions, 1:mergeRest, 2:equiHeight, 3:v-optimal, default=3");
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
    for (int i = 0; i < p.length - 1; i++) {
      if (p[i] < p[i + 1]) {
        throw new RuntimeException("Assertion error: prob. are not sorted");
      }
    }
    int m1 = opt.getInt("m1", p.length);
    int K1 = opt.getInt("K1", K);
    int reduceFlag = opt.getInt("reduce", 3);
    long time0 = System.currentTimeMillis();

    Hist[] results = graphRank3(maxPvalue, K, minSup, H, p, hZ, m1, K1,
                                reduceFlag);

    String sig_hist = opt.getString("sig_hist", "sig.hist");
    Hist.saveHists(results, sig_hist);

    long time1 = System.currentTimeMillis();
    System.err.printf("Time: %.2f sec\n", (time1 - time0) / 1000.0);
    System.err.printf("cnt1=%d, cnt2=%d\n", cnt1, cnt2);
  }

}
