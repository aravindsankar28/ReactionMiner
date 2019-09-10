package ctree.alg;

/**
 * Maximum cardinality matching for bipartite graphs. This algorithm is
 * aimed for fast processing of small bipartite graphs.
 *
 * <pre>
 * Algorithm description:
 * Bipartite graph: (X,Y)
 * for each f in X, f is free {
 *   start:
 *   S = {f}
 *   T = {}
 *   for s in S {
 *     for t is neighbor of s, t not in T {
 *       if t is free {
 *         augment path
 *         goto start
 *       } else {
 *         T <- t
 *         S <- mate(t)
 *       }
 *     }
 *   }
 * }
 *
 * </pre>
 *
 * @author Huahai He
 * @version 1.0
 */
public class MaximumBipartiteMatching {
    private static int[] S = new int[10000]; // left side of augmenting path
    private static int[] T = new int[10000]; // right side of augmenting path

    /**
     *
     * @param adj int[][] adjacency matrix
     * @param n1 number of vertices in graph 1
     * @param n2 number of vertices in graph 2
     * @param map Initial matching for U. If (u,v) is matched, then map[u]=v,
     * otherwise map[u] = -1
     * @param rmap reverse map of map
     * @param perfect if true, then early stop if there is no semi-perfect matching
     * @return the number of matchings. The matchings are stored in map
     */
    public static int maximumMatching(int[][] adj, int n1, int n2, int[] map,
                                      int[] rmap, boolean perfect) {
        int matches = 0;

        START:
                for (int f = 0; f < n1; f++) {
            if (map[f] >= 0) {
                matches++;
            } else { // f is free
                S[0] = f;
                int nS = 1;
                for (int i = 0; i < n2; i++) {
                    T[i] = -1;

                } while (nS > 0) {
                    int s = S[--nS];
                    for (int t = 0; t < n2; t++) {
                        if (adj[s][t] > 0 && T[t] < 0) {
                            if (rmap[t] < 0) { // t is free
                                // augment path
                                rmap[t] = s;
                                while (map[s] >= 0) {
                                    int t2 = map[s];
                                    map[s] = t;
                                    t = t2;
                                    s = rmap[t2] = T[t2];
                                }
                                map[s] = t;
                                matches++;
                                continue START;
                            } else {
                                T[t] = s;
                                S[nS++] = rmap[t];
                            }
                        }
                    }
                } // end of while
            } // end of else

            if (perfect) {
                return matches;
            }
        } // end of for f
        return matches;
    }
}
