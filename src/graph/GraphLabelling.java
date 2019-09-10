package graph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import globals.Globals;

import ctree.graph.Edge;
import ctree.graph.Vertex;
import ctree.lgraph.LGraph;
import ctree.lgraph.LGraphFile;
import ctree.lgraph.LVertex;
import ctree.lgraph.UnlabeledEdge;
import fi.tkk.ics.jbliss.Graph;
import fi.tkk.ics.jbliss.Utils;
/***
 * 
 * @author aravind
 * This function is a helper class to check if 2 graphs are isomorphic. This has the jbliss library as 
 * a dependency. Requires the library to be included. The isomorphism check is achieved by computing the 
 * canonical label.
 * The main interface functions are getCanonicalLabel and getGraph.
 */
public class GraphLabelling {

	// This sorts edges in increasing order. This assumes that the graph has the
	// nodes number
	// correctly according to the canonical label ordering
	HashMap<String, Integer> atomIntegerMap;
	HashMap<String, ArrayList<Integer>> knownMoleculesLabels;

	void preComputeLabels(HashMap<String, LGraph> knownMolecules)
			throws IOException {
		for (String mol : knownMolecules.keySet()) {
			LGraph g = knownMolecules.get(mol);
			Graph<Integer> g_iso = convertToJblissGraph(g);
			Map<Integer, Integer> canlab = g_iso.canonical_labeling();
			ArrayList<Integer> label = new ArrayList<Integer>(canlab.values());
			getKnownMoleculesLabels().put(mol, label);
		}
	}

	public GraphLabelling() throws IOException {
		atomIntegerMap = Globals.loadAtomIntMap();
		knownMoleculesLabels = new HashMap<String, ArrayList<Integer>>();
	}

	// Retrieve the graph given the canonical label.
	public LGraph getGraph(String canonicalLabel) {
		
		String[] chars = canonicalLabel.split("\\W+");		
		ArrayList<LVertex> verticesList = new ArrayList<LVertex>();
		
		int idx = 0;
		for (int i = 0; i < chars.length; i++) {
			String str = chars[i];
			char c = str.charAt(0);
			idx = i;
			if (Character.isDigit(c) && c != '2' && c != '3')
				break;
			
			verticesList.add(new LVertex(str, false));
		}
		ArrayList<UnlabeledEdge> edgesList = new ArrayList<UnlabeledEdge>();
		
		for (int i = idx; i < chars.length; i += 2) {			
			edgesList.add(new UnlabeledEdge(Integer.parseInt(chars[i]), Integer.parseInt(chars[i+1]), 1, "0", false));
		}
		
		UnlabeledEdge[] edges = edgesList.toArray(new UnlabeledEdge[edgesList
		                                            				.size()]);
		LVertex[] vertices = verticesList.toArray(new LVertex[verticesList
		                                      				.size()]);		
		LGraph g = new LGraph(vertices, edges, "");		
		return g;
	}

	// Returns a string concatenated version of the canonical graph.
	public String getCanonicalLabel(LGraph g) throws IOException {
		Graph<Integer> g_iso = convertToJblissGraph(g);
		ArrayList<Integer> label = new ArrayList<Integer>(g_iso
				.canonical_labeling().values());
		LGraph canonicalGraph = createCanonicalGraph(g, label);
		String canonicalLabel = "";
		for (Vertex v : canonicalGraph.V()) {
			canonicalLabel += v.toString() + " ";
		}
		for (Edge e : canonicalGraph.E()) {
			canonicalLabel += e.v1() + " " + e.v2() + " ";
		}
		return canonicalLabel;
	}

	
	private LGraph canonicalizeEdges(LGraph g) {

		ArrayList<UnlabeledEdge> edgesList = new ArrayList<UnlabeledEdge>(
				Arrays.asList((UnlabeledEdge[]) g.E()));
		// Re order each edge to have the first node smaller than the second.

		ArrayList<UnlabeledEdge> edgesListNew = new ArrayList<UnlabeledEdge>();
		for (UnlabeledEdge e : edgesList) {
			if (e.v1() > e.v2())
				edgesListNew.add(new UnlabeledEdge(e.v2(), e.v1(), e.w(), e
						.stereo(), false));
			else
				edgesListNew.add(e);

		}

		Comparator<UnlabeledEdge> comparator = new Comparator<UnlabeledEdge>() {

			public int compare(UnlabeledEdge o1, UnlabeledEdge o2) {
				if (o1.v1() < o2.v1())
					return -1;
				else if (o1.v1() > o2.v1())
					return 1;
				else {
					if (o1.v2() < o2.v2())
						return -1;
					else
						return 1;
				}
			}

		};

		// sort edges
		Collections.sort(edgesListNew, comparator);

		LGraph canonicalGraph = new LGraph((LVertex[]) g.V(),
				edgesListNew.toArray(new UnlabeledEdge[edgesListNew.size()]),
				"");

		return canonicalGraph;
	}

