package grank.pvalue;

import java.util.*;

import org.apache.commons.math.*;
import org.apache.commons.math.special.*;
import static grank.util.Stat.*;
import grank.util.*;

/**
 *
 * <p>Title: GraphRank</p>
 *
 * Compute the p-value of a feature vector in a set of feature vectors.
 *
 *
 * @author Huahai He
 * @version 1.0
 */
public class PValue {

  //========================================================================

  /**
   * Probability that feature vector X is contained in a feature vector
   * of size Z.
   * Computed through enumeration.
   * @param p Feature probabilities
   * @param X Feature vector X
   * @param z Size of X, i.e., z=\sum x_i
   * @param Z
   * @return P(X,Z)
   */
  public static double probSubset(double[] p, int[] X, int z, int Z) {
    int m = X.length;
    int[] Y = new int[m];
    Result res = new Result();
    enumY(p, X, z, Z, Y, 0, Z - z, res);
    return res.f;
  }

  // Hold the result
  private static class Result {
    double f = 0;
  }

  /**
   * Enumerate every possible Y
   * @param p double[]
   * @param X int[]
   * @param z int
   * @param i Dimension
   * @param Z int
   * @param Y int[]
   * @param rem Remainder of |Y|-|X|
   * @param res Result
   */
  private static void enumY(double[] p, int[] X, int z, int Z, int[] Y, int i,
                            int rem, Result res) {
    int m = X.length;
    if (i == m - 1) { // the last dimension, compute the multinomial
      Y[i] = X[i] + rem;
      res.f += multinomial(p, Y, Z);
      return;
    }
    else {
      for (int k = 0; k <= rem; k++) { // allocate a value to y_i
        Y[i] = X[i] + k;
        enumY(p, X, z, Z, Y, i + 1, rem - k, res);
      }
      return;
    }
  }

  //-------------------------------------------------------------------------

  /**
   * Probability that feature vector X is contained in a feature vector
   * of size Z.
   * Same as probSubset(), but compute through recursion.
   * @param p Feature probabilities
   * @param X Feature vector X
   * @param z Size of X, i.e., z=\sum x_i
   * @param Z
   * @return P(X,Z)
   */
  public static double probSubsetRecursive(double[] p, int[] X, int z, int Z) {
    if (z > Z) {
      return 0;
    }
    int m = X.length;

    // Simplify X by removing x_i's where x_i=0
    double[] p1 = new double[m]; // p_i where x_i>0
    int[] X1 = new int[m]; // x_i where x_i>0
    int m1 = 0;
    double pRest = 0; // sum of p_i where x_i=0
    for (int i = 0; i < m; i++) {
      if (X[i] > 0) {
        p1[m1] = p[i];
        X1[m1] = X[i];
        m1++;
      }
      else {
        pRest += p[i];
      }
    }
    double[] pX1 = probSubsetRecursiveCore(p1, X1, 0, m1, z, Z);
    double sum = 0;
    for (int t = z; t <= Z; t++) {
      sum += pX1[t] * Math.pow(pRest, Z - t) * combination(Z, t);
    }
    return sum;
  }

  /**
   * Same as probSubsetRecursive, but return P(X,s) for s=z to Z
   * @param p double[]
   * @param X int[]
   * @param z int
   * @param Z int
   * @return double[]
   */
  public static double[] probSubsetRecursiveArray(double[] p, int[] X, int z,
                                                  int Z) {
    if (z > Z) {
      return null;
    }
    int m = X.length;

    // Simplify X by removing x_i's where x_i=0
    double[] p1 = new double[m]; // p_i where x_i>0
    int[] X1 = new int[m]; // x_i where x_i>0
    int m1 = 0;
    double pRest = 0; // sum of p_i where x_i=0
    for (int i = 0; i < m; i++) {
      if (X[i] > 0) {
        p1[m1] = p[i];
        X1[m1] = X[i];
        m1++;
      }
      else {
        pRest += p[i];
      }
    }
    double[] pX1 = probSubsetRecursiveCore(p1, X1, 0, m1, z, Z);
    double[] pX = new double[Z + 1];
    Arrays.fill(pX, 0, z, 0); // !!!, necessary?
    for (int s = z; s <= Z; s++) {
      double sum = 0;
      for (int t = z; t <= s; t++) {
        sum += pX1[t] * Math.pow(pRest, s - t) * combination(s, t);
      }
      pX[s] = sum;
      if (pX[s] > 1) {
        pX[s] = 1;
      }
    }
    return pX;
  }

