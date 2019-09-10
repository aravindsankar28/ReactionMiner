package ruleMining;

import graph.*;
import java.io.*;
import java.util.*;
import org.apache.commons.lang3.ArrayUtils;
import globals.Index;
import globals.Globals;
import ruleMining.RPM.*;
import ruleMining.ReactionRule.*;
import ctree.graph.Edge;
import ctree.graph.Graph;
import ctree.lgraph.*;

/**
 * @author aravind Main code for rule mining. Call driver() to perform rule
 *         mining on default reaction database. Modify driver as needed.
 */
/**
 * @author aravind Definitions: Reaction Center: Nodes present in both product
 *         and reactant that have undergone some change - 1-hop neighborhood
 *         should be different.
 */
public class RuleMining {

	static boolean DEBUG;
	// Parameters for rule mining.
	static int hops = 1;

	// Inputs read as input
	ArrayList<String> blacklistedPairs;
	ArrayList<String> blacklistedCompounds;

	// Current reaction rule that is created.
	public ReactionRule rule;

	// Rules and final pairs with mapping information -- that will be stored. 
	public ArrayList<ReactionRule> rules;
	public HashMap<Integer, RPM> finalPairs;
	RuleApply ruleApply;
	GraphLabelling graphLabeller;
	
	int skipCount = 0;
	int skipFullMatch = 0;
	int skipNoMactch = 0;
	int skipBlacklist = 0;

	public RuleMining() throws ClassNotFoundException, IOException {
		DEBUG = Globals.DEBUG;
		rules = new ArrayList<ReactionRule>();
		blacklistedPairs = new ArrayList<String>();
		blacklistedCompounds = new ArrayList<>();
		finalPairs = new HashMap<Integer, RPM>();
		ruleApply = new RuleApply();
		graphLabeller = new GraphLabelling();
		Index.loadMolecules();
		Index.saveknownMolLabels();
		Index.loadKnownMolLabels();
	}

