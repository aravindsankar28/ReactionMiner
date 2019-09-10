package grank.probe;

import static java.lang.Math.*;
import java.util.*;

import org.apache.commons.math.*;
import org.apache.commons.math.special.*;
import static grank.pvalue.PValue.*;
import grank.pvalue.*;
import grank.util.*;

/**
 * Plot approximate multinomial CDF vs. x_i
 * Lower / Upper bounds of P(X,N)
 */
public class ApproxMult {

  // 1 - \sum P(y_i<x_i), lower bound
  static double appro(double[] p, int[] X, int N) {
    int m = X.length;
    double sum = 0;
    for (int i = 0; i < m; i++) {
      double sum1 = 0;
      for (int t = 0; t < X[i]; t++) {
        sum1 += pow(p[i], t) * pow(1 - p[i], N - t) * Stat.combination(N, t);
      }
      sum += sum1;
    }
    return 1 - sum;
  }

  // \prod \sum^{N-(z-x_i)} p_i^t (1-p_i)^(N-t)(N,t), neither lower nor upper
  static double appro3(double[] p, int[] X, int N) {
    double prod = 1;
    int z = sum(X);
    for (int i = 0; i < X.length; i++) {
      double sum = 0;
      int a2 = z - X[i];
      for (int t = X[i]; t <= N - a2; t++) {
        sum += pow(p[i], t) * pow(1 - p[i], N - t) * Stat.combination(N, t);
      }
      prod *= sum;
    }
    return prod;
  }

  // Lower bound, proximate
  static double approLower(double[] p, int[] X, int N) {
    double prod = 1;
    int a = N;
    for (int i = 0; i < X.length; i++) {
      double sum = 0;
      for (int t = X[i]; t <= a; t++) {
        sum += pow(p[i], t) * pow(1 - p[i], a - t) * Stat.combination(a, t);
      }
      prod *= sum;
      a -= X[i];
    }
    return prod;
  }

  // Lower bound, proximate
  static double approLower1(double[] p, int[] X, int N) {
    int m = X.length;
    double[] p1 = new double[m];
    System.arraycopy(p, 0, p1, 0, m);
    int[] X1 = new int[m];
    System.arraycopy(X, 0, X1, 0, m);
    for (int i = 0; i < m - 1; i++) {
      for (int j = i + 1; j < m; j++) {
        if (p1[i] > p1[j]) {
          int tmp = X1[i];
          X1[i] = X1[j];
          X1[j] = tmp;
          double tmp1 = p1[i];
          p1[i] = p1[j];
          p1[j] = tmp1;
        }
      }
    }

    double prod = 1;
    for (int i = 0; i < m; i++) {
      double sum = 0;
      int a = N - sum(X1, 0, i);
      for (int t = X1[i]; t <= a; t++) {
        sum += pow(p1[i], t) * pow(1 - p1[i], a - t) * Stat.combination(a, t);
      }
      prod *= sum;
    }
    return prod;
  }

  public static double approLower2(double[] p, int[] X, int Z) throws
      MathException {
    double prod = 1;
    int a = Z;
    for (int i = 0; i < X.length; i++) {
      double sum = Beta.regularizedBeta(p[i], X[i], a - X[i] + 1);
      prod *= sum;
      a -= X[i];
    }
    return prod;
  }

  // By Chernoff bounds
  // P[X(n)<=(1-epsilon)pn]<=e^{-epsilon^2 np/2}
  public static double approLower3(double[] p, int[] X, int Z) throws
      MathException {
    double prod = 1;
    int a = Z;
    for (int i = 0; i < X.length; i++) {
      if(X[i]==0) continue;
      //double sum = Beta.regularizedBeta(p[i], X[i], a - X[i] + 1);

      double epsilon=1-(X[i]-1)/(p[i]*a);
      double sum = 1-Math.exp(-epsilon*epsilon*a*p[i]/2);

      //double epsilon = (X[i]) / (p[i] * a) - 1;
      //double sum = Math.exp( -epsilon * epsilon * a * p[i] / 3);

      prod *= sum;
      a -= X[i];
    }
    return prod;
  }

