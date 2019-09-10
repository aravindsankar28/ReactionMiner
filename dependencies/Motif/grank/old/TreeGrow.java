package grank.old;

import java.util.*;

import org.apache.commons.math.*;
import grank.graph.*;
import grank.pvalue.*;

import grank.mine.*;

/**
 * Enumerate trees and compute their histograms on the fly.
 * Trees are enumerated using right-most extension.
 *
 * Note:
 *   1. Feature set should be complete in order to detect invalid tree
 *      extensions, which result in non-growing histograms.
 *
 * @author Huahai He
 * @version 1.0
 */
public class TreeGrow {
  static Link[][] links;

  // right-most child node
  private static TreeNode rightMost(TreeNode node) {
    int cnum = node.cnodes.size();
    if (cnum > 0) {
      return node.cnodes.get(cnum - 1);
    }
    else {
      return null;
    }
  }

  // check if a histogram is a sub-histogram of another
  public static boolean subHist(int[] h1, int[] h2, int m) {
    for (int i = 0; i < m; i++) {
      if (h1[i] > h2[i]) {
        return false;
      }
    }
    return true;
  }

  public static void grow(Tree tree, int[] hist, Vector<Integer> S,
      Environment5 env) throws MathException {
    TreeNode rnode = tree.root;
    Vector<Integer> S1 = new Vector<Integer> ();
    int[] hist1 = new int[env.m];
    while (true) {
      int vlab = rnode.tree.V[rnode.vid];
      int order0 = rnode.rmorder;
      for (int order = order0; order < links[vlab].length; order++) {
        // Add a right-most node
        Link newlink = links[vlab][order];
        int vid2 = tree.vcnt++;
        tree.V[vid2] = newlink.end;
        int eid2 = tree.ecnt++;
        LEdge newedge = new LEdge(rnode.vid, vid2, newlink.edge);
        tree.E[eid2] = newedge;

        rnode.cedges.add(eid2);
        TreeNode newnode = new TreeNode(vid2, tree);
        rnode.cnodes.add(newnode);
        rnode.rmorder = order;

        // Extend histograms
        System.arraycopy(hist, 0, hist1, 0, env.m);
        boolean valid = extendHist(tree, eid2, hist1, env);
        if (valid) {
          // Support
          S1.clear();
          for (Integer idx : S) {
            if (subHist(hist1, env.HD[idx], env.m)) {
              S1.add(idx);
            }
          }
          if (S1.size() >= env.minSup) {
            HistMine5.eval(hist1, PValue.sum(hist1), S1.size(), env);
            // Recurse
            grow(tree, hist1, S1, env);
          }
        }
        // restore rnode
        tree.vcnt--;
        tree.ecnt--;
        int cnum = rnode.cnodes.size();
        rnode.cedges.remove(cnum - 1);
        rnode.cnodes.remove(cnum - 1);
        rnode.rmorder = order0;
      }
      rnode = rightMost(rnode);
      if (rnode == null) {
        break; // reached the right-most node
      }
    }
  }

  static boolean extendHist(Tree tree, int eid2, int[] hist, Environment5 env) {
    Vector<LGraph> results = genFeature(tree, eid2, env.zB);
    for (LGraph g : results) {
      Integer idx = env.pcMap.get(g);
      if (idx != null) {
        if (!env.fbin[idx]) {
          return false;
        }
        hist[idx]++;
      }
      else {
        return false;
      }
    }
    return true;
  }

  // Copy from EnumFeatures
  static Vector<LGraph> genFeature(Tree tree, int eid2, int zB) {
    int n = tree.vcnt;
    int m = tree.ecnt;
    boolean[] visited = new boolean[m];
    Arrays.fill(visited, false);

    int[] vmap = new int[n]; // vmap[i]: the new index of vertex i
    Arrays.fill(vmap, -1);

    int[] eset = new int[zB]; // eset[i]: index of a selected edge
    Arrays.fill(eset, -1);
    Vector<LGraph> results = new Vector<LGraph> ();
    HashSet<String> dup = new HashSet<String> (); // check for duplicate permutation of edges

    LEdge e = tree.E[eid2];
    vmap[e.v1] = 0;
    vmap[e.v2] = 1;
    eset[0] = eid2;
    visited[eid2] = true;
    visit(tree.V, n, tree.E, m, visited, vmap, 2, eset, 1, zB, dup,
          results);
    return results;
  }

// Generate a subgraph from a permutation
  private static LGraph genSub(int[] V, int n, LEdge[] E, int m, int[] vmap,
                               int vcnt, int[] eset, int size, String id) {
    int[] V1 = new int[vcnt];

    for (int k = 0; k < n; k++) { // Copy the subset of V
      if (vmap[k] >= 0) {
        V1[vmap[k]] = V[k];
      }
    }

    LEdge[] E1 = new LEdge[size];
    for (int k = 0; k < size; k++) { // Copy the subset of E
      LEdge e1 = E[eset[k]];
      E1[k] = new LEdge(vmap[e1.v1], vmap[e1.v2], e1.label);
    }
    LGraph sub = new LGraph(id, V1, E1);
    return sub;
  }

