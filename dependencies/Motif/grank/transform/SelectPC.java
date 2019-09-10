package grank.transform;

import java.io.*;
import java.util.*;
import grank.graph.*;
import ctree.util.*;

/**
 * Select a subset of primitive components (PCs).
 * Consider the frequency, the support of each PC,
 * similarity between pairs of PCs, and covariance between pairs of PCs.
 *
 * Input: m PCs; histograms on n PCs; k
 * Output: k PCs
 *
 * Greedily selecting the t^th PC:
 * t = argmax_j w_1 Freq(PC_j) + w_2 Size(PC_j)
 *    - w_3 \sum Sim(PC_i, PC_j)/(t-1) - w_4 \sum Cov(PC_i, PC_j)/(t-1)
 * Freq, Size, Sim, and Cov are normalized to [0,1].
 *
 * @author Huahai He
 * @version 1.0
 */
public class SelectPC {

  public static int[] selectPC(LGraph[] B, Hist[] H, int k, double w1,
                               double w2, double w3, double w4) {
    int m = B.length;
    assert (H[0].hist.length == m);

    // Prepare freq, size, sim, and cov
    System.err.println("Prepare freq, size, sim, and cov");
    double[] freqs = new double[m];
    double[] sizes = new double[m];
    for (int i = 0; i < m; i++) {
      freqs[i] = freq(H, i);
      sizes[i] = size(B[i]);
    }

    System.err.println("Compute similarity matrix");
    double[][] sims = new double[m][m];
    for (int i = 0; i < m - 1; i++) {
      for (int j = i + 1; j < m; j++) {
        sims[i][j] = NaiveGraphSim.graphSim(B[i], B[j]);
      }
    }
    System.err.println("Compute covariance matrix");
    /*
    double[][] covs = new double[m][m];
    for (int i = 0; i < m - 1; i++) {
      for (int j = i + 1; j < m; j++) {
        covs[i][j] = covariance(H, i, j);
      }
      if (i % 10 == 0) {
        System.err.printf("Column: %d\n", i);
      }
    }*/
    double[][] covs = covariance(H);

    // Normalize freq, size, sim, and cov
    double maxFreq = 0;
    double maxSize = 0;
    //double maxNorm = 0;
    for (int i = 0; i < m; i++) {
      if (freqs[i] > maxFreq) {
        maxFreq = freqs[i];
      }
      if (sizes[i] > maxSize) {
        maxSize = sizes[i];
      }
      /*double norm = NaiveGraphSim.norm(B[i]);
             if (norm > maxNorm) {
        maxNorm = norm;
             }*/
    }
    for (int i = 0; i < m; i++) {
      freqs[i] /= maxFreq;
      sizes[i] /= maxSize;
    }

    double maxSim = 0;
    double maxCov = 0;
    for (int i = 0; i < m - 1; i++) {
      for (int j = i + 1; j < m; j++) {
        if (sims[i][j] > maxSim) {
          maxSim = sims[i][j];
        }
        if (Math.abs(covs[i][j]) > maxCov) {
          maxCov = Math.abs(covs[i][j]);
        }
      }
    }
    for (int i = 0; i < m - 1; i++) {
      for (int j = i + 1; j < m; j++) {
        sims[i][j] /= maxSim;
        covs[i][j] /= maxCov;
      }
    }

    // Greedily select k PCs
    System.err.printf("Select %d PCs\n", k);
    int[] sub = new int[k];
    boolean[] mark = new boolean[m];
    Arrays.fill(mark, false);

    for (int t = 0; t < k; t++) {
      double max = Double.NEGATIVE_INFINITY;
      int maxj = -1;
      for (int j = 0; j < m; j++) {
        if (!mark[j]) {
          double value = criteria(freqs, sizes, sims, covs, sub, t, j, w1, w2,
                                  w3, w4);
          if (value > max) {
            max = value;
            maxj = j;
          }
        }
      }
      sub[t] = maxj;
      mark[maxj] = true;
      System.err.printf("%d: %f\n", t, max);
    }

    return sub;
  }

  // Support of PC j
  private static int support(Hist[] H, int j) {
    int sup = 0;
    for (Hist h : H) {
      if (h.hist[j] > 0) {
        sup++;
      }
    }
    return sup;
  }

  // Frequency of PC j
  private static int freq(Hist[] H, int j) {
    //double freq = (double) support(H, j) / H.length;
    int freq = 0;
    for (Hist h : H) {
      freq += h.hist[j];
    }
    return freq;
    /*for (Hist h : H) {
      freq += Math.sqrt(h.hist[j]);
         }
         return freq * freq; */
  }

