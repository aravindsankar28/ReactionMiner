package reactionRuleNetwork;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import ctree.lgraph.LGraph;
import globals.Globals;
import globals.Index;
import ruleMining.*;
import ruleMining.RPM.*;
import ruleMining.ReactionRule.ReactionRule;
import graph.CTreeWrapper;
import graph.GraphLabelling;
import graph.SubgraphMapping;

public class RRNCreationHelper {

	/**
	 * @param args
	 */
	public CTreeWrapper cTreeWrapper;

	public RRNCreationHelper() throws ClassNotFoundException, IOException {
		cTreeWrapper = new CTreeWrapper();
		if (!Index.isLoaded)
			Index.loadAll();
	}

	public HashMap<String, ArrayList<String>> getSignaturesCTreeIndex(Set<String> reactionSignatureLabels)
			throws ClassNotFoundException, IOException {
		// Given a set of reaction signature labels, find the applicable
		// signatures for each molecule in the database.
		Set<String> uniqueSignaturesTrain = reactionSignatureLabels;
		HashMap<String, ArrayList<String>> applicableReactionSignatures = new HashMap<String, ArrayList<String>>();
		int i = 0;
		// cTreeWrapper.ctree = CTree.load("ctree_index_new.ctr");

		for (String label : uniqueSignaturesTrain) {
			LGraph q = Index.signatureCanonicalLabels.get(label);
			ArrayList<String> subMols = cTreeWrapper.subGraphQuery(q);
			for (String str : subMols) {
				if (!applicableReactionSignatures.containsKey(str))
					applicableReactionSignatures.put(str, new ArrayList<String>());
				applicableReactionSignatures.get(str).add(label);
			}
			i++;
			System.out.println("Process " + i + " out of " + uniqueSignaturesTrain.size());
		}
		return applicableReactionSignatures;
		
		/*
		 * 
		 * FileOutputStream fileOut = new
		 * FileOutputStream(Globals.indexDirectory +
		 * "applicableReactionSignatures.ser"); ObjectOutputStream out = new
		 * ObjectOutputStream(fileOut);
		 * out.writeObject(applicableReactionSignatures); out.close();
		 * fileOut.close();
		 */
	}

	public void buildCtreeIndexNewNet()
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		LGraph[] graphs = new LGraph[Index.allRuleMap.size()];
		int i = 0;
		RuleApply ruleApply = new RuleApply();
		for (ReactionRule r : Index.allRuleMap.values()) {
			LGraph g = ruleApply.applyReactionRule((LGraph) r.getReactionSignatureAdded(), r);
			g.setId(String.valueOf(r.getId()));
			graphs[i++] = g;
		}
		cTreeWrapper.buildTree(graphs);
	}
	// Current c tree build code.

	public void buildCtreeIndex()
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		LGraph[] graphs = new LGraph[Index.knownMolecules.size()];
		int i = 0;
		for (LGraph g : Index.knownMolecules.values())
			graphs[i++] = g;
		cTreeWrapper.buildTree(graphs);
	}

	public static void main(String[] args)
			throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException {
		// Given a number of pairs to train on, first get the signatures and
		// then compute applicable signatures using ctree and then create the
		// network.
		// If possible compare it with brute force approach.

		double startTime = System.currentTimeMillis();
		RRNCreationHelper rnc = new RRNCreationHelper();
		rnc.buildCtreeIndex();
		System.out.println("Ctree built");
		rnc.getSignaturesCTreeIndex(new HashSet());
		System.out.println("Elapsed time = " + (System.currentTimeMillis() - startTime) / 1000.0);
	}
}
