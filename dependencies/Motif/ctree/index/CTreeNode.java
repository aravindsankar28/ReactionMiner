package ctree.index;

import java.io.*;
import java.util.*;

import ctree.graph.*;


public class CTreeNode implements Serializable {
  private CTree ctree;
  private CTreeNode parent;
  private Vector entries; //Element type is CTreeNode if internal node, else Graph
  private boolean isLeaf;
  private boolean isRoot;
  private Graph closure;

  private Vector<Hist> childHists; // features of the children

  /**
   * The no-arg constructor is used only for serialization
   */
  protected CTreeNode() {
  }

  public CTreeNode(CTree _ctree, CTreeNode _parent, Vector _entries,
                   boolean _isLeaf,
                   boolean _isRoot) {
    ctree = _ctree;
    parent = _parent;
    entries = _entries;
    isLeaf = _isLeaf;
    isRoot = _isRoot;

    if (!isLeaf) {
      for (int i = 0; i < entries.size(); i++) {
        ( (CTreeNode) entries.elementAt(i)).parent = this;
      }
    }
    computeClosure();

    // compute histograms of children
    childHists = new Vector(entries.size());
    for (int i = 0; i < entries.size(); i++) {
      Hist f;
      if (isLeaf) {
        f = ctree.factory.toHist(childGraphAt(i));
      }
      else {
        Vector vect = ( (CTreeNode) childAt(i)).childHists;
        Hist[] features = new Hist[vect.size()];
        vect.toArray(features);
        f = Hist.ceiling(features);
      }
      childHists.addElement(f);
    }

  }

  public boolean isLeaf() {
    return isLeaf;
  }

  public boolean isRoot() {
    return isRoot;
  }

  public Vector getEntries() {
    return entries;
  }

  public CTreeNode getParent() {
    return parent;
  }

  public void setParent(CTreeNode _parent) {
    parent = _parent;
  }

  public void insert(Graph g) {

    if (isLeaf) {
      entries.addElement(g);
      childHists.addElement(ctree.factory.toHist(g));

      if (entries.size() > ctree.M) { //split
        split();
      }
      else { // update closure
        addClosure(g);
      }
    }
    else {
      // find an appropriate child node to insert the graph
      CTreeNode child = probeChild(entries, g);
      child.insert(g);
    }
  }

  protected void addClosure(Graph g) {
    if (closure == null) {
      closure = g; // !!! clone?
    }
    else {
      closure = ctree.closure(g, closure);
    }

    /**@todo feature !!!*/

    if (!isRoot) {
      parent.addClosure(closure);
    }
  }

  public Graph getClosure() {
    return closure;
  }

  protected CTreeNode probeChild(Vector entries, Graph g) {
    // CTreeNode child = randomProbe(entries, g);
    CTreeNode child = minNormProbe(entries, g);
    // CTreeNode child = minVolumeProbe(entries, g);
    //CTreeNode child = minLogVolumeProbe(entries, g);
    // CTreeNode child = maxSimProbe(entries, g);
    
    if(child == null)
    {
    	System.out.println("Child null ");
    	System.exit(0);
    }
    assert (child != null);
    return child;
  }

  /**
   * The norm of a graph. ||G|| = |V|*w_v + |E|*w_e
   * @return int
   */
   private int norm(Graph g) {
     return g.numV() * 10 + g.numE();
   }

  /**
   * The volume of a graph closure in logarithmetic representation.
   *
   * volume(G) =\Pi volume(v) * \Pi volume(e)
   *
   * log_volume(G) = \sum log(volume(v)) + \sum log(volume(e))
   *
   * Aproximation:
   * log_volume(G) = |VC|*w_v + |EC|*w_e
   * where VC and EC are vertex closure and edge closure
   *
   * @return int
   */

  /* private int logVolume(Graph g) {
     Vertex[] vertices = g.V();
     Edge[] edges = g.E();
     int volume = 0;
     for (int i = 0; i < vertices.length; i++) {
       if (vertices[i] instanceof VertexClosure) {
         volume++;
       }
     }
     volume *= 1; // weight of vertices
     for (int i = 0; i < edges.length; i++) {
       if (edges[i].containsNull) {
         volume++;
       }
     }
     return volume;
   }*/

