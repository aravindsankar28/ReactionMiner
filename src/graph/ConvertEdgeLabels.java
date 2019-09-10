package graph;

import java.io.IOException;
import java.util.ArrayList;
import globals.Index;
import globals.Globals;
import ctree.graph.Edge;
import ctree.graph.Vertex;
import ctree.lgraph.LGraph;
import ctree.lgraph.LVertex;
import ctree.lgraph.UnlabeledEdge;

/**
 * ConvertEdgeLabels is a very important class that includes edge labels in the
 * graph. It adds new nodes for edge that is not a single bond. Both bond orders
 * and stereo chemical information are being stored here.
 * 
 * It has methods to :
 * 
 * Add edge label nodes based on 1) bond orders and 2) stereo chemistry. 2 flags
 * to set if used : INCLUDE_EDGE_LABELS (for bond orders) and INCLUDE_STEREO
 * (for stereo labels)
 **/

public class ConvertEdgeLabels {

	/*
	 * This function removes the edge label node and create edge labels on the
	 * edges instead. This is the reverse of what we generally do. This is
	 * currently done for bond orders (of 2 and 3 only) and stereo nodes.
	 */
	public LGraph removeEdgeLabelNodes(LGraph g) {
		ArrayList<UnlabeledEdge> edgeList = new ArrayList<UnlabeledEdge>();
		ArrayList<LVertex> verticesList = new ArrayList<LVertex>();
		int[] nodeCounter = new int[g.numV()];
		int i = 0;
		for (LVertex vertex : (LVertex[]) g.V()) {
			// For now check only 2 and 3. Later can be generalized to a number.
			if (vertex.toString().contentEquals("2")
					|| vertex.toString().contentEquals("3")) {
				if (i >= 1)
					nodeCounter[i] = nodeCounter[i - 1] + 1;
				else
					nodeCounter[i] = 1;
			} else {
				verticesList.add(vertex);
				if (i >= 1)
					nodeCounter[i] = nodeCounter[i - 1];
				else
					nodeCounter[i] = 0;
			}
			i++;
		}

		boolean[] processedEdges = new boolean[g.numE()];
		for (i = 0; i < g.numE(); i++) {
			if (processedEdges[i])
				continue;
			UnlabeledEdge e_i = (UnlabeledEdge) g.E()[i];

			if (g.V()[e_i.v2()].toString().contentEquals("2")
					|| g.V()[e_i.v2()].toString().contentEquals("3")
					|| g.V()[e_i.v1()].toString().contentEquals("2")
					|| g.V()[e_i.v1()].toString().contentEquals("3")) {

				int dummy;
				int v1;
				if (g.V()[e_i.v2()].toString().contentEquals("2")
						|| g.V()[e_i.v2()].toString().contentEquals("3")) {
					dummy = e_i.v2();
					v1 = e_i.v1();
				} else {
					dummy = e_i.v1();
					v1 = e_i.v2();
				}

				int v2 = -1;
				for (int j = 0; j < g.numE(); j++) {
					UnlabeledEdge e_j = (UnlabeledEdge) g.E()[j];
					if (e_j.v1() == dummy && j != i) {
						v2 = e_j.v2();
						processedEdges[j] = true;
						break;
					}
					if (e_j.v2() == dummy && j != i) {
						v2 = e_j.v1();
						processedEdges[j] = true;
						break;
					}
				}
				int w = Integer.parseInt(g.V()[dummy].toString());
				edgeList.add(new UnlabeledEdge(v1 - nodeCounter[v1], v2
						- nodeCounter[v2], w, "0", false));
				processedEdges[i] = true;
				continue;
			}
			edgeList.add(new UnlabeledEdge(e_i.v1() - nodeCounter[e_i.v1()],
					e_i.v2() - nodeCounter[e_i.v2()], e_i.w(), "0", false));
		}

		LVertex[] vertices_arr = verticesList.toArray(new LVertex[verticesList
				.size()]);

		UnlabeledEdge[] edges_arr = edgeList.toArray(new UnlabeledEdge[edgeList
				.size()]);

		LGraph new_g = new LGraph(vertices_arr, edges_arr, "");
		if (Globals.INCLUDE_STEREO) {
			new_g = removeStereoNodes(new_g);
		}
		return new_g;
	}

