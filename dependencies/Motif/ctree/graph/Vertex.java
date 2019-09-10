package ctree.graph;

/**
 * <p>Title: Closure Tree</p>
 *
 * <p> </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p> </p>
 *
 * @author Huahai He
 * @version 1.0
 */
public interface Vertex extends java.io.Serializable {
  /**
   * Whether this vertex can be mapped onto v
   * @param v Vertex
   * @return boolean
   */
  boolean mappable(Vertex v);
}
