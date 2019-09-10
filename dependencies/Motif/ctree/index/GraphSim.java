package ctree.index;

import ctree.graph.*;

/**
 * <p> Closure-tree</p>
 *
 * @author Huahai He
 * @version 1.0
 */
public interface GraphSim extends java.io.Serializable {
  int sim(Graph g1, Graph g2, int[] map);

  int simUpper(Graph g1, Graph g2);

  int norm(Graph g);
}
