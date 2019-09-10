package grank.pvalue;


//import org.apache.math.special.*;
/**
 *
 * @author Huahai He
 * @version 1.0
 */
public class PValueNative {
  static {
    System.loadLibrary("libgslcblas");
    System.loadLibrary("libgsl");
  }

  /**
   *
   * @param p double[]
   * @param X int[]
   * @param Z int
   * @return double
   */
  public static native double lowerProb(double[] p, int[] X, int Z);

  /*
     public static double lowerProb(double[] p, int[] X, int Z) throws
    MathException {
     double prod = 1;
     int a = Z;
     //for (int i = 0; i < X.length; i++) {
     for (int i = X.length - 1; i >= 0; i--) {
    if (X[i] == 0) {
      continue;
    }
    double sum = Beta.regularizedBeta(p[i], X[i], a - X[i] + 1);
    prod *= sum;
    a -= X[i];
     }
     return prod;
   }
   */
  public static double computePvalue(double[] dbP, int[] dbN,
                                     int nG, int sup) {
    int k = dbP.length;

    double[] pX = PValue.sumBino(dbN, dbP, 0, k, sup, nG);
    double pvalue = 0;
    for (int i = sup; i <= nG; i++) {
      pvalue += pX[i];
    }
    if (pvalue > 1) {
      pvalue = 1;
    }
    //double pvalue = PValue.sumBinoApprox(dbN, P, sup);
    return pvalue;
  }

  public static double[] sumBino(int[] N, double[] P, int pos, int m, int z,
                                 int Z) {
    assert (z <= Z);
    if (m >= 2) {
      int m1 = m / 2;
      int m2 = m - m1;
      int a1 = sum(N, pos, m1);
      int a2 = sum(N, pos + m1, m2);
      assert (Z <= a1 + a2);

      int z1 = Math.max(z - a2, 0);
      int Z1 = Math.min(a1, Z);
      int z2 = Math.max(z - a1, 0);
      int Z2 = Math.min(a2, Z);
      double[] pX1 = sumBino(N, P, pos, m1, z1, Z1);
      double[] pX2 = sumBino(N, P, pos + m1, m2, z2, Z2);
      double[] pX = new double[Z + 1];
      for (int s = z; s <= Z; s++) {
        double sum = 0;
        int min1 = Math.max(s - a2, 0);
        int max1 = Math.min(a1, s);
        for (int t = min1; t <= max1; t++) {
          sum += pX1[t] * pX2[s - t];
        }
        pX[s] = sum;
      }
      return pX;
    }
    else {
      assert (Z <= N[pos]);

      double[] pX = new double[Z + 1];
      int n = N[pos];
      double p = P[pos];
      /*
             for (int s = z; s <= Z; s++) {
        pX[s] = Math.pow(p, s) * Math.pow(1 - p, n - s) *
            combination(n, s);
             }*/

      if (p >= 1) { // if p==1, then pX[n]=1, pX[i]=0 for i!=n
        for (int s = z; s <= Z; s++) {
          pX[s] = 0;
        }
        if (Z == n) {
          pX[Z] = 1;
        }
      }
      else { // reuse common expression
        double com0 = Math.pow(p, z) * Math.pow(1 - p, n - z) *
            combination(n, z);
        pX[z] = com0;
        double com1 = p / (1 - p);
        for (int s = z + 1; s <= Z; s++) {
          com0 *= com1 * (n - s + 1) / s;
          pX[s] = com0;
        }
      }

      return pX;
    }
  }

  private static final int MAXN = 10000;
  private static final double[] logFac1 = new double[MAXN + 1];
  static {
    logFac1[0] = 0;
    logFac1[1] = 0;
    for (int i = 2; i <= MAXN; i++) {
      logFac1[i] = logFac1[i - 1] + Math.log(i);
    }
  }

  /**
   * Log factorial. When N is larger than MAXN, it is approximated by
   * Stirling's formula: ln n! = n ln n - n
   * @param N int
   * @return double
   */
  public static double logFac(int N) {
    if (N <= MAXN) {
      return logFac1[N];
    }
    else {
      return N * Math.log(N) - N;
    }
  }

  /**
   * Combination, i.e., N choosing n
   */
  public static double combination(int N, int n) {
    if (n == 0 || n == N) {
      return 1;
    }
    if (N > 20) {
      return Math.exp(logFac(N) - logFac(n) - logFac(N - n));
    }

    if (n > N - n) {
      n = N - n;
    }

    long f = 1; // must be an integer
    for (int i = N; i > N - n; i--) {
      f *= i;
    }
    for (int i = n; i > 1; i--) {
      f /= i;
    }

    /*double f=1;
       for(int i=n;i>0;i--) {
      f*=(double)(N-n+i)/i;
       }*/
    return f;

  }

  public static int sum(int[] X, int pos, int len) {
    int s = 0;
    for (int i = 0; i < len; i++) {
      s += X[pos + i];
    }
    return s;
  }

  public static int sum(int[] X) {
    int s = 0;
    for (int i = 0; i < X.length; i++) {
      s += X[i];
    }
    return s;
  }

  public static void main(String[] args) {
    PValueNative pvaluenative = new PValueNative();
  }
}
