package grank.old;

import java.io.*;
import java.util.*;

import grank.transform.*;
import grank.pvalue.*;
import grank.util.*;
import grank.mine.*;
import ctree.util.*;

/**
 * HistMine: Histogram mining
 *
 * Assuming p_1<=p_2<=...<=p_m
 *
 * HistMine: mutli-level, amortized search, top-down
 *
 * @author Huahai He
 * @version 1.0
 */
public class HistMine1 {

  /**
   * Dive-and-join, splitting at the bottom level
   *
   * INPUT:
   * p[dim]:      Original probabilities, non-descreasing order
   * dim:         Original dimensions, i.e., at the leaf level
   * curr_dim:    Current level dimensions. Dimension at level 0 is one.
   * next_dim:    Next level dimensions
   * diff:        leaf level - current level
   *
   * Candidate:
   *    int[] hist:       Histogram at current level
   *    int[] seg:        Segment of histogram at next level, bottom up
   *    double[] pseg:    Lowerbound of P(X,N) for the segment, bottom up
   *
   * pos:         Starting position of current segment
   * len:         Length of current segment
   *
   * H:           Histograms at current level
   * nextH:       Histograms at next level
   *
   * dbZ:         Sizes of database histograms
   * dbN:         N[i] is the number of database histograms of size dbZ[i]
   * nG:          Number of database histograms
   *
   * maxPvalue:   Pvalues greater than maxPvalue are pruned
   * minSup:  Supports smaller than minSup are pruned
   *
   * prest:       P(X,Z) for rest bins
   * zrest:       sum of rest bins
   * maxZ:        Maximum size of database histograms
   *
   * Output:
   * Candidates at next level
   *
   */
  public static Vector<Candidate> divide(double[] p, int dim, int curr_dim,
                                         int next_dim, int diff,
                                         Candidate cand, int pos, int len,
                                         int[][] H, int[][] nextH,
                                         int[] dbZ, int[] dbN, int nG,
                                         double maxPvalue, int minSup,
                                         double[] prest, int zrest, int maxZ) {
    assert (p.length == dim);
    if (len >= 2) { // Divide current segment in half and recurse down
      int len1 = len / 2;
      int len2 = len - len1; // len1>=len2

      Vector<Candidate> ans = new Vector<Candidate> ();

      // First part
      double[] ppart2 = new double[maxZ + 1]; // prob. of part2
      int z2 = PValue.sum(cand.hist, pos + len1, len2);

      // Position at the original histograms, (pos-1)*2^dlev+1, if starting at 1
      int pos2 = (pos + len1) * (1 << diff);
      for (int s = z2; s <= maxZ; s++) {
        ppart2[s] = Math.pow(p[pos2], s);
      }
      double[] prest1 = (len == curr_dim) ? ppart2 :
          mergeProb(ppart2, prest, z2, zrest, maxZ);
      Vector<Candidate>
          C1 = divide(p, dim, curr_dim, next_dim, diff,
                      cand, pos, len1,
                      H, nextH,
                      dbZ, dbN, nG,
                      maxPvalue, minSup,
                      prest1, z2 + zrest, maxZ);

      if (C1.size() == 0) {
        return ans;
      }

      // Second part
      double[] ppart1 = new double[maxZ + 1]; // prob. of part1
      int z1 = PValue.sum(cand.hist, pos, len1);
      int pos1 = pos * (1 << diff);
      for (int s = z1; s <= maxZ; s++) {
        ppart1[s] = Math.pow(p[pos1], s);
      }
      double[] prest2 = (len == curr_dim) ? ppart1 :
          mergeProb(ppart1, prest, z1, zrest, maxZ);
      Vector<Candidate>
          C2 = divide(p, dim, curr_dim, next_dim, diff,
                      cand, pos + len1, len2,
                      H, nextH,
                      dbZ, dbN, nG,
                      maxPvalue, minSup,
                      prest2, z1 + zrest, maxZ);
      if (C2.size() == 0) {
        return ans;
      }


      // Join (Cartesian Product)
      for (Candidate c1 : C1) {
        for (Candidate c2 : C2) {
          int sup = countSupport(H, curr_dim, nextH, cand.hist, c1.seg, pos * 2,
                                 c2.seg, (pos + len1) * 2);
          if (sup < minSup) {
            continue;
          }

          double[] pseg = mergeProb(c1.pseg, c2.pseg, z1, z2, maxZ);
          double[] plow;
          if (len == curr_dim) {
            plow = pseg;
          }
          else {
            plow = mergeProb(pseg, prest, z1 + z2, zrest, maxZ);
          }

          double pvalue = PValue.computePvalue(plow, dbZ, dbN, nG, sup);
          if (pvalue > maxPvalue) {
            continue;
          }

          int[] seg = new int[c1.seg.length + c2.seg.length];
          System.arraycopy(c1.seg, 0, seg, 0, c1.seg.length);
          System.arraycopy(c2.seg, 0, seg, c1.seg.length, c2.seg.length);
          Candidate c = new Candidate(cand.hist, seg, pseg, pvalue, sup);
          ans.add(c);

        }
      }
      return ans;
    }

    else { // Split
      if (pos == curr_dim - 1 && curr_dim * 2 > next_dim) { // the last bin might not need splitting

        int[] seg = {cand.hist[pos]};
        double[] pseg = new double[maxZ + 1];
        int pos1 = pos * (1 << diff);
        for (int s = seg[0]; s <= maxZ; s++) {
          pseg[s] = Math.pow(p[pos1], s);
        }

        Candidate c = new Candidate(cand.hist, seg, pseg, 0, 0);
        Vector<Candidate> ans = new Vector<Candidate> ();
        ans.add(c);
        return ans;
      }

      // Split one bin into two bins
      Vector<Candidate> ans = new Vector<Candidate> ();
      int z = cand.hist[pos];
      for (int a1 = 0; a1 <= z; a1++) {
        int a2 = z - a1;
        int[] seg = {a1, a2};

        // actual support
        int sup = countSupport(H, curr_dim, nextH, cand.hist, seg, pos * 2);
        if (sup < minSup) {
          continue;
        }

        // Positions at the original histograms
        int pos1 = pos * (1 << diff);
        int pos2 = pos1 + (1 << (diff - 1));

        // Lower bound of P(X,N) for the segment
        double[] pseg = new double[maxZ + 1];
        for (int s = z; s <= maxZ; s++) {
          double sum = 0;
          for (int t = a1; t <= s - a2; t++) {
            sum += Math.pow(p[pos1], t) * Math.pow(p[pos2], s - t) *
                Stat.combination(s, t);
          }
          pseg[s] = sum;
        }
        // Lower bound of P(X,N)
        double[] plow;
        if (len == curr_dim) {
          plow = pseg;
        }
        else {
          plow = mergeProb(pseg, prest, z, zrest, maxZ);
        }

        // p-value
        double pvalue = PValue.computePvalue(plow, dbZ, dbN, nG, sup);
        if (pvalue > maxPvalue) {
          continue;
        }
        Candidate c = new Candidate(cand.hist, seg, pseg, pvalue, sup);
        ans.add(c);

      }

      return ans;
    }
  }

