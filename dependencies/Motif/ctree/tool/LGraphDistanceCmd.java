// args[0] : File containing paired mol IDs
// Ex:
//
// C02355 C01368
// C02355 C00080
// C00001 C01368
// C00001 C00080

// args[1] : Directory where mol files are stored

package ctree.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import ctree.alg.Ullmann;
import ctree.graph.Graph;
import ctree.index.Util;
import ctree.lgraph.LGraph;
import ctree.lgraph.LGraphDistance;
import ctree.lgraph.LGraphFile;
import ctree.lgraph.LGraphWeightMatrix;
import ctree.mapper.GraphMapper;
import ctree.mapper.NeighborBiasedMapper;
import ctree.util.Opt;

public class LGraphDistanceCmd {

	private static void usage() {
		System.err
				.println("\nUsage:\nargs[0] : File containing paired mol IDs\nargs[1] : Directory where mol files are stored\n");
		System.err
		.println("args[0] Format: MolID1;MolID2;Reaction\n");
		System.err
		.println("Output: MolID1;MolID2;Distance;Size_of_first;Size_of_second;Reaction\n");
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Opt opt = new Opt(args);
		if (opt.args() < 2) {
			usage();
			return;
		}

		File file = new File(args[0]);
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);

		String line;
		int totalCount = 0;
		while ((line = bufferedReader.readLine()) != null) {
			String[] parts = line.split(";");
			String first = args[1].concat("/").concat(parts[0]).concat(".mol");
			String second = args[1].concat("/").concat(parts[1]).concat(".mol");
			
			
			//For testing
			// String first =
			// ("/Users/panks/Documents/ddp/graphviz/tmpMol/phosphate.mol");
			// String second =
			// ("/Users/panks/Documents/ddp/graphviz/tmpMol/C01368.mol");

			LGraph[] graphs1 = LGraphFile.loadLGraphs(second);
			LGraph[] graphs2 = LGraphFile.loadLGraphs(first);

			Graph a = graphs1[0]; // is pdt.
			Graph b = graphs2[0]; // is reactant


			LGraphDistance lgd = new LGraphDistance();
			GraphMapper mapper = new NeighborBiasedMapper(
					new LGraphWeightMatrix());
			int[] map = mapper.map(a, b);

			double dist = lgd.d(a, b, map, true);
					
			System.out.println(parts[0] + ";" + parts[1] + ";"+dist+";"+(b.numE()+b.numV())+";"+(a.numE()+a.numV())+";"+parts[2]);
			
			// System.out.println(subgraphFrequencyCounter.count(graphs2,
			// graphs1[0]));

		}
		bufferedReader.close();
		// System.out.println("Total count is: "+totalCount);

	}

}
