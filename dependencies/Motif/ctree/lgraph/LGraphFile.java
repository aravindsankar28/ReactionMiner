package ctree.lgraph;

import java.io.*;
import java.util.*;

import ctree.graph.*;

/**
 * File format:
 *
 *  id
 * label, label, label, ...      //vertices
 * int int, int int, ...         //edges
 *
 * id
 * label, ...
 * int int, ...
 *
 *
 * @author Huahai He
 * @version 1.0
 */
public class LGraphFile  {
  public LGraphFile() {
  }

  /**
   * load graphs
   *
   * @param filename String
   * @return Graph[]
   */
  public static LGraph[] loadLGraphs(String filename) throws
      IOException {
    BufferedReader in = new BufferedReader(new FileReader(filename));
    Vector<LGraph> graphs = new Vector();

    String line;
    while (true) {
      do {
        line = in.readLine();
        if (line == null) {
          break;
        }
        line = line.trim();
      }
      while (line.length() == 0);

      if (line == null) {
        break;
      }

      // graph ID
      assert (line.charAt(0) == '#');
      String id = line.substring(1).trim();

      // vertices
      line = in.readLine().trim();
      int n = Integer.parseInt(line);
      LVertex[] vertices = new LVertex[n];
      for (int i = 0; i < n; i++) {
        line = in.readLine().trim();
        vertices[i] = new LVertex(line);
      }

      //edges
      line = in.readLine().trim();
      int m = Integer.parseInt(line);
      UnlabeledEdge[] edges = new UnlabeledEdge[m];
      for (int i = 0; i < m; i++) {
        line = in.readLine().trim();
        //String[] fields = line.split(" +");
        //int v1 = Integer.parseInt(fields[0]);
        //int v2 = Integer.parseInt(fields[1]);
        //!!!
        StringTokenizer tokens = new StringTokenizer(line);
        int v1 = Integer.parseInt(tokens.nextToken());
        int v2 = Integer.parseInt(tokens.nextToken());
        int w = Integer.parseInt(tokens.nextToken());
        String st  = "0";
        // TODO ::
        st = tokens.nextToken();
        
        edges[i] = new UnlabeledEdge(v1, v2, w,st, false);
      }

      LGraph g = new LGraph(vertices, edges, id);
      graphs.addElement(g);
    }

    in.close();
    LGraph[] array = new LGraph[graphs.size()];
    graphs.toArray(array);
    return array;
  }

  /**
   * saveGraphs
   *
   * @param graphs Graph[]
   * @param filename String
   */
  public static void saveLGraphs(LGraph[] graphs, String filename) throws
      IOException {
    PrintStream out = new PrintStream(new FileOutputStream(filename));
    for (int i = 0; i < graphs.length; i++) {
      out.println(graphs[i].toString());
    }
    out.close();

  }
}
