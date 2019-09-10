package grank.old;

import java.io.*;
import java.util.*;
import grank.graph.*;
import ctree.util.*;

/**
 * Generate primitive components (PCs) from a graph file by enumeration.
 * Each PC is a connected subgraph with diameter 2.
 *
 * @author Huahai He
 * @version 1.0
 */
public class GenPC2 {
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

  public static Collection<LGraph> genFeatures(LGraph[] graphs) {
    HashSet<LGraph> F = new HashSet<LGraph> ();
    for (LGraph g : graphs) {
      Vector<LGraph> results = genFeature(g);
      for (LGraph f : results) {
        F.add(f);
      }
    }
    int cnt = 0;
    for (LGraph f : F) {
      f.id = "F" + (cnt);
      cnt++;
    }
    return F;

  }

  // for adjacency list
  private static class Pair {
    int vlabel; // neighbor label
    int elabel; // edge label
    public Pair(int _vlabel, int _elabel) {
      vlabel = _vlabel;
      elabel = _elabel;
    }
  }

  /**
   * Enumerate subgraphs from a graph
   * @param g LGraph
   * @param size the size of subgraphs in terms of the number of edges
   * @return Vector
   */
  public static Vector<LGraph> genFeature(LGraph g) {
    int n = g.V.length;

    // generate adjacency list
    Vector<Pair> [] adjlist = new Vector[n];
    for (int i = 0; i < n; i++) {
      adjlist[i] = new Vector<Pair> ();
    }

    for (LEdge e : g.E) {
      Pair pair1 = new Pair(g.V[e.v2], e.label);
      adjlist[e.v1].add(pair1);
      Pair pair2 = new Pair(g.V[e.v1], e.label);
      adjlist[e.v2].add(pair2);
    }

    // enumerate each vertex
    Vector<LGraph> results = new Vector<LGraph> ();

    for (int i = 0; i < n; i++) {
      int size = adjlist[i].size(); // number of neighbors
      if (size <= 1 ) {  // discard leaf vertices
        continue;
      }

      // create a small subgraph
      int[] V = new int[size + 1];
      V[0] = g.V[i];        // root vertex
      LEdge[] E = new LEdge[size];
      for (int j = 0; j < size; j++) {
        Pair pair = adjlist[i].get(j);
        V[j + 1] = pair.vlabel;
        E[j] = new LEdge(0, j + 1, pair.elabel);
      }
      LGraph sub = new LGraph(null, V, E);
      results.add(sub);
    }
    return results;
  }

  public static void main(String[] args) throws IOException {
    Opt opt = new Opt(args);
    if (opt.args() < 2) {
      System.err.println("Usage: [options] graph_file basis_file");
      System.err.println("  -map_file=FILE \t default=label.map");
      System.exit(1);
    }
    String map_file = opt.getString("map_file", "label.map");
    String graph_file = opt.getArg(0);
    String basis_file = opt.getArg(1);
    LGraph[] graphs = GraphFile.loadGraphs(graph_file, map_file);
    Collection<LGraph> features = genFeatures(graphs);
    PrintStream out = new PrintStream(basis_file);
    LabelMap labelMap = new LabelMap(map_file);
    for (LGraph f : features) {
      out.println(GraphFile.graph2String(f, labelMap));
    }
    out.close();
  }
}
