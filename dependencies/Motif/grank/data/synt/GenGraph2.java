package grank.data.synt;

import java.io.*;
import java.util.*;

import grank.graph.*;
import grank.util.*;
import grank.transform.*;
import ctree.util.*;

/**
 * Generate synthetic graphs.
 * Adjusted from from GenGraph
 *
 * Procedure:
 * 1. Generate building blocks B of fixed size;
 * 2. Generate Significant subgraphs A of Poisson distributed size;
 * 3. Pick up a subgraph from A with probability P_A, then insert subgraphs to
 *   it from B until the size reaches a Poisson distributed size.
 *   Repeat this and generate the graph dataset D;
 *
 * Sizes are measured by the number of edges.
 * @author Huahai He
 * @version 1.0
 */
public class GenGraph2 {

  private static Random rand = new Random(1);

  /**
   * Generate building blocks which are tiny subgraphs
   * @param Lv number of vertex labels
   * @param Le number of edge labels
   * @param nB number of building blocks
   * @param zB size of each building block in terms of number of edges
   * @param randomSize If true, then use Poisson distribution with mean size zB.
   * @return LGraph[]
   */
  public static LGraph[] genBlocks(int Lv, int Le, int nB, int zB,
                                   boolean randomSize) {
    LGraph[] B = new LGraph[nB];
    for (int i = 0; i < nB; i++) {
      // Number of edges
      int zB1;
      if (randomSize) {
        zB1 = Stat.nextPoisson(zB, rand);
      }
      else {
        zB1 = zB;
      }

      // Number of vertices
      int maxV = zB1 + 1;
      int minV = (int) Math.ceil( (1 + Math.sqrt(1 + 8 * zB1)) / 2);
      assert (minV <= maxV);
      int n = minV + rand.nextInt(maxV - minV + 1);

      B[i] = genBlock(Lv, Le, n, zB1);
    }
    return B;
  }

  /**
   * Generate a small subgraph
   * @param Lv Number of vertex labels
   * @param Le Number of edge labels
   * @param numV Number of vertices
   * @param numE Number of edges

   * @return LGraph
   */
  public static LGraph genBlock(int Lv, int Le, int numV, int numE) {
    int[] V = new int[numV];
    LEdge[] E = new LEdge[numE];
    int[][] adj = new int[numV][numV]; // adjacency matrix
    for (int i = 0; i < numV; i++) {
      Arrays.fill(adj[i], 0);
    }

    // Generate labels on vertices
    for (int i = 0; i < numV; i++) {
      V[i] = rand.nextInt(Lv);
    }

    // Generate edges
    int vcnt = 0;
    for (int i = 0; i < numE; i++) {
      int v1, v2; // endpoints
      if (vcnt == 0) {
        v1 = 0;
        v2 = 1;
        vcnt = 2;
      }
      else if (vcnt < numV) { // ensure a new vertex is added in
        v1 = rand.nextInt(vcnt);
        v2 = vcnt;
        vcnt++;
      }
      else { // now that the graph has been connected, find any two vertices
        do {
          v1 = rand.nextInt(numV);
          v2 = rand.nextInt(numV);
        }
        while (adj[v1][v2] > 0 || v1 == v2);
      }
      int elabel = rand.nextInt(Le);
      adj[v1][v2] = elabel + 1;
      adj[v2][v1] = elabel + 1;
      E[i] = new LEdge(v1, v2, elabel);
    }
    LGraph g = new LGraph(null, V, E);
    //checkDuplicate(g);
    return g;
  }

  /**
   * Make a choice based on the probabilities
   * @return int
   */
  public static int probChoice(double[] accuProb, Random rand) {
    assert (accuProb[0] == 0);

    double p = rand.nextDouble();

    // Binary search a, b such that prob[a]<=p<prob[b]
    int a = 0, b = accuProb.length - 1;
    if (p >= accuProb[b]) {
      return b;
    }

    while (b > a + 1) {
      int c = (a + b) / 2;
      if (p >= accuProb[c]) {
        a = c;
      }
      else {
        b = c;
      }
    }
    return a;
  }

  // Accumulative probabilities
  private static double[] acuProb(double[] prob) {
    double[] part = new double[prob.length];
    part[0] = 0;
    for (int i = 1; i < prob.length; i++) {
      part[i] = part[i - 1] + prob[i - 1];
    }
    return part;
  }

