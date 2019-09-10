package grank.graph;

import java.io.*;
import java.util.*;
import ctree.util.*;

/**
 * Load graphs from or save graphs into a file.
 * Format:
 * #id
 * Number of V
 * label of V[0]
 * ...
 * Number of E
 * v1 v2 label
 * ...
 *
 *
 * @author Huahai He
 * @version 1.0
 */
public class GraphFile {

  // Save graphs
  /*
     public static String toString(LGraph g) {
    String s = "#" + g.id + "\n";
    s += g.V.length + "\n";
    for (int i = 0; i < g.V.length; i++) {
      s += g.V[i] + "\n";
    }
    s += g.E.length + "\n";
    for (int i = 0; i < g.E.length; i++) {
      LEdge e = g.E[i];
      s += e.v1 + " " + e.v2+ " " + e.label + "\n";
    }
    return s;
     }
   */

  public static String graph2String(LGraph g, LabelMap map) {
    String s = "#" + g.id + "\n";
    s += g.V.length + "\n";
    for (int i = 0; i < g.V.length; i++) {
      s += map.vlab[g.V[i]] + "\n";
    }
    s += g.E.length + "\n";
    for (int i = 0; i < g.E.length; i++) {
      LEdge e = g.E[i];

      s += e.v1 + " " + e.v2 + " " + map.elab[e.label] + "\n";
    }
    return s;
  }

  public static void saveGraphs(LGraph[] D, String graph_file, String map_file) throws
      IOException {
    PrintStream out = new PrintStream(graph_file);
    LabelMap map = new LabelMap(map_file);
    for (int i=0;i<D.length;i++) {
      String s = graph2String(D[i], map);
      out.println(s);
    }
    out.close();
  }

  /*
     public static LGraph[] loadGraphs(String graph_file) throws
      IOException {
    BufferedReader in = new BufferedReader(new FileReader(graph_file));
    Vector<LGraph> graphs = new Vector<LGraph> ();

    Hashtable<String, Integer> vmap = new Hashtable<String, Integer> ();
    int vlabelCount = 0;
    Hashtable<String, Integer> emap = new Hashtable<String, Integer> ();
    int elabelCount = 0;

    String line;
    while (true) {
      line = readLine(in, false);
      if (line == null) {
        break;
      }

      // graph ID
      assert (line.charAt(0) == '#');
      String id = line.substring(1).trim();

      // vertices
      line = readLine(in, false).trim();
      int n = Integer.parseInt(line);
      int[] vertices = new int[n];
      for (int i = 0; i < n; i++) {
        line = readLine(in, false);
        Integer vlabel = vmap.get(line);
        if (vlabel == null) {
          vmap.put(line, vlabelCount);
          vlabel = vlabelCount;
          vlabelCount++;
        }
        vertices[i] = vlabel;
      }

      //edges
      line = readLine(in, false);
      int m = Integer.parseInt(line);
      LEdge[] edges = new LEdge[m];
      for (int i = 0; i < m; i++) {
        line = readLine(in, false);
        String[] fields = line.split(" +");
        int v1 = Integer.parseInt(fields[0]);
        int v2 = Integer.parseInt(fields[1]);
        Integer elabel = emap.get(fields[2]);
        if (elabel == null) {
          emap.put(fields[2], elabelCount);
          elabel = elabelCount;
          elabelCount++;
        }
        edges[i] = new LEdge(v1, v2, elabel);
      }

      LGraph g = new LGraph(id, vertices, edges);
      graphs.addElement(g);
    }

    in.close();
    LGraph[] array = new LGraph[graphs.size()];
    graphs.toArray(array);
    return array;
     } */
  public static LGraph[] loadGraphs(String graph_file, String map_file) throws
      IOException {
    LabelMap map = new LabelMap(map_file);
    return loadGraphs(graph_file, map);
  }

