package grank.graph;

import java.io.*;
import java.util.*;
import grank.graph.*;
import static grank.graph.GraphFile.*;
import ctree.util.*;

/**
 * Convert between CloseGraph format and graph format.
 *
 * @author Huahai He
 * @version 1.0
 */
public class CG2Graph {

  /**
   * Convert a CG file into a graph file.
   * @param cg_file String
   * @param graph_file String
   * @throws IOException
   */
  public static void cg2graph(String cg_file, String graph_file,
                              LabelMap labelMap) throws
      IOException {
    BufferedReader in = new BufferedReader(new FileReader(cg_file));
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
      Vector<String[]> edges = new Vector<String[]> ();
      while (true) {
        String[] list = line.split(" +");
        edges.add(list);

        line = readLine(in, true);
        if (line == null || !line.startsWith("e")) {
          break;
        }
      }

      // output to graph format
      out.println("#" + id.replace("*",","));
      out.println(vertices.size());
      for (int i = 0; i < vertices.size(); i++) {
        int idx = Integer.parseInt(vertices.get(i));
        out.println(labelMap.vlab[idx]);
      }
      out.println(edges.size());
      for (int i = 0; i < edges.size(); i++) {
        String[] list = edges.get(i);
        int idx= Integer.parseInt(list[3]);
        out.printf("%s %s %s\n", list[1],list[2],labelMap.elab[idx]);
      }
      out.println();
    }
    in.close();
    out.close();
  }

  /**
   * Convert a graph file into an CG file.
   * @param graph_file String
   * @param cg_file String
   * @throws IOException
   */
  public static void graph2cg(String graph_file, String cg_file,
                              LabelMap labelMap) throws
      IOException {
    LGraph[] graphs = GraphFile.loadGraphs(graph_file, labelMap);
    PrintWriter out = new PrintWriter(cg_file);

    for (int i = 0; i < graphs.length; i++) {
      LGraph g = graphs[i];
      out.printf("t # %s\n", g.id);

      for (int j = 0; j < g.V.length; j++) { // vertices
        out.printf("v %d %d\n", j, g.V[j]);
      }

      for (int j = 0; j < g.E.length; j++) { // edges
        out.printf("e %d %d %d\n", g.E[j].v1, g.E[j].v2, g.E[j].label);
      }
    }
    out.close();
  }

  /**
   * Convert between an FSG file and a Graph file.
   * @param args String[]
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    Opt opt = new Opt(args);
    if (opt.args() < 2 || opt.opts()<1) {
      System.err.println(
          "Usage: ... -cg graph_file cg_file  \t Graph file -> CG file");
      System.err.println(
          "           -g cg_file graph_file  \t CG file -> Graph file");
      System.err.println("  -map_file=FILE \t default=label.map");
      System.exit(1);
    }

    LabelMap labelMap = new LabelMap(opt.getString("map_file", "label.map"));
    if (opt.hasOpt("cg")) {
      graph2cg(opt.getArg(0), opt.getArg(1), labelMap);
    }
    else if (opt.hasOpt("g")) {
      cg2graph(opt.getArg(0), opt.getArg(1), labelMap);
    }
  }
}
