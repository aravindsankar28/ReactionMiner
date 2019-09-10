package ctree.experiment;

import ctree.graph.*;
import ctree.index.*;
import java.util.*;
import ctree.mapper.*;

import ctree.util.*;

import ctree.lgraph.*;


/**
 * Evaluate a graph mapping algorithm. Select two datasets and compute
 * the mapping for every pair. For each mapping, compute sim/simUp and
 * report along simUp. Average running time is also available.
 *
 * @author Huahai He
 * @version 1.0
 */
public class EvaluateGraphMapper {
  public static void evaluateMapper(Graph[] dataset1, Graph[] dataset2,
                                    GraphMapper mapper, GraphSim graphSim) {
    DataSum stat = new DataSum();
    long time0 = System.currentTimeMillis();
    for (int i = 0; i < dataset1.length; i++) {
      for (int j = 0; j < dataset2.length; j++) {
        int[] map = mapper.map(dataset1[i], dataset2[j]);
        int sim = graphSim.sim(dataset1[i], dataset2[j], map);
        int simUp = graphSim.simUpper(dataset1[i], dataset2[j]);

        stat.add("sim", sim);
        stat.add("simUp", simUp);
        stat.add("sim/simUp", simUp == 0 ? 1 : (double) sim / simUp);
      }
      if (i % 100 == 0) {
        System.err.println(i);
      }
    }
    long time = System.currentTimeMillis() - time0;

    double[][] H = stat.reportOnKey("simUp", "sim", "sim/simUp");
    for (double[] row : H) {
      if (row[3] == 0) {
        continue;
      }
      System.out.printf("%d %d %f %d\n", (int) row[0], (int) row[1], row[2],
                        (int) row[3]);
    }
    System.err.printf("Average time = %fms\n",
                      (double) time / (dataset1.length * dataset2.length));
  }

  private static void usage() {
      System.err.println("Usage: ... graph_file");
      System.err.println("  -mapper=[nbm|bi|wtbi|ss] \t default=nbm");
      System.err.println("  -m1=INT \t number of graphs in dataset 1");
      System.err.println("  -m2=INT \t number of graphs in dataset 2");
  }

  public static void main(String[] args) throws java.io.IOException {
      Opt opt = new Opt(args);
    if (opt.args() < 1) {
      usage();
      System.exit(1);
    }
    Graph[] graphs = LGraphFile.loadLGraphs(opt.getArg(0));
    GraphMapper mapper = null;
    String tag=opt.getString("mapper", "nbm");
    if (tag.equals("wext")) {
      mapper = new NeighborBiasedMapper(new LGraphWeightMatrix());
    }
    else if (tag.equals("bi")) {
      mapper = new BipartiteMapper();
    }
    else if (tag.equals("wt")) {
      mapper = new WeightedBipartiteMapper(new LGraphWeightMatrix());
    }
    else if (tag.equals("ss")) {
      mapper = new StateSearchMapper();
    }
    else {
      usage();
      System.exit(1);
    }

    int m1 = opt.getInt("m1");
    int m2 = opt.getInt("m2");
    Graph[] data1 = new Graph[m1]; // dataset 1
    Graph[] data2 = new Graph[m2]; // dataset 2
    Random rand = new Random(1);
    for (int i = 0; i < m1; i++) {
      int x;
      do {
        x = rand.nextInt(graphs.length);
      }
      while (graphs[x] == null);
      data1[i] = graphs[x];
      graphs[x] = null;

    }
    for (int j = 0; j < m2; j++) {
      int x;
      do {
        x = rand.nextInt(graphs.length);
      }
      while (graphs[x] == null);
      data2[j] = graphs[x];
      graphs[x] = null;
    }
    evaluateMapper(data1, data2, mapper,new LGraphSim());
  }


}
