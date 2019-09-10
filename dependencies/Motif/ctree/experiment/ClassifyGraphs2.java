package ctree.experiment;

import java.util.*;
import ctree.graph.*;
import ctree.index.*;
import ctree.lgraph.*;

import ctree.mapper.*;
import ctree.tool.*;


import ctree.chem.*;
/**
 * <p>Title: Closure Tree</p>
 *
 * <p> </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p> </p>
 *
 * @author Huahai He
 * @version 1.0
 */
public class ClassifyGraphs2 {
  static class Result {
    int TP = 0; // true positive
    int FP = 0; // false positive
    int predict = 0; // predicts
    int real = 0; // reals
  }

  static final double[] priorProb = {Math.sqrt(422.0 / 42682), Math.sqrt(1081.0 / 42682),
      Math.sqrt(41179.0 / 42682)};
  //static final double[] priorProb = {1,1,1};
  static final int categories = priorProb.length;

  public static void main(String[] args) throws Exception {
    if (args.length < 5) {
      System.err.println(
          "Usage: ... ctree category_file query_file [numQueries] [k]");
      System.exit(1);
    }
    CTree ctree = CTree.load(args[0]);
    HashMap<String, Integer> map = SeparateCompounds.loadCategoryFile(args[1]);
    Graph[] graphs = LGraphFile.loadLGraphs(args[2]);
    int numQueries = graphs.length;
    if (args.length >= 4) {
      numQueries = Integer.parseInt(args[3]);
    }
    int k = 11;
    if (args.length >= 5) {
      k = Integer.parseInt(args[4]);
    }

    int[] hist = new int[categories];
    Arrays.fill(hist, 0);
    Result[] results = new Result[categories];
    for(int j=0;j<categories;j++) results[j] = new Result();
    GraphMapper mapper = new NeighborBiasedMapper(new LGraphWeightMatrix());
    GraphSim graphSim = new LGraphSim();
    for (int i = 0; i < numQueries; i++) {
      Graph g = graphs[i];
      if(!map.containsKey(((LGraph)g).getId())) continue;
      if (i % 10 == 0) {
        System.err.println("Query at " + i);
      }
      // k-NN queries
      Vector<RankerEntry> ans = SimQuery.kNNQuery(ctree, mapper, graphSim, g, k, true);

      for (RankerEntry e : ans) {
        String id = ((LGraph)e.getGraph()).getId();
        if(id.equals(((LGraph)g).getId())) continue;
        int c = map.get(id);
        hist[c]++;
      }

      // predict the category
      double maxRate = -1;
      int predict = -1;
      for (int j = 0; j < categories; j++) {
        if (hist[j] / priorProb[j] > maxRate) {
          maxRate = hist[j] / priorProb[j];
          predict = j;
        }
      }
      int real = map.get(((LGraph)g).getId());

      // statistics
      results[predict].predict++;
      results[real].real++;
      if (predict == real) {
        results[real].TP++;
      }
      else {
        results[predict].FP++;
      }
    }

    for (int i = 0; i < categories; i++) {
      System.err.printf(
          "%d: TP=%d, FP=%d, Predicts=%d, Reals=%d, TP rate=%1.3f, Precision=%1.3f\n",
          i, results[i].TP, results[i].FP, results[i].predict, results[i].real,
          (double) results[i].TP / results[i].real,
          (double) results[i].TP / results[i].predict);
    }

  }
}
