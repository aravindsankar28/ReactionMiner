package grank.transform;

import java.io.*;
import java.util.*;
import grank.graph.*;
import ctree.util.*;

/**
 * Generate primitive components (PCs) from a graph file by enumeration.
 * Each PC is a connected subgraph with a fixed given size.
 *
 * @author Huahai He
 * @version 1.0
 */
public class GenPC {
  /*
     static int[][] adjlist(LGraph g) {
    int n = g.V.length;
    LinkedList<Integer> [] llist = new LinkedList[n];
    for (int i = 0; i < n; i++) {
      llist[i] = new LinkedList<Integer> ();
    }
    for (LEdge e : g.E) {
      llist[e.v1].add(e.v2);
      llist[e.v2].add(e.v1);
    }
    int[][] adjlist = new int[n][];
    for (int i = 0; i < n; i++) {
      adjlist[i] = new int[llist[i].size()];
      Iterator<Integer> it = llist[i].listIterator();
      int cnt = 0;
      while (it.hasNext()) {
        adjlist[i][cnt++] = it.next();
      }
    }
    return adjlist;
     }
   */

  public static Collection<LGraph> enumGraphs(LGraph[] graphs, int zB) {
    HashSet<LGraph> F = new HashSet<LGraph> ();
    for (LGraph g : graphs) {
      Vector<LGraph> results = enumGraph(g, zB);
      for (LGraph f : results) {
        F.add(f);
      }
    }
    int cnt=0;
    for(LGraph f:F) {
      f.id="F"+(cnt);
      cnt++;
    }
    return F;

  }

  /**
   * Enumerate subgraphs from a graph
   * @param g LGraph
   * @param size the size of subgraphs in terms of the number of edges
   * @return Vector
   */
  public static Vector<LGraph> enumGraph(LGraph g, int zB) {
    int n = g.V.length;
    int m = g.E.length;
    boolean[] visited = new boolean[m];
    Arrays.fill(visited, false);

    int[] vmap = new int[n]; // vmap[i]: the new index of vertex i
    Arrays.fill(vmap, -1);

    int[] eset = new int[zB]; // eset[i]: index of a selected edge
    Arrays.fill(eset, -1);
    Vector<LGraph> results = new Vector<LGraph> ();
    //GenGraph.checkDuplicate(g);
    HashSet<String> dup = new HashSet<String> (); // check for duplicate permutation of edges

    visit(g.V, n, g.E, m, visited, vmap, 0, eset, 0, zB, dup, results);
    return results;
  }

  // Generate a subgraph from a permutation
  private static LGraph genSub(int[] V, int n, LEdge[] E, int m, int[] vmap,
                               int vcnt, int[] eset, int size, String id) {
    int[] V1 = new int[vcnt];

    for (int k = 0; k < n; k++) { // Copy the subset of V
      if (vmap[k] >= 0) {
        V1[vmap[k]] = V[k];
      }
    }

    LEdge[] E1 = new LEdge[size];
    for (int k = 0; k < size; k++) { // Copy the subset of E
      LEdge e1 = E[eset[k]];
      E1[k] = new LEdge(vmap[e1.v1], vmap[e1.v2], e1.label);
    }
    LGraph sub = new LGraph(id, V1, E1);
    return sub;
  }

  private static void visit(int[] V, int n, LEdge[] E, int m, boolean[] visited,
                            int[] vmap, int vcnt, int[] eset, int depth,
                            int zB, HashSet<String> dup,
                            Vector<LGraph> results) {
    if (depth >= zB) { // End of a permutation
      // generate a subgraph
      LGraph sub = genSub(V, n, E, m, vmap, vcnt, eset, zB,null);
      results.add(sub);
    }
    else { // Add another edge
      if (depth == 0) { // the first edge
        for (int i = 0; i < m; i++) {
          LEdge e = E[i];
          visited[i] = true;
          eset[depth] = i;
          vmap[e.v1] = vcnt++;
          vmap[e.v2] = vcnt++;
          visit(V, n, E, m, visited, vmap, vcnt, eset, depth + 1, zB, dup,
                results);

          // Restore
          visited[i] = false;
          vmap[e.v1] = -1;
          vmap[e.v2] = -1;
          vcnt -= 2;
          assert (vcnt == 0);
        }
      }
      else {
        //int i0 = eset[depth - 1] + 1;
        for (int i = 0; i < m; i++) {
          if (!visited[i]) { // E[i] is available
            LEdge e = E[i];
            if (vmap[e.v1] >= 0 || vmap[e.v2] >= 0) { // E[i] is adjacent to the current subgraph
              visited[i] = true;
              eset[depth] = i;
              int newv = -1;
              if (vmap[e.v1] < 0) {
                vmap[e.v1] = vcnt++;
                newv = e.v1;
              }
              else if (vmap[e.v2] < 0) {
                vmap[e.v2] = vcnt++;
                newv = e.v2;
              }
              if (!checkDup(eset, depth + 1, dup)) {
                visit(V, n, E, m, visited, vmap, vcnt, eset, depth + 1, zB,
                      dup, results);
              }

              // Restore
              visited[i] = false;
              if (newv >= 0) {
                vmap[newv] = -1;
                vcnt--;
              }
            }
          }

        }
      } // end of else
    }
  }

  private static int[] dupbuf = new int[100]; // buffer for edge indices
  // Check if a partial eset is duplicate with previous permutations
  private static boolean checkDup(int[] eset, int depth, HashSet<String> dup) {
    System.arraycopy(eset, 0, dupbuf, 0, depth);
    Arrays.sort(dupbuf, 0, depth);
    String str = "" + dupbuf[0];
    for (int z = 1; z < depth; z++) {
      str += "," + dupbuf[z];
    }
    if (dup.contains(str)) {
      return true;
    }
    else {
      dup.add(str);
      return false;
    }

  }

  public static void main(String[] args) throws IOException {
    Opt opt = new Opt(args);
    if (opt.args() < 2) {
      System.err.println("Usage: [options] graph_file basis_file");
      System.err.println("  -map_file=FILE \t default=label.map");
      System.err.println("  -zB=NUMBER \t Size of PCs, default=3");
      System.exit(1);
    }
    String map_file = opt.getString("map_file", "label.map");
    int zB = opt.getInt("zB", 3);
    String graph_file = opt.getArg(0);
    String basis_file = opt.getArg(1);
    LGraph[] graphs = GraphFile.loadGraphs(graph_file, map_file);
    Collection<LGraph> PCs = enumGraphs(graphs, zB);
    PrintStream out = new PrintStream(basis_file);
    LabelMap labelMap = new LabelMap(map_file);
    for (LGraph pc : PCs) {
      out.println(GraphFile.graph2String(pc, labelMap));
    }
    out.close();
  }
}
