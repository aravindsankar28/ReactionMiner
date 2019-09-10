package ctree.tool;

import java.util.*;

import ctree.index.*;
import ctree.graph.*;
import ctree.mapper.*;
import ctree.util.*;
import ctree.lgraph.*;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author Huahai He
 * @version 1.0
 */

// args[0] : File containing paired mol IDs
// Ex:
//
// C02355 C01368
// C02355 C00080
// C00001 C01368
// C00001 C00080

// args[1] : Directory where mol files are stored

public class SimQueryCmd {
	/*
	 * static private double confidence; static private double validity; static
	 * private double precision; static private double confidence_L;
	 */

	private static void usage() {
		System.err
				.println("\nUsage:\nargs[0] : File containing paired mol IDs\nargs[1] : Directory where mol files are stored\n");
	}

	public static void main(String[] args) throws Exception {
		// System.out.println("\nUsage:\nargs[0] : File containing paired mol IDs\nargs[1] : Directory where mol files are stored\n\n");
		Opt opt = new Opt(args);
		if (opt.args() < 2) {
			usage();
			return;
		}

		File file = new File(args[0]);
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);

		String line;
		GraphMapper mapper1 = new NeighborBiasedMapper(
				new LGraphWeightMatrix());
		GraphSim graphSim1 = new LGraphSim();
		GraphMapper mapper = new NeighborBiasedMapper(
				new LGraphWeightMatrix());
		GraphSim graphSim = new LGraphSim();
		while ((line = bufferedReader.readLine()) != null) {
			
			String[] parts = line.split(";");
			String first = args[1].concat("/").concat(parts[0]).concat(".mol");
			String second = args[1].concat("/").concat(parts[1]).concat(".mol");
			System.out.print(parts[0] + ";" + parts[1] + ";"+ parts[2]);



			// System.err.println("Load graphs");
			LGraph[] graphs = LGraphFile.loadLGraphs(first);
			Graph[] queries = LGraphFile.loadLGraphs(second);
			/** @todo args incorrect */

			
			LabelMap labelMap = new LabelMap(graphs);
			int L = labelMap.size();
			int dim1 = Math.min(97, L);
			int dim2 = Math.min(97, L * L);
			GraphFactory factory = new LGraphFactory(labelMap, dim1, dim2);

			CTree ctree = new CTree(1, 19, mapper1, graphSim1, factory);

			for (int i = 0; i < graphs.length; i++) {
				ctree.insert(graphs[i]);
			}

			boolean knn;
			int k = 0;
			double range = 0;
			range = 100;
			knn = false;

			int nQ = 1;
			boolean strict = true;

			String output = null;
			PrintWriter out = null;



			DataSum stat = new DataSum();

			// System.err.println("Query");
			for (int i = 0; i < nQ; i++) {
				long query_time = System.currentTimeMillis();

				Vector<RankerEntry> ans;
				if (knn) {
					ans = kNNQuery(ctree, mapper, graphSim, queries[i], k,
							strict);
				} else {
					ans = rangeQuery(ctree, mapper, graphSim, queries[i],
							-range, strict);
				}
				query_time = System.currentTimeMillis() - query_time;

				// Output answers to this query
				if (output != null) {
					out.println(ans.size());
					for (RankerEntry e : ans) {
						Graph g = e.getGraph();
						out.println(((LGraph) g).getId());
					}
				}

				// statistics
				stat.add("query_time", query_time);
				stat.add("ans_size", ans.size());
				stat.add("access_ratio", (double) accessCount / ctree.size());

				double sim = 0, simUp = 0, rate = 0;
				int size = ans.size();
				for (int j = 0; j < size; j++) {
					RankerEntry entry = (RankerEntry) ans.elementAt(j);
					double temp1, temp2;
					sim += temp1 = -entry.getDist();
					simUp += temp2 = graphSim.simUpper(queries[i],
							entry.getGraph());
					// ratio += (double) accessCount / ctree.size();
					rate += temp1 / temp2;
				}
				stat.add("sim", sim / size);
				stat.add("simUp", simUp / size);
				stat.add("rate", rate / size); // sim/simUp
				stat.add("norm", graphSim.norm(queries[i]));

				if ((i + 1) % 10 == 0) {
					System.err.println("Query at " + (i + 1));
				}
			} // for queries

			if (output != null) {
				out.close();
			}

		}
		fileReader.close();
	}

	private static int accessCount;

	/**
	 * Query using NNRanker
	 * 
	 * @param ctree
	 *            CTree
	 * @param mapper
	 *            GraphMapper
	 * @param graphSim
	 *            GraphSim
	 * @param query
	 *            Graph
	 * @param k
	 *            int
	 * @param preciseRanking
	 *            boolean
	 * @return Vector
	 */
	public static Vector<RankerEntry> kNNQuery(CTree ctree, GraphMapper mapper,
			GraphSim graphSim, Graph query, int k, boolean strictRanking) {
		/*
		 * SimRanker ranker = new SimRanker(ctree, mapper, query,
		 * preciseRanking); RankerEntry entry; Vector ans = new Vector(k); //
		 * answer set while ( (entry = ranker.nextNN()) != null && ans.size() <
		 * k) { ans.addElement(entry); } accessCount = ranker.getAccessCount();
		 * ranker.clear(); return ans;
		 */
		// System.out.println("Came in kNN");
		SimRanker ranker = new SimRanker(ctree, mapper, graphSim, query,
				strictRanking);
		Vector<RankerEntry> ans = ranker.optimizedKNNQuery(k);
		accessCount = ranker.getAccessCount();

		/*
		 * // compute confidence, validity and precision double simAtK =
		 * -ans.elementAt(k - 1).getDist(); Vector<RankerEntry> ansUp =
		 * ranker.upperRangeQuery(simAtK);
		 * 
		 * if (ansUp.size() <= k) { confidence = 1; validity = 1; precision = 1;
		 * confidence_L = k - 1; } else { confidence = (double) (k - 1) /
		 * (ansUp.size() - 1); // omit the answer which is the query double
		 * simUpAtK = -ansUp.elementAt(k - 1).getDist(); int j; for (j = 0; j <
		 * k; j++) { if ( -ans.elementAt(j).getDist() < simUpAtK) { break; } }
		 * validity = (double) (j - 1) / (k - 1); precision = simAtK / simUpAtK;
		 * confidence_L = ansUp.size() - 1; }
		 */
		return ans;
	}

	/**
	 * Range query
	 * 
	 * @param ctree
	 *            CTree
	 * @param mapper
	 *            GraphMapper
	 * @param graphSim
	 *            GraphSim
	 * @param query
	 *            Graph
	 * @param range
	 *            double
	 * @param preciseRanking
	 *            boolean
	 * @return Vector
	 * @throws IOException
	 */
	public static Vector<RankerEntry> rangeQuery(CTree ctree,
			GraphMapper mapper, GraphSim graphSim, Graph query, double range,
			boolean preciseRanking) throws IOException {
		SimRanker ranker = new SimRanker(ctree, mapper, graphSim, query,
				preciseRanking);
		// System.out.println("Came in range");

		RankerEntry entry;
		Vector<RankerEntry> ans = new Vector(); // answer set
		while ((entry = ranker.nextNN()) != null) {
			// System.out.print(" "+ (0-entry.getDist())+"\n");
			double dist = entry.getGraph().V().length
					+ entry.getGraph().E().length + query.V().length
					+ query.E().length + (2 * entry.getDist());
			// System.out.println(" " + dist);
			if (entry.getDist() <= range)
				ans.addElement(entry);
		}
		accessCount = ranker.getAccessCount();
		ranker.clear();
		return ans;
	}

}
