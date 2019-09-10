package grank.old;

import grank.graph.*;
import grank.mine.*;

/**
 * Rooted ordered tree.
 * @author Huahai He
 * @version 1.0
 */
public class Tree {
  public TreeNode root;
  static final int MAX_V=1000;
  static final int MAX_E = 1000;
  int[] V=new int[MAX_V];
  int vcnt=0;
  LEdge[] E = new LEdge[MAX_E];
  int ecnt=0;
  /**
   * root node label
   * @param rootLab int
   */
  public Tree(int rootLab) {
    V[vcnt++]=rootLab;
    root=new TreeNode(0,this);
  }
}
