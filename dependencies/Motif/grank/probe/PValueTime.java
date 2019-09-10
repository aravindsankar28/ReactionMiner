package grank.probe;

import java.util.*;

import org.apache.commons.math.*;
import grank.pvalue.*;

/**
 * Evaluate the performance of pvalue computation (both accurate and lower bound)
 * using random data.
 * @author Huahai He
 * @version 1.0
 */
public class PValueTime {
  // Generate a histogram with a given size
  static void alloc(int[] X, int z, int pos, int len, Random rand) {
    if (len <= 1) {
      X[pos] = z;
    }
    else if (z == 0) {
      Arrays.fill(X, pos, pos + len, 0);
    }
    else {
      int a = rand.nextInt(z);
      int len1 = len / 2;
      int len2 = len - len1;
      alloc(X, a, pos, len1, rand);
      alloc(X, z - a, pos + len1, len2, rand);
    }
  }

  public static void main(String[] args) throws MathException {

    Random rand=new Random(1);
    // Generate dbZ, dbN
    int nG=1000;       // number of database histograms
    int minZ = 50;     // minimum size of database histograms
    int maxZ = 300;    // maximum size of database histograms
    int numBino = 100;    // number of binomials
    int[] dbZ = new int[numBino];
    int[] dbN = new int[numBino];
    for (int i = 0; i < numBino; i++) {
      dbZ[i] = rand.nextInt(maxZ - minZ + 1) + minZ;
    }
    alloc(dbN,nG,0,numBino,rand);
    assert(nG==PValue.sum(dbN));

    int sup = rand.nextInt(nG);

    for (int m = 5; m <= 100; m += 5) {
      // Generate feature probabilities
      double[] p = new double[m];
      Random rand1 = new Random(1);
      for (int i = 0; i < m; i++) {
        p[i] = rand1.nextDouble();
      }
      double sum = 0;
      for (int i = 0; i < m; i++) {
        sum += p[i];
      }
      for (int i = 0; i < m; i++) {
        p[i] /= sum;
      }

      // Sort p[]
      for (int i = 0; i < m - 1; i++) {
        for (int j = i + 1; j < m; j++) {
          if (p[i] < p[j]) {
            double tmp = p[i];
            p[i] = p[j];
            p[j] = tmp;
          }
        }
      }


      // Generate X, z
      Random rand2=new Random(1);
      int z = 30;
      int[] X = new int[m];
      alloc(X, z, 0, m, rand2);
      int z1 = PValue.sum(X);
      assert (z1 == z);



      int repeat1 = 10;
      long time0 = System.currentTimeMillis();

      // Lower bound of p-value
      for (int i = 0; i < repeat1; i++) {
        double[] probs = new double[maxZ + 1];
        Arrays.fill(probs, 0, z, 0);
        for (int s = z; s <= maxZ; s++) {
          probs[s] = PValue.lowerProb(p, X, s);
          //probs[s] = PValueNative.lowerProb(p, X, s);
        }
        double pvalue = PValue.computePvalue(probs, dbZ, dbN, nG, sup);
      }

      // Accurate computation of p-value
      long time1 = System.currentTimeMillis();
      int repeat2 = 20;
      for (int i = 0; i < repeat2; i++) {
        double[] probs = new double[maxZ + 1];
        probs = PValue.probSubsetRecursiveArray(p, X, z, maxZ);
        double pvalue = PValue.computePvalue(probs, dbZ, dbN, nG, sup);
      }
      long time2 = System.currentTimeMillis();
      System.out.printf("%d %g %g\n", m, (double) (time1 - time0) / repeat1,
                        (double) (time2 - time1) / repeat2);

    }
  }
}
