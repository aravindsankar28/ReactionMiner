package grank.simpvalue;

import java.io.*;
import java.util.*;

import Dictionary.vertexDictionary;
import grank.transform.*;

/**
 * Simplified model basis probabilities.
 *
 * @author Huahai He
 * @version 1.0
 */
public class SimBasisProb {
  /**
   * Compute background probabilities of a basis
   */
  public static double[][] genBasisProb(String hist_file) throws IOException,
      NumberFormatException {
    System.err.println("Load histograms");
    Hist[] hists=Hist.loadHists(hist_file);

    System.err.println("Generate prior probabilities");

    int m = hists[0].hist.length; // number of distinct features
    int n = hists.length;

    double[][] p = new double[m][];
    int[] buf = new int[n];
    for (int i = 0; i < m; i++) {
      int max = 0;
      Arrays.fill(buf, 0);
      for (int j = 0; j < n; j++) {
        int temp = hists[j].hist[i];
        buf[temp]++;
        if (temp > max) {
          max = temp;
        }
      }
      if(max>980000) {
        System.err.println();
      }
      p[i] = new double[max + 1];
      double sum = 0;
      for (int j = max; j >= 0; j--) {
        double temp = (double) buf[j] / n;
        sum += temp;
        p[i][j] = Math.log(sum);
      }
    }
    return p;
  }

  public static double[][] genBasisProb(Hist[] hists,int maxVal) throws IOException,
  NumberFormatException {
//System.err.println("Load histograms");
//Hist[] hists=Hist.loadHists(hist_file);

//System.err.println("Generate prior probabilities");

	  double[][] p;
//	System.out.println(hists.length);
	if(hists.length==0)
	{
		 p = new double[vertexDictionary.freq.size()][maxVal];
		return p;
	}
	int m = hists[0].hist.length; // number of distinct features
	int n = hists.length;
//	System.out.println(n);
	p = new double[m][maxVal];
int[] buf = new int[maxVal];
for (int i = 0; i < m; i++) {
  int max = 0;
  Arrays.fill(buf, 0);
  for (int j = 0; j < n; j++) {
    int temp = hists[j].hist[i];
    //System.out.println(hists[j].hist[i]);
    buf[temp]++;
    //if (temp > max) {
      //max = temp;
    //}
  }
  //if(max>maxVal) {
    //System.err.println();
  //}
  //p[i] = new double[max + 1];
  double sum = 0;
  for (int j = maxVal-1; j >= 0; j--) {
    double temp = (double) buf[j] / n;
    sum += temp;
    p[i][j] = Math.log(sum);
    //System.out.print(sum+"\t");
  }
  //System.out.println();
}
//for(int i=0;i<p.length;i++)
	//System.out.println(Arrays.toString(p[i]));
//new Scanner(System.in).next();
return p;
}

  /**
   * Load prob. from a file
   */
  public static double[][] loadProb(String prob_file) throws IOException {
    Scanner sc = new Scanner(new File(prob_file));
    int m = sc.nextInt(); // dimensions

    double[][] p = new double[m][];
    for (int i = 0; i < m; i++) {
      int n = sc.nextInt(); // bins on this dimension
      p[i] = new double[n];
      for (int j = 0; j < n; j++) {
        p[i][j] = sc.nextDouble();
      }
    }
    sc.close();
    return p;
  }

  public static void saveProb(double[][] prob, String file) throws IOException {

    PrintStream out = new PrintStream(file);
    out.println(prob.length); // dimensions
    for (int i = 0; i < prob.length; i++) {
      out.print(prob[i].length + " "); // bins on this dimension
      for (int j = 0; j < prob[i].length; j++) {
        out.print(prob[i][j] + " ");
      }
      out.println();
    }
    out.close();

  }

  public static void main(String[] args) throws IOException {
    if (args.length < 2) {
      System.err.println("Usage: ... hist_file prob_file");
      System.exit(0);
    }
    double[][] p = genBasisProb(args[0]);
    System.err.println("Save prior probabilities");
    saveProb(p, args[1]);
  }
}
