package ctree.experiment;

import java.util.*;
import ctree.graph.*;
import ctree.index.*;
import ctree.lgraph.*;

import ctree.mapper.*;
import ctree.tool.*;

import ctree.chem.*;
import ctree.lgraph.*;

import ctree.mapper.*;
import ctree.util.*;

/**
 * Classify graphs by k-NN
 *
 * @author Huahai He
 * @version 1.0
 */
public class ClassifyGraphs {
  static class Result {
    double sim1, sim2, sim3;
    int predict, real;
  }

  public static void main(String[] args) throws Exception {
      Opt opt=new Opt(args);
    if (opt.args() < 5) {
      System.err.println(
          "Usage: ... [options] ctree1 ctree2 ctree3 category_file query_file");
      System.err.println("  -nQ=INT \t number of queries, default=queries in query_file");
      System.err.println("  -k=INT \t number of nearest neighbors, default=1");
      System.exit(1);
    }

    CTree ctree1 = CTree.load(opt.getArg(0));
    CTree ctree2 = CTree.load(opt.getArg(1));
    CTree ctree3 = CTree.load(opt.getArg(2));
    HashMap<String, Integer> map = SeparateCompounds.loadCategoryFile(opt.getArg(3));
    Graph[] graphs = LGraphFile.loadLGraphs(opt.getArg(4));

    int numQueries = opt.getInt("queries", graphs.length);
    Result[] results = new Result[numQueries];

    int k = opt.getInt("k", 1);

    GraphMapper mapper = new NeighborBiasedMapper(new LGraphWeightMatrix());
    GraphSim graphSim = new LGraphSim();
    for (int i = 0; i < numQueries; i++) {
      Graph g = graphs[i];
      if (i % 10 == 0) {
        System.err.println("Query at " + i);
      }
      // k-NN query on each category dataset
      Vector<RankerEntry>
          ans1 = SimQuery.kNNQuery(ctree1, mapper, graphSim, g, k, true);
      Vector<RankerEntry>
          ans2 = SimQuery.kNNQuery(ctree2, mapper, graphSim, g, k, true);
      Vector<RankerEntry>
          ans3 = SimQuery.kNNQuery(ctree3, mapper, graphSim, g, k, true);

      // average similarity
      Result r = new Result();
      r.sim1 = avgSim(ans1, g);
      r.sim2 = avgSim(ans2, g);
      r.sim3 = avgSim(ans3, g);

      // classify to the most similar category
      double sim = r.sim1;
      if (sim < r.sim2) {
        sim = r.sim2;
        r.predict = 1;
      }
      if (sim < r.sim3) {
        sim = r.sim3;
        r.predict = 2;
      }
      r.real = map.get( ( (LGraph) g).getId());
      results[i] = r;
    }

    int truePositive = 0;
    int[] predicts = new int[3];
    int[] reals = new int[3];
    Arrays.fill(predicts, 0);
    Arrays.fill(reals, 0);

    for (int i = 0; i < results.length; i++) {
      Result r = results[i];

      // statistics
      reals[r.real]++;
      predicts[r.predict]++;
      if (r.predict == r.real) {
        truePositive++;
      }

      System.out.printf("%d %d %f %f %f %d %d\n", i, graphSim.norm(graphs[i]),
                        r.sim1, r.sim2, r.sim3, r.predict, r.real);
    }
    System.err.printf("True positives: %f%%\n",
                      ( (double) truePositive / numQueries));
    System.err.printf("Predict: ca=%d, cm=%d, ci=%d\n", predicts[0], predicts[1],
                      predicts[2]);
    System.err.printf("Real   : ca=%d, cm=%d, ci=%d\n", reals[0], reals[1],
                      reals[2]);
  }

  private static double avgSim(Vector<RankerEntry> ans, Graph query) {
    double sim = 0;
    int count = 0;
    for (int i = 0; i < ans.size(); i++) {
      RankerEntry e = ans.elementAt(i);
      if ( ( (LGraph) e.getGraph()).getId().equals( ( (LGraph) query).getId())) {
        continue;
      }
      sim += -e.getDist();
      count++;
      if (count >= ans.size() - 1) {
        break;
      }
    }
    sim /= count;
    return sim;
  }
}