	// vertex label set equality for 2 graphs - pre ci
	boolean vertexLabelInvariant(LGraph g1, LGraph g2) {
		HashMap<String, Integer> map1 = new HashMap<String, Integer>();
		HashMap<String, Integer> map2 = new HashMap<String, Integer>();

		for (Vertex v : g1.V()) {
			if (map1.containsKey(v.toString()))
				map1.put(v.toString(), map1.get(v.toString()) + 1);
			else
				map1.put(v.toString(), 1);
		}

		for (Vertex v : g2.V()) {
			if (map2.containsKey(v.toString()))
				map2.put(v.toString(), map2.get(v.toString()) + 1);
			else
				map2.put(v.toString(), 1);
		}

		if (map1.size() != map2.size())
			return false;

		for (String v : map1.keySet()) {
			if (!map2.containsKey(v))
				return false;
			if (map1.get(v) != map2.get(v))
				return false;
		}

		return true;
	}

	// Isomorphism between a known molecule and a new one g.
	public boolean isIsomorphic(LGraph g, LGraph knownMol,
			ArrayList<Integer> knownLabel) throws IOException {

		if (g.numE() != knownMol.numE() || g.numV() != knownMol.numV())
			return false;

		if (!vertexLabelInvariant(g, knownMol))
			return false;

		Graph<Integer> g_iso = convertToJblissGraph(g);
		Map<Integer, Integer> canlab = g_iso.canonical_labeling();
		if (Globals.DEBUG) {
			System.out.print("A canonical labeling for the graph g is: ");
			Utils.print_labeling(System.out, canlab);
			System.out.println("");
		}

		LGraph o1 = createCanonicalGraph(g,
				new ArrayList<Integer>(canlab.values()));

		LGraph o2 = createCanonicalGraph(knownMol, knownLabel);
		boolean flag = isGraphsEqual(o1, o2);

		/*
		 * if (flag) System.out.println("Isomorphic"); else
		 * System.out.println("Not Isomorphic");
		 */

		return flag;
	}

	Graph<Integer> convertToJblissGraph(LGraph g) throws IOException {

		Graph<Integer> g_iso = new Graph<Integer>();
		for (int i = 0; i < g.numV(); i++) {
			g_iso.add_vertex(i, atomIntegerMap.get(g.V()[i].toString()));
		}

		for (Edge e : g.E()) {
			g_iso.add_edge(e.v1(), e.v2());
		}
		return g_iso;

	}

	// Generic isomorphism between 2 graphs
	public boolean isIsomorphic(LGraph g1, LGraph g2) throws IOException {

		if (g1.numE() != g2.numE() || g1.numV() != g2.numV())
			return false;

		if (!vertexLabelInvariant(g1, g2))
			return false;

		Graph<Integer> g1_iso = convertToJblissGraph(g1);
		Graph<Integer> g2_iso = convertToJblissGraph(g2);

		Map<Integer, Integer> canlab1 = g1_iso.canonical_labeling();
		if (Globals.DEBUG) {
			System.out.print("A canonical labeling for the graph g1 is: ");
			Utils.print_labeling(System.out, canlab1);
			System.out.println("");
		}

		Map<Integer, Integer> canlab2 = g2_iso.canonical_labeling();
		if (Globals.DEBUG) {
			System.out.print("A canonical labeling for the graph g2 is: ");
			Utils.print_labeling(System.out, canlab2);
			System.out.println("");
		}
		LGraph o1 = createCanonicalGraph(g1,
				new ArrayList<Integer>(canlab1.values()));

		LGraph o2 = createCanonicalGraph(g2,
				new ArrayList<Integer>(canlab2.values()));
		boolean flag = isGraphsEqual(o1, o2);
		if (Globals.DEBUG) {
			if (flag)
				System.out.println("Isomorphic");
			else
				System.out.println("Not Isomorphic");
		}
		return flag;

	}

