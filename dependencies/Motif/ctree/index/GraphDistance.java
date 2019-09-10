package ctree.index;

import ctree.graph.*;

/**
 * <p> Closure-tree</p>
 *
 * @author Huahai He
 * @version 1.0
 */
public interface GraphDistance extends java.io.Serializable {
  double d(Graph g1, Graph g2, int[] map, boolean sub);
}