  /**
   * Compute multinomial CDF recursively.
   * Each recursion computes an array P(X,s) for z<=s<=Z,
   * P(X,s)=\sum_{t=a1}^{s-a2} P(X1,t)*P(X2,s-t)*combination(s,t)
   * @param p double[]
   * @param X Current "X" is X[pos]~X[pos+m]
   * @param pos Start position of current "X"
   * @param m Dimension of current "X"
   * @param z Size of current "X"
   * @param Z int
   * @return double[]
   */
  private static double[] probSubsetRecursiveCore(double[] p, int[] X, int pos,
                                                  int m, int z, int Z) {
    if (m >= 2) { // split dimension in half
      int m1 = m / 2; // dimension in the first half
      int m2 = m - m1; // dimension in the second half
      int a1 = sum(X, pos, m1); // sum of X1
      int a2 = sum(X, pos + m1, m2); // sum of X2
      assert (a1 > 0 && a2 > 0 && z == a1 + a2);

      // Recurse
      double[] pX1 = probSubsetRecursiveCore(p, X, pos, m1, a1, Z - a2);
      double[] pX2 = probSubsetRecursiveCore(p, X, pos + m1, m2, a2, Z - a1);

      // Recombine X1 and X2
      double[] pX = new double[Z + 1];

      double com0 = combination(z, a1);
      for (int s = z; s <= Z; s++) { // P(X,s) for z<=s<=Z
        double sum = 0;

        //double com1 = combination(s, a1);
        if (s > z) {
          com0 *= (double) s / (s - a1);
        }
        double com1 = com0;

        for (int t = a1; t <= s - a2; t++) {
          //sum += pX1[t] * pX2[s - t] * combination(s, t);
          if (t > a1) {
            com1 *= (s - t + 1.0) / t;
          }
          sum += pX1[t] * pX2[s - t] * com1;
        }
        pX[s] = sum;
      }
      return pX;
    }
    else { // one dimension
      double[] pX = new double[Z + 1];

      /*for (int s = z; s <= Z; s++) {
        pX[s] = Math.pow(p[pos], s);
             }*/
      double p0 = Math.pow(p[pos], z);
      pX[z] = p0;
      for (int s = z + 1; s <= Z; s++) {
        p0 *= p[pos];
        pX[s] = p0;
      }
      return pX;
    }
  }

  /**
   * Sum of an array
   * @param X int[]
   * @param pos Start position of X
   * @param len int
   * @return int
   */
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

  //========================================================================

  /**
   * Expected number of instances of feature vector X in a feature vector
   * of size Z. Instances can be overlapped.
   * Not used currently.
   * @param p double[]
   * @param X int[]
   * @param n1 int
   * @param n2 int
   * @return double
   */
  public static double instancesOverlap(double[] p, int[] X, int z, int Z) {
    int m = X.length;
    double f = logFac(Z) - logFac(Z - z);
    for (int i = 0; i < m; i++) {
      f -= logFac(X[i]);
    }
    for (int i = 0; i < m; i++) {
      f += X[i] * Math.log(p[i]);
    }
    return Math.exp(f);
  }

  /**
   * Expected number of instances of feature vector X in a feature vector
   * of size Z. Instances cannot be overlapped.
   * Not used currently.
   * @param p double[]
   * @param X int[]
   * @param n1 int
   * @param n2 int
   * @return double
   */
  public static double instancesNonOverlap(double[] p, int[] X, int z, int Z) {
    int m = X.length;
    int[] Y = new int[m];
    Result res = new Result();
    enumYNonOverlap(p, X, z, Z, Y, 0, Z - z, res);
    return res.f;
  }

  private static void enumYNonOverlap(double[] p, int[] X, int z, int Z,
                                      int[] Y, int i, int rem, Result res) {
    int m = X.length;
    if (i == m - 1) { // last dimension
      Y[i] = X[i] + rem;
      int min = Integer.MAX_VALUE; // min of y_i/x_i
      for (int t = 0; t < X.length; t++) {
        if (Y[t] / X[t] < min) {
          min = Y[t] / X[t];
        }
      }
      res.f += multinomial(p, Y, Z) * min;
      return;
    }
    else {
      for (int k = 0; k <= rem; k++) {
        Y[i] = X[i] + k;
        enumY(p, X, z, Z, Y, i + 1, rem - k, res);
      }
      return;
    }
  }