  /**
   * Merge two prob. using the recursive equation
   */
  public static double[] mergeProb(double[] p1, double[] p2, int a1, int a2,
                                   int Z) {
    double[] p = new double[Z + 1];
    int z = a1 + a2;
    for (int s = z; s <= Z; s++) {
      double sum = 0;
      for (int t = a1; t <= s - a2; t++) {
        sum += p1[t] * p2[s - t] * Stat.combination(s, t);
      }
      p[s] = sum;
    }
    return p;
  }

  /**
   * Count the support
   */
  public static int countSupport(int[][] H, int curr_dim, int[][] nextH,
                                 int[] hist, int[] seg, int pos) {
    int sup = 0;
    for (int i = 0; i < H.length; i++) {
      boolean flag = true;
      for (int j = 0; j < curr_dim; j++) { // check current level
        if (H[i][j] <
            hist[j]) {
          flag = false;
          break;
        }
      }
      if (flag) {
        for (int k = 0; k < seg.length; k++) { // check segment at next level
          if (nextH[i][pos + k] < seg[k]) {
            flag = false;
            break;
          }
        }
        if (flag) {
          sup++;
        }
      }

    }
    return sup;
  }

  public static int countSupport(int[][] H, int curr_dim, int[][] nextH,
                                 int[] hist, int[] seg1, int pos1, int[] seg2,
                                 int pos2) {
    int sup = 0;
    for (int i = 0; i < H.length; i++) {
      boolean flag = true;
      for (int j = 0; j < curr_dim; j++) { // check current level
        if (H[i][j] < hist[j]) {
          flag = false;
          break;
        }
      }
      if (flag) {
        for (int k = 0; k < seg1.length; k++) { // check segment 1 at next level
          if (nextH[i][pos1 + k] < seg1[k]) {
            flag = false;
            break;
          }
        }
      }
      if (flag) {
        for (int k = 0; k < seg2.length; k++) { // check segment 2 at next level
          if (nextH[i][pos2 + k] < seg2[k]) {
            flag = false;
            break;
          }
        }
        if (flag) {
          sup++;
        }
      }

    }
    return sup;
  }