  // Upper bound, proximate
  static double approUpper(double[] p, int[] X, int N) {
    double prod = 1;
    int z = sum(X);
    for (int i = 0; i < X.length; i++) {
      double sum = 0;
      for (int t = X[i]; t <= N; t++) {
        sum += pow(p[i], t) * pow(1 - p[i], N - t) *
            Stat.combination(N, t);
      }
      prod *= sum;
    }
    return prod;
  }

  static double approUpper1(double[] p, int[] X, int N) throws MathException {
    double prod = 1;
    int z = sum(X);
    for (int i = 0; i < X.length; i++) {
      double sum = Beta.regularizedBeta(p[i], X[i], N - X[i] + 1);
      prod *= sum;
    }
    return prod;
  }

  // By Chernoff bounds
  static double approUpper3(double[] p, int[] X, int N) throws MathException {
    double prod = 1;
    int z = sum(X);
    for (int i = 0; i < X.length; i++) {
      //double sum = Beta.regularizedBeta(p[i], X[i], N - X[i] + 1);
      double epsilon = (X[i]) / (p[i] * N) - 1;
      double sum = Math.exp( -epsilon * epsilon * N * p[i] / 3);
      prod *= sum;
    }
    return prod;
  }

  public static void main(String[] args) throws MathException {
    // Feature probabilities
    int m = 5;
    //double[] p = {0.5, 0.3, 0.2};
    double[] p = new double[m];
    Random rand = new Random(1);
    for (int i = 0; i < m; i++) {
      p[i] = rand.nextDouble();
    }
    double sum = 0;
    for (int i = 0; i < m; i++) {
      sum += p[i];
    }
    for (int i = 0; i < m; i++) {
      p[i] /= sum;
    }

    int[] X = new int[m];
    for (int i = 0; i < m; i++) {
      X[i] = rand.nextInt(3) + 1;
    }

    for (int i = 0; i < m - 1; i++) {
      for (int j = i + 1; j < m; j++) {
        if (p[i] > p[j]) {
          //if (X[i] > X[j]) {
          double tmp = p[i];
          p[i] = p[j];
          p[j] = tmp;
          int tmp2 = X[i];
          X[i] = X[j];
          X[j] = tmp2;
        }
      }
    }

    int z = PValue.sum(X);
    int N = Math.min(200, z * 4);

    int i = 2;
    int a2 = z - X[i];
    int[] X1 = new int[m];
    System.arraycopy(X, 0, X1, 0, m);
    for (int t = X[i]; t <= N - a2; t++) {
      X[i] = t;
      double p1 = PValue.probSubsetRecursive(p, X, PValue.sum(X), N);
      X1[i] = t - 1;
      double p0 = PValue.probSubsetRecursive(p, X1, PValue.sum(X1), N);

      // y_i is always x_i-1, upper bound
      double p2 = p0 * (1 - Math.pow(1 - p[i], N - sum(X1)));

      // P(X_2,N-x_i) ~ (1-p_i)^(N-x_i), lower bound
      double p3 = p0 -
          Math.pow(p[i], X1[i]) * Math.pow(1 - p[i], N - X1[i]) *
          Stat.combination(N, X1[i]);

      double p4 = appro(p, X, N);
      double p5 = appro3(p, X, N);
      double p6 = approLower(p, X, N);
      double p7 = approUpper(p, X, N);
      double p8 = approUpper1(p, X, N);
      double p9 = approLower2(p, X, N);
      double p10 = approLower3(p, X, N);
      double p11 = approUpper3(p, X, N);

      System.out.printf("%d %g %g %g %g %g %g %g\n", t, p1, p6, p9, p7, p8, p10,
                        p11);
    }
    long time0 = System.currentTimeMillis();
    for (int t = 0; t < 1000; t++) {
      double tmp = approUpper(p, X, N);
    }
    long time1 = System.currentTimeMillis();
    for (int t = 0; t < 1000; t++) {
      double tmp = approUpper1(p, X, N);
    }
    long time2 = System.currentTimeMillis();
    System.out.printf("%f, %f\n", (time1 - time0) / 1000.0,
                      (time2 - time1) / 1000.0);
    System.out.println(Beta.regularizedBeta(0.5, 6, 5));
    System.out.println(Beta.regularizedBeta(0.5, 0, 3));
    //System.out.printf("%g\n",Double.MIN_VALUE);
  }
}