  private static void visit(int[] V, int n, LEdge[] E, int m, boolean[] visited,
                            int[] vmap, int vcnt, int[] eset, int depth,
                            int zB, HashSet<String> dup,
      Vector<LGraph> results) {
    if (depth >= zB) { // End of a permutation
      // generate a subgraph
      LGraph sub = genSub(V, n, E, m, vmap, vcnt, eset, zB, null);
      results.add(sub);
    }
    else { // Add another edge
      for (int i = 0; i < m; i++) {
        if (!visited[i]) { // E[i] is available
          LEdge e = E[i];
          if (vmap[e.v1] >= 0 || vmap[e.v2] >= 0) { // E[i] is adjacent to the current subgraph
            visited[i] = true;
            eset[depth] = i;
            int newv = -1;
            if (vmap[e.v1] < 0) {
              vmap[e.v1] = vcnt++;
              newv = e.v1;
            }
            else if (vmap[e.v2] < 0) {
              vmap[e.v2] = vcnt++;
              newv = e.v2;
            }
            if (!checkDup(eset, depth + 1, dup)) {
              visit(V, n, E, m, visited, vmap, vcnt, eset, depth + 1, zB,
                    dup, results);
            }

            // Restore
            visited[i] = false;
            if (newv >= 0) {
              vmap[newv] = -1;
              vcnt--;
            }
          }
        }

      }
    }
  }

  private static int[] dupbuf = new int[100]; // buffer for edge indices
// Check if a partial eset is duplicate with previous permutations
  private static boolean checkDup(int[] eset, int depth, HashSet<String> dup) {
    System.arraycopy(eset, 0, dupbuf, 0, depth);
    Arrays.sort(dupbuf, 0, depth);
    String str = "" + dupbuf[0];
    for (int z = 1; z < depth; z++) {
      str += "," + dupbuf[z];
    }
    if (dup.contains(str)) {
      return true;
    }
    else {
      dup.add(str);
      return false;
    }

  }

  // Edge tuple
  static class EdgeTuple {
    int v1; // vertex label
    int v2; // vertex label
    int e; // edge label
    private int hash = -1;
    public EdgeTuple(int _v1, int _v2, int _e) {
      if (_v1 <= _v2) {
        v1 = _v1;
        v2 = _v2;
      }
      else {
        v1 = _v2;
        v2 = _v1;
      }
      e = _e;
    }

    public int hashCode() {
      if (hash == -1) {
        hash = (v1 << 20) | (v2 << 10) | e;
      }
      return hash;
    }

    public boolean equals(Object o) {
      return hashCode() == o.hashCode();
    }
  }

  /**
   * Generate frequent links.
   * @param graphs LGraph[]
   * @param VL number of vertex labels
   * @param minSup Minimum support
   */
  public static void initLinks(LGraph[] graphs, int VL, int EL, int minSup) {
    // generate edge tuples
    int m = VL * (VL + 1) / 2 * EL; // number of tuples
    EdgeTuple[] tuples = new EdgeTuple[m];
    int cnt = 0;
    HashMap<EdgeTuple, Integer> tmap = new HashMap<EdgeTuple, Integer> (m);
    for (int v1 = 0; v1 < VL; v1++) { // Edge tuples are in lexicographical order
      for (int e = 0; e < EL; e++) {
        for (int v2 = v1; v2 < VL; v2++) {
          tuples[cnt] = new EdgeTuple(v1, v2, e);
          tmap.put(tuples[cnt], cnt);
          cnt++;
        }
      }
    }
    assert (cnt == m);

    // Count frequency of edge tuples
    boolean[] bins = new boolean[m];
    int[] hist = new int[m];
    Arrays.fill(hist, 0);
    for (LGraph g : graphs) {
      Arrays.fill(bins, false);
      for (LEdge e : g.E) {
        EdgeTuple t = new EdgeTuple(g.V[e.v1], g.V[e.v2], e.label);
        int idx = tmap.get(t);
        bins[idx] = true;
      }
      for (int i = 0; i < m; i++) {
        if (bins[i]) {
          hist[i]++;
        }
      }
    }

    // Generate frequent links
    Vector<Link> [] array = new Vector[VL];
    for (int i = 0; i < VL; i++) {
      array[i] = new Vector<Link> ();
    }
    for (int i = 0; i < m; i++) {
      if (hist[i] >= minSup) {
        EdgeTuple t = tuples[i];
        array[t.v1].add(new Link(t.e, t.v2));
        array[t.v2].add(new Link(t.e, t.v1));
      }
    }
    links = new Link[VL][];
    for (int i = 0; i < VL; i++) {
      links[i] = new Link[array[i].size()];
      array[i].toArray(links[i]);
    }
  }
}
