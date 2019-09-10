package graph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.lang3.ArrayUtils;
import globals.Globals;

import ctree.graph.Graph;
import ctree.lgraph.LGraph;
import ctree.lgraph.LGraphDistance;
import ctree.lgraph.LGraphFile;
import ctree.lgraph.LGraphWeightMatrix;
import ctree.mapper.GraphMapper;
import ctree.mapper.NeighborBiasedMapper;

/**
 * @author aravind : Computation of graph edit distance/subgraph edit distance using Neighbor Biased
 *         Mapper defined Closure Tree. Takes an input parameter sub
 *         (true/false) to indicate subgraph/graph edit distance.
 *         Takes input two command line arguments with file names of the 2 graph.
 *         Assumes that two input graphs are the raw format - without any added edge label nodes.
 */

// This computes the edit distance based on the neighbourhood biased mapper.
public class GraphDistance {
	ArrayList<Integer> mapping = new ArrayList<Integer>();
	public double getEditDistance(LGraph reactant, LGraph product, boolean sub) {
		// Graph distance between reactant and product.
		Graph a = product;
		Graph b = reactant;
		ConvertEdgeLabels cel = new ConvertEdgeLabels();
		if (Globals.INCLUDE_EDGE_LABELS) {
			a = cel.addEdgeLabelNodes((LGraph) a);
			b = cel.addEdgeLabelNodes((LGraph) b);
		}
		LGraphDistance lgd = new LGraphDistance();
		GraphMapper mapper = new NeighborBiasedMapper(new LGraphWeightMatrix());
		int[] map = mapper.map(a, b);
		mapping = new ArrayList<Integer>(Arrays.asList(ArrayUtils.toObject(map)));
		// NOTE: changed to false here for graph edit distance
		double dist = lgd.d(a, b, map, sub);
		double distanceMin = dist;
		Graph t = a;
		a = b;
		b = t;
		map = mapper.map(a, b);
		dist = lgd.d(a, b, map, sub);
		if (dist < distanceMin) {
			distanceMin = dist;
			mapping = new ArrayList<Integer>(Arrays.asList(ArrayUtils.toObject(map)));
		}
		return distanceMin;
	}

	public static void main(String[] args) throws IOException {
		GraphDistance ged = new GraphDistance();
		double d = ged.getEditDistance(LGraphFile.loadLGraphs(Globals.molDirectory + "/" + args[0] + ".mol")[0],
				LGraphFile.loadLGraphs(Globals.molDirectory + "/" + args[1] + ".mol")[0],
				Boolean.parseBoolean(args[2]));
		System.out.println("Distance : "+d);
	}
}