  /**
   * The sizes of graphs in a dataset and the numbers of graphs at each size,
   * i.e., the histogram of graph sizes.
   * @param D int[]
   * @return int[][] {dbZ[], dbN[]}
   */
  public static int[][] dbSizes(int[] D) {
    HashMap<Integer, Integer> sizes = new HashMap<Integer, Integer> ();
    for (int i = 0; i < D.length; i++) {
      Integer cnt = sizes.get(D[i]);
      if (cnt == null) {
        sizes.put(D[i], 1);
      }
      else {
        sizes.put(D[i], cnt + 1);
      }
    }

    int k = sizes.size();
    int[] dbZ = new int[k];
    int[] dbN = new int[k];
    Set<Map.Entry<Integer, Integer>> entries = sizes.entrySet();
    int i = 0;
    for (Map.Entry<Integer, Integer> e : entries) {
      dbZ[i] = e.getKey();
      dbN[i] = e.getValue();
      i++;
    }
    return new int[][] {dbZ, dbN};
  }

  /**
   * Compute the sum of binomial distributions by recursion,
   * @param N int[]
   * @param P double[]
   * @param pos int
   * @param l number of binomials
   * @param minMu minimum support
   * @param maxMu maximum support
   * @return double[]
   */
  public static double[] sumBino(int[] N, double[] P, int pos, int l, int minMu,
                                 int maxMu) {
    assert (minMu <= maxMu);
    if (l >= 2) {
      // Find the ranges of further recursions
      int l1 = l / 2;
      int l2 = l - l1;
      int a1 = sum(N, pos, l1);
      int a2 = sum(N, pos + l1, l2);
      assert (maxMu <= a1 + a2);

      int minMu1 = Math.max(minMu - a2, 0);
      int maxMu1 = Math.min(a1, maxMu);
      int minMu2 = Math.max(minMu - a1, 0);
      int maxMu2 = Math.min(a2, maxMu);
      double[] pX1 = sumBino(N, P, pos, l1, minMu1, maxMu1);
      double[] pX2 = sumBino(N, P, pos + l1, l2, minMu2, maxMu2);
      double[] pX = new double[maxMu + 1];
      for (int s = minMu; s <= maxMu; s++) {
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
      assert (maxMu <= N[pos]);

      double[] pX = new double[maxMu + 1];
      int n = N[pos];
      double p = P[pos];
      /*
             for (int s = z; s <= Z; s++) {
        pX[s] = Math.pow(p, s) * Math.pow(1 - p, n - s) *
            combination(n, s);
             }*/

      if (p >= 1) { // if p==1, then pX[n]=1, pX[i]=0 for i!=n
        for (int s = minMu; s <= maxMu; s++) {
          pX[s] = 0;
        }
        if (maxMu == n) {
          pX[maxMu] = 1;
        }
      }
      else { // reuse common expression
        double com0 = Math.pow(p, minMu) * Math.pow(1 - p, n - minMu) *
            combination(n, minMu);
        pX[minMu] = com0;
        double com1 = p / (1 - p);
        for (int s = minMu + 1; s <= maxMu; s++) {
          com0 *= com1 * (n - s + 1) / s;
          pX[s] = com0;
        }
      }

      return pX;
    }
  }

  /**
   * Compute the sum of binomial distributions by a heuristic binomial.
   *
   * @param N int[]
   * @param P double[]
   * @param mu0 int
   * @return double
   */
  public static double sumBinoApprox(int[] N, double[] P, int mu0) {
    double tmp1 = 0; // \sum n_i * p_i
    double tmp2 = 0;

    for (int i = 0; i < N.length; i++) { // \sum n_i * p_i^2
      tmp1 += N[i] * P[i];
      tmp2 += N[i] * P[i] * P[i];
    }
    double binoN = tmp1 * tmp1 / tmp2; // effective binomial N
    double binoP = tmp2 / tmp1; // effective binomial P

    double pvalue;
    if (binoN <= 10000) {
      pvalue = Stat.binormialDist(binoP, (int) (binoN + 0.5), mu0);
    }
    else {
      double mu = tmp1;
      double sigma = Math.sqrt(mu * (1 - binoP));
      pvalue = Stat.normalDist(mu, sigma, mu0);
    }
    //double pvalue=Beta.regularizedBeta(binoP,mu0,binoN-mu0+1);

    return pvalue;
  }

  /**
   * Compute the sum of binomial distributions by simulation
   * @param N int[]
   * @param P double[]
   * @param mu0 int
   * @return double
   */
  public static double sumBinoSimu(int[] N, double[] P, int mu0) {
    int times = 1000;
    Random rand = new Random(1);
    int cnt = 0;
    for (int t = 0; t < times; t++) {
      int mu = 0;
      for (int i = 0; i < N.length; i++) {
        for (int j = 0; j < N[i]; j++) {
          double dice = rand.nextDouble();
          if (dice <= P[i]) {
            mu++;
          }
        }
      }
      if (mu >= mu0) {
        cnt++;
      }
    }
    return (double) cnt / times;
  }

  //========================================================================

  /**
   * Compute p-value of X. The sum of Binomials is approximated by
   * a heuristic Binomial.
   *
   * @deprecated
   * @param p double[]
   * @param X int[]
   * @param Z int[]
   * @param N int[]
   * @param mu0 Real support of X
   * @return REntry
   */
  public static REntry pvalueApproxBino(double[] p, int[] X, int[] dbZ,
                                        int[] dbN, int sup) {
    int z = sum(X); // size of X
    assert (dbZ.length == dbN.length);
    int k = dbZ.length; // number of distinct Zs
    double[] probs = new double[k]; // probs[i] is P(X,Z[i])

    // find maximum Z

    int maxZ = -1;
    for (int i = 0; i < k; i++) {
      if (dbZ[i] > maxZ) {
        maxZ = dbZ[i];
      }
    }
    double[] pZ = probSubsetRecursiveArray(p, X, z, maxZ);
    for (int i = 0; i < k; i++) {
      probs[i] = pZ[dbZ[i]];
      if (probs[i] > 1) {
        probs[i] = 1;
      }
    }

    double tmp1 = 0; // \sum n_i * p_i
    double tmp2 = 0;
    for (int i = 0; i < k; i++) { // \sum n_i * p_i^2
      tmp1 += dbN[i] * probs[i];
      tmp2 += dbN[i] * probs[i] * probs[i];
    }
    double binoN = tmp1 * tmp1 / tmp2; // effective binomial N
    double binoP = tmp2 / tmp1; // effective binomial P

    double pvalue;
    if (binoN <= 10000) {
      pvalue = Stat.binormialDist(binoP, (int) (binoN + 0.5), sup);
    }
    else {
      double mu = tmp1;
      double sigma = Math.sqrt(mu * (1 - binoP));
      pvalue = Stat.normalDist(mu, sigma, sup);
    }
    //System.err.printf("pvalue=%g\n",pvalue);
    //System.out.printf("mu=%f\n",binoN*binoP);
    //double pvalueSimu=sumBinoSimu(N,probs,mu0);
    //System.err.printf("pvalue=%g, pvalue_sim=%g\n",pvalue,pvalueSimu);
    return new REntry(pvalue, tmp1, sup);
  }

  /**
   * Compute p-value. The sum of binomials is computed by recursion.
   * @param p double[]
   * @param X int[]
   * @param Z Z[i] is a size of database histograms
   * @param N N[i] is the number of database histograms with size Z[i]
   * @param sup Actual support
   * @return REntry
   */
  public static REntry pvalue(double[] p, int[] X, int[] dbZ, int[] dbN,
                              int sup) {
    int z = sum(X); // size of X
    assert (dbZ.length == dbN.length);
    int k = dbZ.length; // number of distinct Zs
    double[] probs = new double[k]; // probs[i] is P(X,Z[i])

    // find maximum Z

    int maxZ = -1;
    for (int i = 0; i < k; i++) {
      if (dbZ[i] > maxZ) {
        maxZ = dbZ[i];
      }
    }
    double[] pZ = probSubsetRecursiveArray(p, X, z, maxZ);
    for (int i = 0; i < k; i++) {
      probs[i] = pZ[dbZ[i]];
      if (probs[i] > 1) {
        probs[i] = 1;
      }
    }

    /*
         for (int i = 0; i < k; i++) {
      //binoP[i] = probSubset(p,X,z,Z[i]);

      //probs[i] = probSubset(p, X, z, Z[i]);  //debug
      probs[i] = probSubsetRecursive(p, X, z, Z[i]);
      // prob. might be great than 1 due to accumulative error
      if (probs[i] > 1) {
        probs[i] = 1;
      }
         }
     */

    int nG = sum(dbN); // number of database graphs
    double[] pX = sumBino(dbN, probs, 0, dbN.length, sup, nG);
    double cdf = 0;
    for (int i = sup; i <= nG; i++) {
      cdf += pX[i];
    }
    if (cdf > 1) {
      cdf = 1;
    }

    double mean = 0;
    for (int i = 0; i < dbN.length; i++) {
      mean += dbN[i] * probs[i];
    }
    return new REntry(cdf, mean, sup);

  }

  /**
   * Compute p-value given P(X,N)
   */
  public static double computePvalue(double[] prob, int[] dbZ, int[] dbN,
                                     int nG, int sup) {
    assert (dbZ.length == dbN.length);
    int k = dbZ.length;
    double[] P = new double[k];
    for (int i = 0; i < k; i++) {
      P[i] = prob[dbZ[i]];
      assert (P[i] <= 1);
    }

    double pvalue = 0;
    if (nG <= 50000) {  // !!!
      double[] pX = PValue.sumBino(dbN, P, 0, k, sup, nG);
      for (int i = sup; i <= nG; i++) {
        pvalue += pX[i];
      }
      if (pvalue > 1) {
        pvalue = 1;
      }
    }
    else { // database too large
      pvalue = PValue.sumBinoApprox(dbN, P, sup);
    }
    return pvalue;
  }

  public static double computePvalue(double[] dbP, int[] dbN,
                                     int nG, int sup) {
    int k = dbP.length;

    double pvalue = 0;
    if (nG <= 50000) {  // !!!
      double[] pX = PValue.sumBino(dbN, dbP, 0, k, sup, nG);
      for (int i = sup; i <= nG; i++) {
        pvalue += pX[i];
      }
      if (pvalue > 1) {
        pvalue = 1;
      }
    }
    else { // database too large
      pvalue = PValue.sumBinoApprox(dbN, dbP, sup);
    }
    return pvalue;
  }

  /**
   * Fast upper bound of P(X,N) by regularized Beta functions
   * @param p double[]
   * @param X int[]
   * @param Z int
   * @return double
   * @throws MathException
   */
  public static double upperProb(double[] p, int[] X, int Z) throws
      MathException {
    double prod = 1;
    for (int i = 0; i < X.length; i++) {
      if (X[i] == 0) {
        continue;
      }
      double sum = Beta.regularizedBeta(p[i], X[i], Z - X[i] + 1);
      prod *= sum;
    }
    return prod;
  }

  /**
   * Fast lower bound of P(X,N) by regularized Beta functions
   * @param p double[]
   * @param X int[]
   * @param Z int
   * @return double
   */
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

  public static void main(String[] args) {
    // Feature probabilities
    int m = 5;
    //A-B,A-C,B-B,B-C,C-C
    double[] p = {6 / 17.0, 2 / 17.0, 3 / 17.0, 5 / 17.0, 1 / 17.0};
    System.out.print("p[] = ");
    for (int i = 0; i < m; i++) {
      System.out.printf("%f ", p[i]);
    }
    System.out.println();

    int[] dbZ = {3, 4}; // Zs
    int[] dbN = {3, 2}; // number of graphs of Z_i

    // Feature vectors of subgraphs
    // g_1: A-B
    // g_2: A-B-B
    // g_3: A-B-B2, A-B2
    // g_4: A-B1-B, B1-C
    // g_5: A-B1-B2, A-B2, B1-C

    int[][] Xs = { {1, 0, 0, 0, 0}, {1, 0, 1, 0, 0}, {2, 0, 1, 0, 0},
        {1, 0, 1, 1, 0}, {2, 0, 1, 1, 0}
    };
    int[] mu0s = {4, 3, 2, 2, 1}; // real supports

    for (int i = 0; i < Xs.length; i++) {
      REntry res = pvalue(p, Xs[i], dbZ, dbN, mu0s[i]);
      System.out.printf("g%d: mu0=%d, pvalue=%g, mean=%f\n", i + 1, res.mu0,
                        res.pvalue,
                        res.mean);
    }

    int[] X = Xs[2];
    double p1 = probSubsetRecursive(p, X, sum(X), 3);
    double p2 = probSubsetRecursive(p, X, sum(X), 4);
    System.out.printf("P(g3, 3) = %g, P(g3, 4) = %g\n", p1, p2);
    System.out.println("Support distribution of g2:");
    for (int mu0 = 0; mu0 <= 5; mu0++) {
      REntry res = pvalue(p, X, dbZ, dbN, mu0);
      double prob;
      if (mu0 < 5) {
        REntry res1 = pvalue(p, X, dbZ, dbN, mu0 + 1);
        prob = res.pvalue - res1.pvalue;
      }
      else {
        prob = res.pvalue;
      }
      System.out.printf("%d %g\n", mu0, prob);
    }
  }
}
