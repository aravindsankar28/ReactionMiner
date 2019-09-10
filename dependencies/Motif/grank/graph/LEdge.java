package grank.graph;

/**
 *
 * @author Huahai He
 * @version 1.0
 */
public class LEdge {
  public int v1, v2; // assume undirected egdge, v1<=v2
  public int label;
  public LEdge(int _v1, int _v2, int _label) {
    if(_v1<=_v2) {
      v1 = _v1;
      v2 = _v2;
    } else {
      v1=_v2;
      v2=_v1;
    }
    label = _label;
  }

}
