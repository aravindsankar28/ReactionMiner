package ctree.test;

import java.util.*;

import ctree.index.*;

import ctree.graph.*;
import ctree.lgraph.*;
import ctree.mapper.*;
import ctree.experiment.*;
import ctree.tool.*;


/**
 * <p>Title: Closure Tree</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author Huahai He
 * @version 1.0
 */

public class TestSerialization {
  public static void main(String[] args) throws Exception {
    GraphMapper mapper = new BipartiteMapper();

    // Generate graphs
    LGraph[] graphs = GraphGenerator.generateGraphs(1000, 20, 1000, 10, 20);
    LGraphFile.saveLGraphs(graphs, "test.txt");

    // Build a ctree
    System.err.println("Build ctree");
    GraphSim graphSim = new LGraphSim();

    GraphFactory factory = new LGraphFactory(new LabelMap(graphs),97,97);
    CTree ctree = new CTree(10, 19, mapper, graphSim, factory);
    for (int i = 0; i < graphs.length; i++) {
      if ( (i + 1) % 1000 == 0) {
        System.err.println("Insert graph " + (i + 1));
      }
      ctree.insert(graphs[i]);
    }

    // save the ctree and load again
    ctree.saveTo("test.ctr");
    CTree ctree2 = CTree.load("test.ctr");

    System.err.println("Reload ctree");
    // Use NNRanker to enumerate graphs and store in another array
    DistanceRanker ranker = new DistanceRanker(ctree2, mapper, new LGraphDistance(), graphs[0], false);
    LGraph[] graphs2 = new LGraph[graphs.length];
    Arrays.fill(graphs2, null);
    RankerEntry entry;
    while ( (entry = ranker.nextNN()) != null) {
      Graph g = entry.getGraph();
      int index = Integer.parseInt(((LGraph)g).getId().substring(1));
      graphs2[index] = (LGraph)g;
    }
    for (int i = 0; i < graphs2.length; i++) {
      assert(graphs2[i] != null);
    }

    // save the graphs to another file
    // the two files are to be compared
    System.err.println("Save graphs again");
    LGraphFile.saveLGraphs(graphs2, "test2.txt");
  }

}