  /**
   * Generate a graph using the building blocks
   * @param size int
   * @param seeds LGraph[]
   * @param prob double[]
   * @return LGraph
   */
  public static LGraph genGraph(LGraph[] blocks, double[] accuProb, int size,
                                double degree) {
    int idx = probChoice(accuProb, rand);
    LGraph g = new LGraph(null, blocks[idx].V, blocks[idx].E);
    while (g.E.length < size) {
      idx = probChoice(accuProb, rand);
      LGraph g2 = blocks[idx];
      g = mergeGraphs(g, g2, degree);
    }
    return g;
  }

  /**
   * Check if V1[i] and V2[j] can be merged
   */
  private static boolean canMerge(int[] V1, int[][] adj1, int n1, int[] V2,
                                  int[][] adj2, int n2, int[] map, int[] rmap,
                                  int i, int j) {

    if (map[i] < 0 && rmap[j] < 0 && V1[i] == V2[j]) { // labels on i and j are identical
      // check if the labels of adjacent edges are compromised
      for (int k = 0; k < n1; k++) {
        if (adj1[i][k] > 0) { // vertex k is adjacent to i
          for (int l = 0; l < n2; l++) {
            if (adj2[j][l] > 0) { // vertex l is adjacent to j
              // k has been mapped to l, but labels on (i,k) and (j,l) are different
              if (map[k] == l) { //&& adj1[i][k] != adj2[j][l]) {
                return false;
              }
            }
          }
        }
      }
      return true;
    }
    else {
      return false;
    }
  }

  // debug
  // Check if there are duplicate edges
  private static boolean checkDuplicate(LGraph g) {
    int n = g.V.length;
    int[][] matrix = new int[n][n];
    for (int[] row : matrix) {
      Arrays.fill(row, -1);
    }
    for (LEdge e : g.E) {
      if (matrix[e.v1][e.v2] > 0) {
        throw new RuntimeException("Assertion failed: checkDuplicate");
        //return false;
      }
      matrix[e.v1][e.v2] = e.label + 1;
      matrix[e.v2][e.v1] = e.label + 1;
    }
    return true;
  }

  /**
   * Merge two graphs. Merge vertices
   * @param g1 LGraph
   * @param g2 LGraph
   * @return LGraph
   */
  public static LGraph mergeGraphs(LGraph g1, LGraph g2, double degree) {
    //checkDuplicate(g1);
    //checkDuplicate(g2);
    int n1 = g1.V.length;
    int n2 = g2.V.length;

    // Find a mapping between V1 and V2
    int cnt = 0; // number of vertices that overlap
    int[] map = new int[n1];
    Arrays.fill(map, -1);
    int[] rmap = new int[n2];
    Arrays.fill(rmap, -1);

    int[][] adj1 = g1.adjmatrix();
    int[][] adj2 = g2.adjmatrix();

    for (int i = 0; i < n1; i++) {
      for (int j = 0; j < n2; j++) {
        // Check if i and j can be merged
        if (canMerge(g1.V, adj1, n1, g2.V, adj2, n2, map, rmap, i, j)) {
          map[i] = j;
          rmap[j] = i;
          cnt++;
          break;
        }
      }
      // control the average degree
      if (cnt > 0 &&
          (g1.E.length + g2.E.length) * 2.0 / (n1 + n2 - cnt) > degree) {
        break;
      }
    }
    if (cnt == 0) {
      return g1; // g1 and g2 do not share a vertex
    }

    // merge g1 and g2

    // Merge vertices
    int n = n1 + n2 - cnt;
    int[] V = new int[n];
    System.arraycopy(g1.V, 0, V, 0, n1);
    int vcnt = n1;
    for (int i = 0; i < n2; i++) {
      if (rmap[i] < 0) {
        V[vcnt] = g2.V[i];
        rmap[i] = vcnt;
        vcnt++;
      }
    }
    assert (vcnt == n);

    // Merge edges
    Vector<LEdge> array = new Vector<LEdge> ();
    for (LEdge e : g1.E) {
      array.add(e);
    }
    for (LEdge e : g2.E) {
      int v1 = rmap[e.v1];
      int v2 = rmap[e.v2];
      if (v1 >= n1 || v2 >= n1 || adj1[v1][v2] == 0) { // e is not in g1.E
        LEdge e1 = new LEdge(v1, v2, e.label);
        array.add(e1);
      }
    }
    LEdge[] E = new LEdge[array.size()];
    array.toArray(E);
    LGraph g = new LGraph(null, V, E);
    //checkDuplicate(g);
    return g;
  }