  public static LGraph[] loadGraphs(String graph_file, LabelMap map) throws
      IOException {
    BufferedReader in = new BufferedReader(new FileReader(graph_file));
    Vector<LGraph> graphs = new Vector<LGraph> ();

    String line;
    while (true) {
      line = readLine(in, false);
      if (line == null) {
        break;
      }

      // graph ID
      assert (line.charAt(0) == '#');
      String id = line.substring(1).trim();

      // vertices
      line = readLine(in, false);
      int n = Integer.parseInt(line);
      int[] V = new int[n];
      for (int i = 0; i < n; i++) {
        line = readLine(in, false);
        Integer vlabel = map.vmap.get(line);
        assert (vlabel != null);
        V[i] = vlabel;
      }

      //edges
      line = readLine(in, false);
      int m = Integer.parseInt(line);
      LEdge[] E = new LEdge[m];
      for (int i = 0; i < m; i++) {
        line = readLine(in, false);
        String[] fields = line.split(" +");
        int v1 = Integer.parseInt(fields[0]);
        int v2 = Integer.parseInt(fields[1]);
        Integer elabel = map.emap.get(fields[2]);
        E[i] = new LEdge(v1, v2, elabel);
      }

      LGraph g = new LGraph(id, V, E);
      graphs.addElement(g);
    }

    in.close();
    LGraph[] array = new LGraph[graphs.size()];
    graphs.toArray(array);
    return array;
  }

  /**
   * Read a non-empty line
   * @param in BufferedReader
   * @param omitSharp if true, then omit lines starting with "#"
   * @return A non-empty line, or null if reached the end of file.
   * @throws IOException
   */
  public static String readLine(BufferedReader in, boolean omitSharp) throws
      IOException {
    String line;
    while (true) {
      line = in.readLine();
      if (line == null) {
        break;
      }
      line = line.trim();
      if (line.length() > 0 && ! (omitSharp && line.startsWith("#"))) {
        break;
      }
    }
    return line;
  }

  public static void main(String[] args) throws IOException {
    Opt opt = new Opt(args);
    if (opt.args() < 1) {
      System.err.println("Usage: ... [options] graph_file");
      System.err.println("  -map_file=FILE \t default=label.map");
      System.err.println("  -order \t save graphs in descending order");
      System.exit(1);
    }

    String map_file = opt.getString("map_file", "label.map");
    String graph_file = opt.getArg(0);
    LGraph[] graphs = GraphFile.loadGraphs(graph_file, map_file);

    // Stat
    int maxDeg = 0;
    double avgDeg = 0;
    for (LGraph g : graphs) {
      int[][] adj = g.adjmatrix();
      for (int r = 0; r < adj.length; r++) {
        int cnt = 0;
        for (int j = 0; j < adj[r].length; j++) {
          if (adj[r][j] > 0) {
            cnt++;
          }
        }
        if (cnt > maxDeg) {
          maxDeg = cnt;
        }
      }
      avgDeg += 2.0 * g.E.length / g.V.length;
    }
    avgDeg /= graphs.length;
    System.out.printf("# of graphs: %d\n", graphs.length);
    System.out.printf("maxDeg = %d, avgDeg=%.2f\n", maxDeg, avgDeg);
    if (opt.hasOpt("order")) {
      for (int i = 0; i < graphs.length; i++) {
        for (int j = i + 1; j < graphs.length; j++) {
          LGraph g1 = graphs[i];
          int z1 = g1.E.length;
          String sub = g1.id.substring(g1.id.lastIndexOf(' ') + 1);
          int sup1 = Integer.parseInt(sub);

          LGraph g2 = graphs[j];
          int z2 = g2.E.length;
          sub = g2.id.substring(g2.id.lastIndexOf(' ') + 1);
          int sup2 = Integer.parseInt(sub);
          if (z1 < z2 || (z1 == z2 && sup1 < sup2)) {
            LGraph temp = graphs[i];
            graphs[i] = graphs[j];
            graphs[j] = temp;
          }
        }
      }
      saveGraphs(graphs, graph_file, map_file);
    }
  }
}
