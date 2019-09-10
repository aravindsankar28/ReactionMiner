package grank.old;

import java.io.*;

import grank.transform.*;

/**
 * Suppress histograms until there is one bin.
 * @author Huahai He
 * @version 1.0
 */
public class SuppressHist {
  /**
   * Condense histograms of dimension m into dimension ceil(m/2)
   * @param H int[][]
   * @param m int
   * @return int[][]
   */
  public static int[][] condenseHist(int[][] H, int m) {
    assert (m > 1);
    int m1 = (m + 1) / 2;
    int[][] H1 = new int[H.length][];
    for (int i = 0; i < H.length; i++) {
      int[] h = H[i];
      int[] h1 = new int[m1];
      for (int j = 0; j < m1; j++) {
        if (j < m1 - 1 || j * 2 + 1 < m) { // condense two bins into one
          h1[j] = h[j * 2] + h[j * 2 + 1];
        }
        else { // last bin and non condense
          h1[j] = h[j * 2];
        }
      }
      H1[i] = h1;
    }
    return H1;
  }

  /**
   * The number of levels to condense dimensions into one, e.g., dim=5, level=4
   * @param dim int
   * @return int
   */
  public static int levelOfDim(int dim) {
    int level = 0;

    while ( (1 << level) < dim) {
      level++;
    }
    return level + 1;
  }

  /**
   * Condense histograms until one dimension
   * @param DB Hist[]
   * @return [0]: one dimension, ..., [level]: m dimensions
   */
  public static int[][][] condenseAll(Hist[] DB) {
    int m = DB[0].hist.length;
    int[][] H = new int[DB.length][];
    for (int i = 0; i < H.length; i++) {
      H[i] = DB[i].hist;
    }
    int level = levelOfDim(m);

    int[][][] Hs = new int[level][][];
    Hs[level - 1] = H;
    int m1 = m;
    for (int i = level - 2; i >= 0; i--) {
      Hs[i] = condenseHist(Hs[i + 1], m1);
      m1 = (m1 + 1) / 2;
    }
    return Hs;
  }

  public static double[][] condenseProb(double[] p) {
    int m = p.length;
    int level = levelOfDim(m);
    double[][] ps = new double[level][];
    ps[level - 1] = p;
    int m0 = m;
    for (int i = level - 2; i >= 0; i--) {
      double[] p0 = ps[i + 1];   // level i+1
      int m1 = (m0 + 1) / 2;     // dim at this level
      double[] p1 = new double[m1];
      for (int j = 0; j < m1; j++) {
        if (j < m1 - 1 || j * 2 + 1 < m0) {  // condense two bins
          p1[j] = p0[j * 2] + p0[j * 2 + 1];
        }
        else {    // last bin
          p1[j] = p0[j * 2];
        }
      }
      ps[i] = p1;
      m0 = m1;
    }
    return ps;
  }

  public static void main(String[] args) throws IOException {
    if (args.length < 1) {
      System.err.println("Usage: ... hist_file");
      System.exit(1);
    }
    Hist[] DB = Hist.loadHists(args[0]);
    int[][][] Hs = condenseAll(DB);
    for (int level = 0; level < Hs.length; level++) {
      String file = args[0] + "." + level;
      int[][] H = Hs[level];
      for (int i = 0; i < DB.length; i++) {
        DB[i].hist = H[i];
      }
      Hist.saveHists(DB, file);
    }
  }
}
