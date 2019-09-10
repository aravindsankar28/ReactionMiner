package grank.simpvalue;

import java.io.IOException;

/**
 * Show the prior probabilities of the simplified model.
 * For each feature, show the prob. that it appears at least once.
 *
 * @author Huahai He
 * @version 1.0
 */
public class ShowProb {
  public static void main(String[] args) throws IOException {
    double[][] p=SimBasisProb.loadProb(args[0]);
    for(int i=0;i<p.length;i++) {
      System.out.println(Math.exp(p[i][1]));
    }
  }
}
