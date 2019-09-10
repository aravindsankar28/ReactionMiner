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
import ctree.lgraph.LGraphFile;
import ctree.util.Opt;

public class SubGraphIsoSingleCmd {

	private static void usage() {
		System.err
				.println("\nUsage:\nargs[0] : File containing paired mol IDs\nargs[1] : Directory where mol files are stored\n");
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Opt opt = new Opt(args);
		if (opt.args() < 2) {
			usage();
			return;
		}

//		File file = new File(args[0]);
//		FileReader fileReader = new FileReader(file);
//		BufferedReader bufferedReader = new BufferedReader(fileReader);

//		String line;
		int totalCount = 0;
//		while ((line = bufferedReader.readLine()) != null) {
//			String[] parts = line.split(" ");
			String first = args[0];
			String second = args[1];
//			System.out.print(parts[0] + ", " + parts[1] + ", ");
			
			
			//For testing
			// String first =
			// ("/Users/panks/Documents/ddp/graphviz/tmpMol/phosphate.mol");
			// String second =
			// ("/Users/panks/Documents/ddp/graphviz/tmpMol/C01368.mol");

			LGraph[] graphs1 = LGraphFile.loadLGraphs(first);
			LGraph[] graphs2 = LGraphFile.loadLGraphs(second);

			Graph a = graphs1[0];
			Graph b = graphs2[0];

			int[][] B = Util.getBipartiteMatrix2(a, b);
			int[] map = Ullmann.subgraphIsomorphism(a.adjMatrix(),
					b.adjMatrix(), B);

			// for(int i=0; i<B.length; i++){
			// for(int j=0; j<B[i].length; j++){
			// System.out.print(B[i][j]);
			// }
			// System.out.println();
			// }

			if (map != null) {
				totalCount++;
				for (int i = 0; i < map.length; i++) {
					System.out.print(map[i] + " ");
				}
			} else {
				System.out.print("null");
			}
			System.out.println();
			// System.out.println(subgraphFrequencyCounter.count(graphs2,
			// graphs1[0]));

//		}
//		bufferedReader.close();
		// System.out.println("Total count is: "+totalCount);

	}

}
