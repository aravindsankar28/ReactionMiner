package grank.old;

import java.util.*;

import grank.mine.*;

public class TreeNode {
  int rmorder; // link order of right-most child, 0 if there is no child
  Vector<Integer> cedges = new Vector<Integer> (); // children edge id
  Vector<TreeNode> cnodes = new Vector<TreeNode> ();
  int vid; // vertex index of this node
  Tree tree;
  public TreeNode(int _vid, Tree _tree) {
    vid = _vid;
    tree = _tree;
    rmorder=0;
  }
}