  /**
   * The volume of a graph closure
   * @param g GraphClosure
   * @return double
   */
  /*private double volume(Graph g) {
    AbstractVertex[] vertices = g.getV();
    Edge[] edges = g.getE();
    double volume = 1;
    for (int i = 0; i < vertices.length; i++) {
      if (vertices[i] instanceof VertexClosure) {
        volume *= 2;
      }
    }
    volume *= 1; // weight of vertices
    for (int i = 0; i < edges.length; i++) {
      if (edges[i].containsNull) {
        volume *= 2;
      }
    }
    return volume;
     }*/

  private CTreeNode maxSimProbe(Vector entries, Graph g) {
    CTreeNode best_entry = null;
    double maxSim = 0;
    for (int i = 0; i < entries.size(); i++) {
      CTreeNode entry = (CTreeNode) entries.elementAt(i);
      int[] map = ctree.mapper.map(g, entry.getClosure());
      double sim = ctree.graphSim.sim(g, entry.getClosure(), map);
      //double sim = GraphSim.simUpper(g, entry.getClosure());
      if (sim > maxSim) {
        maxSim = sim;
        best_entry = entry;
      }
    } // end of for

    return best_entry;

  }

  /**
   * Mininum enlargement of absolute volume computed by logarithmetic
   * representation. Assume that if a volume is enlarged, it is enlarged
   * by ratio of at least 2.
   */
  /*private CTreeNode minLogVolumeProbe(Vector entries, Graph g) {
    CTreeNode best_entry = null;
    int min_volume = Integer.MAX_VALUE;
    int min_volume0 = 0;

    for (int i = 0; i < entries.size(); i++) {
      CTreeNode entry = (CTreeNode) entries.elementAt(i);
      GraphClosure gc = ctree.closure(g, entry.getClosure());
      int volume = logVolume(gc);
      int volume0 = logVolume(entry.getClosure());
      boolean flag = false;
      if (volume < min_volume) {
        if (volume0 >= min_volume0 || volume == volume0 ||
            min_volume != min_volume0) {
          flag = true;
        }
      }
      else if (volume == min_volume) {
        if (volume0 > min_volume0) {
          flag = true;
        }
      }
      else {
        if (volume0 > min_volume0 && min_volume != min_volume0 &&
            volume == volume0) {
          flag = true;
        }
      }
      if (flag) {
        min_volume = volume;
        min_volume0 = volume0;
        best_entry = entry;
      }
    }

    return best_entry;
     }*/

  /**
   * Minimum enlargement of absolute volume.
   * Warning: volume could be exceed Double.MAX_VALUE
   * @param entries Vector
   * @param g Graph
   * @return CTreeNode
   */
  /*private CTreeNode minVolumeProbe(Vector entries, Graph g) {
    CTreeNode best_entry = null;
    double min_volume = Double.MAX_VALUE;
    double min_volume0 = 0;
    for (int i = 0; i < entries.size(); i++) {
      CTreeNode entry = (CTreeNode) entries.elementAt(i);
      GraphClosure gc = ctree.closure(g, entry.getClosure());
      double volume1 = volume(gc);
      double volume0 = volume(entry.getClosure());
      double volume = volume1 - volume0;
      if (volume < min_volume) {
        min_volume = volume;
        min_volume0 = volume0;
        best_entry = entry;
      }
      else if (volume == min_volume) {
        if (volume0 < min_volume0) {
          min_volume0 = volume0;
          best_entry = entry;
        }
      }
    } // end of for

    return best_entry;
     }*/

  /**
   * Minimum norm probing
   */
  private CTreeNode minNormProbe(Vector entries, Graph g) {
    CTreeNode best_entry = null;
    int min_norm = Integer.MAX_VALUE;
    for (int i = 0; i < entries.size(); i++) {
      CTreeNode entry = (CTreeNode) entries.elementAt(i);
      Graph gc = ctree.closure(g, entry.getClosure());
      int norm = norm(gc);
      if (norm < min_norm) {
        best_entry = entry;
        min_norm = norm;
      }
    }
    return best_entry;

     }

  /**
   * Random probing
   */
  private CTreeNode randomProbe(Vector entries, Graph g) {
    return (CTreeNode) entries.elementAt(ctree.rand.nextInt(entries.size()));
  }

