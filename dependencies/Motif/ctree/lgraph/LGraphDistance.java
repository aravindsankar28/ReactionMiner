package ctree.lgraph;

import java.util.ArrayList;

import ctree.graph.*;
import ctree.index.*;

/**
 * Graph metric of edit distance (L1).
 * 
 * @author Huahai He
 * @version 1.0
 */

public class LGraphDistance implements GraphDistance {
	public static int VERTEX_DISTANCE = 1;
	public static int VERTEX_NORM = 1;
	public static int EDGE_NORM = 1;

	public static double d(Vertex v1, Vertex v2) {
		if (v1.mappable(v2)) {
			return 0;
		} else {
			return 1;
		}
	}

	public static double norm(Vertex v) {
		return 1;
	}

	public static double norm(Edge e) {
		return 1;
	}

	public static ArrayList<String> edgeChanges(Graph g1, Graph g2, int[] map) {
		
		ArrayList<String> edges  = new ArrayList<String>();
		
		Vertex[] V1 = g1.V();
		Vertex[] V2 = g2.V();
		int m = V1.length;
		int n = V2.length;
		// assert(m == map.length);
		// map[i] gives mapped vertex in g2
		double d_vertex = 0;
		int[] rmap = Util.get_rmap(map, n); // reverse map

		double d_edge = 0;
		int[][] adj2 = g2.adjMatrix();
		Edge[] E1 = g1.E();
		for (Edge e : E1) {
			if (map[e.v1()] == -1 || map[e.v2()] == -1
					|| adj2[map[e.v1()]][map[e.v2()]] == 0) {
				d_edge += norm(e);
				edges.add(V1[e.v1()].toString()+" "+V1[e.v2()].toString()+" 1");
			} else {

			}
		}

		int[][] adj1 = g1.adjMatrix();
		Edge[] E2 = g2.E();
		for (Edge e : E2) {
			if (rmap[e.v1()] == -1 || rmap[e.v2()] == -1
					|| adj1[rmap[e.v1()]][rmap[e.v2()]] == 0) {
				d_edge += EDGE_NORM;				
				edges.add(V2[e.v1()].toString()+" "+V2[e.v2()].toString()+" 1");				
			}
		}
		return edges;
	}

	public double d(Graph g1, Graph g2, int[] map, boolean sub) {
		Vertex[] V1 = g1.V();
		Vertex[] V2 = g2.V();
		int m = V1.length;
		int n = V2.length;
		// assert(m == map.length);
		// map[i] gives mapped vertex in g2
		double d_vertex = 0;
		int[] rmap = Util.get_rmap(map, n); // reverse map

		// vertices
		for (int i = 0; i < m; i++) {
			if (map[i] == -1) {
				d_vertex += norm(V1[i]);
				;

			} else {
				d_vertex += d(V1[i], V2[map[i]]);
			}
		}

		if (sub == false) {
			for (int i = 0; i < n; i++) {

				if (rmap[i] == -1) {
					// d_vertex += norm(V1[i]);
					d_vertex += VERTEX_NORM;
				}
			}
		}

		// edges
		double d_edge = 0;
		int[][] adj2 = g2.adjMatrix();
		Edge[] E1 = g1.E();
		for (Edge e : E1) {
			if (map[e.v1()] == -1 || map[e.v2()] == -1
					|| adj2[map[e.v1()]][map[e.v2()]] == 0) {
				d_edge += norm(e);
			} else {

			}
		}

		if (!sub) {
			int[][] adj1 = g1.adjMatrix();
			Edge[] E2 = g2.E();
			for (Edge e : E2) {
				if (rmap[e.v1()] == -1 || rmap[e.v2()] == -1
						|| adj1[rmap[e.v1()]][rmap[e.v2()]] == 0) {
					d_edge += EDGE_NORM;
				}
			}
		}

		return d_vertex + d_edge;
	}

}
