package grank.graph;

import grank.data.synt.*;

/**
 * Generate a set of graph. For each pair of graphs, test graph isomorphism
 * and ucode equality.
 * @author Huahai He
 * @version 1.0
 */
public class TestUcode2 {

  public static void main(String[] args) throws Exception {

    // Generate a set of graphs
    System.out.println("Generate graphs");
    int m = 2000;
    LGraph[] D = GenGraph.genBlocks(3, 2, m, 4, false);

    int cnt1 = 0;
    int cnt2 = 0;
    boolean flag=true;

    // Check each pair for isomorphism and ucode equality
    System.out.println("Test");
    for (int i = 0; i < m - 1; i++) {
      ByteArray ucode1 = D[i].ucode();
      for (int j = i + 1; j < m; j++) {
        boolean flag1 = SubgraphIsom.subIsom(D[i], D[j]);
        if (flag1) {
          cnt1++;
        }

        ByteArray ucode2 = D[j].ucode();
        boolean flag2 = ucode1.equals(ucode2);
        if (flag2) {
          cnt2++;
        }
        if (flag1 != flag2) {
          System.err.println("Mismatched");
          System.err.printf("Isom=%b, same ucode=%b\n", flag1, flag2);
          flag=false;
        }
      }
    }
    System.out.printf("Passed = %b\n",flag);
    System.out.printf("Isom's = %d, same ucode's = %d\n", cnt1, cnt2);
  }
}