  /**
   * GraphRank - the top level of the algorithm
   * @param p double[]
   * @param Hs int[][][]
   * @param hZ int
   * @param hz int
   * @param maxPvalue double
   * @param K int
   * @param minSup int
   * @return Significant histograms
   */
  public static Hist[] graphRank(double[] p, double[][] ps, int[][][] Hs,
                                 int hZ, int hz,
                                 double maxPvalue, int K, int minSup, int frame) {
    int maxLevel = Hs.length - 1;
    int dim = p.length;

    assert (Hs[0][0].length == 1);
    int nG = Hs[0].length;

    // Get dbZ, dbN, maxZ
    int[] dbsizes = new int[nG];
    for (int i = 0; i < nG; i++) {
      dbsizes[i] = Hs[0][i][0];
    }
    int[][] tmp = PValue.dbSizes(dbsizes);
    int[] dbZ = tmp[0];
    int[] dbN = tmp[1];
    int maxZ = 0;
    for (int size : dbsizes) {
      if (size > maxZ) {
        maxZ = size;
      }
    }

    // mine sub-histogram of given sizes
    Vector<Candidate> results = new Vector<Candidate> ();

    for (int size = hZ; size >= hz; size--) {
      System.err.printf("Size: %d\n", size);
      int[] hist = new int[] {size}; // histogram at level 0 has one bin
      Candidate root = new Candidate(hist, null, null, 0, 0);
      Candidate[] cands0 = new Candidate[] {root}; // candidates at level 0

      double[] prest = new double[maxZ + 1];
      Arrays.fill(prest, 1);

      long time0 = System.currentTimeMillis();

      maxPvalue = amortize(p, ps, Hs, maxPvalue, minSup, K, dim, nG, dbZ, dbN,
                           maxZ,
                           cands0, prest, 0, maxLevel, frame, results);
      long time1 = System.currentTimeMillis();
    }

    Hist[] array = new Hist[Math.min(results.size(), K)];
    for (int i = 0; i < array.length; i++) {
      Candidate cand = results.get(i);
      int size = PValue.sum(cand.seg);
      String id = size + "-" + i + "," + cand.sup + "," + cand.lPvalue;
      array[i] = new Hist(id, cand.seg);
    }
    return array;
  }

  private static UpperComparator upperComp = new UpperComparator();

