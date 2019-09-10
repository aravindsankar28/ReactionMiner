package ctree.test;

import ctree.index.*;
import ctree.graph.*;

import ctree.util.*;

import ctree.lgraph.LGraphFile;

/**
 *
 * @author Huahai He
 * @version 1.0
 */

public class TestPseudoIsom {
  public static void main(String[] args) throws Exception {
    if (args.length < 2) {
      System.err.println("Usage: ... graph_file query_file");
      System.exit(1);
    }
    Graph[] graphs = LGraphFile.loadLGraphs(args[0]);
    Graph[] queries = LGraphFile.loadLGraphs(args[1]);
    DataSum stat = new DataSum();
    for (int i = 0; i < queries.length; i++) {
      if(i%10==0) {
        System.err.println(i);
      }
      int T1 = 0, F1 = 0;
      long time0 = System.currentTimeMillis();
      for (int j = 0; j < graphs.length; j++) {
        boolean flag1 = Util.pseudoSubIsomorphic(queries[i], graphs[j], 1);
        if (flag1 == true) {
          T1++;
        }
        else {
          F1++;
        }
      }
      long time1 = System.currentTimeMillis() - time0;

      int T2 = 0, F2 = 0;
      time0 = System.currentTimeMillis();
      for (int j = 0; j < graphs.length; j++) {
        boolean flag2 = Util.subIsomorphic(queries[i], graphs[j]);
        if (flag2 == true) {
          T2++;
        }
        else {
          F2++;
        }
      }
      assert(T2<=T1);
      long time2 = System.currentTimeMillis() - time0;
      stat.add("pseudo", T1);
      stat.add("isom", T2);
      stat.add("accuracy", T1==0?1:(double) T2 / T1);
      stat.add("pseudo_time", time1);
      stat.add("isom_time", time2);
    }
    int n = graphs.length;
    System.out.printf("Isom: %f, Pseudo: %f, Accuracy: %f\n", stat.sum("isom"),
                      stat.sum("pseudo"), stat.mean("accuracy"));
    System.out.printf("Isom_time: %f, Pseudo_time: %f\n",
                      stat.mean("isom_time") / n, stat.mean("pseudo_time") / n);

  }

}