	/*
	 * Read blacklisted compounds and pairs - high degree pairs.
	 */
	public void readBlackListedPairs() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(Globals.blackListPairsFile));
		String line = "";
		while ((line = br.readLine()) != null)
			blacklistedPairs.add(line);
		br.close();
	}

	/*
	 * Read blacklisted compounds. TODO: check if needed.
	 */
	public void readBlackListedCompounds() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(Globals.blackListCompsFile));
		String line = "";
		while ((line = br.readLine()) != null)
			blacklistedCompounds.add(line);
		br.close();
	}

	/*
	 * To compute subgraph removed from reactant. This contains the subgraph in
	 * the reactant that got removed as part of the reaction.
	 */
	boolean getSubgraphRemoved(Graph reactantMol, Graph productMol, ArrayList<Integer> mapping,
			ArrayList<Integer> signatureProductMapping) {
		boolean[] isValidVertices = new boolean[reactantMol.numV()];
		boolean[] isValidEdges = new boolean[reactantMol.numE()];
		rule.getMetaData().setConnectingEdges(new ArrayList<Edge>());
		// The subgraphRemoved contains all nodes in reactant that are unmapped.
		for (int v = 0; v < reactantMol.numV(); v++) {
			if (mapping.contains(v))
				isValidVertices[v] = false;
			else
				isValidVertices[v] = true;
		}

		int nodeRemoveCount[] = new int[isValidVertices.length];
		for (int i = 0; i < reactantMol.numV(); i++) {
			if (!isValidVertices[i] && i != 0)
				nodeRemoveCount[i] = nodeRemoveCount[i - 1] + 1;
			else if (!isValidVertices[i] && i == 0)
				nodeRemoveCount[i] = 1;
			else if (i != 0)
				nodeRemoveCount[i] = nodeRemoveCount[i - 1];
			else
				nodeRemoveCount[i] = 0;
		}
		int i = 0;
		for (Edge e : reactantMol.E()) {
			int v1 = e.v1();
			int v2 = e.v2();
			int w = e.w();
			String st = e.stereo();
			// The following 2 conditions are to find the connecting edges - the
			// edge should involve a reaction center.
			if (!mapping.contains(v1) && rule.getReactionCentersReactant().contains(v2)) {
				int productVertex = mapping.indexOf(v2);
				if (signatureProductMapping.contains(productVertex)) {
					int reactionCenterVertex = signatureProductMapping.indexOf(productVertex);
					rule.getMetaData().getConnectingEdges()
							.add(new UnlabeledEdge(v1 - nodeRemoveCount[v1], reactionCenterVertex, w, st, false));
				} else
					return false;
			}
			if (!mapping.contains(v2) && rule.getReactionCentersReactant().contains(v1)) {
				int productVertex = mapping.indexOf(v1);
				if (signatureProductMapping.contains(productVertex)) {
					int reactionCenterVertex = signatureProductMapping.indexOf(productVertex);
					rule.getMetaData().getConnectingEdges().add(new UnlabeledEdge(v2 - nodeRemoveCount[v2],
							reactionCenterVertex, e.w(), e.stereo(), false));
				} else
					return false;
			}
			// Now, actually compute the diff graph.
			if (mapping.contains(v1) || mapping.contains(v2))
				isValidEdges[i] = false;
			else
				isValidEdges[i] = true;
			i++;
		}
		// Now create the diff graph based on the valid nodes and edges.
		rule.setSubgraphRemoved(CreateValidSubGraph.createValidSubgraph(reactantMol, isValidVertices, isValidEdges));
		return true;
	}

	/*
	 * List of k-hop neighbors given the set of reaction centers in the product
	 * molecule.
	 */
	ArrayList<Integer> kHopNeighbours(Set<Integer> reactionCenters, Graph g, int hops) {
		if (hops == 0)
			return new ArrayList<Integer>(reactionCenters);
		else {
			Set<Integer> neighbors = (Set<Integer>) new HashSet<Integer>();
			for (Integer r : reactionCenters) {
				int[] x = g.adjList()[r];
				neighbors.add(r);
				neighbors.addAll(new HashSet<Integer>(Arrays.asList(ArrayUtils.toObject(x))));
			}
			return new ArrayList<Integer>(kHopNeighbours(neighbors, g, hops - 1));
		}
	}

	/*
	 * Get inter reaction center bond changes b/w reactant and product
	 */
	boolean getInterRCEdges(Graph reactantMol, Graph productMol, ArrayList<Integer> mapping,
			ArrayList<Integer> signatureProductMapping) {
		for (Integer i : rule.getReactionCentersReactant()) {
			for (Integer j : rule.getReactionCentersReactant()) {
				if (rule.getReactionCentersReactant().indexOf(j) < rule.getReactionCentersReactant().indexOf(i))
					continue;
				boolean inReactant = false;
				boolean inProduct = false;
				int w = -1;
				String st = "";
				for (Edge e : reactantMol.E()) {
					if ((e.v1() == i && e.v2() == j) || (e.v2() == i && e.v1() == j)) {
						inReactant = true;
						w = e.w();
						st = e.stereo();
						break;
					}
				}
				for (Edge e : productMol.E()) {
					if ((e.v1() == mapping.indexOf(i) && e.v2() == mapping.indexOf(j))
							|| (e.v2() == mapping.indexOf(i) && e.v1() == mapping.indexOf(j))) {
						inProduct = true;
						break;
					}
				}
				/*
				 * Now we have checked if an edge exists between i and j in the
				 * reactant and product.
				 */
				if (inReactant == inProduct)
					continue;
				int productVertex1 = mapping.indexOf(i);
				int productVertex2 = mapping.indexOf(j);
				int rcVertex1 = signatureProductMapping.indexOf(productVertex1);
				int rcVertex2 = signatureProductMapping.indexOf(productVertex2);
				if (productVertex1 == -1 || productVertex2 == -1 || rcVertex1 == -1 || rcVertex2 == -1)
					return false;
				if (inReactant && !inProduct)
					rule.getMetaData().getInterRCEdges().add(new InterRCEdge(rcVertex1, rcVertex2, w, st, 'J'));
				else if (!inReactant && inProduct)
					rule.getMetaData().getInterRCEdges().add(new InterRCEdge(rcVertex1, rcVertex2, w, st, 'R'));
			}
		}
		return true;
	}

	/*
	 * Using the mapping as input, compute reaction centers
	 */
	ArrayList<Integer> getReactionCentersReactant(Graph reactantMol, Graph productMol, ArrayList<Integer> mapping) {
		ArrayList<Integer> reactionCenters = new ArrayList<Integer>();
		if (DEBUG)
			System.out.println("Mapping : " + mapping);
		for (int v = 0; v < reactantMol.numV(); v++) {
			// Ignore nodes that are not even present in product.
			if (!mapping.contains(v))
				continue;
			int vertexInProduct = mapping.indexOf(v);
			// Look at 1-hop neighbors
			int j = 0;
			int[] oneHopNeighbors = new int[reactantMol.adjList()[v].length];
			for (int neighbor : reactantMol.adjList()[v]) {
				if (mapping.contains(neighbor))
					// set the id of the node in the product molecule
					oneHopNeighbors[j] = mapping.indexOf(neighbor);
				else
					oneHopNeighbors[j] = -1;
				j++;
			}
			Arrays.sort(oneHopNeighbors);
			int[] adjListProduct = productMol.adjList()[vertexInProduct];
			Arrays.sort(adjListProduct);
			boolean isReactionCenter = !Arrays.equals(oneHopNeighbors, adjListProduct);
			if (isReactionCenter)
				reactionCenters.add(v);
		}

		if (DEBUG) {
			System.out.println("Reaction centers (in reactant) :" + reactionCenters);
			System.out.print("Mapped reaction centers (in product) ");
			for (Integer integer : reactionCenters)
				System.out.print(mapping.indexOf(integer) + " ");
			System.out.println();
		}
		return reactionCenters;
	}

	/*
	 * From a reactant product pair, generate a reaction rule.
	 */
	public boolean generateRulePair(String molDirectory, RPM pair) throws IOException, ClassNotFoundException {
		String reactant = pair.getRpair().getReactant();
		String product = pair.getRpair().getProduct();
		if (DEBUG) {
			System.out.println("Pair " + pair.getId());
			System.out.println("Reactant " + reactant);
			System.out.println("Product " + product);
		}
		String pairString = reactant + " " + product;
		if (blacklistedPairs.contains(pairString)) {
			skipBlacklist++;
			if (DEBUG)
				System.out.println("Skipping pair due to a black listed pair");
			return false;
		}
		if (blacklistedCompounds.contains(reactant) || blacklistedCompounds.contains(product)) {
			skipBlacklist++;
			if (DEBUG)
				System.out.println("Skipping pair due to a black listed compound");
			return false;
		}
		/*
		 * We first check if there is some match at all. If there's no match,
		 * then all entries in map will be -1.
		 */
		boolean matchPresent = false;
		for (Integer entry : pair.getMapping()) {
			if (entry != -1)
				matchPresent = true;
		}
		if (!matchPresent) {
			skipNoMactch++;
			if (DEBUG)
				System.err.println("Skipping pair " + pair.getRpair().getReactant() + " "
						+ pair.getRpair().getProduct() + " due to no match");
			skipCount++;
			return false;
		}
		LGraph[] graphs_1 = LGraphFile
				.loadLGraphs(molDirectory + "/" + pair.getRpair().getReactant() + ".mol");
		LGraph[] graphs_2 = LGraphFile
				.loadLGraphs(molDirectory + "/" + pair.getRpair().getProduct() + ".mol");
		ConvertEdgeLabels cel = new ConvertEdgeLabels();
		Graph reactantMol = graphs_1[0];
		Graph productMol = graphs_2[0];
		if (Globals.INCLUDE_EDGE_LABELS) {
			reactantMol = cel.addEdgeLabelNodes((LGraph) reactantMol);
			productMol = cel.addEdgeLabelNodes((LGraph) productMol);
		}
		ArrayList<String> helperReactants = new ArrayList<String>();
		ArrayList<String> helperProducts = new ArrayList<String>();
		helperReactants.addAll(pair.getRpair().getReaction().getReactants());
		helperProducts.addAll(pair.getRpair().getReaction().getProducts());
		helperReactants.remove(reactant);
		helperProducts.remove(product);
		return generateRule(pair.getId(), (LGraph) reactantMol, (LGraph) productMol, pair.getMapping(), helperReactants,
				helperProducts);
	}

	public boolean generateRule(int id, LGraph reactantMol, LGraph productMol, ArrayList<Integer> mapping,
			ArrayList<String> helperReactants, ArrayList<String> helperProducts) throws IOException {
		// We check if there is a full match, for which no -1 present.
		// Such a pair is useless - so skip.
		rule = new ReactionRule();
		rule.setId(id);
		GraphDistance sged = new GraphDistance();
		// && sged.getEditDistance(reactantMol, productMol, false) < 1.0
		if (reactantMol.numE() == productMol.numE() && reactantMol.numV() == productMol.numV() && !mapping.contains(-1)
				&& sged.getEditDistance(reactantMol, productMol, false) < 1.0) {
			skipFullMatch++;
			System.err.println("Skipping pair " + " due to full match - so no rule generated");
			return false;
		}
		/*
		 * Identify reaction centers in the reactant mol. These are the nodes
		 * for which the 1-hop neighborhood is different across reactant and
		 * product. This means that node has undergone some change.
		 */
		rule.setReactionCentersReactant(getReactionCentersReactant(reactantMol, productMol, mapping));
		ArrayList<Integer> mappedReactionCenters = new ArrayList<Integer>();
		for (Integer r : rule.getReactionCentersReactant())
			mappedReactionCenters.add(mapping.indexOf(r));
		rule.setReactionCentersProduct(mappedReactionCenters);
		/*
		 * The reaction signature (in product side) is obtained by taking a
		 * k-hop neighborhood of the mapped reaction centers in the product mol.
		 */
		ArrayList<Integer> kHopNeighbours = kHopNeighbours(new HashSet<Integer>(mappedReactionCenters), productMol,
				hops);
		/*
		 * * List nodesToRemove - contains the nodes which have to be removed
		 * from the product molecule in order to re construct the reactant .
		 * These are the nodes which have map = -1 - same as subgraph added.
		 */
		ArrayList<Integer> subgraphAdded = new ArrayList<Integer>();
		boolean[] signatureVertices = new boolean[productMol.numV()];
		boolean[] signatureEdges = new boolean[productMol.numE()];
		/*
		 * Array isValidVertices - mark all vertices which are not in the k-hop
		 * neighborhood as invalid, so that they can be removed in order to
		 * construct the signature. Here, we mark only those vertices (as
		 * invalid) which have map as not -1, the nodes with -1 will be removed
		 * anyway.
		 */
		for (int v = 0; v < productMol.numV(); v++) {
			if (!kHopNeighbours.contains(v) && mapping.get(v) != -1)
				signatureVertices[v] = false;
			else
				signatureVertices[v] = true;
			if (mapping.get(v) == -1)
				subgraphAdded.add(v);
		}
		if (DEBUG)
			System.out.println("Nodes to remove (unmapped in product)/ Subgraph Added : " + subgraphAdded);
		/**
		 * Find the appropriate mapping the reaction signature graph to store
		 * the right nodes in subgraphAdded.
		 */

		int nodeRemoveCount[] = new int[signatureVertices.length];
		/*
		 * Stores number of nodes before a node that got removed. (Remove nodes
		 * that are present in both reactant & product and are not in the k-hop
		 * neighborhood).
		 */
		ArrayList<Integer> mySignMap = new ArrayList<Integer>();
		for (int i = 0; i < productMol.numV(); i++) {
			if (!signatureVertices[i] && i != 0)
				nodeRemoveCount[i] = nodeRemoveCount[i - 1] + 1;
			else if (!signatureVertices[i] && i == 0)
				nodeRemoveCount[i] = 1;
			else if (i != 0)
				nodeRemoveCount[i] = nodeRemoveCount[i - 1];
			else
				nodeRemoveCount[i] = 0;
			if (signatureVertices[i])
				mySignMap.add(i);
		}
		ArrayList<Integer> subgraphAddedMapped = new ArrayList<Integer>();
		for (Integer x : subgraphAdded)
			subgraphAddedMapped.add(x - nodeRemoveCount[x]);
		rule.setSubgraphAdded(subgraphAddedMapped);
		// Here, subgraphAdded has been computed and stored.

		/*
		 * signatureEdges marks those edges as invalid, which are between two
		 * mapped nodes, that are not a part of the k-hop neighborhood.
		 * signatureEdges contains edges that are either in k-hop neighborhood
		 * or are part of subgraphAdded.
		 */
		int i = 0;
		for (Edge e : productMol.E()) {
			if (!kHopNeighbours.contains(e.v1()) && mapping.get(e.v1()) != -1
					|| !kHopNeighbours.contains(e.v2()) && mapping.get(e.v2()) != -1)
				signatureEdges[i] = false;
			else
				signatureEdges[i] = true;
			i++;
		}
		
		if (DEBUG)
			System.out.println("Reaction signature + subgraphAdded graph :");
		rule.setReactionSignatureAdded(
				CreateValidSubGraph.createValidSubgraph(productMol, signatureVertices, signatureEdges));
		String reactionSignatureAddedString = graphLabeller.getCanonicalLabel((LGraph) rule.getReactionSignatureAdded());
		rule.setReactionSignatureAddedString(reactionSignatureAddedString);
		if (DEBUG)
			System.out.println(rule.getReactionSignatureAdded());
		/*
		 * Next, we get the mapping between signature (sub of) product mol.
		 */
		if (rule.getReactionSignatureAdded().numV() == 0)
			return false;
		ArrayList<Integer> signatureProductMapping = mySignMap;
		if (DEBUG)
			System.out.println("Signature product map :" + signatureProductMapping);
		/*
		 * The next step is to obtain reactant ~ product graph. The reactant -
		 * product subgraph is computed, along inter rc changes.
		 */
		if (DEBUG)
			System.out.println("Reactant - Product graph : subgraphRemoved :");
		boolean status = getSubgraphRemoved(reactantMol, productMol, mapping, signatureProductMapping);
		if (!status)
			return false;
		if (DEBUG)
			System.out
					.println("Connecting edge (from subgraphRemoved to RCs)" + rule.getMetaData().getConnectingEdges());
		if (DEBUG)
			System.out.println("Nodes to remove (Subgraph/Nodes Added) :" + rule.getSubgraphAdded());
		/*
		 * Now, we get the product-reactant subgraph, this is ideally not
		 * required. But,in the process we also get the bonds to remove.
		 */

		// Next step is to find the inter-RC edge changes
		status = false;
		status = getInterRCEdges(reactantMol, productMol, mapping, signatureProductMapping);
		if (!status)
			return false;
		if (DEBUG)
			System.out.println("Inter RC edges" + rule.getMetaData().getInterRCEdges());
		if (rule.getMetaData().getConnectingEdges().size() == 0 && rule.getMetaData().getInterRCEdges().size() == 0) {
			if (DEBUG)
				System.err.println("Skipping pair, as no change in edges");
			skipCount++;
			return false;
		}

		LGraph reconstructedReactant = ruleApply.applyReactionRule((LGraph) productMol, rule);
		rule.setHelperReactants(new ArrayList<String>(helperReactants));
		rule.setHelperProducts(new ArrayList<String>(helperProducts));
		GraphLabelling giso = new GraphLabelling();
		String res = Index.knownMolLabels.get(giso.getCanonicalLabel(reconstructedReactant));
		if (res != null && !res.contentEquals("null"))
			rules.add(rule);
		else {
			System.err.println("Problem with " + rule.getId());
			return false;
		}
		if (DEBUG)
			System.out.println("Rule generated");
		return true;
	}

	/*
	 * Main function to mine rules from a list of reactant product pairs. It
	 * uses the default reaction file given in the Globals file
	 */
	void mineRules(ArrayList<RPM> pairs) throws ClassNotFoundException, IOException {
		int count = 0;
		for (RPM pair : pairs) {
			System.out.println(pair.getId());
			// TODO: Hacks here. 
			if (pair.getId() == 16965 || pair.getId() == 26589 || pair.getId() == 27803 || pair.getId() == 27804)
				continue;
			
			boolean f = generateRulePair(Globals.molDirectory, pair);
			if (f) {
				System.out.println(pair.getId());
				finalPairs.put(pair.getId(), pair);
			} else
				count++;		
		}
		// System.exit(0);
		System.out.println("Skip due to no match " + skipNoMactch);
		System.out.println("Skip due to full match " + skipFullMatch);
		System.out.println("Skip due to black list " + skipBlacklist);
		System.out.println("Total skip :" + count);
		System.out.println("Total " + rules.size());
	}

	public void driver() throws IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(Globals.indexDirectory + "pairs.ser"));
		readBlackListedPairs();
		readBlackListedCompounds();
		ComputeRPM rpp = (ComputeRPM) in.readObject();
		in.close();
		mineRules(rpp.getRpairMap());

		System.out.println("Rule mining over");
		System.out.println(rules.size() + " " + finalPairs.size());
		FileOutputStream fileOut = new FileOutputStream(Globals.indexDirectory + "rules.ser");
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(rules);
		out.close();
		fileOut.close();
		fileOut = new FileOutputStream(Globals.indexDirectory + "finalPairs.ser");
		out = new ObjectOutputStream(fileOut);
		out.writeObject(finalPairs);
		out.close();
		fileOut.close();
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		RuleMining mr = new RuleMining();
		mr.driver();
	}
}
