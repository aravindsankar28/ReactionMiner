package grank.probe;

import java.util.*;
import grank.pvalue.*;

// Plot multinomial PDF and CDF vs. N
public class PlotMultinomial {

  public static void main(String[] args) {
    int m = 7;
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
      X[i] = rand.nextInt(2);
    }
    int z = PValue.sum(X);
    double[] p1 = new double[m];
    for (int i = 0; i < m; i++) {
      p1[i] = (double) X[i] / z;
    }
    int[] X1 = new int[m];
    System.arraycopy(X, 0, X1, 0, m);
    int Z = 800;
    double[] prob = new double[Z];

    for (int s = z; s < Z; s++) {
      if (s % 100 == 0) {
        System.err.println(s);
      }
      prob[s] = PValue.probSubsetRecursive(p, X, z, s);
      if (prob[s] > 0.9999) {
        Arrays.fill(prob, s + 1, Z, 1);
        break;
      }
    }
    for (int s = z + 1; s < Z; s++) {
      System.out.printf("%d %g %g\n", s, prob[s] - prob[s - 1], prob[s]);
    }
  }
}
