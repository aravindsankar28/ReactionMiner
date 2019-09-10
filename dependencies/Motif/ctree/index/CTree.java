package ctree.index;

import java.io.*;
import java.util.*;
import ctree.graph.*;
import ctree.mapper.*;


/**
 * Closure-Tree, the index structure for graphs.
 *
 * @author Huahai He
 * @version 1.0
 */

public class CTree implements Serializable {
  protected int m;
  protected int M;
  protected CTreeNode root;
  protected GraphMapper mapper; // for closure
  protected GraphSim graphSim;  // for insertion/splitting criteria
  public GraphFactory factory;    // implementation support

  protected Random rand = new Random(1); // for random splitting

  static CTree ctree_stub; // for deserialization reference

  int size;  // number of database graphs

  /**
   * The no-arg constructor is used only for serialization
   */
  protected CTree() {
  }

  /**
   * Create an empty Closure-tree.
   * @param _m minimum number of fan-out
   * @param _M maximum number of fan-out
   * @param _mapper Graph mapper for construction of graph closures
   * @param _graphSim GraphSim
   * @param _factory Graph factory
   */
  public CTree(int _m, int _M, GraphMapper _mapper, GraphSim _graphSim, GraphFactory _factory) {
    m = _m;
    M = _M;

    mapper = _mapper;
    graphSim = _graphSim;
    factory = _factory;
    root = new CTreeNode(this, null, new Vector(M + 1), true, true);

    size = 0;

  }

  /**
   * Insert a graph
   * @param g Graph
   */
  public void insert(Graph g) {
    root.insert(g);
    size++;
  }

  /**
   * The number of graphs in the ctree.
   * @return int
   */
  public int size() {
    return size;
  }

  public void setSize(int _size) {
    size = _size;
  }

  public int maxDepth() {
    return root.maxDepth();
  }

  public int minDepth() {
    return root.minDepth();
  }

  public String toString() {
    return root.toString();
  }

  public Graph closure(Graph g1, Graph g2) {
    int[] map = mapper.map(g1, g2);
    return factory.graphClosure(g1, g2, map);
  }

  public boolean check() {
    return root.check();
  }

  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    out.writeInt(m);
    out.writeInt(M);
    out.writeInt(size);
    out.writeObject(rand);
    out.writeObject(mapper);
    out.writeObject(graphSim);
    out.writeObject(factory);

    out.writeObject(root);

  }

  private void readObject(ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    m = in.readInt();
    M = in.readInt();
    size = in.readInt();
    rand = (Random) in.readObject();

    mapper = (GraphMapper) in.readObject();
    graphSim = (GraphSim) in.readObject();
    factory = (GraphFactory) in.readObject();

    ctree_stub = this;
    root = (CTreeNode) in.readObject();

  }

  /**
   * Load a closure tree from file
   * @param filename String
   */
  public static CTree load(String filename) throws IOException,
      ClassNotFoundException {
    ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new
        FileInputStream(filename)));
    CTree ctree = (CTree) in.readObject();
    in.close();
    return ctree;
  }

  public void saveTo(String filename) throws IOException {
    ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new
        FileOutputStream(filename)));
    out.writeObject(this);
    out.close();
  }

  public CTreeNode getRoot() {
    return root;
  }

  public void setRoot(CTreeNode _root) {
    root = _root;
  }

  public int get_m() {
    return m;
  }

  public int get_M() {
    return M;
  }
}