  /**
   * Generate graphs using the building blocks or the seeds
   * @param seeds LGraph[]
   * @param prob double[]
   * @param q size of the graph
   * @param randomSize boolean
   * @param d Number of graphs
   * @return LGraph[]
   */
  public static LGraph[] genGraphs(LGraph[] seeds, double[] accuProb, int size,
                                   boolean randomSize, int num, double degree) {
    LGraph[] graphs = new LGraph[num];
    for (int i = 0; i < num; i++) {
      int size1;
      if (randomSize) {
        size1 = Stat.nextPoisson(size, rand);
      }
      else {
        size1 = size;
      }
      graphs[i] = genGraph(seeds, accuProb, size1, degree);
    }
    return graphs;
  }

  /**
   * Generate a database graph. Pick up a significant subgraph from A with
   * probability PA, then insert subgraphs from B.
   * @param B LGraph[]
   * @param accuProbB double[]
   * @param A LGraph[]
   * @param PA double
   * @param zG int
   * @return LGraph
   */
  public static LGraph genDBGraph(LGraph[] B, double[] accuProbB, LGraph[] A,
                                  double PA, int zG, double degree) {
    double r = rand.nextDouble();
    LGraph g;
    int idx;
    if (r < PA) {  // pick up a significant subgraph from A
      idx = rand.nextInt(A.length);
      g = new LGraph(null, A[idx].V, A[idx].E);
    }
    else {
      idx = probChoice(accuProbB, rand);
      g = new LGraph(null, B[idx].V, B[idx].E);
    }

    while (g.E.length < zG) {  // insert subgraphs from B
      idx = probChoice(accuProbB, rand);
      LGraph g2 = B[idx];
      g = mergeGraphs(g, g2, degree);
    }
    return g;
  }

  public static LGraph[] genDBGraphs(LGraph[] B, double[] accuProbB, LGraph[] A,
                                     double PA, int zG, boolean randomSize,
                                     int nG, double degree) {
    LGraph[] D = new LGraph[nG];
    for (int i = 0; i < nG; i++) {
      int zG1;
      if (randomSize) {
        zG1 = Stat.nextPoisson(zG, rand);
      }
      else {
        zG1 = zG;
      }
      D[i] = genDBGraph(B, accuProbB, A, PA, zG1, degree);
    }
    return D;
  }

  /**
   * Generate a graph databases
   * @param Lv int
   * @param Le int
   * @param zB Size of each building block
   * @param nB Number of building blocks
   * @param zA Mean size of significant subgraphs
   * @param k Number of significant subgraphs
   * @param zG Mean size of graphs in the graph dataset
   * @param nG Number of graphs in the graph dataset
   * @param PA Probability of choosing a seed from A
   * @return Object[] {LGraph[] B, LGraph[] A, double[] probB, LGraph[] D}
   */
  public static Res genDB(int Lv, int Le, int zB, int nB,
                          int zA, int nA, int zG,
                          int nG, double PA, double degreeA, double degreeG) {
    // Building blocks
    LGraph[] B = genBlocks(Lv, Le, nB, zB, false);

    /**@todo B+A, merge graphs */
    double[] probB = new double[nB];
    for (int i = 0; i < nB; i++) {
      probB[i] = 1.0 / nB;
    }
    double[] accuProbB = acuProb(probB);

    // Significant subgraphs
    LGraph[] A = genGraphs(B, accuProbB, zA, true, nA, degreeA);
    //LGraph[] A=genBlocks(Lv,Le,k,r,true);

    // database graphs
    LGraph[] D = genDBGraphs(B, accuProbB, A, PA, zG, true, nG, degreeG);

    for (int i = 0; i < B.length; i++) {
      B[i].id = "B" + i;
    }
    for (int i = 0; i < A.length; i++) {
      A[i].id = "S" + i;
    }
    for (int i = 0; i < D.length; i++) {
      D[i].id = "G" + i;
    }

    return new Res(B, A, D);

  }

