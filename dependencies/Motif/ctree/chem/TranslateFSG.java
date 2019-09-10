package ctree.chem;

import java.io.*;
import java.util.*;

import ctree.lgraph.*;


/**
 * Translate graphs in FSG input format into the format used in closure tree.
 * FSG is a tool for frequent subgraph discovery.
 *
 * Reference:
 *  M. Kuramochi and G. Karypis, "Frequent Subgraph Discovery", IEEE
 *  conference on Data Mining 2001.
 *
 * @see http://www-users.cs.umn.edu/~karypis/software/index.html
 *
 * @author Huahai He
 * @version 1.0
 */

public class TranslateFSG {
  public static void main(String[] args) throws Exception {
    if (args.length < 2) {
      System.err.println("Usage: ... input_file output_file");
      System.exit(1);
    }
    System.err.println("Load FSG graphs");
    LGraph[] graphs = loadFSGGraph(args[0]);
    GraphsInfo.statGraphs(graphs);
    System.err.println("Save to " + args[1]);
    LGraphFile.saveLGraphs(graphs, args[1]);
  }

  private static String readLine(BufferedReader in) throws IOException {
    String line;
    while (true) {
      line = in.readLine();
      if (line == null) {
        break;
      }
      line = line.trim();
      if (line.length() > 0 && !line.startsWith("#")) {
        break;
      }

    }
    return line;
  }

  public static LGraph[] loadFSGGraph(String filename) throws
      IOException {
    BufferedReader in = new BufferedReader(new FileReader(filename));
    Vector graphs = new Vector();
    String line;
    Vector vect1 = new Vector();
    Vector vect2 = new Vector();

    line = readLine(in);
    while (line != null) {
      // graph ID
      assert(line.startsWith("t # "));
      String id = line.substring(4);

      // vertices
      vect1.clear();
      while ( (line = readLine(in).trim()).startsWith("v")) {
        //Vertex v = new Vertex(line.split(" ")[2]);
        //!!!
        StringTokenizer tokens = new StringTokenizer(line);
        tokens.nextToken();
        tokens.nextToken();
        LVertex v = new LVertex(tokens.nextToken());

        vect1.addElement(v);
      }

      vect2.clear();
      do {
        //String[] pairs = line.split(" ");
        //int v1 = Integer.parseInt(pairs[1]);
        //int v2 = Integer.parseInt(pairs[2]);
        //!!!
        StringTokenizer tokens = new StringTokenizer(line);
        tokens.nextToken();
        int v1 = Integer.parseInt(tokens.nextToken());
        int v2 = Integer.parseInt(tokens.nextToken());

        UnlabeledEdge e = new UnlabeledEdge(v1, v2, false);
        vect2.addElement(e);
      }
      while ( (line = readLine(in)) != null &&
             (line.startsWith("u") || line.startsWith("e")));

      LVertex[] vertices = new LVertex[vect1.size()];
      vect1.toArray(vertices);
      UnlabeledEdge[] edges = new UnlabeledEdge[vect2.size()];
      vect2.toArray(edges);
      LGraph g = new LGraph(vertices, edges, id);
      graphs.addElement(g);
    }

    in.close();
    LGraph[] array = new LGraph[graphs.size()];
    graphs.toArray(array);
    return array;

  }
}
