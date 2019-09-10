package ctree.tool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import ctree.alg.Ullmann;
import ctree.graph.Graph;
import ctree.index.Util;

public class SubGraphIsoMapping {

	public ArrayList<Integer> getMapping(Graph reactionSignature,
			Graph productMol) {
		// This productMol here, can be any graph. eg: reactionSignature subset
		// of
		// query mol

		int[][] B = Util.getBipartiteMatrix2(reactionSignature, productMol);
		int[] map = Ullmann.subgraphIsomorphism(reactionSignature.adjMatrix(),
				productMol.adjMatrix(), B);

		if (map != null) {
			/*
			 * for (int i = 0; i < map.length; i++) { System.out.print(map[i] +
			 * " "); }
			 */
			return new ArrayList<Integer>(Arrays.asList(ArrayUtils
					.toObject(map)));
		} else
			return null;
	}

	public static void main(String[] args) throws IOException {
		SubGraphIsoMapping sgim = new SubGraphIsoMapping();

	}

}