	// Given a label re-ordering and a graph, re-map the nodes and return a
	// graph - called the canonical graph
	// This label is a direct mapping.

	LGraph createCanonicalGraph(LGraph g, ArrayList<Integer> label) {
		ArrayList<LVertex> verticesList = new ArrayList<LVertex>();
		ArrayList<UnlabeledEdge> edgesList = new ArrayList<UnlabeledEdge>();

		for (UnlabeledEdge edge : (UnlabeledEdge[]) g.E()) {
			int v1 = edge.v1();
			int v2 = edge.v2();
			int w = edge.w();
			String st = edge.stereo();
			v1 = label.get(v1);
			v2 = label.get(v2);
			edgesList.add(new UnlabeledEdge(v1, v2, w, st, false));

		}

		for (int i = 0; i < g.numV(); i++) {
			// verticesList.add((LVertex) g.V()[label.get(i)]);
			verticesList.add((LVertex) g.V()[i]);
		}

		for (int i = 0; i < g.numV(); i++) {
			verticesList.set(label.get(i), (LVertex) g.V()[i]);
		}

		LVertex[] verticesArr = verticesList.toArray(new LVertex[verticesList
				.size()]);
		UnlabeledEdge[] edgeArr = edgesList.toArray(new UnlabeledEdge[edgesList
				.size()]);

		LGraph canonicalGraph = new LGraph(verticesArr, edgeArr, "");
		// System.out.println(canonicalGraph);

		return canonicalizeEdges(canonicalGraph);
	}

	boolean isGraphsEqual(LGraph g1, LGraph g2) {
		// This assumes that both g1 and g2 have been renumbered according to
		// their label orderings. This
		// only does a final node label and edge matching in order.
		// Note that edge labels are not considered as we assume they'll be set
		// to 1 always.
		if (g1.numV() != g2.numV() || g1.numE() != g2.numE())
			return false;

		// Matching node labels
		for (int i = 0; i < g1.numV(); i++) {
			String l1 = g1.V()[i].toString();
			String l2 = g2.V()[i].toString();
			if (!l1.contentEquals(l2))
				return false;
		}

		// Matching edges
		for (int i = 0; i < g1.numE(); i++) {
			UnlabeledEdge e1 = (UnlabeledEdge) g1.E()[i];
			UnlabeledEdge e2 = (UnlabeledEdge) g2.E()[i];
			// || e1.w() != e2.w()
			if (e1.v1() != e2.v1() || e1.v2() != e2.v2())
				return false;
		}

		return true;
	}

	public static void main(String[] args) throws IOException {
		System.out.println(System.getProperty("java.library.path"));
		GraphLabelling giso = new GraphLabelling();

		System.out.println(giso.isIsomorphic(
				LGraphFile.loadLGraphs(Globals.molDirectory + "/"
						+ "C00267.mol")[0],
				LGraphFile.loadLGraphs(Globals.molDirectory + "/"
						+ "C00031.mol")[0]));
		System.out
				.println(giso
						.getGraph("2 2 2 2 C C CH CH CH CH CH2 CH2 CH3 CH3 CH3 O O O O P S 0 8 0 9 1 4 1 6 2 5 2 7 3 19 3 20 4 8 4 15 5 9 5 18 6 7 10 13 10 16 11 14 11 17 12 15 16 19 17 19 18 19"));
		System.exit(0);

	}

	HashMap<String, ArrayList<Integer>> getKnownMoleculesLabels() {
		return knownMoleculesLabels;
	}

}
