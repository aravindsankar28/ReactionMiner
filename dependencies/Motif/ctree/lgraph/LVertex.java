package ctree.lgraph;

import ctree.graph.*;

/**
 * <p> Closure-tree</p>
 *
 * @author Huahai He
 * @version 1.0
 */
public class LVertex implements Vertex {
  protected String label;
  protected boolean containsNull;
  public LVertex() {
  }

  public LVertex(String _label, boolean _containsNull) {
    label = _label;
    containsNull = _containsNull;
  }

  public LVertex(String _label) {
    label = _label;
    containsNull = false;
  }

  /**
   * compatible
   *
   * @param v Vertex
   * @return boolean
   */
  public boolean mappable(Vertex v) {
    return (v instanceof LVertex) &&
        ( (LVertex) v).label.equals(label);
  }

  public String toString() {
    String s = label;
    if (containsNull) {
      s += "|null";
    }
    return s;

  }
}
