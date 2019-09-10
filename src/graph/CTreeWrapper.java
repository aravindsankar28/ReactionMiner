package graph;

import java.io.*;
import java.util.*;
import globals.Index;
import ctree.index.*;
import ctree.lgraph.LGraph;
import ctree.lgraph.LGraphFactory;
import ctree.lgraph.LGraphSim;
import ctree.lgraph.LGraphWeightMatrix;
import ctree.lgraph.LabelMap;
import ctree.mapper.*;
import ctree.tool.*;

public class CTreeWrapper {

	/**
	 * @param args
	 *            Wrapper over Closure Tree to create index for our use.
	 */
	public CTree ctree;

	public void buildTree(LGraph[] graphs)
			throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		// Build ctree from a list of graphs.
		GraphMapper mapper = new NeighborBiasedMapper(new LGraphWeightMatrix());
		GraphSim graphSim = new LGraphSim();
		LabelMap labelMap = new LabelMap(graphs);
		int L = labelMap.size();
		int dim1 = Math.min(97, L);
		int dim2 = Math.min(97, L * L);
		int m = 20;
		int M = 2 * m - 1;
		GraphFactory factory = new LGraphFactory(labelMap, dim1, dim2);
		ctree = new CTree(m, M, mapper, graphSim, factory);
		long time0 = System.currentTimeMillis();
		ctree = BuildCTree.buildCTree(graphs, m, M, mapper, graphSim, labelMap, factory);

		long time = System.currentTimeMillis() - time0;
		System.out.println("Insertion time: " + time / 1000.0);
		// Can save to file using this command.
		// ctree.saveTo("ctree_index_new.ctr");
	}

	/*
	 * Given a query q, return all g for which query is q \subset g. We return
	 * the ids of the graphs. Note that this uses pseudo subgraph isomorphisms
	 * and hence the answer set can contain false positives also.
	 */

	public ArrayList<String> subGraphQuery(LGraph query) {
		Vector<ctree.graph.Graph> cand = SubQuery.subgraphQuery(ctree, query, 3, true);
		ArrayList<String> answerSet = new ArrayList<String>();

		for (ctree.graph.Graph g : cand) {
			LGraph temp = (LGraph) g;
			answerSet.add(temp.getId());
		}
		return answerSet;
	}

	public static void main(String[] args)
			throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException {
		// Code for testing purposes.
		// if (!Index.isLoaded) {
		// Index.loadMolecules();
		// Index.loadKnownMolLabels();
		// Index.loadUniqeSignatures();
		// Index.loadSignatureCanonicalLabels();
		// }
		// CTreeWrapper ctreeWrapper = new CTreeWrapper();
		// LGraph[] graphs = Index.knownMolecules.values().toArray(
		// new LGraph[Index.knownMolecules.values().size()]);
		//
		// ctreeWrapper.buildTree(graphs);
		// int i = 0;
		// for (LGraph q : Index.signatureCanonicalLabels.values()) {
		// // Process each reaction signature and get molecules for which the
		// signature is a subgraph of.
		// System.out.println(ctreeWrapper.subGraphQuery(q));
		// System.out.println("Process " + i);
		// i++;
		// break;
		// }
	}
}
