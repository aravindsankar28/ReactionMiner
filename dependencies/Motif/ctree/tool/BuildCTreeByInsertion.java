package ctree.tool;

import ctree.graph.*;
import ctree.mapper.*;

import ctree.lgraph.*;
import ctree.index.*;

import java.io.IOException;
import ctree.util.Opt;

/**
 * @author Huahai He
 * @version 1.0
 */

public class BuildCTreeByInsertion {
  public static void usage() {
    System.err.println("Usage: ... [-options] graph_file");
    System.err.println("  -m=INT \t minimum number of fan-outs, default=20");
    System.err.println("  -M=INT \t maximum number of fan-outs, default=2*m-1");
    System.err.println(
        "  -mapper=[nbm|bi|wtbi|ss] \t graph mapper, default=nbm\n"
        + "\t\t nbm: Neighbor Biased\n"
        +"\t\t bi: Bipartite\n"
        +"\t\t wtbi: Weighted Bipartite\n"
        +"\t\t ss: State Search");
    System.err.println("  -ctree=FILE \t C-Tree file, default=graph_file with suffix replaced by .ctr");
    System.err.println(
        "  -dim1=INT \t number of dimensions for summarizing vertices, default=97,\n"
        + "\t\t or shinked to # of distinct vertices");
    System.err.println(
        "  -dim2=INT \t number of dimensions for simmarizing edges, default=97,\n"
        + "\t\t or shinked to # of distinct edges");
  }

  public static void main(String[] args) throws IOException,
      ClassNotFoundException,
      IllegalAccessException, InstantiationException {
    Opt opt = new Opt(args);
    if (opt.args() < 1) {
      usage();
      return;
    }

    int m = opt.getInt("m", 20);
    int M = opt.getInt("M", m * 2 - 1);

    String graph_file = opt.getArg(0);

    String ctree_file = opt.getString("ctree");
    if (ctree_file == null) {
      int idx = graph_file.lastIndexOf('.');
      ctree_file = idx >= 0 ? graph_file.substring(0, idx) : graph_file;
      ctree_file += ".ctr";
    }

    GraphMapper mapper = null;
    String tag = opt.getString("mapper", "nbm");
    if (tag.equals("nbm")) {
      mapper = new NeighborBiasedMapper(new LGraphWeightMatrix());
    }
    else if (tag.equals("bi")) {
      mapper = new BipartiteMapper();
    }
    else if (tag.equals("wtbi")) {
      mapper = new WeightedBipartiteMapper(new LGraphWeightMatrix());
    }
    else if (tag.equals("ss")) {
      mapper = new StateSearchMapper();
    }
    else {
      usage();
      return;
    }

    System.err.println("Load graphs");
    LGraph[] graphs = LGraphFile.loadLGraphs(graph_file);

    LabelMap labelMap = new LabelMap(graphs);

    // Dimensions for summarizing graphs
    int dim1 = opt.getInt("dim1", 97);
    int dim2 = opt.getInt("dim2", 97);

    int L = labelMap.size();
    if (dim1 > L) {
      dim1 = L;
    }
    if (dim2 > L * L) {
      dim2 = L * L;
    }

    GraphSim graphSim = new LGraphSim();
    GraphFactory factory = new LGraphFactory(labelMap, dim1, dim2);

    CTree ctree = new CTree(m, 2 * m - 1, mapper, graphSim, factory);

    // Insert graphs one by one
    long time0 = System.currentTimeMillis();
    for (int i = 0; i < graphs.length; i++) {
      ctree.insert(graphs[i]);

      if ( (i + 1) % 100 == 0) {
        System.err.println("Insert graph " + (i + 1));
        //System.err.println(Runtime.getRuntime().freeMemory());
      }
    }
    long time = System.currentTimeMillis() - time0;
    System.out.println("Insertion time: " + time / 1000.0);

    System.err.println("Save to " + ctree_file);
    ctree.saveTo(ctree_file);
    long time2 = System.currentTimeMillis() - time0;
    System.out.println("Build time: " + time2 / 1000.0);
    System.err.println("CTree depth = " + ctree.maxDepth());

  }

}
