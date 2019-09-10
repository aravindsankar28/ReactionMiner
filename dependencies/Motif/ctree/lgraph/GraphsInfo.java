package ctree.lgraph;

import java.io.*;
import ctree.graph.*;
import ctree.util.*;


/**
 * Graphs information
 *
 * @author Huahai He
 * @version 1.0
 */

public class GraphsInfo {


  /**
   * Get statistics of graphs
   * @param graphs Graph[]
   */
  public static void statGraphs(Graph[] graphs) {
    //LabelMap labelMap = new LabelMap();
    //int[] hist=new int[100];
    //Arrays.fill(hist,0);
    DataSum stat = new DataSum();
    for (Graph g: graphs) {
      int n = g.numV();
      int m = g.numE();
      stat.add("numV", n);
      stat.add("numE", m);
      stat.add("degree", n==0?0:2.0*m/n);

      /*int[] VL=labelMap.importGraph2(g);
      for(int j =0;j<VL.length;j++) {
        hist[VL[j]]++;
      }*/

    }
    System.err.println("Number of graphs = " + graphs.length);
    //System.err.println("Number of labels = " + labelMap.size());
    System.err.println("Average |V| = " + stat.mean("numV"));
    System.err.println("Average |E| = " + stat.mean("numE"));
    System.err.println("Average degree = " + stat.mean("degree"));
    System.err.println("Max |V| = " + stat.max("numV"));
    System.err.println("Max |E| = " + stat.max("numE"));
    System.err.println("Min |V| = " + stat.min("numV"));
    System.err.println("Min |E| = " + stat.min("numE"));

    System.err.println();
  }




  public static void main(String[] args) throws IOException {
    if (args.length < 1) {
        System.err.println("Usage: ... graph_file");
      System.exit(1);
    }
      Graph[] graphs = LGraphFile.loadLGraphs(args[0]);
      statGraphs(graphs);
  }

}