  private void computeClosure() {
    if (entries == null || entries.size() == 0) {
      closure = null;
      //mask = null;
    }
    else {
      closure = childGraphAt(0);
      assert (closure != null);
      for (int i = 1; i < entries.size(); i++) {
        closure = ctree.closure(childGraphAt(i), closure);
      }
    }
  }

  /**
   * Random partitioning.
   * @param entries Vector
   * @return Vector[]
   */
  protected Vector[] randomPartition(Vector entries) {

    Vector vect1 = new Vector(ctree.M + 1);
    Vector vect2 = new Vector(ctree.M + 1);
    int m = entries.size() / 2;
    for (int i = 0; i < entries.size(); i++) {

      if (vect1.size() >= m) {
        vect2.addElement(entries.elementAt(i));
      }
      else if (vect2.size() >= m) {
        vect1.addElement(entries.elementAt(i));
      }
      else {
        int r = ctree.rand.nextInt(2);
        if (r == 0) {
          vect1.addElement(entries.elementAt(i));
        }
        else {
          vect2.addElement(entries.elementAt(i));
        }
      }
    }

    return new Vector[] {vect1, vect2};
  }

  public Object childAt(int i) {
    return entries.elementAt(i);
  }

  public Graph childGraphAt(int i) {
    if (isLeaf) {
      return (Graph) entries.elementAt(i);
    }
    else {
      return ( (CTreeNode) entries.elementAt(i)).getClosure();
    }
  }

  /**
   * A simple graph distance derived from graph similarity
   * @param g1 Graph
   * @param g2 Graph
   * @return int
   */
  private double dist(Graph g1, Graph g2) {
    int[] map = ctree.mapper.map(g1, g2);
    return 10000 - ctree.graphSim.sim(g1, g2, map);
  }

  /**
   * Linear partitioning.
   * 1. Choose a random g0
   * 2. Choose the farthest g1 from g0
   * 3. Choose the farthest g2 rom g1, let g1-g2 be the pivot
   * 4. For all g, sort d(g,g1)-d(g,g2) in ascending order
   * 5. First m assigned to g1, last m assigned to g2
   *    rest, <0 assigned to g1, >0 assigned to g2, =0 favor partion size
   *
   * @param entries Vector
   * @return Vector[]
   */
  protected Vector[] linearPartition(Vector entries) {
    int n = entries.size();
    assert (n > ctree.M);

    int n0 = ctree.rand.nextInt(n);
    Graph g0 = childGraphAt(n0); // g0

    // choose g1
    double dmax = 0;
    int n1 = -1;
    for (int i = 0; i < n; i++) {
      Graph g = childGraphAt(i);
      if (i == n0) {
        continue;
      }
      double d = dist(g0, g);
      if (d > dmax) {
        dmax = d;
        n1 = i;
      }
    }
    Graph g1 = childGraphAt(n1);

    // choose g2
    dmax = 0;
    int n2 = -1;
    double[] dist = new double[n];
    for (int i = 0; i < n; i++) {
      Graph g = childGraphAt(i);
      if (i == n1) {
        continue;
      }
      double d = dist(g1, g);
      if (d > dmax) {
        dmax = d;
        n2 = i;
      }
      dist[i] = d; // distance to g1
    }
    Graph g2 = childGraphAt(n2);

    // compute d(g,g1)-d(g,g2)
    for (int i = 0; i < n; i++) {
      Graph g = childGraphAt(i);
      dist[i] = dist[i] - dist(g2, g);
    }

    // sort in ascedning order
    int[] order = new int[n];
    for (int i = 0; i < n; i++) {
      order[i] = i;
    }
    for (int i = 0; i < n - 1; i++) {
      for (int j = i + 1; j < n; j++) {
        if (dist[i] > dist[j]) {
          double temp = dist[i]; // swap i,j
          dist[i] = dist[j];
          dist[j] = temp;
          int temp2 = order[i];
          order[i] = order[j];
          order[j] = temp2;
        }
      }
    }

    // find a middle point to partition
    int mid;
    for (mid = ctree.m; mid < n - ctree.m; mid++) {
      if (dist[mid] > 0 || (dist[mid] == 0 && mid > n / 2)) {
        break;
      }
    }

    Vector vect1 = new Vector(ctree.M + 1);
    Vector vect2 = new Vector(ctree.M + 1);
    for (int i = 0; i < mid; i++) {
      vect1.addElement(entries.elementAt(order[i]));
    }
    for (int i = mid; i < n; i++) {
      vect2.addElement(entries.elementAt(order[i]));
    }
    return new Vector[] {vect1, vect2};
  }

