package ctree.lgraph;

import java.io.IOException;
import java.util.*;

import ctree.graph.*;
import ctree.index.*;

import ctree.tool.SubGraphIsoMapping;

/**
 * <p>
 * Closure-tree
 * </p>
 * 
 * @author Huahai He
 * @version 1.0
 */
public class LGraph implements Graph {
	protected final static boolean isDirected = false;
	protected LVertex[] V;
	protected UnlabeledEdge[] E;
	protected String id;

	public LGraph() {
	}

	public int hashCode()
	{
		return 1;
	    //return id.hashCode();
	}
	
	public boolean equals(Object o) {
		
		// If the object is compared with itself then return true
		if (o == this) {
			return true;
		}

		LGraph c = (LGraph) o;

		if (this.numE() != c.numE() || this.numV() != c.numV())
			return false;
		SubGraphIsoMapping giso;
		giso = new SubGraphIsoMapping();
		ArrayList<Integer> x  = giso.getMapping(c,this);
		if (x == null)
			return false;
		else 
			return true;

	}

	public LGraph(LVertex[] _V, UnlabeledEdge[] _E, String _id) {
		V = _V;
		E = _E;
		id = _id;
	}

	/**
	 * @return Vertex[]
	 */
	public Vertex[] V() {
		return V;
	}

	/**
	 * @return Edge[]
	 */
	public Edge[] E() {
		return E;
	}

	/**
	 * adjMatrix
	 * 
	 * @return int[][]
	 */
	public int[][] adjMatrix() {
		int[][] adj = new int[V.length][V.length];
		for (int i = 0; i < V.length; i++) {
			Arrays.fill(adj[i], 0);
		}
		for (int i = 0; i < E.length; i++) {
			adj[E[i].v1()][E[i].v2()] = 1;
		}
		if (!isDirected) {
			for (int i = 0; i < E.length; i++) {
				adj[E[i].v2()][E[i].v1()] = 1;
			}
		}
		return adj;
	}

	/**
	 * adjList
	 * 
	 * @return int[][]
	 */
	public int[][] adjList() {
		int n = V.length;
		LinkedList<Integer>[] llist = new LinkedList[n];
		for (int i = 0; i < n; i++) {
			llist[i] = new LinkedList<Integer>();
		}
		for (UnlabeledEdge e : E) {
			llist[e.v1].add(e.v2);
			if (!isDirected) {
				llist[e.v2].add(e.v1);
			}
		}
		int[][] adjlist = new int[n][];
		for (int i = 0; i < n; i++) {
			adjlist[i] = new int[llist[i].size()];
			Iterator<Integer> it = llist[i].listIterator();
			int cnt = 0;
			while (it.hasNext()) {
				adjlist[i][cnt++] = it.next();
			}
		}
		return adjlist;

	}

	public Edge[][] adjEdges() {
		int n = V.length;
		LinkedList<Edge>[] llist = new LinkedList[n];
		for (int i = 0; i < n; i++) {
			llist[i] = new LinkedList<Edge>();
		}
		for (UnlabeledEdge e : E) {
			llist[e.v1].add(e);
			if (!isDirected) {
				llist[e.v2].add(e);
			}
		}
		Edge[][] adjEdges = new Edge[n][];
		for (int i = 0; i < n; i++) {
			adjEdges[i] = new UnlabeledEdge[llist[i].size()];
			Iterator<Edge> it = llist[i].listIterator();
			int cnt = 0;
			while (it.hasNext()) {
				adjEdges[i][cnt++] = it.next();
			}
		}
		return adjEdges;

	}

	/**
	 * @return int
	 */
	public int numE() {
		return E.length;
	}

	/**
	 * @return int
	 */
	public int numV() {
		return V.length;
	}

	public String getId() {
		return id;
	}

	public void setId(String _id) {
		id = _id;
	}

	public String toString() {
		String s = "#" + id + "\n";
		s += V.length + "\n";
		for (int i = 0; i < V.length; i++) {
			s += V[i] + "\n";
		}
		s += E.length + "\n";
		for (int i = 0; i < E.length; i++) {
			s += E[i] + "\n";
		}
		return s;

	}

}
