package grank.util;

import java.util.*;

/**
 * Common probability functions.
 *
 * @author Huahai He
 * @version 1.0
 */
public class Stat {

  // A precomputed array of log factorial(N)
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
   * Factorial
   */
  public static double factorial(int n) {
    if (n > 20) {
      return Math.exp(logFac(n));
    }
    long f = 1;
    for (int i = 2; i <= n; i++) {
      f *= i;
    }
    return f;
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

    long f = 1;  // must be an integer
    for (int i = N; i > N - n; i--) {
      f *= i;
    }
    for(int i=n;i>1;i--) {
      f/=i;
    }

    /*double f=1;
    for(int i=n;i>0;i--) {
      f*=(double)(N-n+i)/i;
    }*/
    return f;

  }

  /**
   * Binormial probability function
   * @param p double
   * @param N Number of trials
   * @param n Number of successes
   * @return double
   */
  public static double binormial(double p, int N, int n) {
    double f = logFac(N) - logFac(n) - logFac(N - n) + n * Math.log(p) +
        (N - n) * Math.log(1 - p);
    return Math.exp(f);
  }

  /**
   * Binormial cumulative distribution function, i.e., at least n0 success
   * @param p double
   * @param N int
   * @param n0 int
   * @return double
   */
  public static double binormialDist(double p, int N, int n0) {
    double f = 0;
    for (int i = n0; i <= N; i++) {
      f += binormial(p, N, i);
    }
    return f;
  }

  /**
   * p.d.f. of a multinomial ditribution.
   * @param p double[] the probability of each event
   * @param X int[] the number of occurrences of each event
   * @param N \sum x_i
   * @return The probability P(X=[x_1 ... x_m])
   */
  public static double multinomial(double[] p, int[] X, int N) {
    double f = logFac(N);
    for (int i = 0; i < X.length; i++) {
      f -= logFac(X[i]);
    }
    for (int i = 0; i < X.length; i++) {
      f += X[i] * Math.log(p[i]);
    }
    return Math.exp(f);
  }

  // Constants of Hastings' formula.
  private static double a0 = 0.47047;
  private static double a1 = 0.3084284;
  private static double a2 = -0.0849713;
  private static double a3 = 0.6627698;
  private static double sqrt2 = Math.sqrt(2);
  private static double sqrtpi = Math.sqrt(Math.PI);
  /**
   * Calculate the standard normal distribution function, i.e., P(X>x), using
   * Hastings' formula.
   *
   * Reference:
   *   Oscar Kempthorne and Leroy Folks. Probability, Statistics, and Data Analysis. page 97.
   *
   * @param x (-infty,+infty)
   * @return double
   */
  public static double normalDist(double x) {
	  //int n=1/0;
    if (x == 0) {
      return 0.5;
    }
    else if (x > 0) {
      double eta = 1 / (1 + a0 * x / sqrt2);
      double psi = eta * (a1 + eta * (a2 + a3 * eta)) * Math.exp( -x * x / 2) /
          sqrtpi;
      return psi;
    }
    else {
      return 1 - normalDist( -x);
    }
  }

  public static double normalDist(double mu, double sigma, double mu0) {
    double cdf = normalDist( (mu0 - mu) / sigma);
    return cdf;
  }

  /**
   * Poisson probability function
   * @param lambda int
   * @param n int
   * @return double
   */
  public static double poisson(int lambda, int n) {
    double log = Math.log(lambda) * n - lambda - logFac(n);
    return Math.exp(log);
  }

  /**
   * Return the next Poisson value
   * @param lambda the mean value
   * @param rand Random
   * @return int
   */
  public static int nextPoisson(double lambda, Random rand) {
    double elambda = Math.exp( -1 * lambda);
    int x = 0;
    double product = 1;
    while (product >= elambda) {
      product *= rand.nextDouble();
      x++;
    }
    return x > 0 ? x - 1 : 0;
  }
}
