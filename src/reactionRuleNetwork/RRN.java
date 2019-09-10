package reactionRuleNetwork;

import java.io.*;
import java.util.*;

import globals.*;
import ruleMining.RPM.RPM;
import ruleMining.ReactionRule.ReactionRule;

/***
 * 
 * @author aravind Modify this class to use just the unique rules obtained after
 *         clustering, as the nodes in the network. The nodes must store
 *         auxiliary information about the set of helper reactants and reaction
 *         ids for each such rule. Check the reaction rule class and think of
 *         the best way to modify the code structure. This is necessary to
 *         prevent unnecessary duplicate storage and computation of reaction
 *         rules.
 */

public class RRN {
	HashMap<String, ArrayList<Integer>> reactionSignatureRuleMapping;
	// Maybe move it to index.
	// Stores a list of rules (ids) indexed by the same reaction signature.
	// Create mapping from each signature (canonical label) to a list of nodes.

	// Note: We Store label of signature along with reaction rule.

	HashMap<Integer, Set<Integer>> adjacencyList;
	// List of adjacent nodes for each node.
	// Node ids will be the same as rule ids - yes.

	public HashMap<Integer, Set<Integer>> getAdjacencyList() {
		return adjacencyList;
	}

	public HashMap<String, ArrayList<Integer>> getReactionSignatureRuleMapping() {
		return reactionSignatureRuleMapping;
	}

	HashMap<String, ArrayList<String>> applicableReactionSignatures;
	// For each molecule, get the applicable signatures.

	public HashMap<String, ArrayList<String>> getApplicableReactionSignatures() {
		return applicableReactionSignatures;
	}

	public RRN() throws ClassNotFoundException, IOException {
		this.applicableReactionSignatures = new HashMap<String, ArrayList<String>>();
		adjacencyList = new HashMap<Integer, Set<Integer>>();
		this.reactionSignatureRuleMapping = new HashMap<String, ArrayList<Integer>>();
		this.preCompute();
	}

	// TODO: Compute only the things that are used.
	void preCompute() throws ClassNotFoundException, IOException {
		if (!Index.isLoaded)
			Index.loadAll();
		// computeApplicableReactionSignatures(); // -- This is to be just once
		// - reload later.
		loadApplicableSignatures();
	}

	/**
	 * Create nodes as the set of all rules that have been mined earlier. Create
	 * the signature to rule mapping which is needed to map back from unique
	 * signatures to rules.
	 */

	void createNodes(ArrayList<Integer> ruleIds) {
		adjacencyList.clear();
		reactionSignatureRuleMapping.clear();
		for (int ruleId : ruleIds) {
			adjacencyList.put(ruleId, new HashSet<Integer>());
			if(! Index.allRuleMap.containsKey(ruleId))
				continue;
			String reactionSignatureString = Index.allRuleMap.get(ruleId).getReactionSignatureAddedString();
			if (!reactionSignatureRuleMapping.containsKey(reactionSignatureString))
				reactionSignatureRuleMapping.put(reactionSignatureString, new ArrayList<Integer>());
			reactionSignatureRuleMapping.get(reactionSignatureString).add(ruleId);
		}
	}

	void createEdges(ArrayList<Integer> rpmIds) {
		for (Integer pair_id : rpmIds) {
			RPM pair = Index.finalPairs.get(pair_id);
			String reactantName = pair.getRpair().getReactant();
			String productName = pair.getRpair().getProduct();
			if (productName.contentEquals("C16302") || reactantName.contentEquals("C16302"))
				continue;
			Set<String> nodes1 = new HashSet<String>();
			// Stores all reaction signatures applicable on the reactant.
			if (applicableReactionSignatures.containsKey(reactantName))
				nodes1 = new HashSet<String>(applicableReactionSignatures.get(reactantName));
			if(! Index.allRuleMap.containsKey(Index.reverseClusterMap.get(pair_id)))
				continue;
			ReactionRule rule = Index.allRuleMap.get(Index.reverseClusterMap.get(pair_id));
			
			// Use the rule of the cluster rep.
			String node2_string = rule.getReactionSignatureAddedString();
			// For each node represented by node 2 signature string, add edges.
			// if(reactionSignatureRuleMapping.get(node2_string).size() > 150)
			// continue;
			for (int node2 : reactionSignatureRuleMapping.get(node2_string)) {
				for (String node1_string : nodes1) {
					// Note : there may be some reaction signatures here that
					// are not applicable since they were not in the reaction
					// input set.
					
					if(! reactionSignatureRuleMapping.containsKey(node1_string))
						continue;
					for (int node1 : reactionSignatureRuleMapping.get(node1_string))
						adjacencyList.get(node2).add(node1);
				}
			}
		}
		System.out.println("Edges created");
	}

	/**
	 * Compute the applicable reaction signatures using the Ctree index.
	 * 
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	void computeApplicableReactionSignatures()
			throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException {
		RRNCreationHelper rrnhelper = new RRNCreationHelper();
		rrnhelper.buildCtreeIndex();
		applicableReactionSignatures = rrnhelper.getSignaturesCTreeIndex(reactionSignatureRuleMapping.keySet());
		FileOutputStream fileOut = new FileOutputStream(Globals.indexDirectory + "applicableReactionSignatures.ser");
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(applicableReactionSignatures);
		out.close();
		fileOut.close();
	}

	@SuppressWarnings("unchecked")
	public void loadApplicableSignatures() throws ClassNotFoundException, IOException {
		ObjectInputStream in = new ObjectInputStream(
				new FileInputStream(Globals.indexDirectory + "applicableReactionSignatures.ser"));
		this.applicableReactionSignatures = (HashMap<String, ArrayList<String>>) in.readObject();
		in.close();
	}

	public void driver(ArrayList<String> reactionIds)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
		// Compute the rule ids that are applicable based on the reaction ids
		// and the pairs involved.
		if(reactionIds.contains("R00709"))
			System.out.println("HELLO");
		
		ArrayList<Integer> ruleIds = new ArrayList<>();
		ArrayList<Integer> rpmIds = new ArrayList<>();
		for (Integer x : Index.allRuleMap.keySet()) {
			String rid = Index.finalPairs.get(x).getRpair().getReaction().getId();
			if (reactionIds.contains(rid)) {
				ruleIds.add(Index.reverseClusterMap.get(x));
				// Add the rule number of the cluster this rule belongs to.
				rpmIds.add(x);
			}
		}

		createNodes(ruleIds);
		System.out.println("#nodes " + adjacencyList.size());
		createEdges(rpmIds);
	}

	public void driver() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
		createNodes(new ArrayList<Integer>(Index.uniqueRuleMap.keySet()));
		System.out.println("#nodes " + adjacencyList.size());
		createEdges(new ArrayList<Integer>(Index.finalPairs.keySet()));
	}

	public static void main(String[] args)
			throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException {
		RRN rrn = new RRN();
		rrn.driver();
	}
}
