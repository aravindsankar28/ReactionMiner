package ctree.lgraph;

import java.util.*;
import ctree.graph.*;
import ctree.index.*;

/**
 * <p> Closure-tree</p>
 *
 * @author Huahai He
 * @version 1.0
 */
public class LGraphFactory implements GraphFactory {
  private LabelMap labelMap;
  private int dim1, dim2;

  protected LGraphFactory() {
  }

  public LGraphFactory(LabelMap _labelMap, int _dim1, int _dim2) {
    labelMap = _labelMap;
    dim1 = _dim1;
    dim2 = _dim2;
  }

  public Graph graphClosure(Graph g1, Graph g2, int[] map) {
    return graphClosure( (LGraph) g1, (LGraph) g2, map);
  }

  public static LGraph graphClosure(LGraph g1, LGraph g2,
                                          int[] map) {
    LVertex[] V1 = (LVertex[]) g1.V();
    LVertex[] V2 = (LVertex[]) g2.V();
    int n1 = V1.length;
    int n2 = V2.length;
    assert (n1 == map.length);

    // reverse map
    int[] rmap = new int[n2];
    Arrays.fill(rmap, -1);
    int cnt = 0;
    for (int i = 0; i < n1; i++) {
      if (map[i] >= 0) {
        rmap[map[i]] = i;
        cnt++;
      }
    }

    // Merge vertices
    int n = n1 + n2 - cnt; // number of closure vertices
    LVertex[] V = new LVertex[n]; // closure vertices

    // copy V1 and add V2 having mapping
    for (int i = 0; i < n1; i++) {
      if (map[i] == -1) { // has no mapping
        V[i] = new LVertex(V1[i].label, true);
      }
      else { // has mapping
        boolean b = V1[i].containsNull | V2[map[i]].containsNull;
        V[i] = new LVertex( ( (LVertex) V1[i]).label, b);
      }
    }

    // copy unmapped vertices in V2
    cnt = n1;
    for (int j = 0; j < n2; j++) {
      if (rmap[j] == -1) {
        LVertex vc = new LVertex( ( (LVertex) V2[j]).label, true);
        V[cnt] = vc;
        rmap[j] = cnt; // update mapping index
        cnt++;
      }
    }
    assert (cnt == n);

    // Merge edges
    int[][] adj2 = g2.adjMatrix();
    Vector<UnlabeledEdge> edges = new Vector(); // new edges

    // edges in g1
    UnlabeledEdge[] E1 = (UnlabeledEdge[]) g1.E();
    for (UnlabeledEdge e : E1) {
      // if e=1 and e'=1 then 1 else 2
      int p1 = map[e.v1()];
      int p2 = map[e.v2()]; // e'=(p1,p2) is a mapping of e
      if (e.containsNull == false && p1 >= 0 && p2 >= 0 &&
          adj2[p1][p2] == 1) {
        edges.addElement(new UnlabeledEdge(e.v1(), e.v2(), e.containsNull)); // need clone?
      }
      else {
        edges.addElement(new UnlabeledEdge(e.v1(), e.v2(), true));
      }
    }

    // unmapped edges in g2
    UnlabeledEdge[] E2 = (UnlabeledEdge[]) g2.E();
    int[][] adj1 = g1.adjMatrix();
    for (UnlabeledEdge e : E2) {
      int p1 = rmap[e.v1];
      int p2 = rmap[e.v2];
      // if e is unmapped then 2
      if (p1 >= n1 || p2 >= n1 || adj1[p1][p2] == 0) {
        edges.addElement(new UnlabeledEdge(p1, p2, true));
        // check why this is done.
        //if(p1>=n1 || p2>=n1) 
        	//new Scanner(System.in).next();
      }
    }

    UnlabeledEdge[] E = new UnlabeledEdge[edges.size()];
    edges.toArray(E);
    LGraph gc = new LGraph(V, E,  null);
    return gc;
  }

  /**
   * Merge two graphs, similar to graph closure except that null vertices
   * and null edges are removed.
   * @param g1 LabeledGraph
   * @param g2 LabeledGraph
   * @return Graph
   */
  public static LGraph mergeGraphs(LGraph g1, LGraph g2,
                                         int[] map) {
    LGraph g = graphClosure(g1, g2, map);
    for (LVertex v : g.V) {
      v.containsNull = false;
    }
    for (UnlabeledEdge e : g.E) {
      e.containsNull = false;
    }
    return g;
  }

  /**
   * Generate a histogram from a graph
   * @param g Graph
   */
  public Hist toHist(Graph g) {

    short[] hist = new short[dim1 + dim2];
    Arrays.fill(hist, (short) 0);

    // bins for vertices
    int[] vlabels = labelMap.vertexLabels(g);
    for (int i = 0; i < vlabels.length; i++) {
      int x = vlabels[i];
      if (x < 0) {
        continue;
      }
      hist[x % dim1]++;
    }

    // bins for edges
    Edge[] edges = g.E();
    for (int i = 0; i < edges.length; i++) {
      int x1 = vlabels[edges[i].v1()];
      int x2 = vlabels[edges[i].v2()];
      if (x1 < 0 || x2 < 0) {
        continue;
      }
      if (x1 > x2) {
        int tmp = x1;
        x1 = x2;
        x2 = tmp;
      }
      int x = (x1 * labelMap.size() + x2) % dim2;
      hist[dim1 + x]++;
    }

    return new Hist(hist);
  }

}
