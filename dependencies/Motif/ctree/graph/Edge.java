package ctree.graph;

import ctree.lgraph.*;

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
public interface Edge extends java.io.Serializable {
  /**
   * Whether this edge can be mapped onto e
   * @param e Edge
   * @return boolean
   */
  boolean mappable(Edge e);

  int v1();

  int v2();
  
  int w();
  
  String stereo();
  
}