	// Removes stereo edge label nodes. Reverse of what we usually do.
	public LGraph removeStereoNodes(LGraph g) {
		ArrayList<UnlabeledEdge> edgeList = new ArrayList<UnlabeledEdge>();
		ArrayList<LVertex> verticesList = new ArrayList<LVertex>();
		int[] nodeCounter = new int[g.numV()];
		int i = 0;
		for (LVertex vertex : (LVertex[]) g.V()) {
			// For now check only 2 and 3. Later can be generalized to a number.
			if (vertex.toString().contentEquals("WD")
					|| vertex.toString().contentEquals("HS")) {
				if (i >= 1)
					nodeCounter[i] = nodeCounter[i - 1] + 1;
				else
					nodeCounter[i] = 1;
			} else {
				verticesList.add(vertex);
				if (i >= 1)
					nodeCounter[i] = nodeCounter[i - 1];
				else
					nodeCounter[i] = 0;
			}
			i++;
		}

		boolean[] processedEdges = new boolean[g.numE()];
		for (i = 0; i < g.numE(); i++) {

			if (processedEdges[i])
				continue;

			UnlabeledEdge e_i = (UnlabeledEdge) g.E()[i];

			if (g.V()[e_i.v2()].toString().contentEquals("WD")
					|| g.V()[e_i.v2()].toString().contentEquals("HS")
					|| g.V()[e_i.v1()].toString().contentEquals("WD")
					|| g.V()[e_i.v1()].toString().contentEquals("HS")) {

				int dummy;
				int v1;
				if (g.V()[e_i.v2()].toString().contentEquals("WD")
						|| g.V()[e_i.v2()].toString().contentEquals("HS")) {
					dummy = e_i.v2();
					v1 = e_i.v1();
				} else {
					dummy = e_i.v1();
					v1 = e_i.v2();
				}

				int v2 = -1;
				for (int j = 0; j < g.numE(); j++) {
					UnlabeledEdge e_j = (UnlabeledEdge) g.E()[j];
					if (e_j.v1() == dummy && j != i) {
						v2 = e_j.v2();
						processedEdges[j] = true;
						break;
					}
					if (e_j.v2() == dummy && j != i) {
						v2 = e_j.v1();
						processedEdges[j] = true;
						break;
					}
				}
				String stereo = g.V()[dummy].toString();
				edgeList.add(new UnlabeledEdge(v1 - nodeCounter[v1], v2
						- nodeCounter[v2], 1, stereo, false));
				processedEdges[i] = true;

				continue;
			}
			edgeList.add(new UnlabeledEdge(e_i.v1() - nodeCounter[e_i.v1()],
					e_i.v2() - nodeCounter[e_i.v2()], e_i.w(), "0", false));
		}

		LVertex[] vertices_arr = verticesList.toArray(new LVertex[verticesList
				.size()]);

		UnlabeledEdge[] edges_arr = edgeList.toArray(new UnlabeledEdge[edgeList
				.size()]);
		LGraph new_g = new LGraph(vertices_arr, edges_arr, "");
		return new_g;
	}

	// Adding nodes for stereo chemistry information.
	public LGraph addStereoNodes(LGraph g) {
		ArrayList<UnlabeledEdge> edgeList = new ArrayList<UnlabeledEdge>();
		ArrayList<LVertex> verticesList = new ArrayList<LVertex>();

		for (Vertex vertex : g.V()) {
			verticesList.add((LVertex) vertex);
		}

		int nodeCounter = g.numV();
		for (Edge e : g.E()) {
			if (!e.stereo().contentEquals("0")) {
				verticesList.add(new LVertex(e.stereo().toString(), false));
				edgeList.add(new UnlabeledEdge(e.v1(), nodeCounter, e.w(), "0",
						false));
				edgeList.add(new UnlabeledEdge(nodeCounter, e.v2(), e.w(), "0",
						false));
				nodeCounter++;
			} else {
				edgeList.add((UnlabeledEdge) e);
			}
		}
		LVertex[] vertices_arr = verticesList.toArray(new LVertex[verticesList
				.size()]);

		UnlabeledEdge[] edges_arr = edgeList.toArray(new UnlabeledEdge[edgeList
				.size()]);
		LGraph new_g = new LGraph(vertices_arr, edges_arr, "");
		return new_g;
	}

	/*
	 * Works for adding edge label nodes for any generic label 'w' based on bond
	 * order.
	 */
	public LGraph addEdgeLabelNodes(LGraph g) {
		ArrayList<UnlabeledEdge> edgeList = new ArrayList<UnlabeledEdge>();
		ArrayList<LVertex> verticesList = new ArrayList<LVertex>();

		for (Vertex vertex : g.V()) {
			verticesList.add((LVertex) vertex);
		}
		// For every edge that has label != 1, add a new node and two edges.
		int nodeCounter = g.numV();
		for (Edge e : g.E()) {
			if (e.w() != 1) {
				verticesList.add(new LVertex(String.valueOf(e.w()), false));
				edgeList.add(new UnlabeledEdge(e.v1(), nodeCounter, 1, e
						.stereo(), false));
				edgeList.add(new UnlabeledEdge(nodeCounter, e.v2(), 1, e
						.stereo(), false));
				nodeCounter++;
			} else {
				edgeList.add((UnlabeledEdge) e);
			}
		}

		LVertex[] vertices_arr = verticesList.toArray(new LVertex[verticesList
				.size()]);
		UnlabeledEdge[] edges_arr = edgeList.toArray(new UnlabeledEdge[edgeList
				.size()]);
		LGraph new_g = new LGraph(vertices_arr, edges_arr, "");

		if (Globals.INCLUDE_STEREO) {
			new_g = addStereoNodes(new_g);
		}
		return new_g;
	}

	public static void main(String[] args) throws IOException {
		ConvertEdgeLabels cel = new ConvertEdgeLabels();
		LGraph[] g = new LGraph[1];
		Index.loadMolecules();
		g[0] = Index.knownMolecules.get("C00049");
		System.out
				.println(cel.removeStereoNodes(cel.removeEdgeLabelNodes(g[0])));
	}

}
