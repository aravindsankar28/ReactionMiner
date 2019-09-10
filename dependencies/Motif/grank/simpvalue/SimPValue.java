package grank.simpvalue;

import grank.pvalue.*;
import org.apache.commons.math.*;
import org.apache.commons.math.special.*;

/**
 *
 *
 * @author Huahai He
 * @version 1.0
 */
public class SimPValue {
  public static REntry pvalue(double[][] prob, int[] X, int N, int sup) throws
      MathException {
    double log = 0;
    int z = 0;
    for (int i = 0; i < prob.length; i++) {
      if(X[i]<prob[i].length)
      {
 	     log += prob[i][X[i]];
     	     z += X[i];
      }
    }
    double theta = Math.exp(log);
    double pvalue;
    if (theta >= 1) {
      pvalue = 1;
    }
    else if (theta <= 0) {
      pvalue = 0;
    }
    else {
      double np = N * theta;
      double np2 = N * (1 - theta);
      if (np >= 20 && np2 >= 20) { // approximate by normal distribution
        double temp = (sup - np) / Math.sqrt(np2 * theta * 2);
        if (temp >= 0) {
          if (temp > 6) {
            pvalue = 0;
          }
          else {
            pvalue = 0.5 - 0.5 * Erf.erf(temp);
          }
        }
        else {
          if (temp < -6) {
            pvalue = 1;
          }
          else {
            pvalue = 0.5 + 0.5 * Erf.erf( -temp);
          }
        }
      }
      else {
        pvalue = Beta.regularizedBeta(theta, sup, N - sup + 1);
      }
    }
    if (pvalue > 1) {
      pvalue = 1;
    }
    else if (pvalue < 0) {
      pvalue = 0;
    }
    double mean = N * theta;
    REntry entry = new REntry(pvalue, mean, sup);
    return entry;
  }

}
