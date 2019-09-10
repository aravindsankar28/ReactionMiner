package grank.probe;

import java.util.*;

import org.apache.commons.math.*;
import grank.pvalue.*;

/**
 * Evaluate the tightness of pvalue bounds using random data.
 * @author Huahai He
 * @version 1.0
 */
public class PValueBound {
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

    //for (int m = 5; m <= 100; m += 5) {
    int m = 30;
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

    // Generate X, z
    int z0 = 5;
    int[] X = new int[m];
    Random rand2 = new Random(1);
    alloc(X, z0, 0, m, rand2);
    assert (PValue.sum(X) == z0);
    int sup = (int) (nG * 0.05); //rand2.nextInt(nG);

    long time0 = System.currentTimeMillis();
    int z1 = 50;
    int bin0 = rand2.nextInt(m);
    for (int z = z0 + 1; z <= z1; z++) {
      // increment X on a random bin
      //X[rand2.nextInt(m)]++;
      X[bin0]++;

      // Lower bound of p-value
      double[] PX = new double[numBino];
      for (int s = 0; s < numBino; s++) {
        if (dbZ[s] >= z0) {
          PX[s] = PValue.lowerProb(theta, X, dbZ[s]);
        }
        else {
          PX[s] = 0;
        }
      }
      double pvalue_lower = PValue.computePvalue(PX, dbN, nG, sup);

      // Upper bound of p-value
      for (int s = 0; s < numBino; s++) {
        if (dbZ[s] >= z0) {
          PX[s] = PValue.upperProb(theta, X, dbZ[s]);
        }
        else {
          PX[s] = 0;
        }
      }
      double pvalue_upper = PValue.computePvalue(PX, dbN, nG, sup);

      // Accurate computation of p-value
      PX = new double[maxZ + 1];
      PX = PValue.probSubsetRecursiveArray(theta, X, z, maxZ);
      double pvalue = PValue.computePvalue(PX, dbZ, dbN, nG, sup);

      System.out.printf("%d %s %g %g\n", z, pvalue_lower, pvalue, pvalue_upper);

    }
    long time1 = System.currentTimeMillis();
    System.err.printf("Time: %.2f sec\n", (time1 - time0) / 1000.0);

  }
}