  protected void split() {

    // Partition entries into two groups
    //Vector[] partions = randomPartition(entries);
    Vector[] partions = linearPartition(entries);

    // create two nodes
    CTreeNode node1 = new CTreeNode(ctree, parent, partions[0], isLeaf, false);
    CTreeNode node2 = new CTreeNode(ctree, parent, partions[1], isLeaf, false);

    // If it is not root then insert into the parent, split the parent if applicable
    if (!isRoot) {
      int index = parent.entries.indexOf(this);
      parent.entries.removeElementAt(index);
      parent.childHists.removeElementAt(index);
      parent.entries.addElement(node1);
      parent.childHists.addElement(ctree.factory.toHist(node1.getClosure()));
      parent.entries.addElement(node2);
      parent.childHists.addElement(ctree.factory.toHist(node2.getClosure()));
      if (parent.entries.size() > ctree.M) {
        parent.split();
      }
      else {
        Graph gc = ctree.closure(node1.getClosure(), node2.getClosure());
        parent.addClosure(gc);
      }
    }
    else { // otherwise create a new root
      Vector vect = new Vector();
      vect.addElement(node1);
      vect.addElement(node2);
      CTreeNode root = new CTreeNode(ctree, null, vect, false, true);
      ctree.root = root;
    }

  }

  public String toString() {
    String s = "Closure: \n" + closure.toString() + "entries: " + entries.size() +
        "\n";
    for (int i = 0; i < entries.size(); i++) {
      s += entries.elementAt(i).toString();
    }
    return s;
  }

  /**
   * Check integrity
   * @return boolean
   */
  public boolean check() {
    for (int i = 0; i < entries.size(); i++) {
      Graph g;
      if (isLeaf) {
        g = (Graph) entries.elementAt(i);
      }
      else {
        g = ( (CTreeNode) entries.elementAt(i)).getClosure();
      }
      int[] map = ctree.mapper.map(g, closure);
      for (int k = 0; k < map.length; k++) {
        if (map[k] == -1) {
          return false;
        }
      }
    }
    if (!isLeaf) {
      for (int i = 0; i < entries.size(); i++) {
        if (! ( (CTreeNode) entries.elementAt(i)).check()) {
          return false;
        }
      }
    }
    return true;
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.writeBoolean(isLeaf);
    out.writeBoolean(isRoot);
    out.writeObject(closure);
    //out.writeObject(mask);
    if (entries == null) {
      out.writeInt(0);
    }
    else {
      out.writeInt(entries.size());
    }
    for (int i = 0; i < entries.size(); i++) {
      out.writeObject(entries.elementAt(i));
    }
    out.writeObject(childHists);
  }

  private void readObject(ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    isLeaf = in.readBoolean();
    isRoot = in.readBoolean();
    closure = (Graph) in.readObject();
    //mask = (GraphMask)in.readObject();

    ctree = CTree.ctree_stub;

    int size = in.readInt();
    entries = new Vector(size);
    for (int i = 0; i < size; i++) {
      Object child = in.readObject();
      if (isLeaf) {
        assert (child instanceof Graph);
      }
      else {
        ( (CTreeNode) child).parent = this;
      }
      entries.addElement(child);
    }

    childHists = (Vector) in.readObject();
  }

  public int maxDepth() {
    if (isLeaf) {
      return 1;
    }
    else {
      int max = 0;
      for (CTreeNode node : (Vector<CTreeNode>) entries) {
        int d = node.maxDepth();
        if (d > max) {
          max = d;
        }
      }
      return max + 1;
    }
  }

  public Hist histAt(int i) {
    return (Hist) childHists.elementAt(i);
  }

  public int size() {
    if (isLeaf) {
      return entries.size();
    }
    else {
      int size = 0;
      for (CTreeNode node : (Vector<CTreeNode>) entries) {
        size += node.size();
      }
      return size;
    }
  }

  public int minDepth() {
    if (isLeaf) {
      return 1;
    }
    else {
      int min = Integer.MAX_VALUE;
      for (CTreeNode node : (Vector<CTreeNode>) entries) {
        int d = node.minDepth();
        if (d < min) {
          min = d;
        }
      }
      return min + 1;
    }
  }

}
