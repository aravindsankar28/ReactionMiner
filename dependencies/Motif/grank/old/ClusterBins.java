package grank.old;

// incomplete
public class ClusterBins {

  public static void cluster(int[][] H, int m, int k) {
    int n = H.length; // number of points

    assert (H[0].length == m);
    assert (m >= k);
    // mean and variance
    double[] mean = new double[m];
    double[] var = new double[m];
    for (int i = 0; i < m; i++) {
      // mean
      double tmp = 0;
      for (int j = 0; j < n; j++) {
        tmp += H[j][i];
      }
      mean[i] = tmp / n;

      // variance
      tmp = 0;
      for (int j = 0; j < n; j++) {
        tmp += (H[j][i] - mean[i]) * (H[j][i] - mean[i]);
      }
      var[i] = tmp / n;
    }

    //covariance or correlation
    double[][] cov = new double[m][m];
    for (int i = 0; i < m; i++) {
      for (int j = i; j < m; j++) {
        double tmp = 0;
        for (int t = 0; t < n; t++) {
          tmp += H[t][i] * H[t][j];
        }

        cov[i][j] = (tmp / n - mean[i] * mean[j]) / Math.sqrt(var[i] * var[j]);
        cov[j][i] = cov[i][j];
      }
    }
  }
}
