package graph;

import java.io.IOException;
import java.util.*;
import org.apache.commons.lang3.ArrayUtils;
import ctree.alg.Ullmann;
import ctree.graph.Graph;
import ctree.index.Util;

/**
 * @author aravind Used to get mapping between 2 graphs. Returns null if it is
 *         not a subgraph. Includes some helper functions to find subgraph
 *         isomorphic mols (and isomorphic) to a given mol. Here, the molecule
 *         format must be exact -- must include edge label nodes in the input
 *         graphs.
 */
public class SubgraphMapping {

	public ArrayList<Integer> getMapping(Graph reactionSignature, Graph productMol) {
		// This productMol here, can be any graph. eg: signature subset of query
		int[][] B = Util.getBipartiteMatrix2(reactionSignature, productMol);
		int[] map = Ullmann.subgraphIsomorphism(reactionSignature.adjMatrix(), productMol.adjMatrix(), B);
		if (map != null)
			return new ArrayList<Integer>(Arrays.asList(ArrayUtils.toObject(map)));
		else
			return null;
	}

	public static void main(String[] args) throws IOException {

	}
}