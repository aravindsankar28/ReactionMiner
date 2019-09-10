package grank.transform;

import java.io.*;
import java.util.*;
import ctree.util.*;
import grank.simpvalue.*;

/**
 * Compute prior probabilities of a basis.
 * @author Huahai He
 * @version 1.0
 */
public class BasisProb {
  /**
   * Compute background probabilities of a basis
   */
  public static double[] genBasisProb(String hist_file) throws IOException,
      NumberFormatException {
    Hist[] hists = Hist.loadHists(hist_file);
    int m = hists[0].hist.length; // number of distinct features
    double[] p = new double[m];
    Arrays.fill(p, 0);
    for (Hist hist : hists) {
      for (int i = 0; i < m; i++) {
        p[i] += hist.hist[i];
      }
    }
    int sum = 0;
    for (int i = 0; i < m; i++) {
      sum += p[i];
    }
    for (int i = 0; i < m; i++) {
      p[i] /= sum;
    }
    return p;
  }

  /**
   * Load prob. from a file
   */
  public static double[] loadProb(String prob_file) throws IOException {
    Scanner sc = new Scanner(new File(prob_file));
    int n = sc.nextInt();

    double[] p = new double[n];
    for (int i = 0; i < n; i++) {
      p[i] = sc.nextDouble();
    }
    sc.close();
    return p;
  }

  public static void saveProb(double[] prob, String file) throws IOException {
    PrintStream out = new PrintStream(file);
    out.println(prob.length);
    for (int i = 0; i < prob.length; i++) {
      out.println(prob[i]);
    }
    out.close();

  }

  public static void main(String[] args) throws IOException {
    Opt opt = new Opt(args);
    if (opt.args() < 2) {
      System.err.println("Usage: ... [options] hist_file prob_file");
      System.err.println(
          "  -model=[complex|simple] \t prob. model, default=complex");
      System.exit(0);
    }
    String model = opt.getString("model", "complex");
    if (model.equals("complex")) {
      double[] p = genBasisProb(opt.getArg(0));
      saveProb(p, args[1]);
    }
    else if (model.equals("simple")) { // generate simplified model prob.
      double[][] p = SimBasisProb.genBasisProb(opt.getArg(0));
      SimBasisProb.saveProb(p, opt.getArg(1));
    } else {
      System.err.println("Invalid model");
    }
  }
}
