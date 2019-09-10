package ctree.test;

import java.util.*;

import ctree.index.*;
import ctree.graph.*;
import ctree.lgraph.*;
import ctree.mapper.*;

import ctree.tool.*;


/**
 * <p>Title: Closure Tree</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author Huahai He
 * @version 1.0
 */

public class SimpleTest {
  public static void main(String[] args) throws Exception {
    if (args.length < 2) {
      System.err.println("Usage: graph_file query_file");
      return;
    }

    GraphMapper mapper = new NeighborBiasedMapper(new LGraphWeightMatrix());

    System.err.println("Load graphs");
    LGraph[] graphs = LGraphFile.loadLGraphs(args[0]);
    Graph[] queries = LGraphFile.loadLGraphs(args[1]);
    /**@todo args incorrect */

    GraphSim graphSim = new LGraphSim();
    LabelMap labelMap = new LabelMap(graphs);
    int L = labelMap.size();
    int dim1 = Math.min(97, L);
    int dim2 = Math.min(97, L * L);
    GraphFactory factory = new LGraphFactory(labelMap, dim1, dim2);

    CTree ctree = new CTree(10, 19, mapper, graphSim, factory);

    int numV = 0;
    int numE = 0;
    for (int i = 0; i < graphs.length; i++) {
      numV += graphs[i].numV();
      numE += graphs[i].numE();
    }
    numV /= graphs.length;
    numE /= graphs.length;
    System.err.println("Average |V| = " + numV + ", Average |E| = " + numE);

    /*
     for(int i =1;i<graphs.length;i++) {
      int[] map = mapper2.map(graphs[0], graphs[i], true);
      double d = editMetric.d(graphs[0], graphs[i], map, true);
      System.out.println(d);
         }
         System.exit(0);
     */

    int cnt = 0;
    for (int i = 0; i < graphs.length; i++) {
      if (Util.subIsomorphic(queries[0], graphs[i])) {
        cnt++;
      }
    }
    System.err.println(cnt);

    System.err.println("Insert graphs");
    long time0 = System.currentTimeMillis();
    for (int i = 0; i < graphs.length; i++) {
      ctree.insert(graphs[i]);
      if (i % 100 == 0) {
        System.err.println("Insert graph " + i);
        //long time1 = System.currentTimeMillis() - time0;
        //System.out.println(i + " " + (time1 / 1000));
      }
      /*
       if ( (i + 1) % 100000 == 0) {
        System.err.println("Query at database size " + (i + 1));
        statQuery(i + 1, metric, ctree, queries, 100);
             } */
    }
    long time = System.currentTimeMillis() - time0;
    System.err.println("Insertion time = " + time + "ms");

    System.err.println("Check: " + ctree.check());

    //System.err.println("ctree: \n" + ctree);

    System.err.println("NN query");

    statQuery(graphs.length, mapper, new LGraphSim(), ctree, queries, 1);
    System.err.println("Output format:\n db_size avgNNTime(msec)  avgTotalTime(msec) avgCand avgAns avgAcc");

    /*
         System.out.println("\nGraph distances:");
         for (int i = 0; i < graphs.length; i++) {
      int[] map = mapper1.map(query, graphs[i], false);
      System.out.print(i + ", " + metric.d(query, graphs[i], false) + ", ");
      for (int j = 0; j < map.length; j++) {
        System.out.print(map[j] + " ");
      }
      System.out.println();
         }
     */
  }

  private static void statQuery(int db_size, GraphMapper mapper,
                                GraphSim graphSim,
                                CTree ctree, Graph[] queries,
                                int numQueries) {
    int avgNNTime = 0; // average NN query time
    int avgTotalTime = 0; // average total time (NN query + Isomorphic test)
    double avgAcc = 0; // average accuracy
    int avgCand = 0; // average candidate set size
    int avgAns = 0; // average answer set size

    for (int i = 0; i < numQueries; i++) {

      // NN ranking for each query graph
      SimRanker ranker = new SimRanker(ctree, mapper, graphSim, queries[i], true);
      RankerEntry entry;
      Vector cand = new Vector(); // candidate set
      long time0 = System.currentTimeMillis();
      while ( (entry = ranker.nextNN()) != null && entry.getDist() <= 0.001) {
        cand.addElement(entry.getGraph());
      }
      long time = System.currentTimeMillis() - time0;
      int ans_size = 0;
      for (int k = 0; k < cand.size(); k++) {
        Graph g = (Graph) cand.elementAt(k);
        if (Util.subIsomorphic(queries[i], g)) {
          System.out.println( ( (LGraph) g).getId());
          ans_size++;
        }
      }
      long time2 = System.currentTimeMillis() - time0;
      avgNNTime += time;
      avgTotalTime += time2;
      avgAcc += (double) ans_size / cand.size();
      avgCand += cand.size();
      avgAns += ans_size;
      //System.out.println(i + " " + time + " " + cand.size() + " " + ans_size);
    }
    avgNNTime /= numQueries;
    avgTotalTime /= numQueries;
    avgAcc /= numQueries;
    avgCand /= numQueries;
    avgAns /= numQueries;
    System.out.println(db_size + " " + avgNNTime + " " + avgTotalTime + " " +
                       avgCand + " " + avgAns + " " + avgAcc);
  }

}