  private static class Res {
    LGraph[] B, A, D;
    Res(LGraph[] _B, LGraph[] _A, LGraph[] _D) {
      B = _B;
      A = _A;
      D = _D;
    }
  }

  /**
   * Generate label map file which is redundant.
   * @param Lv int
   * @param Le int
   * @param mapfile String
   * @throws IOException
   */
  public static void genMapfile(int Lv, int Le, String mapfile) throws
      IOException {
    PrintStream out = new PrintStream(mapfile);
    out.println(Lv);
    for (int i = 0; i < Lv; i++) {
      out.println(i + ":" + i);
    }
    out.println(Le);
    for (int i = 0; i < Le; i++) {
      out.println(i + ":" + i);
    }
    out.close();
  }

  public static void main(String[] args) throws IOException {
    if (args.length < 1) {
      System.err.println(
          "Usage: ... [options] graph_file");
      System.err.println("  -Lv=NUMBER \t number of vertex labels, default=10");
      System.err.println("  -Le=NUMBER \t number of edge labels, default=10");
      System.err.println(
          "  -nB=NUMBER \t number of building blocks, default=100");
      System.err.println("  -zB=NUMBER \t size of building blocks, default=3");
      System.err.println(
          "  -k=NUMBER \t number of significant subgraphs, default=10");
      System.err.println(
          "  -zA=NUMBER \t size of significant subgraphs, default=8");
      System.err.println(
          "  -PA=DOUBLE \t probability of choosing significant subgraphs, default=0.5");
      System.err.println(
          "  -nG=NUMBER: \t number of database graphs, default=1000");
      System.err.println("  -zG=NUMBER \t size of database graphs, default=20");
      //System.err.println("-graph_file=FILE \t database file, default=D.txt");
      System.err.println("  -block_file=FILE \t block file, default=B.txt");
      System.err.println(
          "  -sig_file=FILE \t significant subgraphs file, default=A.txt");
      System.err.println("  -map_file=FILE \t map file, default=label.map");
      System.err.println(
          "  -prob_file=FILE \t probability file, default=B.prob");
      System.err.println("  -rand=NUMBER \t Random seed number, default=1");
      System.err.println(
          " -degA=DOUBLE \t Degree of significant subgraphs, default=2.1");
      System.err.println(
          " -degG=DOUBLE \t Degree of database graphs, default=2.1");

      System.exit(1);
    }

    // Get parameters
    Opt opt = new Opt(args);
    int Lv = opt.getInt("Lv", 10);
    int Le = opt.getInt("Le", 10);
    int nB = opt.getInt("nB", 100);
    int zB = opt.getInt("zB", 3);
    int k = opt.getInt("k", 10);
    int zA = opt.getInt("zA", 8);
    double PA = opt.getDouble("PA", 0.5);
    int nG = opt.getInt("nG", 1000);
    int zG = opt.getInt("zG", 20);
    String block_file = opt.getString("block_file", "B.txt");
    String sig_file = opt.getString("sig_file", "A.txt");
    String map_file = opt.getString("map_file", "label.map");
    String prob_file = opt.getString("prob_file", "B.prob");
    String graph_file = opt.getArg(0);
    int randSeed = opt.getInt("rand", 1);
    double degA = opt.getDouble("degA", 2.1);
    double degG = opt.getDouble("degG", 2.1);
    rand = new Random(randSeed);

    Res res = genDB(Lv, Le, zB, nB, zA, k,
                    zG, nG, PA, degA, degG);

    genMapfile(Lv, Le, map_file);

    System.err.println("Save graphs");
    GraphFile.saveGraphs(res.D, graph_file, map_file);
    GraphFile.saveGraphs(res.B, block_file, map_file);
    GraphFile.saveGraphs(res.A, sig_file, map_file);

    //BackgroundProb.genProbFile(res.probB, prob_file);
    double[] probB2 = new double[nB];
    for (int i = 0; i < nB; i++) {
      probB2[i] = 1.0 / nB;
    }
    BasisProb.saveProb(probB2, prob_file);
    System.err.println("OK");
  }

}
