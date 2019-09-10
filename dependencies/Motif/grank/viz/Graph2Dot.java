package grank.viz;

import grank.graph.*;
import java.io.*;
import ctree.util.*;

/**
 * Transform a graph into the Graphviz Dot format.
 *
 * @author Huahai He
 * @version 1.0
 */
public class Graph2Dot {
  public static void graph2Dot(LGraph g, LabelMap map, PrintWriter out) {
    out.println("graph \"" + g.id + "\" {");
    for (int i = 0; i < g.V.length; i++) {
      out.printf("%d [label=%s];\n", i, map.vlab[g.V[i]]);
    }
    for (int i = 0; i < g.E.length; i++) {
      LEdge e = g.E[i];
      out.printf("%d -- %d [label=%s];\n", e.v1, e.v2, map.elab[e.label]);
    }
    out.println("}\n");
  }

  public static void graph2CML(LGraph g, LabelMap map, PrintWriter out) {
    out.printf("<molecule id=\"%s\">\n", g.id);
    out.printf("  <atomArray>\n");
    for (int i = 0; i < g.V.length; i++) {
      out.printf("    <atom id=\"a%d\" elementType=\"%s\" />\n", i,
                 map.vlab[g.V[i]]);
    }
    out.printf("  </atomArray>\n  <bondArray>\n");
    for (int i = 0; i < g.E.length; i++) {
      LEdge e = g.E[i];
      out.printf("    <bond atomRefs2=\"a%d a%d\" order=\"%s\" />\n", e.v1, e.v2,
                 map.elab[e.label]);
    }
    out.print("  </bondArray>\n</molecule>\n");
  }

  public static void graph2DotFile(String graph_file, String map_file,
                                   String dot_file, String format) throws
      IOException {
    LabelMap map = new LabelMap(map_file);
    LGraph[] D = GraphFile.loadGraphs(graph_file, map);
    PrintWriter out = new PrintWriter(dot_file);
    for (int i = 0; i < D.length; i++) {
      if (format.equals("dot")) {
        graph2Dot(D[i], map, out);
      }
      else if (format.equals("cml")) {
        graph2CML(D[i], map, out);
      }
    }
    out.close();
  }

  public static void main(String[] args) throws IOException {
    Opt opt = new Opt(args);
    if (opt.args() < 2) {
      System.err.println("Usage: ... [options] graph_file dot_file");
      System.err.println("  -map_file=FILE \t default=label.map");
      System.err.println("  -format=[dot|cml] \t default=cml");
      System.exit(1);
    }
    String map_file = opt.getString("map_file", "label.map");
    String graph_file = opt.getArg(0);
    String dot_file = opt.getArg(1);
    String format = opt.getString("format", "cml");
    graph2DotFile(graph_file, map_file, dot_file, format);
  }
}
