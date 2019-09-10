package graph;

// NeighborBiased mapping - based on neighbourhood biased mapper. This is used between reactant and product.
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import globals.Globals;

import ctree.graph.*;
import ctree.mapper.*;
import ctree.lgraph.*;

public class NeighborBiasedMapping {

	
	public ArrayList<Integer> getSimQueryMapBasic(LGraph reactant, LGraph product)
			throws IOException {
		GraphMapper mapper = new NeighborBiasedMapper(new LGraphWeightMatrix());
		int[] map = mapper.map(product, reactant);
		ArrayList<Integer> mapping = new ArrayList<Integer>(
				Arrays.asList(ArrayUtils.toObject(map)));
		return mapping;
	}

	
	// Assume graphs without any added edge label nodes. 
	public ArrayList<Integer> getMapping(LGraph reactant, LGraph product)
			throws IOException {
		GraphMapper mapper = new NeighborBiasedMapper(new LGraphWeightMatrix());

		ConvertEdgeLabels cel = new ConvertEdgeLabels();

		if (Globals.INCLUDE_EDGE_LABELS) {
			product = cel.addEdgeLabelNodes((LGraph) product);
			reactant = cel.addEdgeLabelNodes((LGraph) reactant);
		}
		int[] map = mapper.map(product, reactant);

		ArrayList<Integer> mapping = new ArrayList<Integer>(
				Arrays.asList(ArrayUtils.toObject(map)));
		
		return mapping;

	}



	public static void main(String[] args) throws IOException {
		NeighborBiasedMapping sqm = new NeighborBiasedMapping();
		System.out.println(sqm.getMapping(LGraphFile.loadLGraphs(Globals.molDirectory+"/C00197.mol")[0], LGraphFile.loadLGraphs(Globals.molDirectory+"/C00631.mol")[0]));
	}

}
