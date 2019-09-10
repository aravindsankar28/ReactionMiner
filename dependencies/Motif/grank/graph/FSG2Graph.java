package grank.graph;

import java.io.*;
import java.util.*;

import static grank.graph.GraphFile.*;
import ctree.util.*;

/**
 * Convert between an FSG file and a graph file.
 * FSG format:
 * t # id
 * v 0 label0
 * ...
 * u 0 1 label0
 * ...
 *
 * @author Huahai He
 * @version 1.0
 */
public class FSG2Graph {

  /**
   * Convert an FSG file into a graph file.
   * @param fsg_file String
   * @param graph_file String
   * @throws IOException
   */
  public static void fsg2graph(String fsg_file, String graph_file) throws
      IOException {
    BufferedReader in = new BufferedReader(new FileReader(fsg_file));
    PrintWriter out = new PrintWriter(graph_file);

    String line = readLine(in, true);
    while (true) {
      if (line == null) {
        break;
      }

      // graph ID
      assert (line.startsWith("t"));
      String id = line.substring(1).trim();
      if (id.startsWith("#")) {
        id = id.substring(1).trim();
      }

      // vertices
      Vector<String> vertices = new Vector<String> ();
      while (true) {
        line = readLine(in, true);
        if (!line.startsWith("v")) {
          break;
        }
        String[] list = line.split(" +");
        int vid = Integer.parseInt(list[1]);
        assert (vid == vertices.size());
        vertices.add(list[2]);
      }

      // edges
      Vector<String> edges = new Vector<String> ();
      while (true) {
        String[] list = line.split(" +");
        edges.add(list[1] + ' ' + list[2] + ' ' + list[3]);

        line = readLine(in, true);
        if (line == null || !line.startsWith("u")) {
          break;
        }
      }

      // output to graph format
      out.println("#" + id);
      out.println(vertices.size());
      for (int i = 0; i < vertices.size(); i++) {
        out.println(vertices.elementAt(i));
      }
      out.println(edges.size());
      for (int i = 0; i < edges.size(); i++) {
        out.println(edges.elementAt(i));
      }
      out.println();
    }
    in.close();
    out.close();
  }

  /**
   * Convert a graph file into an FSG file.
   * @param graph_file String
   * @param fsg_file String
   * @throws IOException
   */
  public static void graph2fsg(String graph_file, String fsg_file) throws
      IOException {
    BufferedReader in = new BufferedReader(new FileReader(graph_file));
    PrintWriter out = new PrintWriter(fsg_file);

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
      String[] vertices = new String[n];
      for (int i = 0; i < n; i++) {
        line = readLine(in, false);
        vertices[i] = line;
      }

      //edges
      line = readLine(in, false);
      int m = Integer.parseInt(line);
      String[] edges = new String[m];
      for (int i = 0; i < m; i++) {
        line = readLine(in, false);
        edges[i] = line;
      }

      // output to fsg format
      out.println("t # " + id);
      for (int i = 0; i < n; i++) {
        out.printf("v %d %s\n", i, vertices[i]);
      }
      for (int i = 0; i < m; i++) {
        out.printf("u %s\n", edges[i]);
      }

    }

    in.close();
    out.close();
  }

  /**
   * Convert between an FSG file and a Graph file.
   * @param args String[]
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    if (args.length < 3) {
      System.err.println(
          "Usage: ... -fsg graph_file fsg_file  \t Graph file -> FSG file");
      System.err.println(
          "           -g fsg_file graph_file  \t FSG file -> Graph file");
      System.exit(1);
    }

    Opt opt = new Opt(args);
    if (opt.hasOpt("fsg")) {
      graph2fsg(opt.getArg(0), opt.getArg(1));
    }
    else if(opt.hasOpt("g")) {
      fsg2graph(opt.getArg(0),opt.getArg(1));
    }
  }
}
