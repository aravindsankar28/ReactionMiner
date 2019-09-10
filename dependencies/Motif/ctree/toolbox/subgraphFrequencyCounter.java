package ctree.toolbox;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Vector;

import ctree.graph.Graph;
import ctree.index.CTree;
import ctree.index.GraphFactory;
import ctree.index.GraphSim;
import ctree.index.Util;
import ctree.lgraph.LGraph;
import ctree.lgraph.LGraphFactory;
import ctree.lgraph.LGraphFile;
import ctree.lgraph.LGraphSim;
import ctree.lgraph.LGraphWeightMatrix;
import ctree.lgraph.LabelMap;
import ctree.mapper.GraphMapper;
import ctree.mapper.NeighborBiasedMapper;
import ctree.tool.BuildCTree;
import ctree.tool.SubQuery;
import ctree.util.DataSum;
import ctree.util.Opt;

public class subgraphFrequencyCounter {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	
	public static int count(LGraph[] db,LGraph query)
	{
		Vector<Graph> answers = new Vector<Graph>();
        int ans_size = 0;
        for (LGraph g : db) {
            if (Util.subIsomorphic(query, g)) {
                ans_size++;
                answers.add(g);
            }
        }
        return answers.size();
	}
	public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		// TODO Auto-generated method stub
	        Opt opt = new Opt(args);
	        if (opt.args() < 2) {
	            SubQuery.usage();
	            return;
	        }
	        LGraph[] graphs = LGraphFile.loadLGraphs(opt.getArg(0));
	        LGraph[] queries = LGraphFile.loadLGraphs(opt.getArg(1));
	        /*
	        SubQuery sq=new SubQuery();
	        System.err.println("Load ctree " + opt.getArg(0));
	        GraphMapper mapper=new NeighborBiasedMapper(new LGraphWeightMatrix());
	        LabelMap labelMap = new LabelMap(graphs);

	        // Dimensions for summarizing graphs
	        int dim1 = opt.getInt("dim1", 97);
	        int dim2 = opt.getInt("dim2", 97);

	        int L = labelMap.size();
	        if (dim1 > L) {
	            dim1 = L;
	        }
	        if (dim2 > L * L) {
	            dim2 = L * L;
	        }

	        GraphFactory factory = new LGraphFactory(labelMap, dim1, dim2);
	        GraphSim graphSim = new LGraphSim();

	        long time0 = System.currentTimeMillis();
	        System.err.println("Build ctree");
	        CTree ctree = BuildCTree.buildCTree(graphs, 20,39, mapper, graphSim, labelMap,
	                                 factory);

	        System.err.printf("Max depth = %d, Min depth = %d\n", ctree.maxDepth(),
	                          ctree.minDepth());
	        long time = System.currentTimeMillis() - time0;

	        System.out.println("Build time: " + time / 1000.0);


	        int nQ = opt.getInt("nQ", queries.length);
	        boolean usingHist = opt.getString("hist", "yes").equals("yes");

	        int pseudo_level = opt.getInt("pseudo", 1);

	        

			*/
	        System.err.println("Query");
	        for (int i = 0; i < queries.length; i++)
	        {

	        	

	            // NN ranking for distance==0
	           

	            //Vector cand = isomQueryByRanker(ctree, metric, queries[i]);
	            /*Vector<Graph>
	                    cand = sq.subgraphQuery(ctree, queries[i], pseudo_level,
	                                         usingHist);

	            long time1 = System.currentTimeMillis() - time0;
	             */
				
	            // check isomorphism

	            //long time2 = System.currentTimeMillis() - time0;
	            System.out.println(subgraphFrequencyCounter.count(graphs,queries[i]));
	        } // end of query

	    
	}

}
