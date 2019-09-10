package ctree.alg;

import java.util.*;

/**
 * This class implements Hopcroft-Karp algorithm for bipartite graph maximum
 * cardinality matching. The time complexity is O(sqrt(n)*m) where n is the
 * number of vertices and m is the number of edges.
 *
 * <p>
 * Reference:
 *   J. Hopcroft and R. Karp. An n^5/2 algorithm for maximum matchings
 *   in bipartite graphs. SIAM J. Computing, 1973.
 *</p>
 *
 * <pre>
 *
 * Algorithm description:
 * Input:
 *   U,V: The two sides of the bipartite graph
 *   matching: An initial matching (optional)
 * Output:
 *   matching: The maximum cardinality matching
 *
 * WHILE TRUE {
 *   # BFS search for augment paths in one phase
 *   # layer: A queue for the even level BFS search
 *   # level[]: The level of each vertex in the BFS search
 *   # found: The set of free vertex in V at the final level
 *   reset level[] to -1
 *   FOR all free u in U
 *     layer.add(u)
 *     level[u] = 0
 *   found = {}
 *   cur_level = 0
 *   WHILE layer is not empty  {
 *     FOR all u in layer
 *       FOR all (u,v) in E AND level[v] < 0     # (u,v) must be unmatched
 *         level[v] = cur_level+1
 *         IF free[v] THEN found.add(v)
 *         ELSE
 *           u' = matching[v]  # (v,u') must be matched
 *           IF level[u'] < 0 THEN level[u'] = cur_level+2; next_layer.add(u')
 *     IF found is not empty THEN break
 *     layer = next_layer
 *     next_layer = {}
 *     cur_level += 2
 *   }
 *   IF found is empty THEN return matching
 *
 *   # DFS backtrack to update augmented matchings
 *   FOR all v in found
 *     IF DFS(v, cur_level+1, augpath) THEN
 *       FOR every pair (u',v') in augpath
 *          matching[u'] = v'; matching[v'] = u'
 *
 * END WHILE
 *
 * DFS(v, cur_level, augpath)
 *   remove v
 *   FOR (u,v) in E
 *     IF level[u]==cur_level-1  # (u,v) must be unmatched
 *       add (u,v) to augpath
 *       remove u
 *       IF cur_level==1 THEN return true
 *       ELSE
 *         v' = matching[u]            # (u,v') must be matched
 *         IF DFS(v', cur_level-2, augpath) THEN return true
 *   END FOR
 *   return false
 *
 * </pre>
 *
 * @author Huahai He
 * @version 1.0
 */

public class HopcroftKarp {

  public static int maximumMatching(int[][] adj, int[] map) {
    return maximumMatching(adj, adj.length, adj[0].length, map);
  }

  /**
   *
   * @param adj int[][] adjacency matrix
   * @param n1 number of vertices in graph 1
   * @param n2 number of vertices in graph 2
   * @param map Initial matching for U. If (u,v) is matched, then map[u]=v,
   * otherwise map[u] = -1
   * @return the number of matchings. The matchings are stored in map
   */
  public static int maximumMatching(int[][] adj, int n1, int n2, int[] map) {
    int n = n1 + n2;

    int matches = 0; // number of matchings
    // reverse map
    int[] rmap = new int[n2];
    Arrays.fill(rmap, -1);
    for (int u = 0; u < n1; u++) {
      if (map[u] >= 0) {
        rmap[map[u]] = u;
        matches++;
      }
    }

    int[] level = new int[n];
    int[] layer = new int[n1];
    int[] next_layer = new int[n1];
    int[] found = new int[n2];
    int layer_cnt, next_layer_cnt;
    int found_cnt;
    int[] augpath = new int[n]; // augment path for DFS

    int cur_level;

    while (true) { // each iteration is a phase
      // reset level[] to -1
      Arrays.fill(level, -1);
      // layer 0
      layer_cnt = 0;
      for (int u = 0; u < n1; u++) {
        if (map[u] == -1) {
          layer[layer_cnt++] = u;
          level[u] = 0;
        }
      }

      // BFS search for augment paths in one phase
      found_cnt = 0;
      next_layer_cnt = 0;
      cur_level = 0;
      while (layer_cnt > 0) { // while layer is not empty
        for (int k = 0; k < layer_cnt; k++) {
          int u = layer[k];
          for (int v = 0; v < n2; v++) {
            if (adj[u][v] > 0 && level[n1 + v] < 0) { // (u,v) in E, v was not visited
              assert (map[u] != v);
              level[n1 + v] = cur_level + 1;
              if (rmap[v] == -1) { // found a free v in V
                found[found_cnt++] = v;
              }
              else { // add to the next layer
                int u1 = rmap[v];
                if (level[u1] < 0) {
                  level[u1] = cur_level + 2;
                  next_layer[next_layer_cnt++] = u1;
                }
              }
            } // end of (u,v)

          } // end of for v
        } // end of for u
        if (found_cnt > 0) {
          break; // reached the final level
        }

        // search the next layer
        System.arraycopy(next_layer, 0, layer, 0, next_layer_cnt);
        layer_cnt = next_layer_cnt;
        next_layer_cnt = 0;
        cur_level += 2;
      } // end of while

      if (found_cnt == 0) { // found is empty, terminate
        //assert(check(map, rmap, adj));
        return matches;
      }

      // DFS backtrack to update augmented matchings
      for (int k = 0; k < found_cnt; k++) {
        int v = found[k];

        // DFS backtrack to find a disjoint augment path
        if (dfs(v, adj, n1, level, cur_level + 1, map, augpath)) {
          // update matchings along this augment path
          for (int i = 0; i < cur_level + 1; i += 2) {
            int u1 = augpath[i];
            int v1 = augpath[i + 1];
            map[u1] = v1;
            rmap[v1] = u1;
            //level[u1] = level[n1 + v1] = -1; // invalidate u1 and v1
          }
          matches++;
        }
      } // end of for

    } // end of top while
  }

  private static boolean dfs(int v, int[][] adj, int n1, int[] level,
                             int cur_level, int[] matching, int[] augpath) {
    level[n1 + v] = -1;
    for (int u = 0; u < n1; u++) {
      if (adj[u][v] > 0 && level[u] == cur_level - 1) { // u precedes v and disjoint
        augpath[cur_level - 1] = u;
        augpath[cur_level] = v;
        level[u] = -1;
        if (cur_level == 1) {
          return true;
        }
        else {
          int v1 = matching[u];
          if (dfs(v1, adj, n1, level, cur_level - 2, matching, augpath)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Check the integrity of mapping
   * @param map int[]
   * @param rmap int[]
   * @param adj int[][]
   * @return boolean
   */
  private static boolean check(int[] map, int[] rmap, int[][] adj) {
    int n1 = adj.length;
    for (int u = 0; u < n1; u++) {
      if (map[u] >= 0) {
        int v = map[u];
        if (rmap[v] != u || adj[u][v] == 0) {
          return false;
        }
      }
    }
    for (int v = 0; v < rmap.length - n1; v++) {
      if (rmap[v] >= 0) {
        int u = rmap[v];
        if (map[u] != v || adj[u][v] == 0) {
          return false;
        }
      }
    }
    return true;
  }

  public static void main(String[] args) {
    int[] map = { -1, -1, -1}; //{0, 1, -1};
    int[][] adj = { {1, 0, 1}, {1, 1, 0}, {0, 1, 0}
    };

    maximumMatching(adj, map);
    for (int i = 0; i < map.length; i++) {
      System.out.print(map[i] + " ");
    }
  }

}
