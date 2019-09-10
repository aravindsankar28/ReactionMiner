package grank.probe;

import grank.util.*;
import static grank.pvalue.PValue.*;
import grank.pvalue.*;

// Search counter examples
public class CounterExample {

  // search counter example P(X3, N)<P(X1, N) and P(X3, N)<P(X2, N)
  // X1=(x1, x2+d), X2=(x1+d, x2), X3=(x1+d1, x2+d2) where d1+d2=d
  private static void search1() {
    int x1 = 0;
    int x2 = 5;
    double[] P = {0.1, 0.11};
    for (int d = 2; d < 50; d++) {
      int z = x1 + x2 + d;
      int[] X1 = {x1, x2 + d};
      int[] X2 = {x1 + d, x2};
      System.out.printf("d=%d\n", d);
      for (int N = z; N < 100; N++) {
        double u1 = PValue.probSubsetRecursive(P, X1, z, N);
        double u2 = PValue.probSubsetRecursive(P, X2, z, N);
        /*if (u2 < u1) {
          System.out.printf("N=%d: %g, %g\n", N, u1, u2);
          break;
                 }*/

        for (int d1 = 1; d1 < d; d1++) {
          int d2 = d - d1;
          int[] X3 = {x1 + d1, x2 + d2};
          double u3 = PValue.probSubsetRecursive(P, X3, z, N);
          if (u3 < u1 && u3 < u2) {
            System.out.printf("N=%d: %g, %g, %g\n", N, u1, u2, u3);
            System.out.printf("%g, %g\n", u1 - u3, u2 - u3);
            break;
          }
        }
        //if(u1>u2) break;
      }
    }
  }

  private static void search2() {
    int x1 = 0;
    int x2 = 10;
    double p1 = 0.05;
    double p2 = 0.1;
    boolean flag = false;
    for (int d = 2; d < 100; d++) {
      int k = x1 + x2 + d;
      double v1 = Stat.combination(k, x1 + d) * Math.pow(p1, d);
      double v2 = Stat.combination(k, x1) * Math.pow(p2, d);

      for (int d1 = 1; d1 < d; d1++) {
        int d2 = d - d1;
        double v3 = Stat.combination(k, x1 + d1) * Math.pow(p1, d1) *
            Math.pow(p2, d2);
        if (v3 < v1 && v3 < v2) {
          System.out.println(v3);
        }
      }
      System.out.printf("%d: %g, %g\n", d, v1, v2);
      if (v1 < v2 && !flag) {
        System.out.println();
        flag = true;
      }
    }

  }

  public static void main(String[] args) {
    search1();
  }
}
