package graph;

import java.util.ArrayList;

import globals.Globals;

import ctree.graph.Graph;
import ctree.lgraph.LGraph;
import ctree.lgraph.LVertex;
import ctree.lgraph.UnlabeledEdge;

/**
 * @author aravind This is used to create a subgraph of an input graph taking as
 *         input the set of valid vertices and edges.
 */
public class CreateValidSubGraph {
	static boolean DEBUG = Globals.DEBUG;
	
	/*
	 * Given a graph and a list of valid vertices and edges in the form of
	 * boolean arrays, we create a new graph that includes only valid vertices
	 * and edges.
	 */
	public static Graph createValidSubgraph(Graph g, boolean[] isValidVertices,
			boolean[] isValidEdges) {

		ArrayList<LVertex> verticesList = new ArrayList<LVertex>();

		for (int i = 0; i < g.numV(); i++) {
			if (isValidVertices[i])
				verticesList.add((LVertex) g.V()[i]);
		}

		if (DEBUG)
			System.out.println("Vertices " + verticesList);

		LVertex[] vertices = verticesList.toArray(new LVertex[verticesList
				.size()]);

		ArrayList<UnlabeledEdge> edgesList = new ArrayList<UnlabeledEdge>();
		int nodeRemoveCount[] = new int[isValidVertices.length];

		// TODO : Check this with an example.
		for (int i = 0; i < g.numV(); i++) {
			if (!isValidVertices[i] && i != 0)
				nodeRemoveCount[i] = nodeRemoveCount[i - 1] + 1;
			else if (!isValidVertices[i] && i == 0)
				nodeRemoveCount[i] = 1;
			else if (i != 0)
				nodeRemoveCount[i] = nodeRemoveCount[i - 1];
			else
				nodeRemoveCount[i] = 0;
			// System.out.print(nodeRemoveCount[i] + " ");
		}

		for (int i = 0; i < g.numE(); i++) {
			if (isValidEdges[i]) {
				int v1 = g.E()[i].v1();
				int v2 = g.E()[i].v2();
				UnlabeledEdge edge = new UnlabeledEdge(
						v1 - nodeRemoveCount[v1], v2 - nodeRemoveCount[v2],
						g.E()[i].w(), g.E()[i].stereo(), false);
				edgesList.add(edge);
			}
		}
		
		if (DEBUG)
			System.out.println("Edges " + edgesList);

		UnlabeledEdge[] edges = edgesList.toArray(new UnlabeledEdge[edgesList
				.size()]);
		LGraph new_g = new LGraph(vertices, edges, "");
		return new_g;
	}
}
