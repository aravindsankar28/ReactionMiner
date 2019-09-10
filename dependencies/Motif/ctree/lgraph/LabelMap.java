package ctree.lgraph;

import java.util.*;
import java.io.*;
import ctree.graph.*;

/**
 *
 * @author Huahai He
 * @version 1.0
 */

public class LabelMap implements Serializable {
  Map<String, Integer> map = new HashMap(100);
  private int cnt;
  public LabelMap() {
  }

  public LabelMap(LGraph g) {
    importGraph(g);
  }

  public LabelMap(LGraph[] graphs) {
    importGraphs(graphs);
  }

  public void clear() {
    map.clear();
    cnt = 0;
  }

  public void importGraphs(LGraph[] graphs) {
    //clear();
    for (LGraph g:graphs) {
      importGraph(g);
    }
  }

  public void importGraph(LGraph g) {
    LVertex[] V = (LVertex[])g.V();
    for (LVertex v:V) {
      if (!map.containsKey(v.label)) {
        map.put(v.label, new Integer(cnt++));
      }
    }
  }

  /** Import and return an array of vertex labels */
  public int[] importGraph2(LGraph g) {
    LVertex[] V = (LVertex[])g.V();
    int[] vlabels = new int[V.length];
    for(int i = 0; i < V.length; i++) {
      String label = V[i].label;
      Integer I = map.get(label);
      if(I==null) {
        vlabels[i] = cnt;
        map.put(label, new Integer(cnt++));
      } else {
        vlabels[i] = I.intValue();
      }
    }
    return vlabels;
  }

  public Map<String, Integer> getMap() {
    return map;
  }

  public int indexOf(Vertex v) {
    Integer I = map.get(((LVertex)v).label);
    if (I == null) {
      return -1;
    }
    return I.intValue();
  }

  public int indexOf(String label) {
    Integer I = map.get(label);
     if (I == null) {
       return -1;
     }
     return I.intValue();

  }

  /**
   * Return label indices of vertices.
   * @param g Graph
   * @return vlabels[i] is the label index of vertex i
   */
  public int[] vertexLabels(Graph g) {
    Vertex[] V = g.V();
    int[] vlabels = new int[V.length];
    for (int i = 0; i < V.length; i++) {
      vlabels[i] = indexOf(V[i]);
    }
    return vlabels;
  }

  /**
   * Return reverse label indices of vertices
   * @param g Graph
   * @return labelv[i]
   */
  public int[] labelVertices(Graph g) {
    Vertex[] V = g.V();
    int[] labelv = new int[size()];
    Arrays.fill(labelv, 0);
    for (int i = 0; i < V.length; i++) {
      int x = indexOf(V[i]);
      if(x<0)continue;
      labelv[x]++;
    }
    return labelv;

  }

  /**
   * Number of labels
   * @return int
   */
  public int size() {
    return map.size();
  }

}
