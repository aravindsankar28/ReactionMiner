package grank.pvalue;

import java.io.*;
import java.util.*;

import grank.graph.*;
import grank.transform.*;
import ctree.util.*;
import grank.simpvalue.*;
import org.apache.commons.math.MathException;

/**
 * Compute p-values of subgraphs and rank them by p-values.
 *
 * Usage:
 * 1. Generate LabelMap
 *    java gmine.graph.LabelMap chem.txt
 *
 * 2. Generate Features
 *    a) Fixed size subgraphs
 *       java gmine.feature.EnumFeatures -zB=3 ca.txt ca.fea
 *
 *    b) Frequent subgraphs
 *       java gmine.graph.FSG2Graph -1 ca.txt ca.fsg
 *       fsg -m 3 -M 3 -s 10 ca.fsg
 *       java gmine.graph.FSG2Graph -2 ca.fp ca.fea
 *
 * 3. Transform graphs into feature vectors
 *    java gmine.feature.Graph2Hist -zB=3 ca.txt ca.fea ca.hist
 *
 * 4. Generate feature probabilities
 *    a) Background graph DB
 *    b) Generate from the graph database
 *       java gmine.feature.FeatureProb ca.hist ca.prob
 * 5. Find frequent subgraphs
 *    a) FSG
 *      fsg -m 5 -M 20 -s 10 ca.fsg
 *      java gmine.graph.FSG2Graph -2 ca.fp freq.txt
 * 6. Transform frequent subgraphs into feature vectors
 *    java gmine.feature.Graph2Hist -zB=3 freq.txt ca.fea freq.hist
 * 7. Compute P-values of frequent subgraphs
 *    java gmine.pvalue.RankSigGraph -pvalue=0.1 -K=1000 -mu0=graph ca.hist freq.hist ca.prob
 *
 * @author Huahai He
 * @version 1.0
 */
public class GraphRank {

  /**
   * The support of a sub-histogram in a histogram dataset, i.e., the number of
   * histograms that contain the sub-histogram.
   * @param subhist int[]
   * @param hists int[][]
   * @return int
   */
  public static int histSupport(Hist[] hists, Hist subhist) {
    int mu = 0;
    for (Hist hist : hists) {
      if (hist.contains(subhist)) {
        mu++;
      }
    }
    return mu;
  }

  //debug
  private static void checkSup(Hist[] hists, Hist subhist) throws IOException {
    String graph_file = "D.txt";
    String subgraph_file = "freq.txt";
    String map_file = "label.map";
    String basis_file = "B.txt";
    LGraph[] graphs = GraphFile.loadGraphs(graph_file, map_file);
    LGraph[] subgraphs = GraphFile.loadGraphs(subgraph_file, map_file);
    LGraph[] features = GraphFile.loadGraphs(basis_file, map_file);
    int i = 0;
    for (; i < subgraphs.length; i++) {
      if (subgraphs[i].id.equals(subhist.id)) {
        break;
      }
    }
    LGraph sub = subgraphs[i];
    assert (hists.length == graphs.length);
    for (int k = 0; k < graphs.length; k++) {
      boolean subisom = SubgraphIsom.subIsom(sub, graphs[k]);
      if (subisom) {
        boolean flag = hists[k].contains(subhist);
        if (flag == false) {
          HashMap<LGraph,
                  Integer> pcMap = Graph2Hist.loadBasis(basis_file, map_file);
          for (int z = 0; z < subhist.hist.length; z++) {
            if (subhist.hist[z] > hists[k].hist[z]) {
              System.err.println(z);
              System.err.println(sub.toString());
              Integer z1 = pcMap.get(sub);
              System.err.println(z);
              LGraph fea = null;
              for (Map.Entry<LGraph, Integer> entry : pcMap.entrySet()) {
                if (entry.getValue() == z) {
                  fea = entry.getKey();
                  break;
                }
              }
              System.err.println("fea:\n" + fea.toString());
              System.err.println(fea.ucode().toString());
              System.err.println(graphs[k].toString());
              Vector<LGraph>
                  F = GenPC.enumGraph(graphs[k], fea.E.length);
              for (LGraph f : F) {
                /*ByteArray ucode1=f.ucode();
                                 ByteArray ucode2=fea.ucode();
                                 boolean tag=true;
                                 for(int y=0;y<f.V.length;y++) {
                  if(ucode1.bytes[y]!=ucode2.bytes[y]) {
                    tag=false;
                    break;
                  }
                                 }
                                 if(tag) {
                  System.err.println(f.toString());
                  System.err.println(f.ucode().toString());
                                 }*/
                System.err.println(f.ucode());
              }
            }
          }
          throw new RuntimeException("Assertion failed");
        }
      }
    }
  }