  // Size of a PC, measured by the number of edges
  private static int size(LGraph g) {
    return g.E.length;
  }

  // Criteria
  static double criteria(double[] freqs, double[] sizes, double[][] sims,
                         double[][] covs, int[] sub, int t, int j,
                         double w1, double w2, double w3, double w4) {
    double value = w1 * freqs[j]; // Frequency of PC j
    value += w2 * sizes[j]; // Size of PC j

    // Similarity between PC i and PC j for i=0..t-1
    if (t > 0) {
      double sim = 0;
      for (int i = 0; i < t; i++) {
        int i1 = sub[i]; // map to the right index
        sim += i1 <= j ? sims[i1][j] : sims[j][i1];
      }
      value -= w3 * sim / t;

      double cov = 0;
      for (int i = 0; i < t; i++) {
        int i1 = sub[i]; // map to the right index
        cov += i1 <= j ? covs[i1][j] : covs[j][i1];
      }
      value -= w4 * cov / t;
    }

    return value;

  }

  // Covariance
  static int covariance(Hist[] H, int i, int j) {
    int cov = 0;
    for (Hist h : H) {
      cov += h.hist[i] * h.hist[j]; // Assume means are zero
    }
    // !!! using correlation?

    return cov;
  }

  static double[][] covariance(Hist[] H) {
    int n = H.length;
    int m = H[0].hist.length;

    // Convert H to sparse representation
    int[][] P = new int[m][]; // position
    int[][] C = new int[m][]; // count
    int[] bufP = new int[n]; // buffer
    int[] bufC = new int[n];

    for (int i = 0; i < m; i++) {
      int cnt = 0;
      for (int j = 0; j < n; j++) {
        if (H[j].hist[i] != 0) {
          bufP[cnt] = j;
          bufC[cnt++] = H[j].hist[i];
        }
      }
      P[i] = new int[cnt];
      System.arraycopy(bufP, 0, P[i], 0, cnt);
      C[i] = new int[cnt];
      System.arraycopy(bufC, 0, C[i], 0, cnt);
    }

    // Compute covariance, assume mean=0
    double[][] cov = new double[m][m];
    for (int i = 0; i < m; i++) {
      for (int j = i + 1; j < m; j++) {
        int sum = 0;
        int pos1 = 0;
        int pos2 = 0;
        while (pos1 < P[i].length && pos2 < P[j].length) {
          if (P[i][pos1] < P[j][pos2]) {
            pos1++;
          }
          else if (P[i][pos1] > P[j][pos2]) {
            pos2++;
          }
          else {
            sum += C[i][pos1] * C[j][pos2];
            pos1++;
            pos2++;
          }
        }
        cov[i][j] = sum;
      }
    }
    return cov;
  }

  public static void main(String[] args) throws IOException {
    Opt opt = new Opt(args);
    if (opt.args() < 2) {
      System.err.println("Usage: [options] pc_file hist_file pc2_file k");
      System.err.println("  -map_file=FILE \t default=label.map");
      System.err.println("  -w1=DOUBLE \t weight for frequency, default=1");
      System.err.println("  -w2=DOUBLE \t weight for size, default=1");
      System.err.println("  -w3=DOUBLE \t weight for similarity, default=1");
      System.err.println("  -w4=DOUBLE \t weight for covariance, default=1");
      System.exit(1);
    }
    String map_file = opt.getString("map_file", "label.map");
    String pc_file = opt.getArg(0);
    Hist[] H = Hist.loadHists(opt.getArg(1));
    String pc2_file = opt.getArg(2);
    int k = Integer.parseInt(opt.getArg(3));
    double w1 = opt.getDouble("w1", 1);
    double w2 = opt.getDouble("w2", 1);
    double w3 = opt.getDouble("w3", 1);
    double w4 = opt.getDouble("w4", 1);
    LGraph[] B = GraphFile.loadGraphs(pc_file, map_file);
    long time0 = System.currentTimeMillis();
    int[] sub = selectPC(B, H, k, w1, w2, w3, w4);
    PrintStream out = new PrintStream(pc2_file);
    LabelMap labelMap = new LabelMap(map_file);
    for (int i = 0; i < sub.length; i++) {
      LGraph g = B[sub[i]];
      out.println(GraphFile.graph2String(g, labelMap));
      //System.err.print(sub[i]+" ");
    }
    out.close();
    long time1 = System.currentTimeMillis();
    System.err.printf("Time: %.2f sec\n", (time1 - time0) / 1000.0);
  }
}