  /**
   * Amortized search, which is a hybrid of breadth-first and depth-first search.
   * Breadth-first at each level, order the nodes, then pick up a frame of
   * nodes and search them at next level.
   *
   * @param p double[]        bottom level
   * @param ps double[][]     prob. at all levels
   * @param Hs int[][][]      Histograms at all levels
   * @param maxPvalue double
   * @param minSup int        Minimum support, absolute number
   * @param K int
   * @param dim int           Original dimensions
   * @param nG int            Number of database graphs
   * @param dbZ int[]
   * @param dbN int[]
   * @param maxZ int          Maximum size of database histograms
   * @param cands Candidate[]
   * @param prest double[]    Prob. of rest
   * @param level int         Current level
   * @param maxLevel int      Maximum level
   * @param frame int         # of nodes to pass down at next level
   * @param results Vector
   * @return double
   */
  public static double amortize(double[] p, double[][] ps, int[][][] Hs,
                                double maxPvalue, int minSup, int K,
                                int dim, int nG, int[] dbZ,
                                int[] dbN, int maxZ,
                                Candidate[] cands, double[] prest,
                                int level, int maxLevel, int frame,
                                Vector<Candidate> results) {
    int curr_dim = Hs[level][0].length;
    int next_dim = Hs[level + 1][0].length;
    int diff = maxLevel - level;
    if (diff > 2) {
      System.err.printf("level: %d, curr_dim: %d, cands: %d\n", level, curr_dim,
                        cands.length);
    }

    Vector<Candidate> ans = new Vector<Candidate> ();
    for (int i = 0; i < cands.length; i++) {
      Candidate cand = cands[i];
      Vector<Candidate>
          some = divide(p, dim, curr_dim, next_dim, diff, cand, 0,
                        cand.hist.length, Hs[level],
                        Hs[level + 1], dbZ, dbN, nG, maxPvalue, minSup,
                        prest, 0, maxZ);
      for (Candidate c : some) {
        c.hist = c.seg;
        c.uPvalue = PValue.pvalue(ps[level + 1], c.seg, dbZ, dbN, c.sup).pvalue;
      }
      if (level < maxLevel - 1) {
        ans.addAll(some);

        // process a frame at next level
        if ( (i + 1) % frame == 0 || i == cands.length - 1) {
          Candidate[] array = new Candidate[ans.size()];
          ans.toArray(array);
          Arrays.sort(array, upperComp);
          maxPvalue = amortize(p, ps, Hs, maxPvalue, minSup, K, dim, nG, dbZ,
                               dbN,
                               maxZ, array,
                               prest, level + 1, maxLevel, frame, results);
          ans.clear();
        }
      }
      else { // at the bottom level, store results and update maxPvalue
        results.addAll(some);
        if (results.size() >= K) {
          Candidate[] array = new Candidate[results.size()];
          results.toArray(array);
          Arrays.sort(array);
          results.clear();
          int len = Math.min(array.length, K);
          for (int j = 0; j < len; j++) {
            results.add(array[j]);
          }
          if (array[len - 1].lPvalue < maxPvalue) {
            maxPvalue = array[len - 1].lPvalue;
            System.err.printf("maxPvalue = %g\n", maxPvalue);
          }
        }
        //System.err.printf("Results: %d\n", results.size());
      }
    }
    return maxPvalue;
  }

  /**
   * Return upper bound of histograms
   * @param H int[][]
   * @return int[]
   */
  public static int[] upperHists(int[][] H) {
    int m = H[0].length;
    int[] U = new int[m];
    Arrays.fill(U, 0);
    for (int[] h : H) {
      for (int j = 0; j < m; j++) {
        if (h[j] > U[j]) {
          U[j] = h[j];
        }
      }
    }
    return U;
  }

  public static void main(String[] args) throws IOException {
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
      System.err.println("  -minSup=NUMBER \t Minimum support, default=1");
      System.err.println("  -mu0=[graph|hist] \t Use graphMu0 or histMu0 as the real support, default=graph");
      System.err.println(
          "  -sig_hist=FILE \t Significant histograms file, default=sig.hist");
      System.err.println("  -frame=NUMBER \t\t Frame size, default=10");
      System.exit(0);
    }

    double maxPvalue = opt.getDouble("pvalue", 1);
    int K = opt.getInt("K", Integer.MAX_VALUE);
    int hZ = opt.getInt("hZ", Integer.MAX_VALUE);
    int hz = opt.getInt("hz", 1);
    int minSup = opt.getInt("minSup", 1);
    boolean mu0Flag = true; // if true, then use graphMu0, o/w use histMu0
    if (opt.hasOpt("mu0") && opt.getString("mu0").equals("hist")) {
      mu0Flag = false;
    }
    Hist[] DB = Hist.loadHists(opt.getArg(0)); // database histograms
    int maxZ = Hist.maxSize(DB);
    if (hZ > maxZ) {
      hZ = maxZ;
    }
    int frame = opt.getInt("frame", 10);

    double[] p = BasisProb.loadProb(opt.getArg(1)); // feature probabilities
    for (int i = 0; i < p.length - 1; i++) {
      if (p[i] > p[i + 1]) {
        throw new RuntimeException("Assertion error: prob. are not sorted");
      }
    }
    int[][][] Hs = SuppressHist.condenseAll(DB);

    /*int[] U = upperHists(Hs[Hs.length-1]);
         for(int i =0;i<U.length;i++) {
      System.err.print(i+" ");
         }
         System.err.println();
     */

    double[][] ps = SuppressHist.condenseProb(p);
    long time0 = System.currentTimeMillis();

    Hist[] results = graphRank(p, ps, Hs, hZ, hz, maxPvalue, K, minSup, frame);
    String sig_hist = opt.getString("sig_hist", "sig.hist");
    Hist.saveHists(results, sig_hist);

    long time1 = System.currentTimeMillis();
    System.err.printf("Time: %.2f sec\n", (time1 - time0) / 1000.0);
  }
}
