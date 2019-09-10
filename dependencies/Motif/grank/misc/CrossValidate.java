package grank.misc;

import java.io.*;
import java.util.*;
import ctree.util.*;
import grank.pvalue.*;
import grank.misc.*;

/**
 * Merge two ranking files into one.
 *
 * @author Huahai He
 * @version 1.0
 */
public class CrossValidate {

  public static void main(String[] args) throws IOException {
    Opt opt = new Opt(args);
    if (opt.args() < 2) {
      System.err.println("Usage: ... rank1_file rank2_file");
      System.exit(1);
    }
    REntry2[] r1 = RecallSigGraph.loadRankings(opt.getArg(0));
    REntry2[] r2 = RecallSigGraph.loadRankings(opt.getArg(1));
    HashMap<String, Integer> map = new HashMap<String, Integer> (r2.length);
    for (int j = 0; j < r2.length; j++) {
      map.put(r2[j].id, j);
    }
    for (int i = 0; i < r1.length; i++) {
      int j = map.get(r1[i].id);
      System.out.printf("%d %s %g %d %d %f %g %d %d %f\n", i, r1[i].id,
                        r1[i].pvalue, r1[i].histMu0, r1[i].graphMu0, r1[i].mean,
                        r2[j].pvalue, r2[j].histMu0, r2[j].graphMu0, r2[j].mean);
    }

  }
}
