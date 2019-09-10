package ctree.experiment;

import java.io.*;
import java.util.*;
import ctree.graph.*;
import ctree.index.Util;

import ctree.lgraph.*;
import ctree.util.*;

/**
 * <p>Title: Closure Tree</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author Huahai He
 * @version 1.0
 */

public class ExtractSubgraphs {
  static Random rand = new Random(2);
  public static void main(String[] args) throws IOException {
      Opt opt = new Opt(args);
    if (opt.args() < 2) {
      System.err.println(
          "Usage: ... [options] graph_file output_file");
      System.err.println("  -numSub=INT \t number of subgraphs");
      System.err.println("  -sizeSub=INT \t size of subgraphs");
      System.err.println("  -dfs \t\t extract subgraphs by depth-first search");
      System.err.println("  -multifile \t store subgraphs in multiple files");
      System.exit(1);
    }

      int numSub = opt.getInt("numSub");
      int sizeSub = opt.getInt("sizeSub");
      boolean dfs=opt.hasOpt("dfs");
      boolean multifile = opt.hasOpt("multifile");

      System.err.println("Load graphs");
      LGraph[] graphs = LGraphFile.loadLGraphs(opt.getArg(0));

      System.err.println("Extract subgraphs");
      LGraph[] subs = extractSubgraphs(graphs, numSub, sizeSub, dfs);

      System.err.println("Save to " + opt.getArg(1));

      if (!multifile) {
        LGraphFile.saveLGraphs(subs, opt.getArg(1));
      }
      else {
        for (int i = 0; i < subs.length; i++) {
          LGraph[] temp = {subs[i]};
          LGraphFile.saveLGraphs(temp, opt.getArg(1) + "." + i);
        }
      }

  }

  /**
   * Extracts subgraphs from graphs.
   * @param graphs Graph[]
   * @param numSub number of subgraphs
   * @param size size of subgraphs
   * @return Graph[]
   */
  public static LGraph[] extractSubgraphs(Graph[] graphs, int numSub, int size,
                                         boolean dfs) {
    int m = graphs.length;
    boolean[] visited = new boolean[m];
    Arrays.fill(visited, false);
    int cnt = 0;
    LGraph[] subs = new LGraph[numSub];
    while (cnt < numSub) {
      int k = rand.nextInt(m);
      if (visited[k] || graphs[k].numV() < size) {
        continue;
      }
      visited[k] = true;
      LGraph sub = extractSubgraph(graphs[k], size, dfs);
      if (!Util.isConnected(sub)) {
        continue;
      }
      subs[cnt++] = sub;
    }
    return subs;
  }

  /**
   * Extracts a subgraph from a graph.
   * @param g Graph
   * @param size size of subgraph which is the number of vertices.
   * @return Graph
   */
  public static LGraph extractSubgraph(Graph g, int size, boolean dfs) {

    Vertex[] vertices = (Vertex[])g.V();
    int[][] alist = g.adjList();
    int n = vertices.length;
    assert(size <= n);

    int[] map = dfs ? dfsSelectVertices(alist, size) :
        selectVertices(alist, size);

    LGraph sub = subgraph(g, map);
    return sub;
  }

  public static LGraph subgraph(Graph g, int[] subV) {
    LGraph g1 = (LGraph) g;
    int[] map = new int[g1.numV()];
    Arrays.fill(map,-1);

    // generate vertices
    LVertex[] V = (LVertex[]) g.V();
    LVertex[] V1 = new LVertex[subV.length];
    int cnt=0;
    for (int i:subV){
      V1[cnt]=V[i];
      map[i]=cnt++;
    }

    // generate edges
    Vector<UnlabeledEdge> vect = new Vector();
    UnlabeledEdge[] edges = (UnlabeledEdge[]) g1.E();
    for (UnlabeledEdge e : edges) {
      if (map[e.v1()] >= 0 && map[e.v2()] >= 0) {
        UnlabeledEdge e1 = new UnlabeledEdge(map[e.v1()], map[e.v2()], false);
        vect.addElement(e1);
      }
    }
    UnlabeledEdge[] E1 = new UnlabeledEdge[vect.size()];
    vect.toArray(E1);

    LGraph sub = new LGraph(V1, E1, "sub" + g1.getId());
    return sub;
  }

  /**
   * Select a subset of vertices which are usually connected.
   * Let S be the set of selected vertices, T be the neighbors of S.
   * Each iteration selects a vertex from T and adds to S.
   * @return mapping. if map[i]>=0 then vertex i is selected and map[i] is the index
   */
  private static int[] selectVertices(int[][] alist, int size) {
    int n = alist.length;
    int[] subV = new int[size];  // selected vertices
    Vector<Integer> T = new Vector(n); // neighbors

    boolean[] marked = new boolean[n];
    Arrays.fill(marked, false);

    // select vertices
    int s;
    for (int i = 0; i < size; i++) {
      if (T.isEmpty()) { // select a new source
        do {
          s = rand.nextInt(n);
        }
        while (marked[s]);

      }
      else { // select one from T
        int k = rand.nextInt(T.size());
        s =  T.elementAt(k);
        T.removeElementAt(k);
      }
      subV[i] = s;
      marked[s]=true;
      for (int t:alist[s]){
        if (!marked[t] && !T.contains(t)) {
          T.add(t);
        }
      }
    }
    return subV;
  }

  /**
   * Select vertices by random DFS searching.
   * @param vertices Vertex[]
   * @param adj int[][]
   * @param size int
   * @return int[]
   */
  private static int[] dfsSelectVertices(int[][] alist, int size) {
    int n = alist.length;
    int[] subV = new int[size];
    boolean[] marked = new boolean[n];
    Arrays.fill(marked, false);
    int cnt = 0;
    while (cnt < size) {
      int s = rand.nextInt(n);
      if (marked[s]) {
        continue;
      }
      cnt = dfs(alist, subV, marked, s, cnt, size);
    }
    return subV;
  }

  private static int dfs(int[][] alist, int[] subV, boolean[] marked, int s, int cnt, int size) {
    marked[s]=true;
    subV[cnt++]=s;
    int[] cand = new int[alist.length];
    while (cnt < size) {
      int cnt1 = 0;
      for (int s1:alist[s]){
        if ( !marked[s1]) {
          cand[cnt1++] = s1;
        }
      }
      if (cnt1 == 0) {
        return cnt;
      }
      int s1 = cand[rand.nextInt(cnt1)];
      cnt = dfs(alist,subV,marked,s1, cnt, size);
    }
    return cnt;
  }

}
