package grank.old;

/**
 * Reduce bins of a histogram, minimzing sum of squared errors.
 *
 * Reference:
 * Optimal Histograms with Quality Guarantees, H. V. Jagadish, N. Koudas,
 * S. Muthukrishnan, V. Poosala, K. C. Sevcik, and T. Suel, VLDB 1998
 *
 * OPT[k,m]: the optimal variance of data in the range [1..m] using k buckets.
 * OPT[k,m] = min {OPT[k-1,x] + SSE[(x+1)..m]}, x<m, k-1<=x, k<=m
 *
 *
 */
public class VOptimal {
  /**
   *
   * @param p double[]
   * @param m Original dimensions
   * @param k Reduced dimensions
   * @return boundaries, boundary[i] is the right boundary of bucket i (exclusively)
   */
  public static int[] V_Optimal(double[] p, int m, int k) {
    assert (p.length == m);
    double[] sse = new double[m + 1]; // sum of squared errors
    double[] opt0 = new double[m + 1];
    double[] opt1 = new double[m + 1];

    // compute SSE and OPT[k=1,1..m]
    for (int x = 1; x <= m; x++) {
      sse[x] = SSE(p, x, m); // [x, m)
      opt0[x] = SSE(p, 0, x); // [0,x)
    }

    int[][] back = new int[k + 1][m + 1]; // back track

    for (int k1 = 2; k1 <= k; k1++) { // number of buckets
      for (int m1 = k1; m1 <= m; m1++) {
        double min = Double.MAX_VALUE;
        for (int x = k1 - 1; x < m1; x++) {
          double tmp = opt0[x] + SSE(p,x,m1);//sse[x];
          if (tmp < min) {
            min = tmp;
            back[k1][m1] = x;
          }
        }
        opt1[m1] = min;
      }
      System.arraycopy(opt1, 0, opt0, 0, m + 1);
    }

    // boundary[i]: right boundary of bucket i (exclusively)
    int[] boundary = new int[k];
    boundary[k - 1] = m;
    int m1 = m;
    for (int k1 = k; k1 > 1; k1--) {
      boundary[k1 - 2] = back[k1][m1];
      m1 = boundary[k1 - 2];
    }

    System.err.printf("Optimal SSE: %f\n", opt1[m]);

    return boundary;
  }

  /**
   * Map each bin to its bucket.
   * @param boundary int[]
   * @param m int
   * @return int[]
   */
  public static int[] boundary2map(int[] boundary, int m) {
    int[] map = new int[m];
    int idx = 0;
    for (int i = 0; i < m; i++) {
      if (i < boundary[idx]) {
        map[i] = idx;
      }
      else {
        idx++;
        map[i] = idx;
      }
    }

    return map;
  }

  /**
   * Sum of squared errors
   * @param p double[]
   * @param begin inclusive
   * @param end exclusive
   * @return double
   */
  private static double SSE(double[] p, int begin, int end) {
    double mean = 0;
    for (int i = begin; i < end; i++) {
      mean += p[i];
    }
    mean /= end - begin;
    double var = 0;
    for (int i = begin; i < end; i++) {
      var += (p[i] - mean) * (p[i] - mean);
    }
    return var;
  }

  public static void main(String[] args) {
    double[] p = {10, 10, 5, 4, 4, 2};
    int[] boundary = V_Optimal(p, p.length, 3);
    System.err.print("Boundary: ");
    for (int i = 0; i < boundary.length; i++) {
      System.err.printf("%4d", boundary[i]);
    }
    System.err.println();
  }
}
