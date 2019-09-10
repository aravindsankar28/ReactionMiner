package ruleMining;

import java.io.*;
import java.util.*;
import org.apache.commons.lang3.ArrayUtils;

import globals.*;
import graph.*;
import ruleMining.ReactionRule.*;
import ctree.graph.Edge;
import ctree.index.Util;
import ctree.lgraph.*;

/**
 * 
 * @author aravind Class for just applying the reaction rule and checking if the
 *         reactant obtained is valid.
 */
public class RuleApply {

	/**
	 * @param args
	 */
	static boolean DEBUG;
	static boolean DISPLAY = false;

	public RuleApply() {
		DEBUG = Globals.DEBUG;
	}

	// Based on hashing canonical labels.
	public String verifyValidity3(LGraph query) throws IOException, ClassNotFoundException {
		if (!Index.isMolsLoaded) {
			Index.loadMolecules();
			Index.loadKnownMolLabels();
			Index.isMolsLoaded = true;
		}
		GraphLabelling giso = new GraphLabelling();
		String canonicalLabel = giso.getCanonicalLabel(query);
		return Index.knownMolLabels.get(canonicalLabel);
	}

	String verifyValidity(LGraph query) throws FileNotFoundException, ClassNotFoundException, IOException {
		if (!Index.isMolsLoaded) {
			Index.loadMolecules();
			Index.loadKnownMolLabels();
			Index.isMolsLoaded = true;
		}

		SubgraphMapping sgim = new SubgraphMapping();
		for (String key : Index.knownMolecules.keySet()) {
			LGraph g = Index.knownMolecules.get(key);
			if (g.numE() != query.numE() || g.numV() != query.numV())
				continue;
			ArrayList<Integer> mapping = sgim.getMapping(query, g);
			if (mapping != null)
				return key;
		}
		return "null";
	}

	public LGraph applyReactionRule(LGraph query, ReactionRule rule) {
		// This function applies rule on the query molecule, to give the
		// corresponding reactant. (along with it's other helper molecules too).

		// First step, get the subgraph iso mapping, i.e reaction sign subset of
		// query.
		SubgraphMapping sgim = new SubgraphMapping();
		ArrayList<Integer> mapping = sgim.getMapping(rule.getReactionSignatureAdded(), query);
		return applyReactionRule(query, rule, mapping);
	}

	public boolean isRuleApplicable(LGraph query, ReactionRule rule) {
		
		if (!Util.pseudoSubIsomorphic(rule.getReactionSignatureAdded(), query, 1))
			return false;
		if (!Util.pseudoSubIsomorphic(rule.getReactionSignatureAdded(), query, 2))
			return false;
		if (!Util.pseudoSubIsomorphic(rule.getReactionSignatureAdded(), query, 3))
			return false;
		if (!Util.pseudoSubIsomorphic(rule.getReactionSignatureAdded(), query, 4))
			return false;
		
		SubgraphMapping sgm = new SubgraphMapping();
		if (sgm.getMapping(rule.getReactionSignatureAdded(), query) == null)
			return false;
		
		return true;
	}

	public LGraph applyReactionRule(LGraph query, ReactionRule rule, ArrayList<Integer> mapping) {
		if (DEBUG)
			System.out.println("Applying rule " + rule.getId());

		if (DEBUG) {
			System.out.println("Reaction signature :");
			System.out.println(rule.getReactionSignatureAdded());
		}
		if (DEBUG)
			System.out.println("Sign. product mapping" + mapping);
		// This mapping contains the corresponding query node for each node in
		// the reaction signature.

		ArrayList<Integer> nodesToRemoveInQuery = new ArrayList<Integer>();

		for (Integer node : rule.getSubgraphAdded())
			nodesToRemoveInQuery.add(mapping.get(node));

		// Second step - Invalidation of nodes and corresponding edges
		ArrayList<Boolean> isValidVertices = new ArrayList<Boolean>();
		ArrayList<Boolean> isValidEdges = new ArrayList<Boolean>();

		for (int v = 0; v < query.numV(); v++) {
			if (nodesToRemoveInQuery.contains(v))
				isValidVertices.add(false);
			else
				isValidVertices.add(true);
		}

		Edge[] queryEdges = query.E();
		for (int i = 0; i < queryEdges.length; i++) {
			Edge e = queryEdges[i];
			if (nodesToRemoveInQuery.contains(e.v1()) || nodesToRemoveInQuery.contains(e.v2()))
				isValidEdges.add(false);
			else
				isValidEdges.add(true);
		}

		ArrayList<Edge> edgeList = new ArrayList<Edge>(Arrays.asList(queryEdges));

		// Step 3 - incorporate bond changes. - join and remove.

		for (InterRCEdge edge : rule.getMetaData().getInterRCEdges()) {
			if (edge.getType() == 'R') {
				for (int i = 0; i < edgeList.size(); i++) {
					if (isValidEdges.get(i)) {
						Edge e = edgeList.get(i);
						if ((e.v1() == mapping.get(edge.getV1()) && e.v2() == mapping.get(edge.getV2()))
								|| (e.v1() == mapping.get(edge.getV2()) && e.v2() == mapping.get(edge.getV1()))) {
							isValidEdges.set(i, false);
							break;
						}
					}
				}
			}

			if (edge.getType() == 'J') {
				edgeList.add(new UnlabeledEdge(mapping.get(edge.getV1()), mapping.get(edge.getV2()), edge.getW(),
						edge.getStereo(), false));
				isValidEdges.add(true);
			}
		}

		int n = query.numV();

		// Addition of nodes from subgraph removed
		LVertex[] vertexLabels = (LVertex[]) query.V();
		ArrayList<LVertex> vertexLabelList = new ArrayList<LVertex>(Arrays.asList(vertexLabels));

		for (LVertex vertex : (LVertex[]) rule.getSubgraphRemoved().V()) {
			vertexLabelList.add(vertex);
			isValidVertices.add(true);
		}

		for (Edge e : rule.getSubgraphRemoved().E()) {
			edgeList.add(new UnlabeledEdge(e.v1() + n, e.v2() + n, e.w(), e.stereo(), false));
			isValidEdges.add(true);
		}

		for (Edge e : rule.getMetaData().getConnectingEdges()) {
			edgeList.add(new UnlabeledEdge(e.v1() + n, mapping.get(e.v2()), e.w(), e.stereo(), false));
			isValidEdges.add(true);
		}

		LGraph g = new LGraph((LVertex[]) vertexLabelList.toArray(new LVertex[vertexLabelList.size()]),
				(UnlabeledEdge[]) edgeList.toArray(new UnlabeledEdge[edgeList.size()]), "#");

		boolean[] arr_vertices = ArrayUtils
				.toPrimitive((Boolean[]) isValidVertices.toArray(new Boolean[isValidVertices.size()]));

		boolean[] arr_edges = ArrayUtils
				.toPrimitive((Boolean[]) isValidEdges.toArray(new Boolean[isValidEdges.size()]));

		LGraph reconstructedReactant = (LGraph) CreateValidSubGraph.createValidSubgraph(g, arr_vertices, arr_edges);

		if (DEBUG) {
			System.out.println("Reconstructed reactant");
			System.out.println("Vertices " + Arrays.asList(reconstructedReactant.V()));
			System.out.println("Edges " + Arrays.asList(reconstructedReactant.E()));
			System.out.println(reconstructedReactant.numE());
		}
		return reconstructedReactant;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}

}
