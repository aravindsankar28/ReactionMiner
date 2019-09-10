package pathwayPrediction;

import java.io.*;
import java.util.*;

import graph.*;
import ruleMining.RuleApply;
import ruleMining.ReactionRule.ReactionRule;
import ctree.lgraph.*;
import globals.*;

/**
 * 
 * @author aravind Basic code for applying a reaction rule on a query molecule.
 *         There is an additional function to perform a single compound query by
 *         examining all rules applicable. (knownMolQuery or unknownMolQuery) It
 *         also contains a verifyValidity function to check if a graph is a
 *         known molecule or not.
 */
public class SingleStepQuery {
	static boolean DEBUG;
	static boolean DISPLAY = false;
	LGraph query;
	// Query outputs.
	RuleApply ruleApply;
	double supIsoTime = 0.0;
	GraphLabelling giso;

	public SingleStepQuery() throws IOException, ClassNotFoundException {
		DEBUG = Globals.DEBUG;
		giso = new GraphLabelling();
		ruleApply = new RuleApply();
		preComputations();
	}

	void preComputations() throws IOException, ClassNotFoundException {
		if (!Index.isLoaded)
			Index.loadAll();
	}

	// Here, query is the reconstructed reactant. - based on subgraph
	// isomorphism based graph isomorphism.
	String verifyValidity(LGraph query) throws IOException {
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

	/*
	 * String verifyValidity2(LGraph query) throws IOException { for (String key
	 * : Index.knownMolecules.keySet()) { LGraph g =
	 * Index.knownMolecules.get(key); if (giso.isIsomorphic(query, g,
	 * giso.getKnownMoleculesLabels().get(key))) return key; } return "null"; }
	 */

	// Based on hashing canonical labels.
	public String verifyValidity3(LGraph query) throws IOException {
		String canonicalLabel = giso.getCanonicalLabel(query);
		return Index.knownMolLabels.get(canonicalLabel);
	}

	public void knownMolQuery(String query) throws IOException, ClassNotFoundException {
		// Example argument : C00299
		LGraph[] graphs = new LGraph[1];
		graphs = LGraphFile.loadLGraphs(Globals.molDirectory + "/" + query + ".mol");
		graphs[0].setId(query);
		unknownMolQuery(graphs[0]);
	}

	void unknownMolQuery(LGraph query) throws IOException {
		ConvertEdgeLabels cel = new ConvertEdgeLabels();
		if (Globals.INCLUDE_EDGE_LABELS)
			query = cel.addEdgeLabelNodes(query);
		this.query = query;
		SubgraphMapping sgim = new SubgraphMapping();
		// Step 1 : identify applicable rules
		HashMap<ReactionRule, ArrayList<Integer>> tentativeRulesMaps = new HashMap<ReactionRule, ArrayList<Integer>>();
		ArrayList<ReactionRule> applicableRules = new ArrayList<>();
		double start_time = System.currentTimeMillis();
		for (Integer ruleId : Index.uniqueRuleMap.keySet()) {
			LGraph reactionSignature = (LGraph) Index.uniqueRuleMap.get(ruleId).getReactionSignatureAdded();
			if (reactionSignature.numE() > query.numE() || reactionSignature.numV() > query.numV())
				continue;
			if (DEBUG) {
				System.out.println("Size of sign. " + reactionSignature.numE() + " query size " + query.numE());
				System.out.println("Query : " + query);
				System.out.println("Signature : " + reactionSignature);
			}
			ArrayList<Integer> mapping = sgim.getMapping(reactionSignature, query);
			if (DEBUG)
				System.out.println(mapping);

			if (mapping != null) 
				applicableRules.add(Index.uniqueRuleMap.get(ruleId));
		}

		if (DEBUG)
			System.out.println("Total applicable rules  :" + tentativeRulesMaps.size());
		supIsoTime += (System.currentTimeMillis() - start_time) / 1000.0;

		// Step 2 : apply these rules and verify.

		// for (ReactionRule rule : tentativeRulesMaps.keySet()) {
		for (ReactionRule rule : applicableRules) {
			LGraph reconstructedReactant = ruleApply.applyReactionRule(query, rule);
			// String res = verifyValidity3(reconstructedReactant);
			// verifyValidity3(reconstructedReactant);
			String res = verifyValidity3(reconstructedReactant);
			if (res != null && !res.contentEquals("null")) {
				System.out.println("Reactions found ");
				System.out.println("Original reaction : ");
				System.out.println(Index.finalPairs.get(rule.getId()).getRpair().getReaction());
				System.out.println(res + " ");
				System.out.println();
			}
		}
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		SingleStepQuery q = new SingleStepQuery();
		long timeNow = System.currentTimeMillis();
		// q.knownMolQuery("METHYL_PHENYLACETATE");
		q.knownMolQuery("C00022");
		if (DISPLAY)
			System.out.println((System.currentTimeMillis() - timeNow) / 1000.0 + " seconds");
	}
}
