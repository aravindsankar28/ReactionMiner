package ctree.experiment;

import java.util.*;
import ctree.graph.*;


import ctree.lgraph.*;

/**
 * <p>Title: Closure Tree</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author Huahai He
 * @version 1.0
 */

public class SelectGraphs {
  static Random rand = new Random(2);
  public static void main(String[] args) throws Exception {
    if (args.length < 3) {
      System.err.println(
          "Usage: ... graph_file numGraphs output_file");
      System.exit(1);
    }
    int num = Integer.parseInt(args[1]);
    System.err.println("Load graphs");
    LGraph[] graphs = LGraphFile.loadLGraphs(args[0]);

    System.err.println("Select graphs");
    LGraph[] graphs2 = selectGraphs(graphs, num);

    System.err.println("Save to " + args[2]);
    LGraphFile.saveLGraphs(graphs2, args[2]);

  }

  public static LGraph[] selectGraphs(LGraph[] graphs, int num) {
    LGraph[] graphs2 = new LGraph[num];
    boolean[] marked = new boolean[num];
    Arrays.fill(marked, false);
    int cnt = 0;
    while (cnt < num) {
      int k = rand.nextInt(num);
      if (marked[k]) {
        continue;
      }
      marked[k] = true;
      graphs2[cnt++] = graphs[k];
    }
    return graphs2;
  }
}