  public static void main(String[] args) throws IOException, MathException {
    Opt opt = new Opt(args);
    if (opt.args() < 3) {
      System.err.println(
          "Usage: ... [options] hist_file subhist_file prob_file ");
      System.err.println("  -pvalue=DOUBLE \t Maximum p-value, default=1");
      System.err.println("  -K=NUMBER \t default=MAX_INT");
      System.err.println("  -mu0=[graph|hist] \t Use graph_mu0 or hist_mu0 as the real support, default=graph");
      System.err.println("  -model=[complex|simple] \t default=complex");
      System.exit(0);
    }
    double max_pvalue = opt.getDouble("pvalue", 1);
    int K = opt.getInt("K", Integer.MAX_VALUE);
    boolean graph_mu0 = true; // if true, then use graph_mu0, o/w use hist_mu0
    if (opt.hasOpt("mu0") && opt.getString("mu0").equals("hist")) {
      graph_mu0 = false;
    }
    String model = opt.getString("model", "complex");

    Hist[] hists = Hist.loadHists(opt.getArg(0)); // Hists of database graphs
    Hist[] subhists = Hist.loadHists(opt.getArg(1)); // Hists of frequent subgraphs
    double[] prob = model.equals("complex") ? BasisProb.loadProb(opt.getArg(2)) : null; // feature probabilities
    double[][] simProb = model.equals("simple") ?
        SimBasisProb.loadProb(opt.getArg(2)) : null; // simplified model prob.

    // Uniform feature probabilities
    //for(int i=0;i<p.length;i++) p[i]=1.0/p.length;

    // Get dbZ and dbN
    int[] D = new int[hists.length];
    for (int i = 0; i < D.length; i++) {
      D[i] = PValue.sum(hists[i].hist);
    }
    int[][] dbtmp = PValue.dbSizes(D);
    int[] dbZ = dbtmp[0];
    int[] dbN = dbtmp[1];

    long time0 = System.currentTimeMillis();
    REntry2[] ans = new REntry2[subhists.length];
    for (int i = 0; i < subhists.length; i++) {

      String[] tmp = subhists[i].id.split(", *");
      int graphMu0 = Integer.parseInt(tmp[1]);

      int histMu0 = histSupport(hists, subhists[i]);
      if (graphMu0 > histMu0) {
        //throw new RuntimeException("Error: graphMu0 > histMu0");
      }
      int mu0 = graph_mu0 ? graphMu0 : histMu0;

      int[] X = subhists[i].hist;

      if (i % 100 == 0) {
        System.err.println(i);
      }
      REntry res = model.equals("complex") ?
          PValue.pvalue(prob, X, dbZ, dbN, mu0) :
          SimPValue.pvalue(simProb, X, hists.length, mu0);
      ans[i] = new REntry2(subhists[i].id, res.pvalue, histMu0, graphMu0,
                           res.mean, PValue.sum(subhists[i].hist));
    }

    Arrays.sort(ans);

    // Output
    for (int i = 0; i < Math.min(ans.length, K); i++) {
      if (ans[i].pvalue > max_pvalue) {
        break;
      }
      // Output format:
      // rank: id pvalue histSup graphSup mean histSize
      String id = ans[i].id.substring(0, ans[i].id.indexOf(','));
      System.out.printf("%d %s %g %d %d %f %d\n",
                        i, id, ans[i].pvalue, ans[i].histMu0, ans[i].graphMu0,
                        ans[i].mean, ans[i].hsize);
    }

    long time1 = System.currentTimeMillis() - time0;
    System.err.printf("Time: %.2f\n", time1 / 1000.0);
  }
}
