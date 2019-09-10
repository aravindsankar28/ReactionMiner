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
public class PValueTime2 {
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

    Random rand = new Random(1);
    // Generate database settings
    int nG = 1000; // number of database histograms
    int minZ = 50; // minimum size of database histograms
    int maxZ = 300; // maximum size of database histograms
    int numBino = 100; // number of binomials
    int[] dbZ = new int[numBino];
    int[] dbN = new int[numBino];
    for (int i = 0; i < numBino; i++) {
      dbZ[i] = rand.nextInt(maxZ - minZ + 1) + minZ;
    }
    alloc(dbN, nG, 0, numBino, rand);
    assert (nG == PValue.sum(dbN));


    for (int m = 5; m <= 100; m += 5) {
      // Generate background probabilities
      double[] theta = new double[m];
      Random rand1 = new Random(1);
      for (int i = 0; i < m; i++) {
        theta[i] = rand1.nextDouble();
      }
      double sum = 0;
      for (int i = 0; i < m; i++) {
        sum += theta[i];
      }
      for (int i = 0; i < m; i++) {
        theta[i] /= sum;
      }

      // Sort theta[]
      /*
             for (int i = 0; i < m - 1; i++) {
        for (int j = i + 1; j < m; j++) {
          if (theta[i] < theta[j]) {
            double tmp = theta[i];
            theta[i] = theta[j];
            theta[j] = tmp;
          }
        }
             }*/

      // Generate X, z
      int z = 30;
      int repeat = 50;
      int[][] Xs=new int[repeat][m];
      int[] sups=new int[repeat];
      double[][] PXs = new double[repeat][numBino];
      for(int i=0;i<repeat;i++) {
        Random rand2 = new Random(i + 1);
        alloc(Xs[i], z, 0, m, rand2);
        int z1 = PValue.sum(Xs[i]);
        assert (z1 == z);
        sups[i] = rand2.nextInt(nG);

      }
      long time0 = System.currentTimeMillis();

      // Lower bound of p-value
      for (int i = 0; i < repeat; i++) {
        double[] PX=PXs[i];
        for (int s = 0; s < numBino; s++) {
          if (dbZ[s] >= z) {
            PX[s] = PValue.lowerProb(theta, Xs[i], dbZ[s]);
          }
          else {
            PX[s] = 0;
          }
        }
        double pvalue = PValue.computePvalue(PX, dbN, nG, sups[i]);
      }

      // Accurate computation of p-value
      long time1 = System.currentTimeMillis();
      PXs = new double[repeat][maxZ+1];
      for (int i = 0; i < repeat; i++) {
        double[] PX=PXs[i];
        PX = PValue.probSubsetRecursiveArray(theta, Xs[i], z, maxZ);
        double pvalue = PValue.computePvalue(PX, dbZ, dbN, nG, sups[i]);
      }
      long time2 = System.currentTimeMillis();
      int repeat3=50;
      for(int j=0;j<repeat3;j++) {
        for (int i = 0; i < repeat; i++) {
          double pvalue = PValue.computePvalue(PXs[i], dbZ, dbN, nG, sups[i]);
        }
      }
      long time3 = System.currentTimeMillis();
      System.out.printf("%d %g %g %g\n", m, (double) (time1 - time0) / repeat,
                        (double) (time2 - time1) / repeat,
                        (double) (time3 - time2) / (repeat*repeat3));

    }
  }
}
