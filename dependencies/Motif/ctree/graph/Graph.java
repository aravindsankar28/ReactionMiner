package ctree.graph;

import ctree.graph.*;

/**
 *
 *
 * @author Huahai He
 * @version 1.0
 */
public interface Graph extends java.io.Serializable {
  Vertex[] V();
  Edge[] E();
  int numV();
  int numE();
  int[][] adjMatrix();
  int[][] adjList();
  Edge[][] adjEdges();
  //boolean isDirected();
}
